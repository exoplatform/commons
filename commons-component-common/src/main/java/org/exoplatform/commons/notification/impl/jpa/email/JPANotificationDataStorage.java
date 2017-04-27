package org.exoplatform.commons.notification.impl.jpa.email;

import org.apache.commons.lang.time.DateUtils;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.storage.NotificationDataStorage;
import org.exoplatform.commons.api.persistence.DataInitializer;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.notification.impl.jpa.email.dao.MailDigestDAO;
import org.exoplatform.commons.notification.impl.jpa.email.dao.MailNotifDAO;
import org.exoplatform.commons.notification.impl.jpa.email.dao.MailParamsDAO;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailDigestEntity;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailNotifEntity;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailParamsEntity;
import org.exoplatform.commons.notification.job.NotificationJob;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.*;

import static org.exoplatform.commons.notification.impl.jpa.EntityConverter.convertParamsEntityToParams;

/**
 * Created by exo on 3/22/17.
 */
public class JPANotificationDataStorage implements NotificationDataStorage {

  private static final Log LOG              = ExoLogger.getLogger(JPANotificationDataStorage.class);

  public static final String DIGEST_DAILY   = "daily";
  public static final String DIGEST_WEEKLY   = "weekly";

  private MailNotifDAO mailNotifDAO;
  private MailDigestDAO mailDigestDAO;
  private MailParamsDAO mailParamsDAO;

  public JPANotificationDataStorage(MailNotifDAO mailNotifDAO, MailDigestDAO mailDigestDAO, MailParamsDAO mailParamsDAO, DataInitializer dataInitializer) {
    this.mailNotifDAO = mailNotifDAO;
    this.mailDigestDAO = mailDigestDAO;
    this.mailParamsDAO = mailParamsDAO;
  }

  @Override
  @ExoTransactional
  public void save(NotificationInfo message) throws Exception {
    try {
      MailNotifEntity notifEntity = new MailNotifEntity();

      notifEntity.setSender(message.getFrom().isEmpty() ? message.getValueOwnerParameter("sender") : message.getFrom());
      notifEntity.setOrder(message.getOrder());
      notifEntity.setType(message.getKey().getId());
      notifEntity.setCreationDate(message.getDateCreated());

      Map<String, String> ownerParameter = message.getOwnerParameter();
      Set<MailParamsEntity> set = new HashSet<MailParamsEntity>();
      if (ownerParameter != null && !ownerParameter.isEmpty()) {
        for (String key : ownerParameter.keySet()) {
          MailParamsEntity paramsEntity = new MailParamsEntity();
          paramsEntity.setName(key);
          paramsEntity.setValue(ownerParameter.get(key));
          paramsEntity.setNotification(notifEntity);
          set.add(paramsEntity);
          mailParamsDAO.create(paramsEntity);
        }
      }
      notifEntity.setArrayOwnerParameter(set);

    } catch (Exception e) {
      LOG.error("Failed to save the NotificationMessage", e);
    }
  }

  @Override
  @ExoTransactional
  public Map<PluginKey, List<NotificationInfo>> getByUser(NotificationContext context, UserSetting setting) {

    Map<PluginKey, List<NotificationInfo>> notificationData = new LinkedHashMap<PluginKey, List<NotificationInfo>>();
    try {
      boolean isWeekly = context.value(NotificationJob.JOB_WEEKLY);
      if (isWeekly) {
        for (String pluginId : setting.getWeeklyPlugins()) {
          putMap(notificationData, PluginKey.key(pluginId), getWeeklyNotifs(pluginId, setting.getUserId()));
        }
      }
      //
      boolean isDaily = context.value(NotificationJob.JOB_DAILY);
      if (isDaily) {
        for (String pluginId : setting.getDailyPlugins()) {
          putMap(notificationData, PluginKey.key(pluginId), getDailyNotifs(context, pluginId, setting.getUserId()));
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to get the NotificationMessage by user: " + setting.getUserId(), e);
    }

    return notificationData;
  }

  private static void putMap(Map<PluginKey, List<NotificationInfo>> notificationData, PluginKey key, List<NotificationInfo> values) {
    if (notificationData.containsKey(key)) {
      List<NotificationInfo> messages = notificationData.get(key);
      for (NotificationInfo notificationMessage : values) {
        if (messages.size() == 0 || messages.contains(notificationMessage) == false) {
          messages.add(notificationMessage);
        }
      }
      //
      if(messages.size() > 0 ) {
        notificationData.put(key, messages);
      }
    } else if (values.size() > 0) {
      notificationData.put(key, values);
    }
  }

  private List<NotificationInfo> getDailyNotifs(NotificationContext context, String pluginId, String userId) throws Exception {
    List<NotificationInfo> messages = new ArrayList<NotificationInfo>();
    List<MailNotifEntity> notifEntities = getNotifsByDate(context, pluginId, userId);// for this user
    for (MailNotifEntity mailNotifEntity : notifEntities) {
      if (!isDigestDailySent(mailNotifEntity, userId)) {
        NotificationInfo model = fillModel(mailNotifEntity);
        messages.add(model.setTo(userId));
        setDailySent(mailNotifEntity, userId);
      }
    }
    return messages;
  }

  private NotificationInfo fillModel(MailNotifEntity notifEntity) throws Exception {
    if(notifEntity == null) return null;
    NotificationInfo message = NotificationInfo.instance()
        .setFrom(notifEntity.getSender())
        .setOrder(notifEntity.getOrder())
        .key(notifEntity.getType())
        .setOwnerParameter(convertParamsEntityToParams(notifEntity.getArrayOwnerParameter()))
        .setLastModifiedDate(notifEntity.getCreationDate())
        .setId(String.valueOf(notifEntity.getId()));

    return message;
  }

  private List<MailNotifEntity> getNotifsByDate(NotificationContext context, String pluginId, String userId) {
    String dayName = context.value(NotificationJob.DAY_OF_JOB);
    return mailNotifDAO.getNotifsByPluginAndDay(pluginId, dayName);
  }

  private List<NotificationInfo> getWeeklyNotifs(String pluginId, String userId) throws Exception {
    List<NotificationInfo> messages = new ArrayList<NotificationInfo>();
    List<MailNotifEntity> notifEntities = getNotifsByWeek(pluginId, userId);
    for (MailNotifEntity mailNotifEntity : notifEntities) {
      if (!isDigestWeeklySent(mailNotifEntity, userId)) {
        NotificationInfo model = fillModel(mailNotifEntity);
        messages.add(model.setTo(userId));
        setWeeklySent(mailNotifEntity, userId);
      }
    }
    return messages;
  }

  private boolean isDigestWeeklySent(MailNotifEntity mailNotifEntity, String userId) {
    boolean found = false;
    for (MailDigestEntity mailDigestEntity : mailNotifEntity.getMailDigestSent()) {
      found = mailDigestEntity.getNotification().equals(mailNotifEntity) && mailDigestEntity.getType().equals("weekly")
          && mailDigestEntity.getUser().equals(userId);
      if (found) {
        break;
      }
    }
    return found;
  }

  private boolean isDigestDailySent(MailNotifEntity mailNotifEntity, String userId) {
    boolean found = false;
    for (MailDigestEntity mailDigestEntity : mailNotifEntity.getMailDigestSent()) {
      found = mailDigestEntity.getNotification().equals(mailNotifEntity) && mailDigestEntity.getType().equals("daily")
      && mailDigestEntity.getUser().equals(userId);
      if (found) {
        break;
      }
    }
    return found;
  }


  private List<MailNotifEntity> getNotifsByWeek(String pluginId, String userId) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(DateUtils.addDays(new Date(), -7));
    Calendar oneWeekAgo = calendar;
    return mailNotifDAO.getNotifsByPluginAndWeek(pluginId, oneWeekAgo);
  }


  @Override
  @ExoTransactional
  public void removeMessageAfterSent() throws Exception {
    for (MailNotifEntity mailNotifEntity : mailNotifDAO.findAll()) {
      if (mailDigestDAO.isDigestSent(mailNotifEntity)) {
        mailNotifDAO.delete(mailNotifEntity);
      }
    }
  }

  public void setDailySent(MailNotifEntity notifEntity, String userId) {
    notifEntity.addMailDigestSent(new MailDigestEntity().setNotification(notifEntity).setType(DIGEST_DAILY).setUser(userId));
  }

  public void setWeeklySent(MailNotifEntity notifEntity, String userId) {
    notifEntity.addMailDigestSent(new MailDigestEntity().setNotification(notifEntity).setType(DIGEST_WEEKLY).setUser(userId));
  }
}

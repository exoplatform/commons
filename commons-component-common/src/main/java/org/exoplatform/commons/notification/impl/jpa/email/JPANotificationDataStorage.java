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

      notifEntity.setSender(message.getFrom());
      notifEntity.setOrder(message.getOrder());
      notifEntity.setType(message.getKey().getId());
      notifEntity.setCreationDate(message.getDateCreated());
      notifEntity = mailNotifDAO.create(notifEntity);

      Map<String, String> ownerParameter = message.getOwnerParameter();
      if (ownerParameter != null && !ownerParameter.isEmpty()) {
        for (String key : ownerParameter.keySet()) {
          MailParamsEntity paramsEntity = new MailParamsEntity();
          paramsEntity.setName(key);
          paramsEntity.setValue(ownerParameter.get(key));
          paramsEntity.setNotification(notifEntity);
          mailParamsDAO.create(paramsEntity);
        }
      }
      MailDigestEntity digestEntityDaily = new MailDigestEntity();
      MailDigestEntity digestEntityWeekly = new MailDigestEntity();
      digestEntityDaily.setNotification(notifEntity).setType(DIGEST_DAILY);
      digestEntityWeekly.setNotification(notifEntity).setType(DIGEST_WEEKLY);
      mailDigestDAO.create(digestEntityDaily);
      mailDigestDAO.create(digestEntityWeekly);

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

  private void putMap(Map<PluginKey, List<NotificationInfo>> notificationData, PluginKey key, List<NotificationInfo> values) {
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
      if (!mailDigestDAO.isDigestDailySent(mailNotifEntity)) {
        NotificationInfo model = fillModel(mailNotifEntity, userId);
        messages.add(model);
      }
    }
    return messages;
  }

  private NotificationInfo fillModel(MailNotifEntity notifEntity, String userId) throws Exception {
    if(notifEntity == null) return null;
    NotificationInfo message = NotificationInfo.instance()
        .setFrom(notifEntity.getSender())
        .setTo(userId)
        .setOrder(notifEntity.getOrder())
        .key(notifEntity.getType())
        .setOwnerParameter(convertParamsEntityToParams(notifEntity.getParameters()))
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
      if (!mailDigestDAO.isDigestWeeklySent(mailNotifEntity)) {
        NotificationInfo model = fillModel(mailNotifEntity, userId);
        messages.add(model);
      }
    }
    return messages;
  }

  private List<MailNotifEntity> getNotifsByWeek(String pluginId, String userId) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(DateUtils.addDays(new Date(), -7));
    Calendar oneWeekAgo = calendar;
    return mailNotifDAO.getNotifsByPluginAndWeek(pluginId, oneWeekAgo);
  }

  @Override
  @ExoTransactional
  public void removeMessageAfterSent(NotificationContext context) throws Exception {
    boolean isWeekly = context.value(NotificationJob.JOB_WEEKLY);
    if (isWeekly) {
      for (MailNotifEntity mailNotifEntity : mailNotifDAO.findAll()) {
        MailDigestEntity digestEntityWeekly = getDigest(mailNotifEntity, DIGEST_WEEKLY);
        if (digestEntityWeekly != null) {
          mailDigestDAO.delete(digestEntityWeekly);
        }
        if (mailDigestDAO.isDigestDailySent(mailNotifEntity)) {
          mailNotifDAO.delete(mailNotifEntity);
        }
      }
    }
    //
    boolean isDaily = context.value(NotificationJob.JOB_DAILY);
    if (isDaily) {
      for (MailNotifEntity mailNotifEntity : mailNotifDAO.findAll()) {
        MailDigestEntity digestEntityDaily = getDigest(mailNotifEntity, DIGEST_DAILY);
        if (digestEntityDaily != null) {
          mailDigestDAO.delete(digestEntityDaily);
        }
        if (mailDigestDAO.isDigestWeeklySent(mailNotifEntity)) {
          mailNotifDAO.delete(mailNotifEntity);
        }
      }
    }
  }

  private MailDigestEntity getDigest(MailNotifEntity mailNotifEntity, String type) {
    return mailDigestDAO.getDigest(mailNotifEntity, type);
  }
}

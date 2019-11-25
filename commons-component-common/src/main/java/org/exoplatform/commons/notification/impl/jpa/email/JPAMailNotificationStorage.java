package org.exoplatform.commons.notification.impl.jpa.email;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.storage.MailNotificationStorage;
import org.exoplatform.commons.api.persistence.DataInitializer;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.notification.impl.jpa.email.dao.MailDigestDAO;
import org.exoplatform.commons.notification.impl.jpa.email.dao.MailNotifDAO;
import org.exoplatform.commons.notification.impl.jpa.email.dao.MailParamDAO;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailDigestEntity;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailNotifEntity;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailParamEntity;
import org.exoplatform.commons.notification.job.NotificationJob;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class JPAMailNotificationStorage implements MailNotificationStorage {

  private static final int                 QUERY_LIMIT   = 100;

  private static final Log                 LOG           = ExoLogger.getLogger(JPAMailNotificationStorage.class);

  public static final String               DIGEST_DAILY  = "daily";

  public static final String               DIGEST_WEEKLY = "weekly";

  @SuppressWarnings("rawtypes")
  public static final ArgumentLiteral<Map> WEEKLY_NOTIFS = new ArgumentLiteral<Map>(Map.class, "weekly_notifications");

  @SuppressWarnings("rawtypes")
  public static final ArgumentLiteral<Map> DAILY_NOTIFS  = new ArgumentLiteral<Map>(Map.class, "daily_notifications");

  private MailNotifDAO                     mailNotifDAO;

  private MailDigestDAO                    mailDigestDAO;

  private MailParamDAO                     mailParamDAO;

  public JPAMailNotificationStorage(MailNotifDAO mailNotifDAO,
                                    MailDigestDAO mailDigestDAO,
                                    MailParamDAO mailParamDAO,
                                    DataInitializer dataInitializer) {
    this.mailNotifDAO = mailNotifDAO;
    this.mailDigestDAO = mailDigestDAO;
    this.mailParamDAO = mailParamDAO;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @ExoTransactional
  public void save(NotificationInfo message) throws Exception {
    MailNotifEntity notifEntity = new MailNotifEntity();

    notifEntity.setSender(message.getFrom());
    notifEntity.setOrder(message.getOrder());
    notifEntity.setType(message.getKey().getId());
    notifEntity.setCreationDate(message.getDateCreated());
    notifEntity = mailNotifDAO.create(notifEntity);
    message.setId(String.valueOf(notifEntity.getId()));

    MailDigestEntity digestEntityDaily = new MailDigestEntity();
    digestEntityDaily.setNotification(notifEntity).setType(DIGEST_DAILY);
    mailDigestDAO.create(digestEntityDaily);

    MailDigestEntity digestEntityWeekly = new MailDigestEntity();
    digestEntityWeekly.setNotification(notifEntity).setType(DIGEST_WEEKLY);
    mailDigestDAO.create(digestEntityWeekly);

    Map<String, String> parameters = message.getOwnerParameter();
    if (parameters != null && !parameters.isEmpty()) {
      for (String key : parameters.keySet()) {
        MailParamEntity paramEntity = new MailParamEntity();
        paramEntity.setName(key);
        paramEntity.setValue(parameters.get(key));
        paramEntity.setNotification(notifEntity);
        mailParamDAO.create(paramEntity);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @ExoTransactional
  public Map<PluginKey, List<NotificationInfo>> getByUser(NotificationContext context, UserSetting setting) {
    Map<PluginKey, List<NotificationInfo>> notificationData = new LinkedHashMap<PluginKey, List<NotificationInfo>>();
    boolean isWeekly = context.value(NotificationJob.JOB_WEEKLY);
    if (isWeekly) {
      for (String pluginId : setting.getWeeklyPlugins()) {
        putMap(notificationData, PluginKey.key(pluginId), getWeeklyNotifs(context, pluginId, setting.getUserId()));
      }
    }
    //
    boolean isDaily = context.value(NotificationJob.JOB_DAILY);
    if (isDaily) {
      for (String pluginId : setting.getDailyPlugins()) {
        putMap(notificationData, PluginKey.key(pluginId), getDailyNotifs(context, pluginId, setting.getUserId()));
      }
    }
    return notificationData;
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @Override
  @ExoTransactional
  public void removeMessageAfterSent(NotificationContext context) throws Exception {
    boolean isWeekly = context.value(NotificationJob.JOB_WEEKLY);

    // Notifications that was sent
    Map<String, List<NotificationInfo>> generalWeeklyNotificationData = null;
    if (isWeekly) {
      generalWeeklyNotificationData = context.value(WEEKLY_NOTIFS);
    } else {
      generalWeeklyNotificationData = context.value(DAILY_NOTIFS);
    }
    if (generalWeeklyNotificationData == null) {
      mailDigestDAO.deleteAllDigestsOfType(isWeekly ? DIGEST_WEEKLY : DIGEST_DAILY);
    } else {
      Set<Long> mailNotifsIds = new HashSet<>();
      Collection<List<NotificationInfo>> notificationsLists = generalWeeklyNotificationData.values();
      for (List<NotificationInfo> notificationsList : notificationsLists) {
        if (notificationsList == null) {
          continue;
        }
        for (NotificationInfo notificationInfo : notificationsList) {
          try {
            mailNotifsIds.add(Long.parseLong(notificationInfo.getId()));
          } catch (NumberFormatException e) {
            LOG.warn("can't parse mail notification with id: " + notificationInfo.getId());
          }
        }
      }
      if (mailNotifsIds != null && !mailNotifsIds.isEmpty()) {
        mailDigestDAO.deleteDigestsOfTypeByNotificationsIds(mailNotifsIds, isWeekly ? DIGEST_WEEKLY : DIGEST_DAILY);
      }
    }
    List<MailNotifEntity> allNotificationsWithoutDigests;
    do {
      allNotificationsWithoutDigests = mailNotifDAO.getAllNotificationsWithoutDigests(0, QUERY_LIMIT);
      if (allNotificationsWithoutDigests != null && !allNotificationsWithoutDigests.isEmpty()) {
        mailParamDAO.deleteParamsOfNotifications(allNotificationsWithoutDigests);
        mailNotifDAO.deleteAll(allNotificationsWithoutDigests);
      }
    } while (allNotificationsWithoutDigests.size() == QUERY_LIMIT);
  }

  @Override
  @ExoTransactional
  public void deleteAllDigests() throws Exception {
    mailDigestDAO.deleteAllDigests();
  }

  private List<NotificationInfo> getWeeklyNotifs(NotificationContext context, String pluginId, String userId) {
    return getNotificationsByDigestAndPluginId(context, WEEKLY_NOTIFS, pluginId, userId);
  }

  private List<NotificationInfo> getDailyNotifs(NotificationContext context, String pluginId, String userId) {
    return getNotificationsByDigestAndPluginId(context, DAILY_NOTIFS, pluginId, userId);
  }

  private List<NotificationInfo> getNotificationsByDigestAndPluginId(NotificationContext context,
                                                                     @SuppressWarnings("rawtypes") ArgumentLiteral<Map> notifsDigestArgument,
                                                                     String pluginId,
                                                                     String userId) {
    // Get notifications by plugin id from context
    @SuppressWarnings("unchecked")
    Map<String, List<NotificationInfo>> notificationsByPluginId = context.value(notifsDigestArgument);
    if (notificationsByPluginId == null) {
      notificationsByPluginId = new HashMap<>();
      context.append(notifsDigestArgument, notificationsByPluginId);
    }

    List<NotificationInfo> notificationInfos = null;
    if (notificationsByPluginId.containsKey(pluginId)) {
      // Get notifications by plugin id from context
      notificationInfos = notificationsByPluginId.get(pluginId);
    } else {
      // Get notifications by plugin id from Storage
      notificationInfos = new ArrayList<NotificationInfo>();
      List<MailNotifEntity> notifEntities = notifsDigestArgument == WEEKLY_NOTIFS ? getNotifsByWeek(pluginId)
                                                                                  : getNotifsByDate(context, pluginId);
      for (MailNotifEntity mailNotifEntity : notifEntities) {
        NotificationInfo model = fillModel(mailNotifEntity, null);
        notificationInfos.add(model);
      }
      // Set notifications to process by plugin id in context
      notificationsByPluginId.put(pluginId, notificationInfos);
    }

    List<NotificationInfo> resultNotificationInfos = new ArrayList<>();
    for (NotificationInfo notificationInfo : notificationInfos) {
      NotificationInfo resultNotificationInfo = notificationInfo.clone(false);
      resultNotificationInfo.setTo(userId);
      resultNotificationInfos.add(resultNotificationInfo);
    }
    return resultNotificationInfos;
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
      if (messages.size() > 0) {
        notificationData.put(key, messages);
      }
    } else if (values.size() > 0) {
      notificationData.put(key, values);
    }
  }

  private NotificationInfo fillModel(MailNotifEntity notifEntity, String userId) {
    if (notifEntity == null)
      return null;
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

  private static Map<String, String> convertParamsEntityToParams(Collection<MailParamEntity> paramsEntityList) {
    Map<String, String> params = new HashMap<String, String>();
    for (MailParamEntity paramsEntity : paramsEntityList) {
      params.put(paramsEntity.getName(), paramsEntity.getValue());
    }
    return params;
  }

  private List<MailNotifEntity> getNotifsByDate(NotificationContext context, String pluginId) {
    String dayName = context.value(NotificationJob.DAY_OF_JOB);
    return mailNotifDAO.getNotifsByPluginAndDay(pluginId, dayName);
  }

  private List<MailNotifEntity> getNotifsByWeek(String pluginId) {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DATE, -7);
    Calendar oneWeekAgo = calendar;
    return mailNotifDAO.getNotifsByPluginAndWeek(pluginId, oneWeekAgo);
  }

}

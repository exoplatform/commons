package org.exoplatform.commons.notification.impl.jpa.web;

import static org.exoplatform.commons.notification.impl.jpa.EntityConverter.convertWebNotifEntityToNotificationInfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.notification.impl.jpa.EntityConverter;
import org.exoplatform.commons.notification.impl.jpa.web.dao.WebNotifDAO;
import org.exoplatform.commons.notification.impl.jpa.web.dao.WebParamsDAO;
import org.exoplatform.commons.notification.impl.jpa.web.dao.WebUsersDAO;
import org.exoplatform.commons.notification.impl.jpa.web.entity.WebNotifEntity;
import org.exoplatform.commons.notification.impl.jpa.web.entity.WebParamsEntity;
import org.exoplatform.commons.notification.impl.jpa.web.entity.WebUsersEntity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class JPAWebNotificationStorage implements WebNotificationStorage {

  private static final Log         LOG                   = ExoLogger.getLogger(JPAWebNotificationStorage.class);

  private final UserSettingService userSettingService;

  private WebNotifDAO              webNotifDAO;

  private WebParamsDAO             webParamsDAO;

  private WebUsersDAO              webUsersDAO;

  private static final String      NTF_NAME_SPACE        = "ntf:";

  public JPAWebNotificationStorage(WebNotifDAO webNotifDAO,
                                   WebParamsDAO webParamsDAO,
                                   WebUsersDAO webUsersDAO,
                                   UserSettingService userSettingService) {
    this.userSettingService = userSettingService;
    this.webNotifDAO = webNotifDAO;
    this.webParamsDAO = webParamsDAO;
    this.webUsersDAO = webUsersDAO;
  }

  @Override
  @ExoTransactional
  public void save(NotificationInfo notification) {
    save(notification, false);
  }

  @Override
  @ExoTransactional
  public List<NotificationInfo> get(WebNotificationFilter filter, int offset, int limit) {
    List<NotificationInfo> result = new ArrayList<NotificationInfo>();
    String pluginId = filter.getPluginKey() != null ? filter.getPluginKey().getId() : null;
    String userId = filter.getUserId();
    List<WebUsersEntity> webUsersEntities;
    if (pluginId != null) {
      // web notifs entities order by lastUpdated DESC
      webUsersEntities = webUsersDAO.findWebNotifsByFilter(pluginId, userId, filter.isOnPopover(), offset, limit);
    } else if (filter.isOnPopover()) {
      webUsersEntities = webUsersDAO.findWebNotifsByFilter(userId, filter.isOnPopover(), offset, limit);
    } else {
      webUsersEntities = webUsersDAO.findWebNotifsByFilter(userId, offset, limit);
    }
    //
    for (WebUsersEntity webUserNotifEntity : webUsersEntities) {
      result.add(convertWebNotifEntityToNotificationInfo(webUserNotifEntity));
    }
    return result;
  }

  @Override
  @ExoTransactional
  public NotificationInfo get(String id) {
    if (StringUtils.isBlank(id) || id.startsWith(NotificationInfo.PREFIX_ID)) {
      return null;
    }
    WebUsersEntity webUsersEntity = getWebNotification(parseNotificationId(id));
    if (webUsersEntity != null) {
      return convertWebNotifEntityToNotificationInfo(webUsersEntity);
    }
    return null;
  }

  @Override
  @ExoTransactional
  public boolean remove(String notificationId) {
    WebUsersEntity webUsersEntity = getWebNotification(parseNotificationId(notificationId));
    if (webUsersEntity != null) {
      webUsersDAO.delete(webUsersEntity);
      return true;
    }
    return false;
  }

  @Override
  @ExoTransactional
  public boolean remove(long seconds) {
    boolean removed = false;
    Calendar cal = Calendar.getInstance();
    long delayTime = System.currentTimeMillis() - (seconds * 1000);
    cal.setTimeInMillis(delayTime);
    List<WebNotifEntity> notifEntities = new ArrayList<>();
    List<WebUsersEntity> webUserNotifs = webUsersDAO.findWebNotifsByLastUpdatedDate(cal);
    for (WebUsersEntity webUsersEntity : webUserNotifs) {
      WebNotifEntity notification = webUsersEntity.getNotification();
      if (!notifEntities.contains(notification)) {
        notifEntities.add(notification);
      }
      webUsersDAO.delete(webUsersEntity);
    }
    removed = notifEntities.size() > 0;
    if(removed) {
      for (WebNotifEntity webNotifEntity : notifEntities) {
        webParamsDAO.deleteAll(new ArrayList<>(webNotifEntity.getParameters()));
      }
      webNotifDAO.deleteAll(notifEntities);
    }
    return removed;
  }

  @Override
  @ExoTransactional
  public boolean remove(String userId, long seconds) {
    Calendar calendar = Calendar.getInstance();
    long timeInMilliseconds = calendar.getTimeInMillis() - seconds * 1000;
    calendar.setTimeInMillis(timeInMilliseconds);

    boolean removed = false;
    for (WebUsersEntity webUsersEntity : webUsersDAO.findWebNotifsOfUserByLastUpdatedDate(userId, calendar)) {
      try {
        webUsersDAO.delete(webUsersEntity);
        removed = true;
      } catch (Exception e) {
        LOG.error("Failed to remove notification with id '" + webUsersEntity.getId() + "' for the user id: " + userId, e);
        return false;
      }
    }
    return removed;
  }

  @Override
  @ExoTransactional
  public void markRead(String notificationId) {
    long notifIdLong = parseNotificationId(notificationId);
    WebUsersEntity webUsersEntity = webUsersDAO.find(notifIdLong);
    if (webUsersEntity != null) {
      webUsersEntity.setRead(true);
      webUsersDAO.update(webUsersEntity);
    }
  }

  @Override
  @ExoTransactional
  public void hidePopover(String notificationId) {
    WebUsersEntity webUsersEntity = webUsersDAO.find(parseNotificationId(notificationId));
    if (webUsersEntity != null) {
      webUsersEntity.setShowPopover(false);
      webUsersDAO.update(webUsersEntity);
    }
  }

  @Override
  @ExoTransactional
  public void markAllRead(String userId) {
    //
    webUsersDAO.markAllRead(userId);
    userSettingService.saveLastReadDate(userId, System.currentTimeMillis());
  }

  @Override
  @ExoTransactional
  public NotificationInfo getUnreadNotification(String pluginId, String activityId, String userId) {
    List<WebUsersEntity> list = webUsersDAO.findUnreadNotification(pluginId, userId, "activityId", activityId);

    if (list.size() > 0) {
      WebUsersEntity webUsersNotification = list.get(0);
      return EntityConverter.convertWebNotifEntityToNotificationInfo(webUsersNotification);
    }
    return null;
  }

  @Override
  @ExoTransactional
  public void update(NotificationInfo notification, boolean moveTop) {
    // if moveTop == true, the number on badge will increase
    // else the number on badge will not increase
    save(notification, moveTop);
  }

  @Override
  @ExoTransactional
  public int getNumberOnBadge(String userId) {
    return webUsersDAO.getNumberOnBadge(userId);
  }

  @Override
  @ExoTransactional
  public void resetNumberOnBadge(String userId) {
    List<WebUsersEntity> notifsWithBadge = webUsersDAO.findNotifsWithBadge(userId);
    if (notifsWithBadge != null && notifsWithBadge.size() > 0) {
      for (WebUsersEntity webUsersEntity : notifsWithBadge) {
        webUsersEntity.setResetNumberOnBadge(true);
      }
      webUsersDAO.updateAll(notifsWithBadge);
    }
  }

  /**
   * Creates the notification message to the specified user.
   *
   * @param notification The notification to save
   * @param moveTop The status to update count on Popover or not
   */
  private void save(NotificationInfo notification, boolean moveTop) {
    WebUsersEntity webUsersEntity = null;
    if (notification.getId() != null && !notification.getId().startsWith(NotificationInfo.PREFIX_ID)) {
      webUsersEntity = webUsersDAO.find(Long.parseLong(notification.getId()));
    }
    boolean isNew = webUsersEntity == null;
    WebNotifEntity webNotifEntity = null;
    if (isNew) {
      webNotifEntity = new WebNotifEntity();
      webUsersEntity = new WebUsersEntity();
    } else {
      webNotifEntity = webUsersEntity.getNotification();
    }
    // fill WebNotifEntity with data from notification
    webNotifEntity.setType(notification.getKey().getId());
    webNotifEntity.setText(notification.getTitle());
    webNotifEntity.setSender(notification.getFrom());
    if (notification.getDateCreated() == null) {
      webNotifEntity.setCreationDate(Calendar.getInstance());
    } else {
      webNotifEntity.setCreationDate(notification.getDateCreated());
    }
    if (isNew) {
      webNotifEntity = webNotifDAO.create(webNotifEntity);
    } else {
      webNotifEntity = webNotifDAO.update(webNotifEntity);
    }

    Map<String, String> ownerParameter = notification.getOwnerParameter();
    Set<WebParamsEntity> parameters = webNotifEntity.getParameters();
    if (ownerParameter != null && !ownerParameter.isEmpty()) {
      for (String key : ownerParameter.keySet()) {
        String propertyName = key.replace(NTF_NAME_SPACE, "");
        // fill WebParamsEntity with data from notification
        WebParamsEntity webParamsEntity = null;
        boolean isParamNew = true;
        if (isNew) {
          webParamsEntity = new WebParamsEntity();
        } else {
          for (WebParamsEntity webParamsEntityTmp : parameters) {
            if (webParamsEntityTmp.getName().equals(propertyName)) {
              webParamsEntity = webParamsEntityTmp;
              isParamNew = false;
              break;
            }
          }
        }
        if (webParamsEntity == null) {
          webParamsEntity = new WebParamsEntity();
        }
        webParamsEntity.setName(propertyName);
        webParamsEntity.setValue(ownerParameter.get(key));
        webParamsEntity.setNotification(webNotifEntity);
        if (isParamNew) {
          webParamsDAO.create(webParamsEntity);
        } else {
          webParamsDAO.update(webParamsEntity);
        }
      }
    }

    // fill WebUsersEntity with data from notification
    webUsersEntity.setReceiver(notification.getTo());
    Calendar calendar = Calendar.getInstance();
    if (moveTop) {
      webUsersEntity.setUpdateDate(calendar);
    } else if (notification.getLastModifiedDate() > 0) {
      calendar.setTimeInMillis(notification.getLastModifiedDate());
      webUsersEntity.setUpdateDate(calendar);
    } else {
      webUsersEntity.setUpdateDate(webNotifEntity.getCreationDate());
    }
    webUsersEntity.setShowPopover(notification.isOnPopOver());

    webUsersEntity.setResetNumberOnBadge(notification.isResetOnBadge());
    webUsersEntity.setRead(notification.isRead());

    webUsersEntity.setNotification(webNotifEntity);
    if (isNew) {
      webUsersEntity = webUsersDAO.create(webUsersEntity);
      notification.setId(String.valueOf(webUsersEntity.getId()));
    } else {
      webUsersDAO.update(webUsersEntity);
    }
  }

  private long parseNotificationId(String notificationId) {
    return Long.parseLong(notificationId);
  }

  @ExoTransactional
  private WebUsersEntity getWebNotification(Long notificationId) {
    return webUsersDAO.find(notificationId);
  }
}

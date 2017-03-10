package org.exoplatform.commons.notification.impl.jpa.web;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.api.persistence.DataInitializer;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.notification.impl.jpa.cache.RDBMSCachedWebNotificationStorage;
import org.exoplatform.commons.notification.impl.jpa.web.dao.WebNotifDAO;
import org.exoplatform.commons.notification.impl.jpa.web.dao.WebParamsDAO;
import org.exoplatform.commons.notification.impl.jpa.web.dao.WebUsersDAO;
import org.exoplatform.commons.notification.impl.jpa.web.entity.WebNotifEntity;
import org.exoplatform.commons.notification.impl.jpa.web.entity.WebParamsEntity;
import org.exoplatform.commons.notification.impl.jpa.web.entity.WebUsersEntity;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.exoplatform.commons.notification.impl.jpa.EntityConverter.convertWebNotifEntityToNotificationInfo;

/**
 * Created by exo on 3/31/17.
 */
public class JPAWebNotificationStorage implements WebNotificationStorage {

  private static final Log LOG = ExoLogger.getLogger(JPAWebNotificationStorage.class);

  private final UserSettingService userSettingService;
  private final DataDistributionManager distributionManager;
  private WebNotificationStorage webNotificationStorage;

  private WebNotifDAO webNotifDAO;
  private WebParamsDAO webParamsDAO;
  private WebUsersDAO webUsersDAO;

  private static final String NTF_NAME_SPACE           = "ntf:";
  private static final String NTF_SHOW_POPOVER         = "showPopover";
  private static final String NTF_READ         = "read";
  private static final String DATE_FRIENDLY_PATTERN = "yyyy-MM-dd";
  private static final String RELATIONSHIP_RECEIVED_PLUGIN = "RelationshipReceivedRequestPlugin";
  private static final String SPACE_INVITATION_PLUGIN = "SpaceInvitationPlugin";
  private static final String REQUEST_JOIN_SPACE_PLUGIN = "RequestJoinSpacePlugin";
  private static final String ACTIVITY_COMMENT_PLUGIN = "ActivityCommentPlugin";
  private static final String STATUS_PARAMETER = "status";

  public JPAWebNotificationStorage(WebNotifDAO webNotifDAO, WebParamsDAO webParamsDAO, WebUsersDAO webUsersDAO,
                                   DataInitializer dataInitializer, DataDistributionManager distributionManager,
                                   UserSettingService userSettingService) {
    this.distributionManager = distributionManager;
    this.userSettingService = userSettingService;
    this.webNotifDAO = webNotifDAO;
    this.webParamsDAO = webParamsDAO;
    this.webUsersDAO = webUsersDAO;
  }

  @Override
  public void save(NotificationInfo notification) {
    save(notification, true);
  }

  /**
   * Creates the notification message to the specified user.
   *
   * @param notification The notification to save
   * @param isCountOnPopover The status to update count on Popover or not
   */
  @ExoTransactional
  private void save(NotificationInfo notification, boolean isCountOnPopover) {
    try {
      WebNotifEntity webNotifEntity = null;
      // if the notification is an invitation accepted for a space, relationship received or request join space notification:
      // - remove the correspondant old notification
      // - store the new notification with isCountOnPopover set to true
      if ((notification.getKey().getId().equals(RELATIONSHIP_RECEIVED_PLUGIN)
          || notification.getKey().getId().equals(SPACE_INVITATION_PLUGIN)
          || notification.getKey().getId().equals(REQUEST_JOIN_SPACE_PLUGIN))
          && notification.getOwnerParameter().containsKey(STATUS_PARAMETER)) {
        String notifId = notification.getTitle().split("data-id=\"")[1].split("\"")[0];
        webNotifEntity = webNotifDAO.find(Long.parseLong(notifId));
      } else if (notification.getKey().getId().equals(ACTIVITY_COMMENT_PLUGIN)) {
        webNotifEntity = webNotifDAO.findWebNotifsOfUserByParam(notification.getTo(),
            ACTIVITY_COMMENT_PLUGIN, notification.getOwnerParameter().get("activityId"), "activityId");
      }
      if (webNotifEntity != null) {
        webNotifDAO.delete(webNotifEntity);
        save(notification, true);
      } else {
        //
        webNotifEntity = new WebNotifEntity();
        WebUsersEntity webUsersEntity = new WebUsersEntity();

        //fill WebNotifEntity with data from notification
        webNotifEntity.setType(notification.getKey().getId());
        webNotifEntity.setText(notification.getTitle());
        webNotifEntity.setSender(notification.getFrom());
        webNotifEntity.setOwner(notification.getTo());
        webNotifEntity.setCreationDate(notification.getDateCreated());

        //fill WebUsersEntity with data from notification
        webUsersEntity.setReceiver(notification.getTo());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(notification.getLastModifiedDate());
        webUsersEntity.setUpdateDate(calendar);
        webUsersEntity.setNotification(webNotifEntity);
        webUsersDAO.create(webUsersEntity);

        Map<String, String> ownerParameter = notification.getOwnerParameter();
        Set<WebParamsEntity> set = new HashSet<WebParamsEntity>();
        if (ownerParameter != null && !ownerParameter.isEmpty()) {
          for (String key : ownerParameter.keySet()) {
            if (!key.equals("resetNumberOnBadge")) {
              String propertyName = key.replace(NTF_NAME_SPACE, "");
              //fill WebParamsEntity with data from notification
              WebParamsEntity webParamsEntity = new WebParamsEntity();
              webParamsEntity.setName(propertyName);
              webParamsEntity.setValue(ownerParameter.get(key));
              set.add(webParamsEntity);
              webParamsEntity.setNotification(webNotifEntity);
              webParamsDAO.create(webParamsEntity);
              webUsersEntity.setShowPopover(Boolean.parseBoolean(ownerParameter.get(NTF_SHOW_POPOVER)) || isCountOnPopover);
              webUsersEntity.setRead(Boolean.parseBoolean(ownerParameter.get(NTF_READ)));
            }
          }
        }

        webNotifEntity.setParameters(set);
        webNotifEntity.setReceiver(webUsersEntity);
        webNotifDAO.update(webNotifEntity);
        webUsersDAO.update(webUsersEntity);
        notification.setId(String.valueOf(webNotifEntity.getId()));
      }
    } catch (Exception e) {
      LOG.error("Failed to save the notificaton.", e);
    }
  }

  @Override
  @ExoTransactional
  public List<NotificationInfo> get(WebNotificationFilter filter, int offset, int limit) {
    List<NotificationInfo> result = new ArrayList<NotificationInfo>();
    String pluginId = filter.getPluginKey() !=null ? filter.getPluginKey().getId() : null;
    String userId = filter.getUserId();
    Boolean isOnPopover = filter.isOnPopover();
    try {
      List<WebNotifEntity> webNotifEntities;
      if (pluginId != null) {
        //web notifs entities order by lastUpdated DESC
        webNotifEntities = webNotifDAO.findWebNotifsByFilter(pluginId, userId, isOnPopover, offset, limit);
      } else if (isOnPopover){
        webNotifEntities = webNotifDAO.findWebNotifsByFilter(userId, isOnPopover, offset, limit);
      } else {
        webNotifEntities = webNotifDAO.findWebNotifsByFilter(userId, offset, limit);
      }
      //
      for(WebNotifEntity webNotifEntity : webNotifEntities) {
        result.add(convertWebNotifEntityToNotificationInfo(webNotifEntity));
      }
    } catch (Exception e) {
      LOG.error("Notifications not found by filter: " + filter.toString(), e);
    }
    return result;
  }

  @Override
  public NotificationInfo get(String id) {
    try {
      return convertWebNotifEntityToNotificationInfo(getWebNotification(Long.parseLong(id)));
    } catch (Exception e) {
      LOG.error("Notification not found by id: " + id, e);
      return null;
    }
  }

  @Override
  @ExoTransactional
  public boolean remove(String notificationId) {
    try {
      WebNotifEntity webNotifEntity = getWebNotification(Long.parseLong(notificationId));
      if (webNotifEntity != null) {
        webNotifDAO.delete(webNotifEntity);
        return true;
      }
    } catch (Exception e) {
      LOG.error("Failed to remove the notification id: " + notificationId, e);
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
    try {
      for (WebNotifEntity webNotifEntity : webNotifDAO.findWebNotifsByLastUpdatedDate(cal)) {
        webNotifDAO.delete(webNotifEntity);
        removed = true;
      }
    } catch (Exception e) {
      LOG.error("Failed to remove all notifications and delay date: " + converDateToFriendlyName(cal), e);
      return false;
    }
    return removed;
  }

  private String converDateToFriendlyName(Calendar cal) {
    return new SimpleDateFormat(DATE_FRIENDLY_PATTERN).format(cal.getTime());
  }

  @Override
  @ExoTransactional
  public boolean remove(String userId, long seconds) {
    Calendar cal = Calendar.getInstance();
    long delayTime = System.currentTimeMillis() - (seconds * 1000);
    cal.setTimeInMillis(delayTime);

    try {
      for (WebNotifEntity webNotifEntity : webNotifDAO.findWebNotifsOfUserByLastUpdatedDate(userId, cal)) {
        webNotifDAO.delete(webNotifEntity);
      }
    } catch (Exception e) {
      LOG.error("Failed to remove all notifications for the user id: " + userId, e);
      return false;
    }
    return true;
  }

  @Override
  @ExoTransactional
  public void markRead(String notificationId) {
    WebNotifEntity webNotifEntity = webNotifDAO.find(Long.valueOf(notificationId));
    if (webNotifEntity != null) {
      try {
        WebUsersEntity webUsersEntity = webNotifEntity.getReceiver();
        webUsersEntity.setRead(true);
        webUsersDAO.update(webUsersEntity);

        for (WebParamsEntity webParamsEntity : webNotifEntity.getParameters()) {
          if (webParamsEntity.getName().equals(NTF_READ)) {
            webParamsEntity.setValue("true");
            webParamsDAO.update(webParamsEntity);
            break;
          }
        }
        webNotifDAO.update(webNotifEntity);
      } catch (Exception e) {
        LOG.error("Failed to update the read notification Id: " + notificationId, e);
      }
    }
  }

  @Override
  @ExoTransactional
  public void hidePopover(String notificationId) {
    WebNotifEntity webNotifEntity = webNotifDAO.find(Long.valueOf(notificationId));
    if (webNotifEntity != null) {
      try {
        WebUsersEntity webUsersEntity = webNotifEntity.getReceiver();
        webUsersEntity.setShowPopover(false);
        webUsersDAO.update(webUsersEntity);

        for (WebParamsEntity webParamsEntity : webNotifEntity.getParameters()) {
          if (webParamsEntity.getName().equals(NTF_SHOW_POPOVER)) {
            webParamsEntity.setValue("false");
            webParamsDAO.update(webParamsEntity);
            break;
          }
        }
        webNotifDAO.update(webNotifEntity);
      } catch (Exception e) {
        LOG.error("Failed to update the read notification Id: " + notificationId, e);
      }
    }
  }

  @Override
  public void markAllRead(String userId) {
    try {
      //
      userSettingService.saveLastReadDate(userId, System.currentTimeMillis());
      //
      if (getWebNotificationStorage() instanceof RDBMSCachedWebNotificationStorage) {
        RDBMSCachedWebNotificationStorage cacheStorage = (RDBMSCachedWebNotificationStorage) getWebNotificationStorage();
        cacheStorage.updateAllRead(userId);
      } else {
        for (WebNotifEntity webNotifEntity : getNewMessage(userId, 0)) {
          markRead(String.valueOf(webNotifEntity.getId()));
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to update the all read for userId:" + userId, e);
    }
  }

  @ExoTransactional
  private WebNotifEntity getWebNotification(Long notificationId) {
    try {
      return webNotifDAO.find(notificationId);
    } catch (Exception e) {
      LOG.error("Failed to get web notification node: " + notificationId, e);
    }
    return null;
  }

  /**
   * Gets {@link WebNotificationStorage}
   * @return
   */
  private WebNotificationStorage getWebNotificationStorage() {
    if (webNotificationStorage == null) {
      webNotificationStorage = CommonsUtils.getService(WebNotificationStorage.class);
    }
    return webNotificationStorage;
  }

  @Override
  @ExoTransactional
  public NotificationInfo getUnreadNotification(String pluginId, String activityId, String owner) {
    try {
      long lastReadDate = getLastReadDateOfUser(owner);
      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(lastReadDate);

      List<WebNotifEntity> list = webNotifDAO.findUnreadNotification(pluginId, owner, activityId, calendar);

      if (list.size() > 0) {
        return getWebNotificationStorage().get(String.valueOf(list.get(0).getId()));
      }
    } catch (Exception e) {
      LOG.debug("Failed to getUnreadNotification ", e);
    }
    return null;
  }

  @Override
  @ExoTransactional
  public void update(NotificationInfo notification, boolean moveTop) {
    //only remove the old notification and create a new one in case of update and move top
    //else just update it
    if (moveTop) {
      remove(notification.getId());
    }
    // if moveTop == true, the number on badge will increase
    // else the number on badge will not increase
    save(notification, moveTop);
  }

  @Override
  @ExoTransactional
  public int getNumberOnBadge(String userId) {
    try {
      int number=0;
      int total=0;
      for (WebNotifEntity webNotifEntity : getNewMessage(userId, 0)) {
        for (WebParamsEntity param : webNotifEntity.getParameters()) {
          if (param.getName().equals("resetNumberOnBadge")) {
            number++;
            break;
          }
        }
        total++;
      }
      return (total-number);
    } catch (Exception e) {
      LOG.error("Failed to getNumberOnBadge() ", e);
    }
    return 0;
  }

  @Override
  @ExoTransactional
  public void resetNumberOnBadge(String userId) {
    WebParamsEntity paramsEntity = new WebParamsEntity();
    paramsEntity.setName("resetNumberOnBadge");
    paramsEntity.setValue("true");
    try {
      for (WebNotifEntity webNotifEntity : getNewMessage(userId, 0)) {
        paramsEntity.setNotification(webNotifEntity);
        webNotifEntity.addParameter(paramsEntity);
        webNotifDAO.update(webNotifEntity);
      }
    } catch (Exception e) {
      LOG.error("Failed to resetNumberOnBadge() ", e);
    }
  }

  @ExoTransactional
  private List<WebNotifEntity> getNewMessage(String userId, int limit) throws Exception {
    if (limit > 0) {
      return webNotifDAO.findWebNotifsByUser(userId, false, 0, limit);
    } else {
      return webNotifDAO.findWebNotifsByUser(userId, false);
    }
  }

  /**
   * @param userId
   * @return
   */
  private long getLastReadDateOfUser(String userId) {
    return userSettingService.get(userId).getLastReadDate();
  }
}

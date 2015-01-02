/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.commons.notification.impl.service.storage.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.notification.impl.service.storage.WebNotificationStorageImpl;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class CachedWebNotificationStorage implements WebNotificationStorage {
  /** Logger */
  private static final Log LOG = ExoLogger.getLogger(CachedWebNotificationStorage.class);
  //
  private final static String WEB_NOTIFICATION_CACHING_NAME = "WebNotificationCache";
  private final static String LIST_WEB_NOTIFICATION_CACHING_NAME = "WebNotificationListCache";
  //
  private final ExoCache<WebNotifInfoCacheKey, WebNotifInfoData> exoWebNotificationCache;
  private final ExoCache<ListWebNotificationsKey, ListWebNotificationsData> exoWebNotificationsCache;
  //
  private FutureExoCache<WebNotifInfoCacheKey, WebNotifInfoData, ServiceContext<WebNotifInfoData>> futureWebNotificationCache;
  private FutureWebNotifExoCache<String, ListWebNotificationsKey, ListWebNotificationsData, ServiceContext<ListWebNotificationsData>> futureWebNotificationsCache;
  
  private final WebNotificationStorageImpl storage;
  
  public CachedWebNotificationStorage(WebNotificationStorageImpl storage, CacheService cacheService) {
    this.storage = storage;
    exoWebNotificationCache = cacheService.getCacheInstance(WEB_NOTIFICATION_CACHING_NAME);
    exoWebNotificationsCache = cacheService.getCacheInstance(LIST_WEB_NOTIFICATION_CACHING_NAME);
    //
    futureWebNotificationCache = createFutureCache(exoWebNotificationCache);
    futureWebNotificationsCache = createFutureWebNotifCache(exoWebNotificationsCache);
  }
  
  @Override
  public void save(NotificationInfo notification) {
     storage.save(notification);
  }

  @Override
  public void markRead(String notificationId) {
    storage.markRead(notificationId);
    //
    updateRead(notificationId, true);
  }
  
  public void updateRead(String notificationId, boolean isRead) {
    WebNotifInfoCacheKey key = WebNotifInfoCacheKey.key(notificationId);
    WebNotifInfoData infoData = exoWebNotificationCache.get(key);
    if (infoData != null) {
      infoData.updateRead(isRead);
    }
  }

  @Override
  public void markAllRead(String userId) {
    storage.markAllRead(userId);
  }

  @Override
  public void hidePopover(String notificationId) {
    storage.hidePopover(notificationId);
    // update data showPopover
    WebNotifInfoCacheKey key = WebNotifInfoCacheKey.key(notificationId);
    WebNotifInfoData infoData = exoWebNotificationCache.get(key);
    if (infoData != null) {
      infoData.updateShowPopover(false);
    }
    // update list
    removePopover(get(notificationId));
  }
  
  @Override
  public List<NotificationInfo> get(final WebNotificationFilter filter, final int offset, final int limit) {
    final ListWebNotificationsKey key = ListWebNotificationsKey.key(filter.getUserId(), filter.isOnPopover());
      //
    ListWebNotificationsData keys = futureWebNotificationsCache.get(
        new ServiceContext<ListWebNotificationsData>() {
          public ListWebNotificationsData execute() {
            List<NotificationInfo> got = storage.get(filter, offset, limit);
            return buildWebNotifDataIds(key, got);
          }
        }, key, offset, limit);
      //
    return buildNotifications(keys, offset, limit);
  }

  @Override
  public boolean remove(String notificationId) {
    NotificationInfo notification = get(notificationId);
    if (notification == null) {
      return false;
    }
    //
    storage.remove(notificationId);
    //
    removePopover(notification);
    //
    removeViewAll(notification);
    //
    WebNotifInfoCacheKey key = WebNotifInfoCacheKey.key(notificationId);
    exoWebNotificationCache.remove(key);
    //
    return true;
  }

  @Override
  public NotificationInfo get(final String notificationId) {
    WebNotifInfoCacheKey key = WebNotifInfoCacheKey.key(notificationId);
    WebNotifInfoData notificationInfo = futureWebNotificationCache.get(
        new ServiceContext<WebNotifInfoData>() {
          public WebNotifInfoData execute() {
            try {
              NotificationInfo got = storage.get(notificationId);
              if (got != null) {
                return new WebNotifInfoData(got);
              }
              return null;
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }
        }, key);
    //
    if(notificationInfo == null) {
      return null;
    }
    return notificationInfo.build();
  }

  public NotificationInfo getUnreadNotification(String pluginId, String activityId, String owner) {
    return storage.getUnreadNotification(pluginId, activityId, owner);
  }

  @Override
  public boolean remove(String userId, long seconds) {
    return storage.remove(userId, seconds);
  }
  
  private ListWebNotificationsData buildWebNotifDataIds(ListWebNotificationsKey key,
                                                        List<NotificationInfo> notifications) {
    ListWebNotificationsData data = this.exoWebNotificationsCache.get(key);
    if (data == null) {
      data = new ListWebNotificationsData(key);
      this.exoWebNotificationsCache.put(key, data);
    }
    //
    for (int i = 0, len = notifications.size(); i < len; i++) {
      NotificationInfo notif = notifications.get(i);
      // handle the activity is NULL
      if (notif == null) {
        continue;
      }
      //
      if (!data.contains(notif.getId())) {
        data.insertLast(notif.getId());
      }
    }
    return data;
  }
  
  private List<NotificationInfo> buildNotifications(ListWebNotificationsData data, final long offset, final long limit) {
    List<NotificationInfo> notifications = new ArrayList<NotificationInfo>();
    long to = Math.min(data.size(), offset + limit);
    List<String> ids = Collections.synchronizedList(data.subList((int)offset, (int)to));
    for (String id : ids) {
      NotificationInfo a = get(id);
      if (a != null) {
        notifications.add(a);
      }
    }
    return notifications;
  }

  @Override
  public void update(NotificationInfo notification) {
    storage.update(notification);
    //
    WebNotifInfoCacheKey key = WebNotifInfoCacheKey.key(notification.getId());
    exoWebNotificationCache.put(key, new WebNotifInfoData(notification));
    //
    moveTopPopover(notification);
    //
    moveTopViewAll(notification);
  }
  
  private void moveTopPopover(NotificationInfo notification) {
    ListWebNotificationsKey userPopoverKey = ListWebNotificationsKey.key(notification.getTo(), true);
    ListWebNotificationsData listData = exoWebNotificationsCache.get(userPopoverKey);
    if (listData != null) {
      listData.moveTop(notification.getId(), notification.getTo());
    }
  }

  private void moveTopViewAll(NotificationInfo notification) {
    ListWebNotificationsKey userViewAllKey = ListWebNotificationsKey.key(notification.getTo(), false);
    ListWebNotificationsData listData = exoWebNotificationsCache.get(userViewAllKey);
    if (listData != null) {
      listData.moveTop(notification.getId(), notification.getTo());
    }
  }

  private void removePopover(NotificationInfo notification) {
    ListWebNotificationsKey userPopoverKey = ListWebNotificationsKey.key(notification.getTo(), true);
    ListWebNotificationsData listData = exoWebNotificationsCache.get(userPopoverKey);
    if (listData != null) {
      listData.removeByValue(notification.getId());
    }
  }
  
  private void removeViewAll(NotificationInfo notification) {
    ListWebNotificationsKey userViewAllKey = ListWebNotificationsKey.key(notification.getTo(), false);
    ListWebNotificationsData listData = exoWebNotificationsCache.get(userViewAllKey);
    if (listData != null) {
      listData.removeByValue(notification.getId());
    }
  }
  
  /**
   * Clear the list caching
   * @param userId
   */
  private void clearCachingList(NotificationInfo notificationInfo) {
    
    try {
      exoWebNotificationsCache.select(new WebNotificationInfosSelector(notificationInfo));
    } catch (Exception e) {
      LOG.error("Failed when clear the caching list", e);
    }
  }
  
  private <T extends Serializable, K extends CacheKey, V extends AbstractWebNotifListData<K, T>> FutureWebNotifExoCache<T, K, V, ServiceContext<V>> createFutureWebNotifCache(ExoCache<K, V> cache) {
    return new FutureWebNotifExoCache<T, K, V, ServiceContext<V>>(new CacheLoader<K, V>(), cache);
  }
  
  private <K extends CacheKey, V extends Serializable> FutureExoCache<K, V, ServiceContext<V>> createFutureCache(ExoCache<K, V> cache) {
    return new FutureExoCache<K, V, ServiceContext<V>>(new CacheLoader<K, V>(), cache);
  }

}

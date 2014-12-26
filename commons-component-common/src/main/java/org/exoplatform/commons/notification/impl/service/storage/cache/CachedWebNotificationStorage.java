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
  private final static String WEB_NOTIFICATION_CACHING_NAME = "WebNotificationCaching";
  private final static String LIST_WEB_NOTIFICATION_CACHING_NAME = "WebNotificationsCaching";
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
    NotificationInfo notification = storage.get(notificationId);
    
    storage.markRead(notificationId);
    
    //
    WebNotifInfoCacheKey key = WebNotifInfoCacheKey.key(notificationId);
    exoWebNotificationCache.remove(key);
    clearCachingList(notification.getTo());
  }

  @Override
  public void markAllRead(String userId) {
    storage.markAllRead(userId);
    exoWebNotificationsCache.clearCache();
  }

  @Override
  public void hidePopover(String notificationId) {
    storage.hidePopover(notificationId);
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
    NotificationInfo notification = storage.get(notificationId);
    if (notification == null) {
      return false;
    }
    //
    storage.remove(notificationId);
    //
    WebNotifInfoCacheKey key = WebNotifInfoCacheKey.key(notificationId);
    exoWebNotificationCache.remove(key);
    clearCachingList(notification.getTo());
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
    return notificationInfo.build();
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
  
  /**
   * Clear the list caching
   * @param userId
   */
  private void clearCachingList(String userId) {
    try {
      if (userId != null) {
        exoWebNotificationsCache.select(new WebNotificationInfosSelector(userId));
      }
    } catch (Exception e) {
      LOG.error("Failed when clear the caching list", e);
    }
  }
  
  private <T extends Serializable, K extends CacheKey, V extends AbstractWebNotifListData<K, T>> FutureWebNotifExoCache<T, K, V, ServiceContext<V>> createFutureWebNotifCache(ExoCache<K, V> cache) {
    return new FutureWebNotifExoCache<T, K, V, ServiceContext<V>>(new CacheLoader<K, V>(), cache);
  }
  
  public <K extends CacheKey, V extends Serializable> FutureExoCache<K, V, ServiceContext<V>> createFutureCache(ExoCache<K, V> cache) {
    return new FutureExoCache<K, V, ServiceContext<V>>(new CacheLoader<K, V>(), cache);
  }
}

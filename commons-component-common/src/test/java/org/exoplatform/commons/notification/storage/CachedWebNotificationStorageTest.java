package org.exoplatform.commons.notification.storage;

import java.util.Calendar;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.notification.BaseNotificationTestCase;
import org.exoplatform.commons.notification.impl.service.storage.cache.CachedWebNotificationStorage;
import org.exoplatform.commons.notification.impl.service.storage.cache.model.*;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

public class CachedWebNotificationStorageTest extends BaseNotificationTestCase {
  protected WebNotificationStorage cachedStorage;
  private final static String WEB_NOTIFICATION_CACHING_NAME = "commons.WebNotificationCache";
  private final static String LIST_WEB_NOTIFICATION_CACHING_NAME = "commons.WebNotificationsCache";
  private final static String WEB_NOTIFICATION_COUNT_CACHING_NAME = "commons.WebNotificationsCache";
  private  CacheService cacheService;
  private ExoCache<ListWebNotificationsKey, ListWebNotificationsData> exoWebNotificationsCache;
  private ExoCache<WebNotifInfoCacheKey, WebNotifInfoData> exoWebNotificationCache;
  private ExoCache<WebNotifInfoCacheKey, IntegerData> exoWebNotificationCountCache;

  public CachedWebNotificationStorageTest() {
    setForceContainerReload(true);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    cachedStorage = getService(WebNotificationStorage.class);
    cacheService = getService(CacheService.class);
    cacheService.getCacheInstance(WEB_NOTIFICATION_CACHING_NAME);
    exoWebNotificationCache = cacheService.getCacheInstance(WEB_NOTIFICATION_CACHING_NAME);
    exoWebNotificationsCache = cacheService.getCacheInstance(LIST_WEB_NOTIFICATION_CACHING_NAME);
    exoWebNotificationCountCache = cacheService.getCacheInstance(WEB_NOTIFICATION_COUNT_CACHING_NAME);

    assertTrue(cachedStorage instanceof CachedWebNotificationStorage);
  }
  
  @Override
  public void tearDown() throws Exception {
    exoWebNotificationCache.clearCache();
    exoWebNotificationsCache.clearCache();
    exoWebNotificationCountCache.clearCache();
    
    super.tearDown();
  }
  
  public void testSave() throws Exception {
    String userId = "demo";
    userIds.add(userId);
    NotificationInfo info = makeWebNotificationInfo(userId);
    cachedStorage.save(info);
    //
    NotificationInfo notifInfo = cachedStorage.get(info.getId());
    assertNotNull(notifInfo);
    assertEquals(1, cachedStorage.get(new WebNotificationFilter(userId, false), 0, 10).size());
  }

  public void testGetNotifications() {
    String userId = "demo";
    userIds.add(userId);
    //
    List<NotificationInfo> onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    assertEquals(0, onPopoverInfos.size());
    for (int i = 0; i < 2; i++) {
      try {
        cachedStorage.save(makeWebNotificationInfo(userId));
      } catch (Exception e) {
        fail(e);
      }
    }
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    //
    assertEquals(2, onPopoverInfos.size());

    for (int i = 0; i < 20; i++) {
      try {
        cachedStorage.save(makeWebNotificationInfo(userId));
      } catch (Exception e) {
        fail(e);
      }
    }
    end();
    begin();

    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 5 , 10);
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 5 , 10);
    assertEquals(10, onPopoverInfos.size());

    //
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 5 , 15);
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 5 , 15);
    assertEquals(15, onPopoverInfos.size());

    //
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 30);
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 30);
    assertEquals(22, onPopoverInfos.size());
  }
  
  public void testRemove() throws Exception {
    String userId = "demo";
    userIds.add(userId);
    NotificationInfo info = makeWebNotificationInfo(userId);
    cachedStorage.save(info);
    //
    NotificationInfo notifInfo = cachedStorage.get(info.getId());
    assertEquals(1, cachedStorage.get(new WebNotificationFilter(userId, false), 0 , 10).size());
    
    NotificationInfo info1 = makeWebNotificationInfo(userId);
    cachedStorage.save(info1);
    //
    notifInfo = cachedStorage.get(info1.getId());
    assertNotNull(notifInfo);
    assertEquals(2, cachedStorage.get(new WebNotificationFilter(userId, false), 0 , 10).size());
    assertEquals(2, cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10).size());
    
    cachedStorage.remove(info1.getId());
    
    assertNull(cachedStorage.get(info1.getId()));
    assertEquals(1, cachedStorage.get(new WebNotificationFilter(userId, false), 0 , 10).size());
    assertEquals(1, cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10).size());
  }
  
  public void testRead() throws Exception {
    String userId = "demo";
    userIds.add(userId);
    NotificationInfo info = makeWebNotificationInfo(userId);
    cachedStorage.save(info);
    //
    NotificationInfo notifInfo = cachedStorage.get(info.getId());
    assertFalse(Boolean.valueOf(notifInfo.getValueOwnerParameter(NotificationMessageUtils.READ_PORPERTY.getKey())));
    //
    cachedStorage.markRead(notifInfo.getId());
    //
    notifInfo = cachedStorage.get(info.getId());
    assertTrue(Boolean.valueOf(notifInfo.getValueOwnerParameter(NotificationMessageUtils.READ_PORPERTY.getKey())));
  }

  public void testMarkAllRead() throws Exception {
    String userId = "demo";
    userIds.add(userId);
    for (int i = 0; i < 10; i++) {
      cachedStorage.save(makeWebNotificationInfo(userId));
    }
    //
    List<NotificationInfo> onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    assertEquals(10, onPopoverInfos.size());
    for (NotificationInfo notifInfo : onPopoverInfos) {
      assertFalse(Boolean.valueOf(notifInfo.getValueOwnerParameter(NotificationMessageUtils.READ_PORPERTY.getKey())));
    }
    List<NotificationInfo> viewAllInfos = cachedStorage.get(new WebNotificationFilter(userId, false), 0 , 10);
    assertEquals(10, viewAllInfos.size());
    for (NotificationInfo notifInfo : viewAllInfos) {
      assertFalse(Boolean.valueOf(notifInfo.getValueOwnerParameter(NotificationMessageUtils.READ_PORPERTY.getKey())));
    }
    //
    cachedStorage.markAllRead(userId);
    //
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    assertEquals(10, onPopoverInfos.size());
    for (NotificationInfo notifInfo : onPopoverInfos) {
      assertTrue(Boolean.valueOf(notifInfo.getValueOwnerParameter(NotificationMessageUtils.READ_PORPERTY.getKey())));
    }
    viewAllInfos = cachedStorage.get(new WebNotificationFilter(userId, false), 0 , 10);
    assertEquals(10, viewAllInfos.size());
    for (NotificationInfo notifInfo : viewAllInfos) {
      assertTrue(Boolean.valueOf(notifInfo.getValueOwnerParameter(NotificationMessageUtils.READ_PORPERTY.getKey())));
    }
    //
    for (NotificationInfo info : viewAllInfos) {
      WebNotifInfoCacheKey notifKey = WebNotifInfoCacheKey.key(info.getId());
      WebNotifInfoData notifData = exoWebNotificationCache.get(notifKey);
      NotificationInfo notifInfo = notifData.build();
      assertTrue(Boolean.valueOf(notifInfo.getValueOwnerParameter(NotificationMessageUtils.READ_PORPERTY.getKey())));
    }
  }

  public void testHidePopover() {
    String userId = "demo";
    userIds.add(userId);
    NotificationInfo info = makeWebNotificationInfo(userId);
    try {
      cachedStorage.save(info);
    } catch (Exception e) {
      fail(e);
    }
    //
    NotificationInfo notifInfo = cachedStorage.get(info.getId());
    assertTrue(notifInfo.isOnPopOver());
    //
    //checks caching
    List<NotificationInfo> infos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);

    ListWebNotificationsKey key = ListWebNotificationsKey.key(userId, true, 0, 10);
    ListWebNotificationsData listData = exoWebNotificationsCache.get(key);
    assertNotNull(listData);
    assertEquals(1,listData.size());

    //
    infos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    assertEquals(infos.get(0), notifInfo);
    assertTrue(infos.get(0).isOnPopOver());

    //
    cachedStorage.hidePopover(notifInfo.getId());

    //
    WebNotifInfoCacheKey notifKey = WebNotifInfoCacheKey.key(notifInfo.getId());
    WebNotifInfoData notifData = exoWebNotificationCache.get(notifKey);
    notifInfo = notifData.build();
    assertFalse(notifInfo.isOnPopOver());
  }
  
  public void testUpdate() throws Exception {
    String userId = "mary";
    userIds.add(userId);
    NotificationInfo info = makeWebNotificationInfo(userId);
    cachedStorage.save(info);
    //
    NotificationInfo createdFirstInfo = cachedStorage.get(info.getId());
    assertEquals(info.getTitle(), createdFirstInfo.getTitle());
    for (int i = 0; i < 5; i++) {
      // this sleep makes sure notifications are saved at different timestamps, so they are sorted correctly when fetching
      Thread.sleep(1);
      cachedStorage.save(makeWebNotificationInfo(userId));
    }
    end();
    begin();

    List<NotificationInfo> onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    assertEquals(6, onPopoverInfos.size());
    List<NotificationInfo> viewAllInfos = cachedStorage.get(new WebNotificationFilter(userId, false), 0 , 10);
    assertEquals(6, viewAllInfos.size());

    //
    NotificationInfo lastOnPopoverInfo = onPopoverInfos.get(onPopoverInfos.size() - 1);
    assertEquals(createdFirstInfo.getId(), lastOnPopoverInfo.getId());
    NotificationInfo lastViewAllInfos = viewAllInfos.get(viewAllInfos.size() - 1);
    assertEquals(createdFirstInfo.getId(), lastViewAllInfos.getId());
    //
    String newTitle = "The new title";
    createdFirstInfo.setTitle(newTitle);

    //
    cachedStorage.update(createdFirstInfo, true);
    end();
    begin();

    //
    createdFirstInfo = cachedStorage.get(info.getId());
    //
    assertEquals(newTitle, createdFirstInfo.getTitle());
    //
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    viewAllInfos = cachedStorage.get(new WebNotificationFilter(userId, false), 0 , 10);
    //
    NotificationInfo firstOnPopoverInfo = onPopoverInfos.get(0);
    assertEquals(newTitle, firstOnPopoverInfo.getTitle());
    NotificationInfo firstViewAllInfos = viewAllInfos.get(0);
    assertEquals(newTitle, firstViewAllInfos.getTitle());

    cachedStorage.resetNumberOnBadge(userId);
    //
    int num = cachedStorage.getNumberOnBadge(userId);
    assertEquals(0, num);
    //moveTop = false, mean no need to increase notification badge number
    cachedStorage.update(createdFirstInfo, false);
    num = cachedStorage.getNumberOnBadge(userId);
    assertEquals(0, num);

    cachedStorage.update(createdFirstInfo, true);
    num = cachedStorage.getNumberOnBadge(userId);
    assertEquals(1, num);
  }

  public void testRemoveByJob() throws Exception {
    // Create data for old notifications 
    /* Example:
     *  PastTime is 1/12/2014
     *  Today is 15/12/2014
     *  Create 50 notifications for:
     *   + 04/12/2014
     *   + 06/12/2014
     *   + 08/12/2014
     *   + 10/12/2014
     *   + 12/12/2014
     *  Case 1: Delay time 9 days, remove all web notification on days:
     *   + 04/12/2014
     *   + 06/12/2014
     *  Expected: remaining is 30 notifications on 3 days
     *  Case 2: Delay time 3 days, remove all web notification on days:
     *   + 08/12/2014
     *   + 10/12/2014
     *   + 12/12/2014
     *  Expected: remaining is 0 notification
    */
    int daySeconds = 86400;
    String userId = "demo";
    Calendar cal = Calendar.getInstance();
    long t = daySeconds * 1000l;
    long current = cal.getTimeInMillis();
    for (int i = 12; i > 3; i = i - 2) {
      cal.setTimeInMillis(current - i * t);
      for (int j = 0; j < 10; j++) {
        NotificationInfo notifInfi = makeWebNotificationInfo(userId);
        NotificationInfo info = notifInfi.setDateCreated(cal);
        notifInfi.setLastModifiedDate(cal);
        //
        cachedStorage.save(info);
      }
    }
    // check data on cache
    List<NotificationInfo>  info = cachedStorage.get(new WebNotificationFilter(userId, false), 0, 60);
    assertEquals(50, info.size());
    //
    cachedStorage.remove(userId, 9 * daySeconds);
    //
    info = cachedStorage.get(new WebNotificationFilter(userId, false), 0, 60);
    assertEquals(30, info.size());
    //
    cachedStorage.remove(userId, 3 * daySeconds);
    info = cachedStorage.get(new WebNotificationFilter(userId, false), 0, 60);
    assertEquals(0, info.size());
  }
  
  public void testGetNewMessage() throws Exception  {
    assertEquals(8, NotificationMessageUtils.getMaxItemsInPopover());
    String userId = "root";
    userIds.add(userId);
    assertEquals(0, cachedStorage.getNumberOnBadge(userId));
    //
    cachedStorage.save(makeWebNotificationInfo(userId));
    //
    assertEquals(1, cachedStorage.getNumberOnBadge(userId));
    cachedStorage.save(makeWebNotificationInfo(userId));
    assertEquals(2, cachedStorage.getNumberOnBadge(userId));
    for (int i = 0; i < 10; ++i) {
      cachedStorage.save(makeWebNotificationInfo(userId));
    }
    //
    List<NotificationInfo> list = cachedStorage.get(new WebNotificationFilter(userId), 0, 15);
    assertEquals(12, list.size());
    //
    assertEquals(12, cachedStorage.getNumberOnBadge(userId));
    //
    cachedStorage.resetNumberOnBadge(userId);
    //
    assertEquals(0, cachedStorage.getNumberOnBadge(userId));
  }
}

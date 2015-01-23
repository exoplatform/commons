package org.exoplatform.commons.notification.storage;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.notification.BaseNotificationTestCase;
import org.exoplatform.commons.notification.impl.service.storage.cache.model.ListWebNotificationsData;
import org.exoplatform.commons.notification.impl.service.storage.cache.model.ListWebNotificationsKey;
import org.exoplatform.commons.notification.impl.service.storage.cache.model.WebNotifInfoCacheKey;
import org.exoplatform.commons.notification.impl.service.storage.cache.model.WebNotifInfoData;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

public class CachedWebNotificationStorageTest extends BaseNotificationTestCase {
  private WebNotificationStorage cachedStorage;
  private final static String WEB_NOTIFICATION_CACHING_NAME = "WebNotificationCaching";
  private final static String LIST_WEB_NOTIFICATION_CACHING_NAME = "WebNotificationsCaching";
  private  CacheService cacheService;
  private ExoCache<ListWebNotificationsKey, ListWebNotificationsData> exoWebNotificationsCache;
  private ExoCache<WebNotifInfoCacheKey, WebNotifInfoData> exoWebNotificationCache;

  //
  @Override
  public void setUp() throws Exception {
    initCollaborationWorkspace();
    super.setUp();
    cachedStorage = getService(WebNotificationStorage.class);
    cacheService = getService(CacheService.class);
    cacheService.getCacheInstance(WEB_NOTIFICATION_CACHING_NAME);
    exoWebNotificationCache = cacheService.getCacheInstance(WEB_NOTIFICATION_CACHING_NAME);
    exoWebNotificationsCache = cacheService.getCacheInstance(LIST_WEB_NOTIFICATION_CACHING_NAME);
  }
  
  @Override
  public void tearDown() throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    for (String userId : userIds) {
      Node userNodeApp = nodeHierarchyCreator.getUserApplicationNode(sessionProvider, userId);
      if (userNodeApp.hasNode(NOTIFICATIONS)) {
        userNodeApp.getNode(NOTIFICATIONS).remove();
        userNodeApp.save();
      }
    }
    
    exoWebNotificationCache.clearCache();
    exoWebNotificationsCache.clearCache();
    
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
  
  private ListWebNotificationsData getWebNotificationsData(ListWebNotificationsKey key) {
    ListWebNotificationsData data = exoWebNotificationsCache.get(key);
    if (data == null) {
      data = new ListWebNotificationsData(key);
      exoWebNotificationsCache.put(key, data);
    }
    return data;
  }

  public void testGetNotifications() {
    String userId = "demo";
    userIds.add(userId);
    //
    ListWebNotificationsKey key = ListWebNotificationsKey.key(userId, true);
    List<NotificationInfo> onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    assertEquals(0, onPopoverInfos.size());
    for (int i = 0; i < 2; i++) {
      cachedStorage.save(makeWebNotificationInfo(userId));
    }
    ListWebNotificationsData data = getWebNotificationsData(key);
    assertFalse(data.isMax());
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    assertTrue(data.isMax());
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    //
    assertEquals(2, onPopoverInfos.size());
    for (int i = 0; i < 20; i++) {
      cachedStorage.save(makeWebNotificationInfo(userId));
    }
    assertFalse(data.isMax());
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 5 , 10);
    assertFalse(data.isMax());
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 5 , 10);
    //
    assertEquals(10, onPopoverInfos.size());
    assertFalse(data.isMax());
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 5 , 15);
    assertFalse(data.isMax());
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 5 , 15);
    //
    assertEquals(15, onPopoverInfos.size());
    //
    assertFalse(data.isMax());
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 30);
    assertTrue(data.isMax());
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 30);
    assertEquals(22, onPopoverInfos.size());
    assertTrue(data.isMax());
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
    cachedStorage.save(info);
    //
    NotificationInfo notifInfo = cachedStorage.get(info.getId());
    assertTrue(Boolean.valueOf(notifInfo.getValueOwnerParameter(NotificationMessageUtils.SHOW_POPOVER_PROPERTY.getKey())));
    //
    ListWebNotificationsKey key = ListWebNotificationsKey.key(userId, true);
    //checks caching
    List<NotificationInfo> infos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    ListWebNotificationsData listData = exoWebNotificationsCache.get(key);
    assertNotNull(listData);
    assertEquals(1,listData.size());
    //
    infos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    assertEquals(infos.get(0), notifInfo);
    assertTrue(Boolean.valueOf(infos.get(0).getValueOwnerParameter(NotificationMessageUtils.SHOW_POPOVER_PROPERTY.getKey())));
    //
    cachedStorage.hidePopover(notifInfo.getId());
    //checks caching
    listData = exoWebNotificationsCache.get(key);
    assertNotNull(listData);
    assertEquals(0,listData.size());
    //
    WebNotifInfoCacheKey notifKey = WebNotifInfoCacheKey.key(info.getId());
    WebNotifInfoData notifData = exoWebNotificationCache.get(notifKey);
    notifInfo = notifData.build();
    assertFalse(Boolean.valueOf(notifInfo.getValueOwnerParameter(NotificationMessageUtils.SHOW_POPOVER_PROPERTY.getKey())));
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
      cachedStorage.save(makeWebNotificationInfo(userId));
    }
    List<NotificationInfo> onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    assertEquals(6, onPopoverInfos.size());
    List<NotificationInfo> viewAllInfos = cachedStorage.get(new WebNotificationFilter(userId, false), 0 , 10);
    assertEquals(6, viewAllInfos.size());
    //
    NotificationInfo lastOnPopoverInfo = onPopoverInfos.get(onPopoverInfos.size() - 1);
    assertEquals(createdFirstInfo.getId(), lastOnPopoverInfo.getId());
    NotificationInfo lastViewAllInfos = onPopoverInfos.get(onPopoverInfos.size() - 1);
    assertEquals(createdFirstInfo.getId(), lastViewAllInfos.getId());
    //
    String newTitle = "The new title";
    createdFirstInfo.setTitle(newTitle);
    //
    cachedStorage.update(createdFirstInfo, true);
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
  }
  
  public void testGetNewMessage() throws Exception  {
    assertEquals(8, NotificationMessageUtils.getMaxItemsInPopover());
    //
    String userId = "root";
    userIds.add(userId);
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
    assertEquals(9, cachedStorage.getNumberOnBadge(userId));
    //
    cachedStorage.resetNumberOnBadge(userId);
    //
    assertEquals(0, cachedStorage.getNumberOnBadge(userId));
  }
}

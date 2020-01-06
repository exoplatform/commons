package org.exoplatform.commons.notification.storage;

import java.util.Calendar;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.notification.BaseNotificationTestCase;
import org.exoplatform.commons.notification.plugin.PluginTest;

public class WebNotificationStorageTest extends BaseNotificationTestCase {

  public WebNotificationStorageTest() {
    setForceContainerReload(true);
  }

  public void testSaveWebNotification() throws Exception {
    String userId = "root";
    userIds.add(userId);
    NotificationInfo info = makeWebNotificationInfo(userId);
    storage.save(info);
    //
    assertEquals(1, storage.get(new WebNotificationFilter(userId), 0, 10).size());
  }

  public void testMarkRead() throws Exception {
    String userId = "root";
    userIds.add(userId);
    NotificationInfo info = makeWebNotificationInfo(userId);
    storage.save(info);
    List<NotificationInfo> list = storage.get(new WebNotificationFilter(userId), 0, 10);
    assertEquals(1, list.size());
    NotificationInfo notif = list.get(0);
    assertFalse(Boolean.valueOf(notif.getOwnerParameter().get(NotificationMessageUtils.READ_PORPERTY.getKey())));
    //
    storage.markRead(notif.getId());
    //
    notif = storage.get(notif.getId());
    assertTrue(Boolean.valueOf(notif.getOwnerParameter().get(NotificationMessageUtils.READ_PORPERTY.getKey())));
  }

  public void testMarkReadAll() throws Exception {
    String userId = "demo";
    userIds.add(userId);
    for (int i = 0; i < 10; i++) {
      NotificationInfo info = makeWebNotificationInfo(userId);
      storage.save(info);
    }
    List<NotificationInfo> list = storage.get(new WebNotificationFilter(userId), 0, 10);
    assertEquals(10, list.size());
    for(NotificationInfo notif : list) {
      assertFalse(notif.isRead());
    }
    //
    storage.markAllRead(userId);
    //
    list = storage.get(new WebNotificationFilter(userId), 0, 10);
    assertEquals(10, list.size());
    //
    for(NotificationInfo notif : list) {
      assertTrue(notif.isRead());
    }
  }
  
  public void testUpdateNotification() throws Exception {
    String userId = "john";
    NotificationInfo info = makeWebNotificationInfo(userId);
    storage.save(info);
    String notifId = info.getId();
    NotificationInfo got = storage.get(notifId);
    assertNotNull(got);
    assertEquals("The title", got.getTitle());
    long lastUpdatedTime = got.getLastModifiedDate();
    
    //update and move top, the lastUpdatedTime will be modified
    got = makeWebNotificationInfo(userId);
    got.setId(notifId);
    got.setTitle("new title");
    storage.update(got, true);
    restartTransaction();

    got = storage.get(got.getId());
    assertEquals("new title", got.getTitle());
    assertFalse(got.getLastModifiedDate() + " should have been modified and not equal anymore to " + lastUpdatedTime, lastUpdatedTime == got.getLastModifiedDate());
    
    //update but don't move top, the lastUpdatedTime will not be modified
    lastUpdatedTime = got.getLastModifiedDate();
    got.setTitle("new new title");
    storage.update(got, false);
    restartTransaction();
    got = storage.get(notifId);
    assertEquals("new new title", got.getTitle());
    assertTrue(got.getLastModifiedDate() + " should be equal to " + lastUpdatedTime, lastUpdatedTime == got.getLastModifiedDate());
  }

  public void testGetNewMessage() throws Exception  {
    assertEquals(8, NotificationMessageUtils.getMaxItemsInPopover());
    //
    String userId = "root";
    userIds.add(userId);
    storage.save(makeWebNotificationInfo(userId));
    //
    assertEquals(1, storage.getNumberOnBadge(userId));
    storage.save(makeWebNotificationInfo(userId));
    assertEquals(2, storage.getNumberOnBadge(userId));
    for (int i = 0; i < 10; ++i) {
      storage.save(makeWebNotificationInfo(userId));
    }
    //
    List<NotificationInfo> list = storage.get(new WebNotificationFilter(userId), 0, 15);
    assertEquals(12, list.size());
    //
    assertEquals(12, storage.getNumberOnBadge(userId));
    //
    storage.resetNumberOnBadge(userId);
    //
    assertEquals(0, storage.getNumberOnBadge(userId));
  }
  
  public void testSpecialUserNameToGetMessage() throws Exception {
    //Test with methods: getUnreadNotification, getNewMessage and remove
    String userId = "don't_blink_polarity";
    userIds.add(userId);
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(cal.getTimeInMillis() - 3 * 86400000l);
    //
    NotificationInfo notification = makeWebNotificationInfo(userId);
    notification.setDateCreated(cal);
    notification.setLastModifiedDate(cal);
    storage.save(notification);
    assertEquals(PluginTest.ID, storage.getUnreadNotification(PluginTest.ID, "TheActivityId", userId).getKey().getId());
    assertEquals(1, storage.getNumberOnBadge(userId));
    assertTrue(storage.remove(userId, 2 * 86400));
    //
    assertEquals(0, storage.getNumberOnBadge(userId));
  }

}
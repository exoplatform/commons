package org.exoplatform.commons.notification.storage;

import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.notification.BaseNotificationTestCase;
import org.exoplatform.commons.notification.plugin.PluginTest;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

public class WebNotificationStorageTest extends BaseNotificationTestCase {

  public WebNotificationStorageTest() {
    setForceContainerReload(true);
  }

  @Override
  public void setUp() throws Exception {
    initCollaborationWorkspace();
    super.setUp();
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
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
    String notifId = info.getId();
    storage.save(info);
    NotificationInfo got = storage.get(notifId);
    assertEquals("The title", got.getTitle());
    long lastUpdatedTime = got.getLastModifiedDate();
    
    //update and move top, the lastUpdatedTime will be modified
    got = makeWebNotificationInfo(userId);
    got.setId(notifId);
    got.setTitle("new title");
    storage.update(got, true);
    got = storage.get(notifId);
    assertEquals("new title", got.getTitle());
    assertFalse(lastUpdatedTime == got.getLastModifiedDate());
    
    //update but don't move top, the lastUpdatedTime will not be modified
    lastUpdatedTime = got.getLastModifiedDate();
    got.setTitle("new new title");
    storage.update(got, false);
    got = storage.get(notifId);
    assertEquals("new new title", got.getTitle());
    assertTrue(lastUpdatedTime == got.getLastModifiedDate());
  }
  
  public void testRemoveByJob() throws Exception {
    // Create data for old notifications 
    /* Example:
     *  PastTime is 1/12/2014
     *  Today is 15/12/2014
     *  Create notification for:
     *   + 04/12/2014
     *   + 06/12/2014
     *   + 08/12/2014
     *   + 10/12/2014
     *   + 12/12/2014
     *  Case 1: Delay time 9 days, remove all web notification on days:
     *   + 04/12/2014
     *   + 06/12/2014
     *  Expected: remaining is 30 notifications / 3 days
     *  Case 2: Delay time 3 days, remove all web notification on days:
     *   + 08/12/2014
     *   + 10/12/2014
     *   + 12/12/2014
     *  Expected: remaining is 0 notification
    */
    long daySeconds = 86400;
    String userId = "demo";
    Calendar cal = Calendar.getInstance();
    long t = 86400000l;
    long current = cal.getTimeInMillis();
    for (int i = 12; i > 3; i = i - 2) {
      cal.setTimeInMillis(current - i * t);
      for (int j = 0; j < 10; j++) {
        NotificationInfo info = makeWebNotificationInfo(userId)
              .setDateCreated(cal)
              .setLastModifiedDate(cal);
        //
        storage.save(info);
      }
    }
    // check data
    //getWebUserDateNode
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    Node parentNode = getOrCreateChannelNode(sProvider, userId);
    assertEquals(5, parentNode.getNodes().getSize());
    //
    NodeIterator iter = null;
    for (int i = 4; i < 13; i = i + 2) {
      cal.setTimeInMillis(current - i * t);
      Node node = getOrCreateWebDateNode(sProvider, cal, userId);
      iter = node.getNodes();
      assertEquals(10, iter.getSize());
    }
    //
    storage.remove(userId, 9 * daySeconds);
    //
    assertEquals(3, parentNode.getNodes().getSize());
    //
    storage.remove(userId, 3 * daySeconds);
    assertEquals(0, parentNode.getNodes().getSize());
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
    storage.save(makeWebNotificationInfo(userId).setDateCreated(cal));
    assertEquals(PluginTest.ID, storage.getUnreadNotification(PluginTest.ID, "TheActivityId", userId).getKey().getId());
    assertEquals(1, storage.getNumberOnBadge(userId));
    assertTrue(storage.remove(userId, 2 * 86400));
    //
    assertEquals(0, storage.getNumberOnBadge(userId));
  }

  public void testRemoveByLiveTime() throws Exception {
    // Create data for old notifications 
    /* Example:
     *  PastTime is 1/12/2014
     *  Today is 15/12/2014
     *  Create notification for:
     *   + 04/12/2014
     *   + 06/12/2014
     *   + 08/12/2014
     *   + 10/12/2014
     *   + 12/12/2014
     *  Case 1: Delay time 9 days, remove all web notification on days:
     *   + 04/12/2014
     *   + 06/12/2014
     *  Expected: remaining is 30 notifications / 3 days
     *  Case 2: Delay time 3 days, remove all web notification on days:
     *   + 08/12/2014
     *   + 10/12/2014
     *   + 12/12/2014
     *  Expected: remaining is 0 notification
    */
    long daySeconds = 86400;
    String userId = "demo";
    Calendar cal = Calendar.getInstance();
    long t = 86400000l;
    long current = cal.getTimeInMillis();
    for (int i = 12; i > 3; i = i - 2) {
      cal.setTimeInMillis(current - i * t);
      for (int j = 0; j < 10; j++) {
        NotificationInfo info = makeWebNotificationInfo(userId).setDateCreated(cal);
        //
        storage.save(info);
      }
    }
    // check data
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    Node parentNode = getOrCreateChannelNode(sProvider, userId);
    assertEquals(5, parentNode.getNodes().getSize());
    //
    storage.remove(userId, 9 * daySeconds);
    //
    assertEquals(3, parentNode.getNodes().getSize());
    //
    storage.remove(userId, 3 * daySeconds);
    assertEquals(0, parentNode.getNodes().getSize());
  }
}
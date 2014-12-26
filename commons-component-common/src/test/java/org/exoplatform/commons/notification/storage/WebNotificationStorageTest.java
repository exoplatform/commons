package org.exoplatform.commons.notification.storage;

import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.notification.BaseNotificationTestCase;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.mortbay.log.Log;

public class WebNotificationStorageTest extends BaseNotificationTestCase {

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
    assertFalse(Boolean.valueOf(notif.getOwnerParameter().get(AbstractService.NTF_READ)));
    //
    storage.markRead(notif.getId());
    //
    notif = storage.get(notif.getId());
    assertTrue(Boolean.valueOf(notif.getOwnerParameter().get(AbstractService.NTF_READ)));
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
      assertFalse(Boolean.valueOf(notif.getOwnerParameter().get(AbstractService.NTF_READ)));
    }
    //
    storage.markAllRead(userId);
    //
    list = storage.get(new WebNotificationFilter(userId), 0, 10);
    assertEquals(10, list.size());
    //
    for(NotificationInfo notif : list) {
      assertTrue(Boolean.valueOf(notif.getValueOwnerParameter(AbstractService.NTF_READ)));
    }
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
        NotificationInfo info = makeWebNotificationInfo(userId).setDateCreated(cal);
        //
        storage.save(info);
      }
    }
    // check data
    //getWebUserDateNode
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    Node parentNode = getOrCreateChannelNode(sProvider, userId);
    NodeIterator it = parentNode.getNodes();
    while(it.hasNext()) {
      Log.info("Node path = " + it.nextNode().getPath());
    }
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
}
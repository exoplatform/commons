package org.exoplatform.commons.notification.storage;

import javax.jcr.Node;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.notification.BaseNotificationTestCase;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

public class CachedWebNotificationStorageTest extends BaseNotificationTestCase {
  @Override
  public void setUp() throws Exception {
    initCollaborationWorkspace();
    super.setUp();
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
    super.tearDown();
  }
  
  public void testSave() throws Exception {
    String userId = "root";
    userIds.add(userId);
    NotificationInfo info = makeWebNotificationInfo(userId);
    storage.save(info);
    //
    NotificationInfo notifInfo = storage.get(info.getId());
    assertNotNull(notifInfo);
    assertEquals(1, storage.get(new WebNotificationFilter(userId, false), 0, 10).size());
  }
  
  public void testRemove() throws Exception {
    String userId = "demo";
    userIds.add(userId);
    NotificationInfo info = makeWebNotificationInfo(userId);
    storage.save(info);
    //
    NotificationInfo notifInfo = storage.get(info.getId());
    assertEquals(1, storage.get(new WebNotificationFilter(userId, false), 0 , 10).size());
    
    NotificationInfo info1 = makeWebNotificationInfo(userId);
    storage.save(info1);
    //
    notifInfo = storage.get(info1.getId());
    assertEquals(2, storage.get(new WebNotificationFilter(userId, false), 0 , 10).size());
    
    storage.remove(notifInfo.getId());
    
    notifInfo = storage.get(info.getId());
    assertEquals(1, storage.get(new WebNotificationFilter(userId, false), 0 , 10).size());
  }
  
}

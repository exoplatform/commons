package org.exoplatform.commons.notification;

import java.util.Calendar;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.service.storage.NotificationDataStorage;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.testing.BaseCommonsTestCase;

public class NotificationServiceTest extends BaseCommonsTestCase {
  
  private NotificationService notificationService;
  
  private NotificationDataStorage notificationDataStorage;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    notificationService = getService(NotificationService.class);
    assertNotNull(notificationService);
    
    notificationDataStorage = getService(NotificationDataStorage.class);
    assertNotNull(notificationDataStorage);
  }
  
  public void testGetMessagesByProviderId() throws Exception {
    
    Map<String, NotificationInfo> list = notificationService.getNotificationMessagesByProviderId("NewUserPlugin", false);
    assertEquals(0, list.size());
    
    NotificationInfo message1 = new NotificationInfo();
    message1.setFrom("root");
    message1.key("NewUserPlugin");
    notificationDataStorage.save(message1);
    addMixin(message1.getId());
    
    NotificationInfo message2 = new NotificationInfo();
    message2.key("ActivityCommentPlugin");
    notificationDataStorage.save(message2);
    addMixin(message2.getId());
    
    NotificationInfo message3 = new NotificationInfo();
    message3.setFrom("demo");
    message3.key("NewUserPlugin");
    notificationDataStorage.save(message3);
    addMixin(message3.getId());
    
    list = notificationService.getNotificationMessagesByProviderId("NewUserPlugin", false);
    assertEquals(2, list.size());
    //check order by date
    assertEquals(message1.getId(), list.get("root").getId());
    assertEquals(message3.getId(), list.get("demo").getId());
    
    //clear all message of type NewUserPlugin
    notificationService.removeNotificationMessages("NewUserPlugin");
    list = notificationService.getNotificationMessagesByProviderId("NewUserPlugin", false);
    assertEquals(0, list.size());
  }
  
  private void addMixin(String msgId) throws Exception {
    StringBuffer queryBuffer = new StringBuffer("/jcr:root");
    queryBuffer.append("/eXoNotification/messageHome").append("//element(*,").append("ntf:message").append(")").append("[fn:name() = '").append(msgId).append("']");

    QueryManager qm = session.getWorkspace().getQueryManager();
    Query query = qm.createQuery(queryBuffer.toString(), Query.XPATH);
    NodeIterator iter = query.execute().getNodes();
    Node msgNode = (iter.getSize() > 0) ? iter.nextNode() : null;
    if (msgNode != null) {
      msgNode.addMixin("exo:datetime");
      msgNode.setProperty("exo:dateCreated", Calendar.getInstance());
      session.save();
    }
  }


}

package org.exoplatform.commons.notification;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.NotificationKey;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.model.UserSetting.FREQUENCY;
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
  
  @Override
  public void tearDown() throws Exception {
    // remove all notification node
    Node homeNode = (Node) session.getItem("/eXoNotification/messageHome");
    NodeIterator iterator = homeNode.getNodes();
    while (iterator.hasNext()) {
      Node node = (iterator.nextNode());
      System.out.println("\n remove " + node.getPath());
      node.remove();
    }
    session.save();
    super.tearDown();
  }
  
  private NotificationInfo saveNotification() throws Exception {
    NotificationInfo notification = NotificationInfo.instance();
    Map<String, String> params = new HashMap<String, String>();
    params.put("objectId", "idofobject");
    notification.key("TestPlugin").setSendToDaily("root")
                .setSendToWeekly("demo").setOwnerParameter(params).setOrder(1);
    notificationDataStorage.save(notification);
    addMixin(notification.getId());
    return notification;
  }

  public void testSave() throws Exception {
    NotificationInfo notification = saveNotification();
    //
    Node node = getMessageNodeByKeyIdAndParam("TestPlugin", "objectId=idofobject");
    assertNotNull(node);
    
    NotificationInfo notification2 = fillModel(node);
    
    assertTrue(notification2.equals(notification));
    
  }
  
  public void testNormalGetByUserAndRemoveMessagesSent() throws Exception {
    NotificationConfiguration configuration = getService(NotificationConfiguration.class);
    configuration.setSendWeekly(false);
    NotificationInfo notification = saveNotification();
    UserSetting userSetting = UserSetting.getInstance();
    userSetting.setUserId("root")
               .addProvider("TestPlugin", FREQUENCY.DAILY);
    userSetting.setActive(true);
    
    Map<NotificationKey, List<NotificationInfo>> map = notificationDataStorage.getByUser(userSetting);
    
    List<NotificationInfo> list = map.get(new NotificationKey("TestPlugin"));
    assertEquals(1, list.size());
    
    assertTrue(list.get(0).equals(notification));
    // after sent, user demo will auto remove from property daily
    Node node = getMessageNodeByKeyIdAndParam("TestPlugin", "objectId=idofobject");
    assertNotNull(node);
    
    NotificationInfo notification2 = fillModel(node);
    
    assertEquals(0, notification2.getSendToDaily().length);
    
    configuration.setSendWeekly(true);
    userSetting.setUserId("demo").addProvider("TestPlugin", FREQUENCY.WEEKLY);
    map = notificationDataStorage.getByUser(userSetting);
    list = map.get(new NotificationKey("TestPlugin"));
    assertEquals(1, list.size());
    
    
    notificationDataStorage.removeMessageAfterSent();
    
    node = getMessageNodeByKeyIdAndParam("TestPlugin", "objectId=idofobject");
    assertNull(node);
  }

  public void testSpecialGetByUserAndRemoveMessagesSent() throws Exception {
    NotificationConfiguration configuration = getService(NotificationConfiguration.class);
    NotificationInfo notification = NotificationInfo.instance();
    Map<String, String> params = new HashMap<String, String>();
    params.put("objectId", "idofobject");
    notification.key("TestPlugin").setSendAll(true)
                .setOwnerParameter(params).setOrder(1);
    notificationDataStorage.save(notification);
    
    UserSetting userSetting = UserSetting.getInstance();
    userSetting.setUserId("root").addProvider("TestPlugin", FREQUENCY.DAILY);
    userSetting.setActive(true);
    // Test send to daily
    configuration.setSendWeekly(false);
    Map<NotificationKey, List<NotificationInfo>> map = notificationDataStorage.getByUser(userSetting);
    
    List<NotificationInfo> list = map.get(new NotificationKey("TestPlugin"));
    assertEquals(1, list.size());
    
    assertTrue(list.get(0).equals(notification));
    // check value from node
    Node node = getMessageNodeByKeyIdAndParam("TestPlugin", "objectId=idofobject");
    assertNotNull(node);
    
    assertEquals(NotificationInfo.FOR_ALL_USER, fillModel(node).getSendToDaily()[0]);
    // remove value on property sendToDaily
    notificationDataStorage.removeMessageAfterSent();

    // after sent, the value on on property sendToDaily will auto removed
    node = getMessageNodeByKeyIdAndParam("TestPlugin", "objectId=idofobject");
    assertEquals(0, fillModel(node).getSendToDaily().length);
    
    // Test send to weekly
    configuration.setSendWeekly(true);
    userSetting.setUserId("demo").addProvider("TestPlugin", FREQUENCY.WEEKLY);
    map = notificationDataStorage.getByUser(userSetting);
    list = map.get(new NotificationKey("TestPlugin"));
    assertEquals(1, list.size());
    
    notificationDataStorage.removeMessageAfterSent();
    
    node = getMessageNodeByKeyIdAndParam("TestPlugin", "objectId=idofobject");
    assertNull(node);
  }
  
  
  private void addMixin(String msgId) throws Exception {
    Node msgNode = getMessageNodeById(msgId);
    if (msgNode != null) {
      msgNode.addMixin("exo:datetime");
      msgNode.setProperty("exo:dateCreated", Calendar.getInstance());
      session.save();
    }
  }
  
  private NotificationInfo fillModel(Node node) throws Exception {
    if(node == null) return null;
    NotificationInfo message = NotificationInfo.instance()
      .setFrom(node.getProperty("ntf:from").getString())
      .setOrder(Integer.valueOf(node.getProperty("ntf:order").getString()))
      .key(node.getProperty("ntf:providerType").getString())
      .setOwnerParameter(node.getProperty("ntf:ownerParameter").getValues())
      .setSendToDaily(NotificationUtils.valuesToArray(node.getProperty("ntf:sendToDaily").getValues()))
      .setSendToWeekly(NotificationUtils.valuesToArray(node.getProperty("ntf:sendToWeekly").getValues()))
      .setId(node.getName());
    
    return message;
  }
  
  private Node getMessageNodeById(String msgId) throws Exception {
    return getMessageNode(new StringBuffer("[fn:name() = '").append(msgId).append("']").toString(), "");
  }

  private Node getMessageNodeByKeyIdAndParam(String key, String param) throws Exception {
    key = "/" + key;
    return getMessageNode(new StringBuffer("[jcr:like(@ntf:ownerParameter, '%").append(param).append("%')]").toString(), key);
  }
  
  private Node getMessageNode(String strQuery, String key) throws Exception {
    StringBuffer queryBuffer = new StringBuffer("/jcr:root");
    queryBuffer.append("/eXoNotification/messageHome").append(key).append("//element(*,").append("ntf:message").append(")").append(strQuery);

    QueryManager qm = session.getWorkspace().getQueryManager();
    Query query = qm.createQuery(queryBuffer.toString(), Query.XPATH);
    NodeIterator iter = query.execute().getNodes();
    return (iter.getSize() > 0) ? iter.nextNode() : null;
  }

}

package org.exoplatform.commons.notification;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.NotificationKey;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.model.UserSetting.FREQUENCY;
import org.exoplatform.commons.api.notification.service.storage.NotificationDataStorage;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.notification.job.NotificationJob;
import org.exoplatform.commons.testing.BaseCommonsTestCase;

public class NotificationServiceTest extends BaseCommonsTestCase {
  
  private NotificationService       notificationService;
  private NotificationDataStorage   notificationDataStorage;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    notificationService = getService(NotificationService.class);
    notificationDataStorage = getService(NotificationDataStorage.class);
  }
  
  @Override
  public void tearDown() throws Exception {
    Node homeNode = (Node) session.getItem("/eXoNotification/messageHome");
    NodeIterator iterator = homeNode.getNodes();
    while (iterator.hasNext()) {
      Node node = (iterator.nextNode());
      node.remove();
    }
    session.save();
    super.tearDown();
  }
  
  private NotificationInfo saveNotification(String userDaily, String userWeekly) throws Exception {
    NotificationInfo notification = NotificationInfo.instance();
    Map<String, String> params = new HashMap<String, String>();
    params.put("objectId", "idofobject");
    notification.key("TestPlugin").setSendToDaily(userDaily)
                .setSendToWeekly(userWeekly).setOwnerParameter(params).setOrder(1);
    notificationDataStorage.save(notification);
    addMixin(notification.getId());
    return notification;
  }
  
  public void testServiceNotNull() throws Exception {
    assertNotNull(notificationService);
    assertNotNull(notificationDataStorage);
    saveNotification("root", "demo");
  }

  public void testSave() throws Exception {
    NotificationInfo notification = saveNotification("root", "demo");
    
    NotificationInfo notification2 = getNotificationInfoByKeyIdAndParam("TestPlugin", "objectId=idofobject");
    assertNotNull(notification2);
    
    assertTrue(notification2.equals(notification));
    
  }
  
  public void testNormalGetByUserAndRemoveMessagesSent() throws Exception {
    NotificationInfo notification = saveNotification("root", "demo");
    UserSetting userSetting = UserSetting.getInstance();
    userSetting.setUserId("root").addProvider("TestPlugin", FREQUENCY.DAILY);
    userSetting.setActive(true);
    NotificationContext context = NotificationContextImpl.cloneInstance();
    context.append(NotificationJob.JOB_DAILY, true);
    context.append(NotificationJob.JOB_WEEKLY, false);
    Map<NotificationKey, List<NotificationInfo>> map = notificationDataStorage.getByUser(context, userSetting);
    
    List<NotificationInfo> list = map.get(new NotificationKey("TestPlugin"));
    assertEquals(1, list.size());
    
    assertTrue(list.get(0).equals(notification));
    // after sent, user demo will auto remove from property daily
    NotificationInfo notification2 = getNotificationInfoByKeyIdAndParam("TestPlugin", "objectId=idofobject");
    assertNotNull(notification2);
    
    assertEquals(0, notification2.getSendToDaily().length);
    
    context = NotificationContextImpl.cloneInstance();
    context.append(NotificationJob.JOB_DAILY, false);
    context.append(NotificationJob.JOB_WEEKLY, true);
    
    userSetting.setUserId("demo").addProvider("TestPlugin", FREQUENCY.WEEKLY);
    map = notificationDataStorage.getByUser(context, userSetting);
    list = map.get(new NotificationKey("TestPlugin"));
    assertEquals(1, list.size());
    
    
    notificationDataStorage.removeMessageAfterSent();
    
    notification2 = getNotificationInfoByKeyIdAndParam("TestPlugin", "objectId=idofobject");
    assertNull(notification2);
  }

  public void testSpecialGetByUserAndRemoveMessagesSent() throws Exception {
    NotificationInfo notification = NotificationInfo.instance();
    Map<String, String> params = new HashMap<String, String>();
    params.put("objectId", "idofobject");
    notification.key("TestPlugin").setSendAll(true).setOwnerParameter(params).setOrder(1);
    notificationDataStorage.save(notification);
    
    UserSetting userSetting = UserSetting.getInstance();
    userSetting.setUserId("root").addProvider("TestPlugin", FREQUENCY.DAILY);
    userSetting.setActive(true);
    // Test send to daily
    NotificationContext context = NotificationContextImpl.cloneInstance();
    context.append(NotificationJob.JOB_DAILY, true);
    context.append(NotificationJob.JOB_WEEKLY, false);
    
    Map<NotificationKey, List<NotificationInfo>> map = notificationDataStorage.getByUser(context, userSetting);
    
    List<NotificationInfo> list = map.get(new NotificationKey("TestPlugin"));
    assertEquals(1, list.size());
    
    assertTrue(list.get(0).equals(notification));
    // check value from node
    NotificationInfo notification2 = getNotificationInfoByKeyIdAndParam("TestPlugin", "objectId=idofobject");
    assertNotNull(notification2);

    assertEquals(NotificationInfo.FOR_ALL_USER, notification2.getSendToDaily()[0]);
    // remove value on property sendToDaily
    notificationDataStorage.removeMessageAfterSent();

    // after sent, the value on on property sendToDaily will auto removed
    notification2 = getNotificationInfoByKeyIdAndParam("TestPlugin", "objectId=idofobject");
    assertEquals(1, notification2.getSendToDaily().length);
    
    // Test send to weekly
    context = NotificationContextImpl.cloneInstance();
    context.append(NotificationJob.JOB_DAILY, false);
    context.append(NotificationJob.JOB_WEEKLY, true);
    userSetting.setUserId("demo").addProvider("TestPlugin", FREQUENCY.WEEKLY);
    map = notificationDataStorage.getByUser(context, userSetting);
    list = map.get(new NotificationKey("TestPlugin"));
    assertEquals(1, list.size());
    
    notificationDataStorage.removeMessageAfterSent();
    
    notification2 = getNotificationInfoByKeyIdAndParam("TestPlugin", "objectId=idofobject");
    assertNull(notification2);
  }

  public void testWithUserNameContainSpecialCharacter() throws Exception {
    String userNameSpecial = "Rabe'e \"AbdelWahabô";
    NotificationContext context = NotificationContextImpl.cloneInstance();
    context.append(NotificationJob.JOB_DAILY, true);
    context.append(NotificationJob.JOB_WEEKLY, false);
    
    NotificationInfo notification = saveNotification(userNameSpecial, "demo");
    //
    UserSetting userSetting = UserSetting.getInstance();
    userSetting.setUserId(userNameSpecial).addProvider("TestPlugin", FREQUENCY.DAILY);
    userSetting.setActive(true);
    //
    Map<NotificationKey, List<NotificationInfo>> map = notificationDataStorage.getByUser(context, userSetting);
    List<NotificationInfo> list = map.get(new NotificationKey("TestPlugin"));
    //
    assertEquals(1, list.size());
    assertTrue(list.get(0).equals(notification));
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
    return getMessageNode(new StringBuffer("exo:name = '").append(msgId).append("'").toString(), "");
  }

  private NotificationInfo getNotificationInfoByKeyIdAndParam(String key, String param) throws Exception {
    Node node = getMessageNode(new StringBuffer("ntf:ownerParameter LIKE '%").append(param).append("%'").toString(), key);
    return fillModel(node);
  }
  
  private Node getMessageNode(String strQuery, String key) throws Exception {
    StringBuilder sqlQuery = new StringBuilder("SELECT * FROM ntf:message WHERE ");
    if (key != null && key.length() > 0) {
      sqlQuery.append(" jcr:path LIKE '").append("/eXoNotification/messageHome/").append(key).append("/%' AND ");
    }
    sqlQuery.append(strQuery);

    QueryManager qm = session.getWorkspace().getQueryManager();
    Query query = qm.createQuery(sqlQuery.toString(), Query.SQL);
    NodeIterator iter = query.execute().getNodes();
    return (iter.getSize() > 0) ? iter.nextNode() : null;
  }

}

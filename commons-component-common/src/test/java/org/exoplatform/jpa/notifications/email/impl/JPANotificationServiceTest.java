package org.exoplatform.jpa.notifications.email.impl;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.notification.impl.jpa.email.JPANotificationDataStorage;
import org.exoplatform.commons.notification.impl.jpa.email.dao.MailNotifDAO;
import org.exoplatform.commons.notification.job.NotificationJob;
import org.exoplatform.commons.notification.plugin.PluginTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.jpa.BaseTest;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by exo on 4/26/17.
 */
@ConfiguredBy({ @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/test-jpa-configuration.xml") })
public class JPANotificationServiceTest extends BaseTest {

  private NotificationService notificationService;
  private JPANotificationDataStorage notificationDataStorage;
  private MailNotifDAO mailNotifDAO;

  @Override
  public void setUp() {
    super.setUp();
    notificationService = getService(NotificationService.class);
    notificationDataStorage = getService(JPANotificationDataStorage.class);
    mailNotifDAO = getService(MailNotifDAO.class);
  }

  @Override
  public void tearDown() {
    mailNotifDAO.deleteAll(mailNotifDAO.findAll());
    super.tearDown();
  }

  private NotificationInfo saveNotification(String userDaily, String userWeekly) throws Exception {
    NotificationInfo notification = NotificationInfo.instance();
    Map<String, String> params = new HashMap<String, String>();
    params.put("objectId", "idofobject");
    notification.key(PluginTest.ID).setSendToDaily(userDaily)
        .setSendToWeekly(userWeekly).setOwnerParameter(params).setOrder(1);
    notificationDataStorage.save(notification);
    return notification;
  }

  public void testServiceNotNull() throws Exception {
    assertNotNull(notificationService);
    assertNotNull(notificationDataStorage);
    saveNotification("root", "demo");
  }

  public void testSave() throws Exception {
    NotificationInfo notification = saveNotification("root", "demo");
    NotificationContext context = NotificationContextImpl.cloneInstance();
    context.append(NotificationJob.JOB_DAILY, true);
    String dayName = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    context.append(NotificationJob.DAY_OF_JOB, dayName);
    UserSetting userSetting = UserSetting.getInstance();
    userSetting.setUserId("root").addPlugin(PluginTest.ID, UserSetting.FREQUENCY.DAILY);
    userSetting.setChannelActive(UserSetting.EMAIL_CHANNEL);
    Map<PluginKey, List<NotificationInfo>> map = notificationDataStorage.getByUser(context, userSetting);
    List<NotificationInfo> list = map.get(new PluginKey(PluginTest.ID));
    assertEquals(2, list.size());

    assertTrue(list.get(0).getTo().equals("root"));
  }

  public void testNormalGetByUserAndRemoveMessagesSent() throws Exception {
    NotificationInfo notification = saveNotification("test", "demo");
    UserSetting userSetting = UserSetting.getInstance();
    userSetting.setUserId("test").addPlugin(PluginTest.ID, UserSetting.FREQUENCY.DAILY);
    userSetting.setChannelActive(UserSetting.EMAIL_CHANNEL);
    NotificationContext context = NotificationContextImpl.cloneInstance();
    context.append(NotificationJob.JOB_DAILY, true);
    String dayName = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    context.append(NotificationJob.DAY_OF_JOB, dayName);
    //
    context.append(NotificationJob.JOB_WEEKLY, false);
    Map<PluginKey, List<NotificationInfo>> map = notificationDataStorage.getByUser(context, userSetting);

    List<NotificationInfo> list = map.get(new PluginKey(PluginTest.ID));
    assertEquals(2, list.size());

    assertTrue(list.get(0).getKey().equals(notification.getKey()));
    assertTrue(list.get(0).getOwnerParameter().equals(notification.getOwnerParameter()));

    context = NotificationContextImpl.cloneInstance();
    context.append(NotificationJob.JOB_DAILY, false);
    context.append(NotificationJob.JOB_WEEKLY, true);

    userSetting.setUserId("demo").addPlugin(PluginTest.ID, UserSetting.FREQUENCY.WEEKLY);
    map = notificationDataStorage.getByUser(context, userSetting);
    list = map.get(new PluginKey(PluginTest.ID));
    assertEquals(2, list.size());


    notificationDataStorage.removeMessageAfterSent(context);
    map = notificationDataStorage.getByUser(context, userSetting);
    list = map.get(new PluginKey(PluginTest.ID));
    assertNull(list);

  }

  public void testSpecialGetByUserAndRemoveMessagesSent() throws Exception {
    NotificationInfo notification = NotificationInfo.instance();
    Map<String, String> params = new HashMap<String, String>();
    params.put("objectId", "idofobject");
    notification.key(PluginTest.ID).setSendAll(true).setOwnerParameter(params).setOrder(1);
    notificationDataStorage.save(notification);

    UserSetting userSetting = UserSetting.getInstance();
    userSetting.setUserId("root").addPlugin(PluginTest.ID, UserSetting.FREQUENCY.DAILY);
    userSetting.setChannelActive(UserSetting.EMAIL_CHANNEL);
    // Test send to daily
    NotificationContext context = NotificationContextImpl.cloneInstance();
    context.append(NotificationJob.JOB_DAILY, true);
    String dayName = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    context.append(NotificationJob.DAY_OF_JOB, dayName);
    //
    context.append(NotificationJob.JOB_WEEKLY, false);

    Map<PluginKey, List<NotificationInfo>> map = notificationDataStorage.getByUser(context, userSetting);

    List<NotificationInfo> list = map.get(new PluginKey(PluginTest.ID));
    assertEquals(2, list.size());

    assertTrue(list.get(0).getKey().equals(notification.getKey()));
    assertTrue(list.get(0).getOwnerParameter().equals(notification.getOwnerParameter()));

    notificationDataStorage.removeMessageAfterSent(context);

    // Test send to weekly
    context = NotificationContextImpl.cloneInstance();
    context.append(NotificationJob.JOB_DAILY, false);
    context.append(NotificationJob.JOB_WEEKLY, true);
    userSetting.setUserId("demo").addPlugin(PluginTest.ID, UserSetting.FREQUENCY.WEEKLY);
    map = notificationDataStorage.getByUser(context, userSetting);
    list = map.get(new PluginKey(PluginTest.ID));
    assertEquals(1, list.size());

    context.append(NotificationJob.JOB_DAILY, true);
    notificationDataStorage.removeMessageAfterSent(context);

    map = notificationDataStorage.getByUser(context, userSetting);

    list = map.get(new PluginKey(PluginTest.ID));
    assertNull(list);
  }

  public void testWithUserNameContainSpecialCharacter() throws Exception {
    String userNameSpecial = "Rabe'e \"AbdelWahabô";
    NotificationContext context = NotificationContextImpl.cloneInstance();
    context.append(NotificationJob.JOB_DAILY, true);
    String dayName = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    context.append(NotificationJob.DAY_OF_JOB, dayName);
    //
    context.append(NotificationJob.JOB_WEEKLY, false);

    NotificationInfo notification = saveNotification(userNameSpecial, "demo");
    //
    UserSetting userSetting = UserSetting.getInstance();
    userSetting.setUserId(userNameSpecial).addPlugin(PluginTest.ID, UserSetting.FREQUENCY.DAILY);
    userSetting.setChannelActive(UserSetting.EMAIL_CHANNEL);
    //
    Map<PluginKey, List<NotificationInfo>> map = notificationDataStorage.getByUser(context, userSetting);
    List<NotificationInfo> list = map.get(new PluginKey(PluginTest.ID));
    //
    assertEquals(1, list.size());
    assertTrue(list.get(0).getKey().equals(notification.getKey()));
    assertTrue(list.get(0).getOwnerParameter().equals(notification.getOwnerParameter()));
  }
}

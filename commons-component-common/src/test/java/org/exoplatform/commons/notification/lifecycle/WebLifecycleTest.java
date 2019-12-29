package org.exoplatform.commons.notification.lifecycle;

import java.util.Arrays;
import java.util.List;

import org.mockito.Mockito;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.template.PluginTemplateBuilderAdapter;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.api.notification.service.WebNotificationService;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.notification.channel.WebChannel;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.notification.impl.jpa.web.dao.WebNotifDAO;
import org.exoplatform.commons.notification.impl.jpa.web.dao.WebParamsDAO;
import org.exoplatform.commons.notification.impl.jpa.web.dao.WebUsersDAO;
import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;

public class WebLifecycleTest extends BaseCommonsTestCase {

  private WebNotificationService webNotificationService;

  private UserSettingService     userSettingService;

  private WebNotifDAO            webNotifDAO;

  private WebParamsDAO           webParamsDAO;

  private WebUsersDAO            webUsersDAO;

  public void setUp() throws Exception {
    super.setUp();
    webNotificationService = getService(WebNotificationService.class);
    userSettingService = getService(UserSettingService.class);
    webNotifDAO = getService(WebNotifDAO.class);
    webParamsDAO = getService(WebParamsDAO.class);
    webUsersDAO = getService(WebUsersDAO.class);

    cleanData();
  }

  public void tearDown() throws Exception {
    super.tearDown();
    cleanData();
  }

  public void testReceiveOneNotificationWhenUsersHaveDefaultSettings() {
    // Given
    NotificationInfo notificationInfo = new NotificationInfo().key("TestPlugin");
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(notificationInfo);

    WebChannel webChannel = new WebChannel();
    WebLifecycle webLifecycle = (WebLifecycle) webChannel.getLifecycle();

    // When
    webLifecycle.process(ctx, "john", "mary");

    // Then
    WebNotificationFilter filterJohn = new WebNotificationFilter("john", true);
    List<NotificationInfo> webNotificationsJohn = webNotificationService.getNotificationInfos(filterJohn, 0, 10);
    assertNotNull(webNotificationsJohn);
    assertEquals(1, webNotificationsJohn.size());
    WebNotificationFilter filterMary = new WebNotificationFilter("mary", true);
    List<NotificationInfo> webNotificationsMary = webNotificationService.getNotificationInfos(filterMary, 0, 10);
    assertNotNull(webNotificationsMary);
    assertEquals(1, webNotificationsMary.size());
  }

  public void testReceiveOneNotificationWhenUsersHaveChannelDisabled() {
    // Given
    NotificationInfo notificationInfo = new NotificationInfo().key("TestPlugin");
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(notificationInfo);

    UserSetting johnSettings = userSettingService.get("john");
    johnSettings.removeChannelActive(WebChannel.ID);
    userSettingService.save(johnSettings);

    WebChannel webChannel = new WebChannel();
    WebLifecycle webLifecycle = (WebLifecycle) webChannel.getLifecycle();

    // When
    webLifecycle.process(ctx, "john", "mary");

    // Then
    WebNotificationFilter filterJohn = new WebNotificationFilter("john");
    List<NotificationInfo> webNotificationsJohn = webNotificationService.getNotificationInfos(filterJohn, 0, 10);
    assertNotNull(webNotificationsJohn);
    assertEquals(0, webNotificationsJohn.size());
    WebNotificationFilter filterMary = new WebNotificationFilter("mary");
    List<NotificationInfo> webNotificationsMary = webNotificationService.getNotificationInfos(filterMary, 0, 10);
    assertNotNull(webNotificationsMary);
    assertEquals(1, webNotificationsMary.size());
  }

  public void testReceiveOneNotificationWhenUsersHavePluginDisabled() {
    // Given
    NotificationInfo notificationInfo = new NotificationInfo().key("TestPlugin");
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(notificationInfo);

    UserSettingService userSettingService = getService(UserSettingService.class);
    UserSetting marySettings = userSettingService.get("mary");
    marySettings.removeChannelPlugin(WebChannel.ID, "TestPlugin");
    userSettingService.save(marySettings);

    WebChannel webChannel = new WebChannel();
    WebLifecycle webLifecycle = (WebLifecycle) webChannel.getLifecycle();

    // When
    webLifecycle.process(ctx, "john", "mary");

    // Then
    WebNotificationFilter filterJohn = new WebNotificationFilter("john");
    List<NotificationInfo> webNotificationsJohn = webNotificationService.getNotificationInfos(filterJohn, 0, 10);
    assertNotNull(webNotificationsJohn);
    assertEquals(1, webNotificationsJohn.size());
    WebNotificationFilter filterMary = new WebNotificationFilter("mary");
    List<NotificationInfo> webNotificationsMary = webNotificationService.getNotificationInfos(filterMary, 0, 10);
    assertNotNull(webNotificationsMary);
    assertEquals(0, webNotificationsMary.size());
  }

  /**
   * This test simulates the following scenario: * john receives a notification *
   * john removes the notification for the popover * john receives an update on
   * this notification ---> the notification must be displayed again in the
   * popover
   */
  public void testReceiveAnUpdatedNotifWhenItWasPreviouslyRemovedFromPopover() {
    // Given
    NotificationInfo notificationInfo = new NotificationInfo().key("TestPlugin");
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(notificationInfo);

    WebChannel webChannel = Mockito.spy(WebChannel.class);
    WebLifecycle webLifecycle = (WebLifecycle) webChannel.getLifecycle();

    // When
    webLifecycle.process(ctx, "john", "mary");

    WebNotificationFilter filterJohn = new WebNotificationFilter("john", true);
    List<NotificationInfo> webNotificationsJohn = webNotificationService.getNotificationInfos(filterJohn, 0, 10);
    assertNotNull(webNotificationsJohn);
    assertEquals(1, webNotificationsJohn.size());
    NotificationInfo notificationInfoJohn = webNotificationsJohn.get(0);
    // Remove from popover
    notificationInfoJohn.setOnPopOver(false);
    webNotificationService.save(notificationInfoJohn);

    Mockito.when(webChannel.getTemplateBuilder(Mockito.eq(PluginKey.key("TestPlugin")))).thenReturn(new TemplateBuilderUpdate());

    NotificationContext ctxUpdate = NotificationContextImpl.cloneInstance();
    notificationInfoJohn.getOwnerParameter().put("ID_FOR_TEST", notificationInfoJohn.getId());
    ctxUpdate.setNotificationInfo(notificationInfoJohn);

    webLifecycle.process(ctxUpdate, "john", "mary");

    // Then
    webNotificationsJohn = webNotificationService.getNotificationInfos(filterJohn, 0, 10);
    assertNotNull(webNotificationsJohn);
    assertEquals(1, webNotificationsJohn.size());
    assertTrue(webNotificationsJohn.get(0).isOnPopOver());
  }

  private void cleanData() {
    webUsersDAO.deleteAll();
    webParamsDAO.deleteAll();
    webNotifDAO.deleteAll();

    // Reset notifications user settings
    UserSetting userSetting = new UserSetting();
    userSetting.setUserId("john");
    userSetting.setChannelActive(UserSetting.EMAIL_CHANNEL);
    userSetting.setChannelActive(WebChannel.ID);
    userSetting.setChannelPlugins(WebChannel.ID, Arrays.asList("TestPlugin"));
    userSettingService.save(userSetting);
    userSetting.setUserId("mary");
    userSettingService.save(userSetting);
  }

  /**
   * Template Builder for tests to simulate the case where the notification must
   * update an existing notification. We force onPopOver to true since we want to
   * test that no matter which value is set for this attribute by the
   * TemplateBuilder, it will always be set to true at the end since we want that
   * a new or an update notification always appears in the popover.
   */
  class TemplateBuilderUpdate extends PluginTemplateBuilderAdapter {
    @Override
    public NotificationInfo getNotificationToStore(NotificationInfo notificationInfo) {
      NotificationInfo notificationToStore = super.getNotificationToStore(notificationInfo);
      if (notificationInfo.getTo().equals("john")) {
        notificationToStore.setId(notificationInfo.getOwnerParameter().get("ID_FOR_TEST"));
        notificationToStore.setOnPopOver(false);
        notificationToStore.setUpdate(true);
      }
      return notificationToStore;
    }
  }
}

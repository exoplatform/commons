package org.exoplatform.jpa.settings.impl;

import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.notification.channel.MailChannel;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.jpa.BaseTest;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.idm.UserImpl;
import org.exoplatform.settings.jpa.JPAUserSettingServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadFactory;

/**
 * Created by exo on 4/18/17.
 */
@ConfiguredBy({
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/test-jpa-configuration.xml") })

public class JPAUserSettingServiceTest extends BaseTest {

  protected static JPAUserSettingServiceImpl userSettingService;
  protected static OrganizationService organizationService;
//  private ExecutorService executor;

//  public JPAUserSettingServiceTest() {}

  @Override
  public void setUp() {
    super.setUp();
    //
    userSettingService = getService(JPAUserSettingServiceImpl.class);
    organizationService = getService(OrganizationService.class);
//    addCreateDateForUser();
//    // init setting home
//    initSettingHome();
    //
    ThreadFactory threadFactory = new ThreadFactory() {
      public Thread newThread(Runnable arg0) {
        return new Thread(arg0, "UserProfile thread");
      }
    };

//    executor = Executors.newFixedThreadPool(20, threadFactory);
  }

  @Override
  protected void tearDown() {
    for (int i = 0; i < 10; i++) {
      try {
        organizationService.getUserHandler().removeUser("user_" + i, false);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
//    session.logout();
  }

  public void test_1_GetDefautSetting() throws Exception {
    for (int i = 0; i < 10; i++) {
      User user = new UserImpl("user_" + i);
      organizationService.getUserHandler().createUser(user, false);
      userSettingService.initDefaultSettings(user.getUserName());
    }
    List<UserSetting> list = userSettingService.getDigestDefaultSettingForAllUser(0, 0);

    assertEquals(list.size(), 10);
  }

  public void testDisabledUser() throws Exception {
    User u = CommonsUtils.getService(OrganizationService.class).getUserHandler().createUserInstance("binh");
    u.setEmail("email@test");
    u.setFirstName("first");
    u.setLastName("last");
    u.setPassword("pwdADDSomeSaltToBeCompliantWithSomeIS00");
    CommonsUtils.getService(OrganizationService.class).getUserHandler().createUser(u, true);

    userSettingService.save(createUserSetting("binh", null, null, null));
    UserSetting userSetting = userSettingService.get("binh");
    assertTrue(userSetting.isChannelActive(MailChannel.ID));
    assertTrue(userSetting.isEnabled());

    //disable user "root"
    CommonsUtils.getService(OrganizationService.class).getUserHandler().setEnabled("binh", false, true);
    userSetting = userSettingService.get("binh");
    assertTrue(userSetting.isChannelActive(MailChannel.ID));
    assertFalse(userSetting.isEnabled());

    //enable user "root" but not change the active channel status
    CommonsUtils.getService(OrganizationService.class).getUserHandler().setEnabled("binh", true, true);
    userSetting = userSettingService.get("binh");
    assertTrue(userSetting.isChannelActive(MailChannel.ID));
    assertTrue(userSetting.isEnabled());

    CommonsUtils.getService(OrganizationService.class).getUserHandler().removeUser("binh", false);
    assertNull(CommonsUtils.getService(OrganizationService.class).getUserHandler().findUserByName("binh"));

  }

  public void test_2_GetUsersSetting() throws Exception {
    userSettingService.save(createUserSetting("root", Arrays.asList("1","2"), Arrays.asList("3","4"), Arrays.asList("5","6")));
    userSettingService.save(createUserSetting("john", Arrays.asList("4","5"), Arrays.asList("2","8"), Arrays.asList("6","7")));
    userSettingService.save(createUserSetting("mary", Arrays.asList("3","5"), Arrays.asList("4","6"), Arrays.asList("1","9")));
    userSettingService.save(createUserSetting("demo", Arrays.asList("2"), Arrays.asList("3","9"), Arrays.asList("2","7")));

    List<String> list = userSettingService.getUserSettingByPlugin("2");
    assertEquals(3, list.size());// root, john, demo

    //disable user "root"
    CommonsUtils.getService(OrganizationService.class).getUserHandler().setEnabled("root", false, true);

    list = userSettingService.getUserSettingByPlugin("2");
    assertEquals(2, list.size());//john, demo
  }

  private UserSetting createUserSetting(String userId, List<String> instantly, List<String> daily, List<String> weekly) {
    UserSetting model = new UserSetting();
    model.setUserId(userId);
    model.setChannelActive(UserSetting.EMAIL_CHANNEL);
    model.setDailyPlugins(daily);
    model.setChannelPlugins(UserSetting.EMAIL_CHANNEL, instantly);
    model.setWeeklyPlugins(weekly);
    return model;
  }

//  public void test_3_AddMixingMultiThreads() throws Exception {
//    for (int i = 0; i < 10; i++) {
//      User user = new UserImpl("user_" + i);
//      organizationService.getUserHandler().createUser(user, false);
//    }
//    for (int i = 0; i < 10; i++) {
//      try {
//        ListAccess<User> list = organizationService.getUserHandler().findAllUsers();
//        //
//        User[] users = list.load(0, list.getSize());
//        for (int j = 0; j < users.length; i++) {
//          userSettingService.addMixin(users[i].getUserName());
//        }
//      } catch (Exception e) {
//        assertFalse(true);
//      }
//    }
//  }
}

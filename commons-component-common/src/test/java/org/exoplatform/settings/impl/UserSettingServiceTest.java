package org.exoplatform.settings.impl;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.jcr.Node;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.notification.impl.setting.UserSettingServiceImpl;
import org.exoplatform.commons.notification.job.NotificationJob;
import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserSettingServiceTest extends BaseCommonsTestCase {
  private UserSettingServiceImpl userSettingService;
  private ExecutorService executor;
  
  public UserSettingServiceTest() {
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    //
    userSettingService = getService(UserSettingServiceImpl.class);
    addCreateDateForUser();
    // init setting home
    initSettingHome();
    //
    ThreadFactory threadFactory = new ThreadFactory() {
      public Thread newThread(Runnable arg0) {
        return new Thread(arg0, "UserProfile thread");
      }
    };

    executor = Executors.newFixedThreadPool(20, threadFactory);
  }

  @Override
  protected void tearDown() throws Exception {
    session.logout();
  }

  public void testGetDefautSetting() throws Exception {
    // before upgrade
    List<UserSetting> list = userSettingService.getDigestDefaultSettingForAllUser(0, 0);
    assertEquals(0, list.size());
    // run upgrade
    runUpgrade();
    // after upgrade
    list = userSettingService.getDigestDefaultSettingForAllUser(0, 0);
    assertEquals(10, list.size());
  }

  public void testGetUsersSetting() throws Exception {
    runUpgrade();
    //
    userSettingService.save(createUserSetting("root", Arrays.asList("1,2"), Arrays.asList("3,4"), Arrays.asList("5,6")));
    userSettingService.save(createUserSetting("john", Arrays.asList("4,5"), Arrays.asList("2,8"), Arrays.asList("6,7")));
    userSettingService.save(createUserSetting("mary", Arrays.asList("32,5"), Arrays.asList("4,6"), Arrays.asList("1,9")));
    userSettingService.save(createUserSetting("demo", Arrays.asList("2"), Arrays.asList("3,9"), Arrays.asList("2,7")));
    //
    List<String> list = userSettingService.getUserSettingByPlugin("2");
    assertEquals(2, list.size());
  }

  private void runUpgrade() throws Exception {
    // run upgrade by run daily
    NotificationContext context = NotificationContextImpl.cloneInstance();
    context.append(NotificationJob.JOB_DAILY, true);
    context.append(NotificationJob.JOB_WEEKLY, false);
    
    getService(NotificationService.class).digest(context);
    //
    initModifiedDate();
  }
  
  private UserSetting createUserSetting(String userId, List<String> instantly, List<String> daily, List<String> weekly) {
    UserSetting model = new UserSetting();
    model.setUserId(userId);
    model.setActive(true);
    model.setDailyProviders(daily);
    model.setInstantlyProviders(instantly);
    model.setWeeklyProviders(weekly);
    return model;
  }
  
  private void initSettingHome() throws Exception {
    Node rootNode = session.getRootNode();
    if (rootNode.hasNode("settings") == false) {
      Node settingNode = rootNode.addNode("settings", "stg:settings");
      settingNode.addNode("user", "stg:subcontext");
      session.save();
    }
  }
  
  private void addLastUpdateTime(String userId) throws Exception {
    Node rootNode = session.getRootNode().getNode("settings").getNode("user").getNode(userId);
    rootNode.addMixin("exo:datetime");
    rootNode.setProperty("exo:lastModifiedDate", Calendar.getInstance());
    session.save();
  }
  
  private void initModifiedDate() throws Exception {
    OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
    ListAccess<User> list = organizationService.getUserHandler().findAllUsers();
    //
    User[] users = list.load(0, list.getSize());
    for (int i = 0; i < users.length; i++) {
      if (users[i] != null && users[i].getUserName() != null) {
        addLastUpdateTime(users[i].getUserName());
      }
    }
  }

  private void addCreateDateForUser() throws Exception {
    OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
    ListAccess<User> list = organizationService.getUserHandler().findAllUsers();
    //
    User[] users = list.load(0, list.getSize());
    for (int i = 0; i < users.length; i++) {
      if (users[i] != null && users[i].getUserName() != null) {
        users[i].setCreatedDate(Calendar.getInstance().getTime());
      }
    }
  }
  
  public void testAddMixingMultiThreads() throws Exception {
    for (int i = 0; i < 500; i++) {
      executor.execute(new Runnable() {
        @Override
        public void run() {
          try {
            OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
            ListAccess<User> list = organizationService.getUserHandler().findAllUsers();
            //
            User[] users = list.load(0, list.getSize());
            for (int i = 0; i < users.length; i++) {
              userSettingService.addMixin(users[i].getUserName());
            }
          } catch (Exception e) {
            assertFalse(true);
          }
        }
      });
    }
    //
    Thread.sleep(1000);
  }
}

package org.exoplatform.settings.impl;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.testing.BaseCommonsTestCase;

public class UserSettingServiceTest extends BaseCommonsTestCase {
  
  private UserSettingService userSettingService;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    userSettingService = getService(UserSettingService.class);
  }

  public void testGetUsersSetting() throws Exception {
    userSettingService.save(createUserSetting("root", Arrays.asList("1,2"), Arrays.asList("3,4"), Arrays.asList("5,6")));
    userSettingService.save(createUserSetting("john", Arrays.asList("4,5"), Arrays.asList("2,8"), Arrays.asList("6,7")));
    userSettingService.save(createUserSetting("mary", Arrays.asList("32,5"), Arrays.asList("4,6"), Arrays.asList("1,9")));
    userSettingService.save(createUserSetting("demo", Arrays.asList("2"), Arrays.asList("3,9"), Arrays.asList("2,7")));
    addLastUpdateTime("root");
    addLastUpdateTime("john");
    addLastUpdateTime("mary");
    addLastUpdateTime("demo");
    List<String> list = userSettingService.getUserSettingByPlugin("2");
    assertEquals(2, list.size());
  }
  
  public void testGetDefautSetting() throws Exception {
    userSettingService.save(createUserSetting("root", null, null, null));
    UserSetting userSetting = userSettingService.get("root");
    assertNotNull(userSetting);
    //add mix:defaultSetting for user root
    userSettingService.addMixin("root");
    
    addLastUpdateTime("root");
    
    List<UserSetting> list = userSettingService.getDefaultDaily(0, 0);
    assertEquals(1, list.size());
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
  
  private void addLastUpdateTime(String userId) throws Exception {
    Node rootNode = session.getRootNode().getNode("settings").getNode("user").getNode(userId);
    rootNode.addMixin("exo:datetime");
    rootNode.setProperty("exo:lastModifiedDate", Calendar.getInstance());
    session.save();
  }
  
}

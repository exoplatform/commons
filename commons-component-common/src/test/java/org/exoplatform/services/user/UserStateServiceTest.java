/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.user;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.exoplatform.commons.notification.BaseNotificationTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 22, 2014  
 */
public class UserStateServiceTest extends BaseNotificationTestCase {
  private String SUPER_USER = "root";

  private UserStateService userStateService;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    //
    loginUser(SUPER_USER, false);

    userStateService = getService(UserStateService.class);
  }

  protected void tearDown() throws Exception {
    //
    userStateService.userStateCache.clearCache();
  }

  public void testGetUserState() throws Exception {
    UserStateModel userModel = 
        new UserStateModel(SUPER_USER,
                           new Date().getTime(),
                           UserStateService.DEFAULT_STATUS);

    userStateService.save(userModel);
    UserStateModel model1 = userStateService.getUserState(SUPER_USER + "temp");
    assertNull(model1);

    UserStateModel model2 = userStateService.getUserState(SUPER_USER);
    assertNotNull(model2);

    assertEquals(SUPER_USER, model2.getUserId());
    assertEquals(userModel.getLastActivity(), model2.getLastActivity());
    assertEquals(UserStateService.DEFAULT_STATUS, model2.getStatus());
  }
  
  public void testPing() throws Exception {
    UserStateModel userModel = 
        new UserStateModel(SUPER_USER,
                           new Date().getTime(),
                           UserStateService.DEFAULT_STATUS);
    userStateService.save(userModel);
    userStateService.ping(userModel.getUserId());

    assertNotSame(userModel.getLastActivity(), userStateService.getUserState(SUPER_USER).getLastActivity());

    Calendar currentTime = new GregorianCalendar();
    Calendar time = (Calendar) currentTime.clone();
    time.add(Calendar.MINUTE, -10);
    userModel.setLastActivity(time.getTime().getTime());

    userStateService.save(userModel);
    userStateService.ping(userModel.getUserId());

    assertNotSame(userModel.getLastActivity(), userStateService.getUserState(SUPER_USER).getLastActivity());

    //
    loginUser("mary", true);
    assertTrue(userStateService.getUserState("mary").getStatus().equals(UserStateService.DEFAULT_STATUS));
    //
    loginUser("demo", false);

    // get status of user Mary by current user Demo
    assertNotNull("User state of 'mary' is null while it was pinged before", userStateService.getUserState("mary"));
    assertNotNull("User state status of 'mary' is null while it was pinged before", userStateService.getUserState("mary").getStatus());
    assertTrue(userStateService.getUserState("mary").getStatus().equals(UserStateService.DEFAULT_STATUS));
    // get status of user Demo by anonymous user
    ConversationState.setCurrent(null);

    assertNull(userStateService.getUserState("demo"));
  }
  
  public void testOnline() throws Exception {
    long date = new Date().getTime();
    UserStateModel userModel = 
        new UserStateModel(SUPER_USER,
                           date,
                           UserStateService.DEFAULT_STATUS);
    userStateService.save(userModel);
    userStateService.ping(userModel.getUserId());

    //
    List<UserStateModel> onlines = userStateService.online();
    assertEquals(1, onlines.size());
    assertEquals(SUPER_USER, onlines.get(0).getUserId());
    assertNotSame(date, onlines.get(0).getLastActivity());
    assertEquals(UserStateService.DEFAULT_STATUS, onlines.get(0).getStatus());
  }
  
  public void testLastLogin() {
    assertNull(userStateService.lastLogin());
    loginUser("user1", true);
    assertEquals("user1", userStateService.lastLogin().getUserId());

    loginUser("user2", true);
    assertEquals("user2", userStateService.lastLogin().getUserId());
  }

  public void testIsOnline() throws Exception {
    long date = new Date().getTime();
    UserStateModel userModel = 
        new UserStateModel(SUPER_USER,
                           date,
                           UserStateService.DEFAULT_STATUS);
    userStateService.save(userModel);
    userStateService.ping(userModel.getUserId());

    assertTrue(userStateService.isOnline(SUPER_USER));
    //
    assertFalse(userStateService.isOnline("demo"));
    //
    loginUser("demo", true);
    assertTrue(userStateService.isOnline("demo"));
  }
  
  private void loginUser(String userId, boolean hasPing) {
    Collection<MembershipEntry> membershipEntries = new ArrayList<MembershipEntry>();
    MembershipEntry membershipEntry = new MembershipEntry("/platform/administrators", "*");
    membershipEntries.add(membershipEntry);
    Identity identity = new Identity(userId, membershipEntries);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
    //
    if (hasPing) {
      userStateService.ping(userId);
    }
  }
}

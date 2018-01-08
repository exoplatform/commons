/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.jpa.settings.impl;

import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.jpa.BaseTest;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.settings.jpa.JPASettingServiceImpl;
import org.exoplatform.settings.jpa.dao.SettingContextDAO;
import org.exoplatform.settings.jpa.dao.SettingScopeDAO;
import org.exoplatform.settings.jpa.dao.SettingsDAO;

/**
 * Test just for the implementation service Created by The eXo Platform SAS
 * Author : eXoPlatform exo@exoplatform.com Nov 12, 2012
 */
@ConfiguredBy({ @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/test-jpa-configuration.xml") })
public class JPASettingServiceImplTest extends BaseTest {

  protected JPASettingServiceImpl settingService;
  protected SettingContextDAO contextDAO;
  protected SettingScopeDAO scopeDAO;
  protected SettingsDAO settingsDAO;

  @Override
  public void setUp() {
    super.setUp();
    settingService = getService(JPASettingServiceImpl.class);
    contextDAO = getService(SettingContextDAO.class);
    scopeDAO = getService(SettingScopeDAO.class);
    settingsDAO = getService(SettingsDAO.class);

    settingsDAO.deleteAll();
    contextDAO.deleteAll();
    scopeDAO.deleteAll();

    ConversationState c = new ConversationState(new Identity("root"));
    ConversationState.setCurrent(c);
  }

  @Override
  protected void tearDown() {
    settingsDAO.deleteAll();
    contextDAO.deleteAll();
    scopeDAO.deleteAll();
  }

  public void testUserSimple() {

    // String
    settingService.set(Context.USER.id("foo"), Scope.SPACE, "a", SettingValue.create("b"));
    assertEquals("b", settingService.get(Context.USER.id("foo"), Scope.SPACE, "a").getValue());

    // Long type
    settingService.set(Context.USER,
                       Scope.SPACE.id("name"),
                       "a",
                       SettingValue.create(new Long(789)));
    Object value = Long.parseLong(settingService.get(Context.USER, Scope.SPACE.id("name"), "a").getValue().toString());
    assertEquals(value, new Long(789));

    // Double
    settingService.set(Context.USER.id("foo"),
                       Scope.SPACE.id("name"),
                       "a",
                       SettingValue.create(new Double(4.5)));
    value = Double.parseDouble(settingService.get(Context.USER.id("foo"), Scope.SPACE.id("name"), "a").getValue().toString());
    assertEquals(value, new Double(4.5));

    // Boolean
    settingService.set(Context.USER.id("foo"),
                       Scope.SPACE.id("name"),
                       "a",
                       SettingValue.create(true));
    value = Boolean.parseBoolean(settingService.get(Context.USER.id("foo"), Scope.SPACE.id("name"), "a").getValue().toString());
    assertEquals(value, true);
  }

  public void testGlobalSimple() {
    settingService.set(Context.GLOBAL, Scope.SPACE, "a", SettingValue.create("b"));
    assertEquals("b", settingService.get(Context.GLOBAL, Scope.SPACE, "a").getValue());
  }

  public void testUserNamed() {
    settingService.set(Context.USER.id("foo"),
                       Scope.SPACE.id("name"),
                       "a",
                       SettingValue.create("b"));
    assertEquals("b", settingService.get(Context.USER.id("foo"), Scope.SPACE.id("name"), "a")
                                    .getValue());
  }

  public void testGlobalNamed() {
    settingService.set(Context.GLOBAL, Scope.SPACE.id("name"), "a", SettingValue.create("b"));
    assertEquals("b", settingService.get(Context.GLOBAL, Scope.SPACE.id("name"), "a").getValue());
  }

  public void testRemoveSimple() {
    settingService.set(Context.USER.id("foo"), Scope.SPACE, "a", SettingValue.create("b"));
    assertEquals("b", settingService.get(Context.USER.id("foo"), Scope.SPACE, "a").getValue());

    settingService.remove(Context.USER.id("foo"), Scope.SPACE, "a");
    assertNull(settingService.get(Context.USER.id("foo"), Scope.SPACE, "a"));

    // remove simple
    settingService.set(Context.GLOBAL, Scope.PAGE.id("name"), "x", SettingValue.create("y"));
    assertEquals("y", settingService.get(Context.GLOBAL, Scope.PAGE.id("name"), "x").getValue());
    settingService.remove(Context.GLOBAL, Scope.PAGE.id("name"), "x");
    // SettingValue sv = settingService.get(Context.GLOBAL, Scope.PAGE, "x");
    assertNull(settingService.get(Context.GLOBAL, Scope.PAGE.id("name"), "x"));
  }

  public void testRemovePage() {
    // remove multi
    settingService.set(Context.GLOBAL, Scope.PAGE.id("name1"), "x1", SettingValue.create("y1"));
    settingService.set(Context.GLOBAL, Scope.PAGE.id("name1"), "x2", SettingValue.create("y2"));
    assertEquals(settingService.get(Context.GLOBAL, Scope.PAGE.id("name1"), "x1").getValue(), "y1");
    assertEquals(settingService.get(Context.GLOBAL, Scope.PAGE.id("name1"), "x2").getValue(), "y2");

    settingService.remove(Context.GLOBAL, Scope.PAGE.id("name1"));
    assertNull(settingService.get(Context.GLOBAL, Scope.PAGE.id("name1"), "x1"));
    assertNull(settingService.get(Context.GLOBAL, Scope.PAGE.id("name1"), "x2"));
  }

  public void testRemoveUser() {

    // the current userId get by session.getUserID()
    settingService.set(Context.USER, Scope.PORTAL.id("name1"), "x1", SettingValue.create("y1"));
    settingService.set(Context.USER, Scope.PORTAL.id("name2"), "x2", SettingValue.create("y2"));
    settingService.set(Context.USER, Scope.PAGE.id("name3"), "x3", SettingValue.create("y3"));
    settingService.set(Context.USER, Scope.PAGE.id("name4"), "x4", SettingValue.create("y4"));

    // verify
    assertEquals(settingService.get(Context.USER, Scope.PORTAL.id("name1"), "x1").getValue(), "y1");
    assertEquals(settingService.get(Context.USER, Scope.PORTAL.id("name2"), "x2").getValue(), "y2");
    assertEquals(settingService.get(Context.USER, Scope.PAGE.id("name3"), "x3").getValue(), "y3");
    assertEquals(settingService.get(Context.USER, Scope.PAGE.id("name4"), "x4").getValue(), "y4");

    // remove all
    settingService.remove(Context.USER);

    // verify again
    assertNull(settingService.get(Context.USER, Scope.PORTAL.id("name1"), "x1"));
    assertNull(settingService.get(Context.USER, Scope.PORTAL.id("name2"), "x2"));
    assertNull(settingService.get(Context.USER, Scope.PAGE.id("name3"), "x3"));
    assertNull(settingService.get(Context.USER, Scope.PAGE.id("name4"), "x4"));

  }

  public void testCountSettingsByNameAndValueAndScope() {
    settingService.set(Context.USER.id("user1"), Scope.PAGE.id("name1"), "x1", SettingValue.create("y1"));
    settingService.set(Context.USER.id("user1"), Scope.PAGE.id("name1"), "x1", SettingValue.create("y2"));
    settingService.set(Context.USER.id("user2"), Scope.PAGE.id("name2"), "x1", SettingValue.create("y2"));
    settingService.set(Context.USER.id("user2"), Scope.PAGE.id("name2"), "x2", SettingValue.create("y2"));
    settingService.set(Context.USER.id("user3"), Scope.PORTAL.id("name1"), "x1", SettingValue.create("y1"));
    settingService.set(Context.GLOBAL, Scope.PAGE.id("name1"), "x1", SettingValue.create("y1"));

    // verify
    assertEquals(1, settingService.countSettingsByNameAndValueAndScope(Scope.PAGE.id("name2"), "x1", "y1"), 1);
    assertEquals(1, settingService.countSettingsByNameAndValueAndScope(Scope.PAGE.id("name1"), "x1", "y2"), 1);
    assertEquals(1, settingService.countSettingsByNameAndValueAndScope(Scope.PAGE.id("name2"), "x1", "y3"), 1);
    assertEquals(1, settingService.countSettingsByNameAndValueAndScope(Scope.PAGE.id("name2"), "x2", "x2"), 1);
  }

  public void testGetContextsByTypeAndScopeAndSettingName() {
    settingService.set(Context.USER.id("user1"), Scope.PAGE.id("name1"), "x1", SettingValue.create("y1"));
    settingService.set(Context.USER.id("user1"), Scope.PAGE.id("name1"), "x1", SettingValue.create("y2"));
    settingService.set(Context.USER.id("user2"), Scope.PAGE.id("name2"), "x1", SettingValue.create("y2"));
    settingService.set(Context.USER.id("user2"), Scope.PAGE.id("name2"), "x2", SettingValue.create("y2"));
    settingService.set(Context.USER.id("user3"), Scope.PORTAL.id("name1"), "x1", SettingValue.create("y1"));
    settingService.set(Context.GLOBAL, Scope.PAGE.id("name1"), "x1", SettingValue.create("y1"));

    // verify
    assertEquals(1, settingService.getContextsByTypeAndScopeAndSettingName(Context.USER.getName(), Scope.PAGE.getName(), "name2", "x1", 0 , 10).size());
    assertEquals(1, settingService.getContextsByTypeAndScopeAndSettingName(Context.USER.getName(), Scope.PAGE.getName(), "name1", "x1", 0 , 10).size());
    assertEquals(1, settingService.getContextsByTypeAndScopeAndSettingName(Context.USER.getName(), Scope.PAGE.getName(), "name2", "x2", 0 , 10).size());
  }

  public void testGetSettingsByContext() {
    settingService.set(Context.USER.id("user1"), Scope.PAGE.id("name1"), "x1", SettingValue.create("y1"));
    settingService.set(Context.USER.id("user1"), Scope.PAGE.id("name1"), "x2", SettingValue.create("y2"));
    settingService.set(Context.USER.id("user2"), Scope.PAGE.id("name2"), "x1", SettingValue.create("y2"));
    settingService.set(Context.USER.id("user2"), Scope.PAGE.id("name2"), "x2", SettingValue.create("y2"));
    settingService.set(Context.USER.id("user3"), Scope.PORTAL.id("name1"), "x1", SettingValue.create("y1"));
    settingService.set(Context.GLOBAL, Scope.PAGE.id("name1"), "x1", SettingValue.create("y1"));

    // verify
    assertEquals(1, settingService.getSettingsByContext(Context.USER.id("user1")).size());
    assertEquals(2, settingService.getSettingsByContext(Context.USER.id("user1")).get(Scope.PAGE.id("name1")).size());
    assertEquals(1, settingService.getSettingsByContext(Context.USER.id("user2")).size());
    assertEquals(2, settingService.getSettingsByContext(Context.USER.id("user2")).get(Scope.PAGE.id("name2")).size());
    assertEquals(1, settingService.getSettingsByContext(Context.USER.id("user3")).size());
    assertEquals(1, settingService.getSettingsByContext(Context.GLOBAL).size());
    assertEquals(0, settingService.getSettingsByContext(Context.GLOBAL.id("user1")).size());
  }

  public void testGetEmptyContextsByScopeAndContextType() {
    settingService.set(Context.USER.id("user1"), Scope.PAGE.id("name1"), "x1", SettingValue.create("y1"));
    settingService.set(Context.USER.id("user1"), Scope.PAGE.id("name1"), "x2", SettingValue.create("y2"));
    settingService.set(Context.USER.id("user2"), Scope.PAGE.id("name1"), "x1", SettingValue.create("y2"));
    settingService.set(Context.USER.id("user2"), Scope.PAGE.id("name2"), "x2", SettingValue.create("y2"));
    settingService.set(Context.USER.id("user3"), Scope.PORTAL.id("name1"), "x1", SettingValue.create("y1"));
    settingService.set(Context.GLOBAL, Scope.PAGE.id("name1"), "x1", SettingValue.create("y1"));

    // verify
    assertEquals(3, settingService.getEmptyContextsByTypeAndScopeAndSettingName(Context.USER.getName(), Scope.PORTAL.getName(), null, "x1", 0, 10).size());
    assertEquals(2, settingService.getEmptyContextsByTypeAndScopeAndSettingName(Context.USER.getName(), Scope.PORTAL.getName(), "name1", "x1", 0, 10).size());
    assertEquals(1, settingService.getEmptyContextsByTypeAndScopeAndSettingName(Context.USER.getName(), Scope.PAGE.getName(), "name1", "x1", 0, 10).size());
    assertEquals(2, settingService.getEmptyContextsByTypeAndScopeAndSettingName(Context.USER.getName(), Scope.PAGE.getName(), "name2", "x2", 0, 10).size());
  }

}

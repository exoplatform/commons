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
package org.exoplatform.settings.impl;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

/**
 * Test just for the implementation service Created by The eXo Platform SAS
 * Author : eXoPlatform exo@exoplatform.com Nov 12, 2012
 */
public class SettingServiceImplTest extends BaseCommonsTestCase {

  protected static SettingService settingService;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    settingService = getService(SettingServiceImpl.class);
    ConversationState c = new ConversationState(new Identity(session.getUserID()));
    ConversationState.setCurrent(c);
  }

  public void testUserSimple() {

    // String
    settingService.set(Context.USER, Scope.SPACE, "a", SettingValue.create("b"));
    assertEquals("b", settingService.get(Context.USER.id("foo"), Scope.SPACE, "a").getValue());

    // Long type
    settingService.set(Context.USER,
                       Scope.SPACE.id("name"),
                       "a",
                       SettingValue.create(new Long(789)));
    assertEquals(settingService.get(Context.USER, Scope.SPACE.id("name"), "a").getValue(),
                 new Long(789));

    // Double
    settingService.set(Context.USER.id("foo"),
                       Scope.SPACE.id("name"),
                       "a",
                       SettingValue.create(new Double(4.5)));
    assertEquals(settingService.get(Context.USER, Scope.SPACE.id("name"), "a").getValue(),
                 new Double(4.5));

    // Boolean
    settingService.set(Context.USER.id("foo"),
                       Scope.SPACE.id("name"),
                       "a",
                       SettingValue.create(true));
    assertEquals(settingService.get(Context.USER, Scope.SPACE.id("name"), "a").getValue(), true);

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

    settingService.remove(Context.USER, Scope.SPACE, "a");
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

}

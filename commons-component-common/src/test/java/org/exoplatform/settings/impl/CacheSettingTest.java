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
import org.exoplatform.commons.api.settings.data.SettingKey;
import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.settings.cache.CacheSettingServiceImpl;

/** test just for the cache
 * Created by The eXo Platform SAS Author : eXoPlatform bangnv@exoplatform.com
 * Nov 22, 2012
 */
public class CacheSettingTest extends BaseCommonsTestCase {
  protected static SettingService               settingService;

  private ExoCache<SettingKey, SettingValue<?>> settingCache;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    settingCache = getService(CacheService.class).getCacheInstance(SettingService.class.getSimpleName());
    settingService = getService(CacheSettingServiceImpl.class);
    ConversationState c = new ConversationState(new Identity(session.getUserID()));
    ConversationState.setCurrent(c);  
    settingCache.clearCache();
  }

  public void testSetAndSimpleRemove() {

    // set a simple Key and String Value
    assertEquals(0, settingCache.getCacheSize());
    settingService.set(Context.USER, Scope.SPACE, "a", SettingValue.create("b"));
    assertEquals(1, settingCache.getCacheSize());
    assertEquals("b", settingCache.get(new SettingKey(Context.USER, Scope.SPACE, "a")).getValue());

    // remove
    settingService.remove(Context.USER, Scope.SPACE, "a");
    assertEquals(0, settingCache.getCacheSize());

    // set a simple Key and Float Value
    settingService.set(Context.USER, Scope.SPACE, "b", SettingValue.create(new Long(987)));
    assertEquals(1, settingCache.getCacheSize());
    assertEquals(new Long(987), settingCache.get(new SettingKey(Context.USER, Scope.SPACE, "b"))
                                            .getValue());
    settingService.remove(Context.USER, Scope.SPACE, "b");
    assertEquals(0, settingCache.getCacheSize());

    // set a simple Key and Double Value
    settingService.set(Context.USER, Scope.SPACE, "b", SettingValue.create(new Double(1.5)));
    assertEquals(1, settingCache.getCacheSize());
    assertEquals(settingCache.get(new SettingKey(Context.USER, Scope.SPACE, "b")).getValue(), 1.5);
    assertEquals(settingCache.get(new SettingKey(Context.USER, Scope.SPACE, "b")).getValue(),
                 new Double(1.5));
    settingCache.clearCache();
    assertEquals(0, settingCache.getCacheSize());

    // set a simple key and Boolean Value
    settingService.set(Context.USER, Scope.SPACE, "b", SettingValue.create(true));
    assertEquals(1, settingCache.getCacheSize());
    assertEquals(settingCache.get(new SettingKey(Context.USER, Scope.SPACE, "b")).getValue(), true);
    assertEquals(settingCache.get(new SettingKey(Context.USER, Scope.SPACE, "b")).getValue(),
                 new Boolean(true));
    settingCache.clearCache();
    assertEquals(0, settingCache.getCacheSize());

  }

  public void testSetAndRemovePage() {
    assertEquals(0, settingCache.getCacheSize());
    settingService.set(Context.GLOBAL, Scope.PAGE.id("name1"), "x1", SettingValue.create("y1"));
    settingService.set(Context.GLOBAL, Scope.PAGE.id("name1"), "x2", SettingValue.create("y2"));
    assertEquals(2, settingCache.getCacheSize());
    settingService.remove(Context.GLOBAL, Scope.PAGE.id("name1"));
    assertEquals(0, settingCache.getCacheSize());
  }

  public void testSetAndRemoveUser() {
    assertEquals(0, settingCache.getCacheSize());
    settingService.set(Context.USER, Scope.PORTAL.id("name1"), "x1", SettingValue.create("y1"));
    settingService.set(Context.USER, Scope.PORTAL.id("name2"), "x2", SettingValue.create("y2"));
    settingService.set(Context.USER, Scope.PAGE.id("name3"), "x3", SettingValue.create("y3"));
    settingService.set(Context.USER, Scope.PAGE.id("name4"), "x4", SettingValue.create("y4"));
    // verify
    assertEquals(4, settingCache.getCacheSize());
    // remove all
    settingService.remove(Context.USER);
    assertEquals(0, settingCache.getCacheSize());
  }

}

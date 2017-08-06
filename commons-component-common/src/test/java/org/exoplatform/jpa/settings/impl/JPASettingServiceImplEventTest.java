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

import org.exoplatform.commons.api.event.EventManager;
import org.exoplatform.commons.api.settings.SettingListener;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.EventType;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.api.settings.data.SettingContext;
import org.exoplatform.commons.api.settings.data.SettingData;
import org.exoplatform.commons.api.settings.data.SettingKey;
import org.exoplatform.commons.api.settings.data.SettingScope;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.jpa.CommonsDAOJPAImplTest;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.settings.jpa.JPASettingServiceImpl;

/**
 * test the events Created by The eXo Platform SAS Author : Nguyen Viet Bang
 * bangnv@exoplatform.com Nov 28, 2012
 */
@ConfiguredBy({ @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/test-jpa-configuration.xml") })
public class JPASettingServiceImplEventTest extends CommonsDAOJPAImplTest {

  protected static JPASettingServiceImpl            settingService;

  private EventManager<SettingService, SettingData> eventManager;

  @SuppressWarnings("unchecked")
  @Override
  public void setUp() throws Exception {
    super.setUp();
    settingService = getService(JPASettingServiceImpl.class);
    ConversationState c = new ConversationState(new Identity("root"));
    ConversationState.setCurrent(c);
    eventManager = getService(EventManager.class);
  }

  public void testEventForSetMethod() {
    ListenerImpl listener = new ListenerImpl();
    listener.setName(EventType.SETTING_SET.toString());
    eventManager.addEventListener(listener);
    Scope spaceScope = Scope.SPACE.id("space");

    settingService.set(Context.USER, spaceScope, "xyz", SettingValue.create("b"));
    // verify the key
    assertEquals(listener.settingContext, new SettingKey(Context.USER, spaceScope, "xyz"));

    // verify the value
    assertEquals(listener.settingValue.getValue(), "b");

    // verify the eventType
    assertEquals(listener.eventype, EventType.SETTING_SET);
  }

  public void testEventForKeyRemove() {
    ListenerImpl listener = new ListenerImpl();
    listener.setName(EventType.SETTING_REMOVE_KEY.toString());
    eventManager.addEventListener(listener);
    Scope spaceScope = Scope.SPACE.id("space");
    settingService.set(Context.USER, spaceScope, "xyz", SettingValue.create("b"));

    settingService.remove(Context.USER, spaceScope, "xyz");

    assertEquals(new SettingKey(Context.USER, spaceScope, "xyz"), listener.settingContext);
    assertEquals(EventType.SETTING_REMOVE_KEY, listener.eventype);
  }

  public void testEventForScopeRemove() {
    settingService.set(Context.GLOBAL, Scope.PAGE.id("name1"), "x1", SettingValue.create("y1"));
    settingService.set(Context.GLOBAL, Scope.PAGE.id("name1"), "x2", SettingValue.create("y2"));

    ListenerImpl listener = new ListenerImpl();
    listener.setName(EventType.SETTING_REMOVE_SCOPE.toString());
    eventManager.addEventListener(listener);

    settingService.remove(Context.GLOBAL, Scope.PAGE.id("name1"));

    assertEquals(listener.settingContext, new SettingScope(Context.GLOBAL, Scope.PAGE.id("name1")));
    assertEquals(listener.eventype, EventType.SETTING_REMOVE_SCOPE);

  }

  public void testEventForUserRemove() {
    ListenerImpl listener = new ListenerImpl();
    listener.setName(EventType.SETTING_REMOVE_CONTEXT.toString());
    eventManager.addEventListener(listener);

    settingService.set(Context.USER, Scope.PORTAL.id("name1"), "x1", SettingValue.create("y1"));
    settingService.set(Context.USER, Scope.PORTAL.id("name2"), "x2", SettingValue.create("y2"));
    settingService.set(Context.USER, Scope.PAGE.id("name3"), "x3", SettingValue.create("y3"));

    settingService.remove(Context.USER);

    assertEquals(listener.settingContext, new SettingContext(Context.USER));
    assertEquals(listener.eventype, EventType.SETTING_REMOVE_CONTEXT);

  }

  class ListenerImpl extends SettingListener {

    private SettingContext  settingContext = null;

    private SettingValue<?> settingValue   = null;

    EventType               eventype;

    @Override
    public void onSet(Event<SettingService, SettingData> event) {
      clearData();
      SettingData data = event.getData();
      if (data.getSettingContext() instanceof SettingKey) {
        settingContext = (SettingKey) data.getSettingContext();
        settingValue = data.getSettingValue();
      }
      eventype = event.getData().getEventType();
    }

    @Override
    public void onRemoveKey(Event<SettingService, SettingData> event) {
      clearData();
      SettingData data = event.getData();
      settingContext = (SettingKey) data.getSettingContext();
      settingValue = data.getSettingValue();
      eventype = event.getData().getEventType();
    }

    @Override
    public void onRemoveScope(Event<SettingService, SettingData> event) {
      clearData();
      SettingData data = event.getData();
      settingContext = (SettingScope) data.getSettingContext();
      eventype = event.getData().getEventType();
    }

    @Override
    public void onRemoveContext(Event<SettingService, SettingData> event) {
      clearData();
      SettingData data = event.getData();
      settingContext = (SettingContext) data.getSettingContext();
      eventype = event.getData().getEventType();
    }

    private void clearData() {
      settingContext = null;
      settingValue = null;
      eventype = null;
    }
  }
}

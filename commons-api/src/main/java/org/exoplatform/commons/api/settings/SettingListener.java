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
package org.exoplatform.commons.api.settings;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.data.EventType;
import org.exoplatform.commons.api.settings.data.SettingData;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;

/**
 * This class is registered with the SettingsService 
 * and is invoked when an event with the same name is broadcasted. 
 * There may be many listeners with the same name to listen to an event.  
 * @LevelAPI Experimental
 */
public abstract class SettingListener extends Listener<SettingService, SettingData> {

  /**
   * Calls back a function when a new setting property is saved. 
   * @param event The event that the setting service dispatches when a new setting property is saved successfully.
   * @LevelAPI Experimental
   */
  public abstract void onSet(Event<SettingService, SettingData> event);
  /**
   * Calls back a function when a setting property is removed with a specified composite key (context, scope, key).
   * @param event The event that the setting service dispatches when a new setting property is removed successfully.
   * @LevelAPI Experimental
   */
  public abstract void onRemoveKey(Event<SettingService, SettingData> event);
  /**
   * Calls back a function when all setting properties in a scope are removed.
   * @param event The event that the setting service dispatches when all setting properties in a scope are removed successfully.
   * @LevelAPI Experimental
   */
  public abstract void onRemoveScope(Event<SettingService, SettingData> event);
  /**
   * Calls back a function when all setting properties in a context are removed.
   * @param event The event that the setting service dispatches when all setting properties in a context are removed successfully.
   * @LevelAPI Experimental
   */
  public abstract void onRemoveContext(Event<SettingService, SettingData> event);

  /**
   * Calls back a function when the setting service dispatches an event.
   * @param event The event that the setting service dispatches.
   * @LevelAPI Experimental
   */
  @Override
  public void onEvent(Event<SettingService, SettingData> event) throws Exception {
    EventType eventType = event.getData().getEventType();
    switch (eventType) {
    case SETTING_SET:
      onSet(event);
      break;
    case SETTING_REMOVE_KEY:
      onRemoveKey(event);
      break;
    case SETTING_REMOVE_SCOPE:
      onRemoveScope(event);
      break;
    case SETTING_REMOVE_CONTEXT:
      onRemoveContext(event);
      break;
    }
  }

}

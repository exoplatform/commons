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
package org.exoplatform.commons.api.settings.data;

import org.exoplatform.commons.api.settings.SettingValue;

/**
 * Supports the setting event management that contains all information of event. 
 * @LevelAPI Experimental
 */
public class SettingData {

  EventType       eventType;

  SettingContext    settingContext;

  SettingValue<?> settingValue;
  
  /**
   * Creates the setting data with the specified event type and context.
   * The context could be USER/GLOBAL context or a specified scope or a specified setting property.  
   * @param eventType The event type that has been dispatched.
   * @param settingContext The event setting context.
   * @LevelAPI Experimental
   */
  public SettingData(EventType eventType, SettingContext settingContext) {
    super();
    this.eventType = eventType;
    this.settingContext = settingContext;
  }

  /**
   * Creates the setting data with the specified event type and setting properties.
   * @param eventType The event type that has been dispatched.
   * @param settingContext The event setting context.
   * @param settingValue The event setting value.
   * @LevelAPI Experimental
   */
  public SettingData(EventType eventType,
                     SettingContext settingContext,
                     SettingValue<?> settingValue) {
    super();
    this.eventType = eventType;
    this.settingContext = settingContext;
    this.settingValue = settingValue;
  }

  /**
   * Gets an event type associated with the setting data.
   * @return The event type.
   * @LevelAPI Experimental
   */
  public EventType getEventType() {
    return eventType;
  }

  /**
   * Sets an event type associated with the setting data.
   * @LevelAPI Experimental
   */ 
  public void setEventType(EventType eventType) {
    this.eventType = eventType;
  }

  /**
   * Gets a setting context associated with the setting data.
   * @return The setting context.
   * @LevelAPI Experimental
   */
  public SettingContext getSettingContext() {
    return settingContext;
  }

  /**
   * Sets a setting context associated with the setting data.
   * @LevelAPI Experimental
   */
  public void setSettingContext(SettingContext settingContext) {
    this.settingContext = settingContext;
  }

  /**
   * Gets a setting value of setting property associated with the setting data.
   * @return The setting value object, or "null" if context is at the Context or Scope level.
   * @LevelAPI Experimental
   */
  public SettingValue<?> getSettingValue() {
	  //TODO: return list of setting value in level Context and Scope
    return settingValue;
  }

  /**
   * Sets a setting value of setting property (SettingKey) associated with the setting data.
   * @LevelAPI Experimental
   */
  public void setSettingValue(SettingValue<?> settingValue) {
	  //TODO: not set setting value in level Context and Scope	  
    this.settingValue = settingValue;
  }
  
  
  
  
  
  
  

}

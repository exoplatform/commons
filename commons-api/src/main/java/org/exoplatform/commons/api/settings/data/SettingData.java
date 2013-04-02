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
 * Class support the setting event management, SettingData contains all information of event to listen. 
 * Created by The eXo Platform SAS Author : Nguyen Viet Bang
 * bangnv@exoplatform.com Nov 27, 2012
 * @LevelAPI Platform
 */
public class SettingData {

  EventType       eventType;

  SettingContext    settingContext;

  SettingValue<?> settingValue;
  
  /**
   * Create setting data with specified event type and context. The context could be USER/GLOBAL context or a specified scope or a specified setting property.  
   * @param eventType	event type has been dispatched
   * @param settingContext setting context data information
   * @LevelAPI Platform
   */
  public SettingData(EventType eventType, SettingContext settingContext) {
    super();
    this.eventType = eventType;
    this.settingContext = settingContext;
  }

  /**
   * Create setting data with specified event type and setting property  
   * @param eventType	event type has been dispatched
   * @param settingContext setting property's context
   * @param settingValue setting property's value
   * @LevelAPI Platform
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
   * get event type associated to this setting data
   * @return event type
   * @LevelAPI Platform
   */
  public EventType getEventType() {
    return eventType;
  }

  /**
   * set event type associated to this setting data
   * @LevelAPI Platform
   */ 
  public void setEventType(EventType eventType) {
    this.eventType = eventType;
  }

  /**
   * get setting context associated to this setting data
   * @return setting context
   * @LevelAPI Platform
   */
  public SettingContext getSettingContext() {
    return settingContext;
  }

  /**
   * set setting context associated to this setting data
   * @LevelAPI Platform
   */
  public void setSettingContext(SettingContext settingContext) {
    this.settingContext = settingContext;
  }

  /**
   * get setting value of setting property associated to this setting data, return null in case of context at level Context and Scope
   * @return setting value object, null if context at level Context and Scope
   * @LevelAPI Platform
   */
  public SettingValue<?> getSettingValue() {
	  //TODO: return list of setting value in level Context and Scope
    return settingValue;
  }

  /**
   * set setting value of setting property (SettingKey) associated to this setting data
   * @LevelAPI Platform
   */
  public void setSettingValue(SettingValue<?> settingValue) {
	  //TODO: not set setting value in level Context and Scope	  
    this.settingValue = settingValue;
  }
  
  
  
  
  
  
  

}

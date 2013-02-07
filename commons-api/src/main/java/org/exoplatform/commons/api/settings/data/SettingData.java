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
 * Created by The eXo Platform SAS Author : Nguyen Viet Bang
 * bangnv@exoplatform.com Nov 27, 2012
 */
public class SettingData {

  EventType       eventType;

  SettingContext    settingContext;

  SettingValue<?> settingValue;
  
  
  

  public SettingData(EventType eventType, SettingContext settingContext) {
    super();
    this.eventType = eventType;
    this.settingContext = settingContext;
  }

  public SettingData(EventType eventType,
                     SettingContext settingContext,
                     SettingValue<?> settingValue) {
    super();
    this.eventType = eventType;
    this.settingContext = settingContext;
    this.settingValue = settingValue;
  }

  public EventType getEventType() {
    return eventType;
  }

  public void setEventType(EventType eventType) {
    this.eventType = eventType;
  }

  public SettingContext getSettingContext() {
    return settingContext;
  }

  public void setSettingContext(SettingContext settingContext) {
    this.settingContext = settingContext;
  }

  public SettingValue<?> getSettingValue() {
    return settingValue;
  }

  public void setSettingValue(SettingValue<?> settingValue) {
    this.settingValue = settingValue;
  }
  
  
  
  
  
  
  

}

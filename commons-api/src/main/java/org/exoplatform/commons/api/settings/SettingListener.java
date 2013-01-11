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
 * Created by The eXo Platform SAS Author : Nguyen Viet Bang
 * bangnv@exoplatform.com Nov 27, 2012
 */
public abstract class SettingListener extends Listener<SettingService, SettingData> {

  public abstract void onSet(Event<SettingService, SettingData> event);

  public abstract void onRemoveKey(Event<SettingService, SettingData> event);

  public abstract void onRemoveScope(Event<SettingService, SettingData> event);

  public abstract void onRemoveContext(Event<SettingService, SettingData> event);

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

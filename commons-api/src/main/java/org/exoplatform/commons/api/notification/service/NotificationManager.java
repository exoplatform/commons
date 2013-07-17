/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.commons.api.notification.service;

import java.util.Map;

import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.api.notification.plugin.NotificationKey;


public interface NotificationManager {
  
  public enum ScheduleType {
    DAILY,
    INSTANTLY,
    WEEKY
  }

  /**
   * Gets the notification plug in list
   * @return
   */
  Map<NotificationKey, AbstractNotificationPlugin> getNotificationPlugins();
  
  AbstractNotificationPlugin get(NotificationKey key);
  
  boolean register(AbstractNotificationPlugin plugin);
  
  boolean unregister(NotificationKey key);
  
  boolean isActivated(NotificationKey key);
  
  /**
   * 
   * @param remoteId
   * @param key
   * @param option
   * @return
   */
  boolean isValid(String remoteId, NotificationKey key, ScheduleType option);
  
}

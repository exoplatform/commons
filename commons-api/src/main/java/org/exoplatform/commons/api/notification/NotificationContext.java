/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.api.notification;

import java.util.List;

import org.exoplatform.commons.api.notification.command.NotificationCommand;
import org.exoplatform.commons.api.notification.command.NotificationExecutor;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.NotificationKey;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.notification.service.setting.PluginContainer;

public interface NotificationContext extends Cloneable {

  /**
   * Append the argument literal.
   * @param param
   * @return
   */
  <T> NotificationContext append(ArgumentLiteral<T> argument, Object value);
  
  /**
   * Removes the query parameter.
   * @param param
   * @return
   */
  <T> NotificationContext remove(ArgumentLiteral<T> filter);
  
  /**
   * Clear all of filter optional
   */
  void clear();
  /**
   * Gets FilterOption which was existing.
   * @param param
   * @return
   */
  <T> T value(ArgumentLiteral<T> argument);
  
  /**
   * Gets notification information
   * @return
   */
  NotificationInfo getNotificationInfo();
  
  /**
   * Sets notification infomation
   * @param notification
   */
  NotificationContext setNotificationInfo(NotificationInfo notification);
  
  /**
   * Sets notification information list
   * @param notifications
   */
  void setNotificationInfos(List<NotificationInfo> notifications);
  
  /**
   * Gets notification message list
   * @return
   */
  List<NotificationInfo> getNotificationInfos();
  
  
  Exception getException();
  
  <T> T getException(Class<T> type);
  
  void setException(Throwable t);
  
  boolean isFailed();
  
  NotificationExecutor getNotificationExecutor();
  
  NotificationCommand makeCommand(NotificationKey key);

  /**
   * Creates and returns a copy of this object
   * 
   * If class extends NotificationContextImpl and implements method clone(),
   * then must use supper.clone();
   * @see java.lang.Object#clone()
   */
  NotificationContext clone();
  
  PluginSettingService getPluginSettingService();
  
  /**
   * Gets the plugin container what contains all plugins on Notification
   * @return
   */
  PluginContainer getPluginContainer();
  
}

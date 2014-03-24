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
package org.exoplatform.commons.api.notification.service.setting;

import java.util.List;

import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.services.organization.User;

public interface UserSettingService {

  /**
   * Saves the notification settings of a user.
   * 
   * @param notificationSetting The notification settings.
   */
  void save(UserSetting notificationSetting);

  /**
   * Gets the notification settings of a user by his remote Id.
   * 
   * @param userId The user's remote Id.
   * @return The notification settings.
   */
  UserSetting get(String userId);

  /**
   * Gets a list of user settings which are registered for daily notifications.
   * 
   * @param offset The start point from which the user settings are got.
   * @param limit The limited number of user settings.
   * @return The list of user settings.
   */
  List<UserSetting> getDaily(int offset, int limit);

  /**
   * Gets a number of users registering for daily notifications.
   * @return The number of users.
   */
  long getNumberOfDaily();
  
  /**
   * Gets all settings of users registering for default daily notifications.
   * @param offset The start point from which the user settings are got.
   * @param limit The limited number of user settings.
   * @return The list of user settings.
   */
  List<UserSetting> getDefaultDaily(int offset, int limit);
  
  /**
   * Gets a number of users used default configuration notifications.
   * @return The number of users.
   */
  long getNumberOfDefaultDaily();
  
  /**
   * Gets all Ids of users registering for notifications by a given plugin.
   * 
   * @param pluginId Id of the plugin.
   * @return The remote Ids of users.
   */
  List<String> getUserSettingByPlugin(String pluginId);
  
  /**
   * Adds the default settings to a user's node.
   * 
   * @param userId The user's remote Id.
   */
  void addMixin(String userId);
  
  /**
   * Adds the default settings to a list of users.
   * 
   * @param users The list of users.
   */
  void addMixin(User[] users);
}

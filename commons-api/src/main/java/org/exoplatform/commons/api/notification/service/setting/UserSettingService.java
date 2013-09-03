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
   * Save notification setting of an user
   * 
   * @param notificationSetting notification setting of user
   */
  void save(UserSetting notificationSetting);

  /**
   * Gets the notification setting of an user by his remote id
   * 
   * @param userId remote id of user
   * @return notification setting
   */
  UserSetting get(String userId);

  /**
   * Gets all user has config to receive notification daily
   * 
   * @param offset
   * @param limit
   * @return list of user's notification setting
   */
  List<UserSetting> getDaily(int offset, int limit);

  /**
   * @return numbers of user has config to receive notification daily
   */
  long getNumberOfDaily();
  
  /**
   * Gets all user has a default notification setting
   * 
   * @return list of user's notification setting
   */
  List<UserSetting> getDefaultDaily();
  
  /**
   * Gets all user has a config to receive the notification of a plugin
   * 
   * @param pluginId id of the plugin
   * @return list of user's remote id
   */
  List<String> getUserSettingByPlugin(String pluginId);
  
  /**
   * Add mix:defaultSetting to the node of a user
   * 
   * @param userId remote id of user
   */
  void addMixin(String userId);
  
  /**
   * Add mix:defaultSetting to the node of users
   * 
   * @param users list of users
   */
  void addMixin(User[] users);
}

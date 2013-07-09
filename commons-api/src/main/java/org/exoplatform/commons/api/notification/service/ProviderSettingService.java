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
package org.exoplatform.commons.api.notification.service;

import java.util.List;
import java.util.Map;

import org.exoplatform.commons.api.notification.plugin.ActiveProviderPlugin;
import org.exoplatform.commons.api.notification.plugin.GroupProviderModel;
import org.exoplatform.commons.api.notification.plugin.GroupProviderPlugin;

public interface ProviderSettingService {
  /**
   * The function to register active provider plugin.
   * 
   * @param activeProviderPlugin
   */
  void registerActiveProviderPlugin(ActiveProviderPlugin activeProviderPlugin);

  /**
   * The function to register group provider plugin.
   * 
   * @param groupProviderPlugin
   */
  void registerGroupProviderPlugin(GroupProviderPlugin groupProviderPlugin);

  /**
   * Get all active Provider's Id for users's setting
   * 
   * @param isAdmin is administrator or not.
   * @return
   */
  List<String> getActiveProviderIds(boolean isAdmin);

  /**
   * Get all active profiver's Id for administrator's setting
   * 
   * @return the Map data with
   *  + Key is active provider's Id
   *  + Value is administrator or not.
   */
  Map<String, Boolean> getActiveProviderIdForSetting();

  /**
   * Save active provider's Id on administrators's setting
   * 
   * @param mapProviderId the map to save
   *  + Key is active provider's Id
   *  + Value is administrator or not.
   */
  void setActiveProviders(Map<String, Boolean> mapProviderId);
  
  /**
   * 
   * @return
   */
  List<GroupProviderModel> getGroupProviders();

}

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

import org.exoplatform.commons.api.notification.model.GroupProvider;
import org.exoplatform.commons.api.notification.model.PluginInfo;
import org.exoplatform.commons.api.notification.plugin.GroupProviderPlugin;
import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;

public interface PluginSettingService {

  /**
   * Registers configuration of a plugin.
   * @param pluginConfig The plugin configuration.
   */
  void registerPluginConfig(PluginConfig pluginConfig);

  /**
   * Registers the plugin configuration of a group.
   * @param pluginConfig The plugin configuration to be registered.
   */
  void registerGroupConfig(GroupProviderPlugin groupConfig);
  
  /**
   * Gets configuration of a plugin from its Id.
   * 
   * @param pluginId Id of the plugin.
   * @return The plugin configuration.
   */
  PluginConfig getPluginConfig(String pluginId);

  /**
   * Gets a list of groups containing plugins.
   * 
   * @return The list of groups.
   */
  List<GroupProvider> getGroupPlugins();

  /**
   * Saves a plugin.
   * 
   * @param pluginId Id of the saved plugin.
   * @param isActive If "true", the plugin is active. If "false", the plugin is inactive.
   */
  void savePlugin(String pluginId, boolean isActive);

  /**
   * Checks if a plugin is active or inactive.
   * 
   * @param pluginId Id of the plugin.
   * @return The returned value is "true" if the plugin is active or "false" if the plugin is inactive.
   */
  boolean isActive(String pluginId);
  
  /**
   * Gets all Ids of active plugins.
   * 
   * @return Ids of the active plugins.
   */
  List<String> getActivePluginIds();

  /**
   * Gets information of all active plugins.
   * 
   * @return Information of the active plugins.
   */
  List<PluginInfo> getActivePlugins();

}

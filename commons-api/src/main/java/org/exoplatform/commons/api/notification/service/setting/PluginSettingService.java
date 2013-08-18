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
   * @param pluginConfig
   */
  void registerPluginConfig(PluginConfig pluginConfig);

  /**
   * @param pluginConfig
   */
  void registerGroupConfig(GroupProviderPlugin groupConfig);
  
  /**
   * @param pluginId
   * @return
   */
  PluginConfig getPluginConfig(String pluginId);

  /**
   * @return
   */
  List<GroupProvider> getGroupPlugins();

  /**
   * @param pluginId
   * @param isActive
   */
  void savePlugin(String pluginId, boolean isActive);

  /**
   * @param pluginId
   * @return
   */
  boolean isActive(String pluginId);
  /**
   * @return
   */
  List<String> getActivePluginIds();

  /**
   * @return
   */
  List<PluginInfo> getActivePlugins();

}

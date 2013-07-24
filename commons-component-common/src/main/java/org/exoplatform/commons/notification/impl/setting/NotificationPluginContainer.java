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
package org.exoplatform.commons.notification.impl.setting;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.api.notification.plugin.NotificationKey;
import org.exoplatform.commons.api.notification.plugin.model.PluginConfig;
import org.exoplatform.commons.api.notification.service.setting.ProviderSettingService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.picocontainer.Startable;

public class NotificationPluginContainer implements Startable {
  private final Map<NotificationKey, AbstractNotificationPlugin> pluginMap;
  private ProviderSettingService settingService;
  
  public NotificationPluginContainer() {
    pluginMap = new HashMap<NotificationKey, AbstractNotificationPlugin>();
    this.settingService = CommonsUtils.getService(ProviderSettingService.class);
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() {
  }

  public AbstractNotificationPlugin getPlugin(NotificationKey key) {
    return pluginMap.get(key);
  }

  public void add(AbstractNotificationPlugin plugin) {
    pluginMap.put(plugin.getKey(), plugin);
    //
    for (PluginConfig pluginConfig : plugin.getPluginConfigs()) {
      settingService.registerPluginConfig(pluginConfig);
    }
  }

  public boolean remove(NotificationKey key) {
    pluginMap.remove(key);
    return true;
  }


}

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
package org.exoplatform.commons.api.notification.plugin;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.organization.OrganizationService;

public abstract class BaseNotificationPlugin extends BaseComponentPlugin {
  /** the flag determines the plugin is origin o*/
  private boolean isOldPlugin = false;
  /** */
  private List<PluginConfig> pluginConfig = new ArrayList<PluginConfig>();
  
  public BaseNotificationPlugin(InitParams initParams) {
    pluginConfig = initParams.getObjectParamValues(PluginConfig.class);
  }
  
  public List<PluginConfig> getPluginConfigs() {
    return pluginConfig;
  }
  
  /**
   * Start the plug in
   * @param context
   * @return
   */
  public void start(NotificationContext ctx) {
    
  }
  
  /**
   * End the plug in
   * @param context
   * @return
   */
  public void end(NotificationContext ctx) {
    
  }
  /**
   * Determines the plugin is old or new mechanism
   * The target adapts the old plugin from PLF 4.1 to work well new mechanism
   * @return
   */
  public boolean isOldPlugin() {
    return isOldPlugin;
  }

  /**
   * Sets the flag value TRUE/FALSE
   * The target adapts the old plugin on PLF 4.1 to work well new mechanism
   * 
   * @param isOldPlugin
   */
  public void setOldPlugin(boolean isOldPlugin) {
    this.isOldPlugin = isOldPlugin;
  }

  /**
   * Gets Notification Plug in key
   * @return
   */
  public abstract String getId();
  
  /**
   * Check, for each plugin, if we will send notification
   * @return
   */
  public abstract boolean isValid(NotificationContext ctx);
  
  /**
   * Makes MessageInfo from given information what keep inside NotificationContext
   * @param context
   * @return
   */
  protected abstract NotificationInfo makeNotification(NotificationContext ctx);
  
  /**
   * Makes notification
   * @param ctx
   * @return
   */
  public NotificationInfo buildNotification(NotificationContext ctx) {
    return makeNotification(ctx);
  }
  
  /**
   * Creates the key for NotificationPlugin
   * @return
   */
  public PluginKey getKey() {
    return PluginKey.key(this);
  }
  
  protected OrganizationService getOrganizationService() {
    return NotificationPluginUtils.getOrganizationService();
  }
}

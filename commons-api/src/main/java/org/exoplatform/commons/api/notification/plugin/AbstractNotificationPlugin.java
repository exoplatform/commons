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

import groovy.text.Template;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationKey;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.organization.OrganizationService;

public abstract class AbstractNotificationPlugin extends BaseComponentPlugin {
  List<PluginConfig> pluginConfig = new ArrayList<PluginConfig>();
  
  private Template engine;
  
  public AbstractNotificationPlugin(InitParams initParams) {
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
   * Makes the MessageInfor from given NotificationMessage what keep inside NotificationContext
   * @param context
   * @return
   */
  protected abstract MessageInfo makeMessage(NotificationContext ctx);
  
  /**
   * Makes the Digest message from given NotificationMessage what keep inside NotificationContext
   * @param ctx
   * @param wtiter
   * @return
   */
  protected abstract boolean makeDigest(NotificationContext ctx, Writer writer);
  
  /**
   * Makes notification
   * @param ctx
   * @return
   */
  public NotificationInfo buildNotification(NotificationContext ctx) {
    return makeNotification(ctx);
  }
  
  /**
   * Makes massage
   * @param ctx
   * @return
   */
  public MessageInfo buildMessage(NotificationContext ctx) {
    NotificationInfo message = ctx.getNotificationInfo();
    MessageInfo messageInfo = makeMessage(ctx);
    return messageInfo.pluginId(getId()).from(NotificationPluginUtils.getFrom(message.getFrom()))
               .to(NotificationPluginUtils.getTo(message.getTo())).end();
  }

  /**
   * Makes digest message
   * @param ctx
   * @param writer
   * @return
   */
  public boolean buildDigest(NotificationContext ctx, Writer writer) {
    return makeDigest(ctx, writer);
  }
  
  /**
   * Creates the key for NotificationPlugin
   * @return
   */
  public NotificationKey getKey() {
    return NotificationKey.key(this);
  }
  
  /**
   * 
   * @param message
   * @return
   */
  protected String getLanguage(NotificationInfo message) {
    return NotificationPluginUtils.getLanguage(message.getTo());
  }
  
  protected OrganizationService getOrganizationService() {
    return NotificationPluginUtils.getOrganizationService();
  }

  /**
   * Get TemplateEngine of plugin
   * @return the TemplateEngine
   */
  public Template getTemplateEngine() {
    return engine;
  }

  /**
   * Set TemplateEngine for plugin
   * @param engine the TemplateEngine to set
   */
  public void setTemplateEngine(Template engine) {
    this.engine = engine;
  }

}

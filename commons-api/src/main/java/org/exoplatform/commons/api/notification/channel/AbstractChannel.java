/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.commons.api.notification.channel;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.lifecycle.AbstractNotificationLifecycle;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.jboss.util.Strings;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 12, 2014  
 */
public abstract class AbstractChannel extends BaseComponentPlugin {
  /** Defines the lifecycle what will handle the notification each channel*/
  private final AbstractNotificationLifecycle lifecycle;
  
  public AbstractChannel(AbstractNotificationLifecycle lifecycle) {
    this.lifecycle = lifecycle;
    this.lifecycle.setChannel(this);
  }
  /**
   * Initialize something when starts to work the channel
   */
  public void start(){};
  
  public void end(){};
  
  
  /**
   * Gets ChannelId
   * @return
   */
  public abstract String getId();
  
  /**
   * Gets Channel Key
   * @return
   */
  public abstract ChannelKey getKey();
  
  /**
   * Gets the lifecycle what assigned to the channel
   * @return
   */
  public AbstractNotificationLifecycle getLifecycle() {
    return this.lifecycle;
  }
  
  /**
   * Process the notification information for the specified user.
   * 
   * @param ctx The NotificationContext
   * @param userId the user will be received the message.
   */
  public abstract void dispatch(NotificationContext ctx, String userId);
  
  /**
   * Process the notification info
   * @param notifInfo the notification information
   */
  public void dispatch(NotificationInfo notifInfo) {}
  
  /**
   * Register the template provider to the channel.
   * @param provider
   */
  public abstract void registerTemplateProvider(TemplateProvider provider);
  
  /**
   * Gets the template by the specified pluginId
   * @param key the plugin key
   */
  public String getTemplateFilePath(PluginKey key) {
    return Strings.EMPTY;
  }
  
  /**
   * Has the template builder of the plugin and the channel
   * @param key the plugin key
   */
  public boolean hasTemplateBuilder(PluginKey key) {
    return false;
  }
  
  /**
   * Gets the template builder by the specified PluginKey
   * @param key the PluginKey
   */
  public AbstractTemplateBuilder getTemplateBuilder(PluginKey key) {
    return null;
  }
  
}
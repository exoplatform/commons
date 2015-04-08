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
package org.exoplatform.commons.api.notification.channel.template;

import java.io.Writer;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.api.notification.service.setting.PluginContainer;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 23, 2014  
 */
public class PluginTemplateBuilderAdapter extends AbstractTemplateBuilder {

  @Override
  protected MessageInfo makeMessage(NotificationContext ctx) {
    BaseNotificationPlugin basePlugin =  getPluginContainer().getPlugin(ctx.getNotificationInfo().getKey());
    if (basePlugin.isOldPlugin()) {
      AbstractNotificationPlugin abstractPlugin = (AbstractNotificationPlugin) basePlugin;
      return abstractPlugin.buildMessage(ctx);
    }
    return null;
  }

  @Override
  protected boolean makeDigest(NotificationContext ctx, Writer writer) {
    BaseNotificationPlugin basePlugin =  getPluginContainer().getPlugin(ctx.getNotificationInfo().getKey());
    if (basePlugin.isOldPlugin()) {
      AbstractNotificationPlugin abstractPlugin = (AbstractNotificationPlugin) basePlugin;
      return abstractPlugin.buildDigest(ctx, writer);
    }
    return false;
  }
  
  /**
   * Gets the plugin container
   * 
   * @return
   */
  private PluginContainer getPluginContainer() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    return (PluginContainer) container.getComponentInstanceOfType(PluginContainer.class);
  }

}

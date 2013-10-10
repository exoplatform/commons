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
package org.exoplatform.commons.notification.impl.command;

import java.io.Writer;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.command.NotificationCommand;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.NotificationKey;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;

public class NotificationCommandImpl implements NotificationCommand {

  private final AbstractNotificationPlugin plugin;
  
  public NotificationCommandImpl(AbstractNotificationPlugin plugin) {
    this.plugin = plugin;
  }
  
  @Override
  public AbstractNotificationPlugin getPlugin() {
    return this.plugin;
  }

  @Override
  public NotificationKey getNotificationKey() {
    return this.plugin.getKey();
  }

  @Override
  public MessageInfo processMessage(NotificationContext ctx) {
    return plugin.buildMessage(ctx);
  }

  @Override
  public NotificationInfo processNotification(NotificationContext ctx) {
    return plugin.buildNotification(ctx);
  }

  @Override
  public void processDigest(NotificationContext ctx, Writer writer) {
    plugin.buildDigest(ctx, writer);
    
  }
  
  @Override
  public String toString() {
    return "NotificationCommand[" + plugin.getKey().getId() + "]";
  }

}

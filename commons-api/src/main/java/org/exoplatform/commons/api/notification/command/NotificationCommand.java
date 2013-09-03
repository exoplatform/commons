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
package org.exoplatform.commons.api.notification.command;

import java.io.Writer;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationKey;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;

public interface NotificationCommand {

  /**
   * Gets the plugin associated of the notification's command
   * @return notification plugin
   */
  AbstractNotificationPlugin getPlugin();
  
  /**
   * Get the notification key associated of the notification's command
   * @return notification key
   */
  NotificationKey getNotificationKey();
  
  /**
   * Build the message info from a notification's context
   * @param ctx notification context
   * @return message info
   */
  MessageInfo processMessage(NotificationContext ctx);
  
  /**
   * Build the notification info from a notification's context
   * @param ctx notification context
   * @return notification info
   */
  NotificationInfo processNotification(NotificationContext ctx);
  
  /**
   * Build the digest message to send daily or weekly
   * @param ctx notification context
   * @param writer to store the digest message
   */
  void processDigest(NotificationContext ctx, Writer writer);
  
  
}

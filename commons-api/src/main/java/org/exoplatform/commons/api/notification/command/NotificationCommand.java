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
   * Gets a plugin associated with the notification command.
   * @return The notification plugin.
   */
  AbstractNotificationPlugin getPlugin();
  
  /**
   * Gets a notification key associated with the notification command.
   * @return The notification key.
   */
  NotificationKey getNotificationKey();
  
  /**
   * Builds information of a message from the notification context.
   * @param ctx The notification context.
   * @return The message information.
   */
  MessageInfo processMessage(NotificationContext ctx);
  
  /**
   * Builds information of a notification from the notification context.
   * @param ctx The notification context.
   * @return The notification information.
   */
  NotificationInfo processNotification(NotificationContext ctx);
  
  /**
   * Builds a digest message which is sent daily or weekly.
   * @param ctx The notification context.
   * @param writer Stores the digest message.
   */
  void processDigest(NotificationContext ctx, Writer writer);
  
  
}

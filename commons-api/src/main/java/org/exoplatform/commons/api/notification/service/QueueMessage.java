/*
 * Copyright (C) 2003-${year} eXo Platform SAS.
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.api.notification.service;

import org.exoplatform.commons.api.notification.model.MessageInfo;

public interface QueueMessage {
  public static final String MESSAGE_ADDED_IN_QUEUE     = "MESSAGE_ADDED_IN_QUEUE";

  public static final String MESSAGE_DELETED_FROM_QUEUE = "MESSAGE_DELETED_FROM_QUEUE";

  public static final String MESSAGE_SENT_FROM_QUEUE    = "MESSAGE_SENT_FROM_QUEUE";

  /**
   * Puts the message into the queue
   * 
   * @param message
   * @return
   * @throws Exception
   */
  boolean put(MessageInfo message) throws Exception;

  /**
   * Peek the message from queue and send
   * @throws Exception
   */
  void send() throws Exception;

  /**
   * Sends mail instantly without passing by queue (use directly the mail
   * service). If sending mail is suspended (by Mail Counter MBean), the message
   * will not be sent, thus it will be suspended.
   * 
   * @param message the message to be sent
   * @return true if the message is sent or mail service is off
   * @throws Exception
   */
  boolean sendMessage(MessageInfo message) throws Exception;

  /**
   * Removes all queue elements
   *
   * @throws Exception
   */
  void removeAll() throws Exception;

  /**
   * Enable/Disable sending mail message
   * 
   * @param enabled true to enable and false to disable
   */
  void enable(boolean enabled);
}

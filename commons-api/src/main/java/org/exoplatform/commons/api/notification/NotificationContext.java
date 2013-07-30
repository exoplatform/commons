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
package org.exoplatform.commons.api.notification;

import java.util.List;

import org.exoplatform.commons.api.notification.command.NotificationCommand;
import org.exoplatform.commons.api.notification.command.NotificationExecutor;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.NotificationKey;
import org.exoplatform.commons.api.notification.model.NotificationMessage;

public interface NotificationContext {

  /**
   * Append the argument literal.
   * @param param
   * @return
   */
  <T> NotificationContext append(ArgumentLiteral<T> argument, Object value);
  
  /**
   * Removes the query parameter.
   * @param param
   * @return
   */
  <T> NotificationContext remove(ArgumentLiteral<T> filter);
  
  /**
   * Clear all of filter optional
   */
  void clear();
  /**
   * Gets FilterOption which was existing.
   * @param param
   * @return
   */
  <T> T value(ArgumentLiteral<T> argument);
  
  /**
   * Gets notification message
   * @return
   */
  NotificationMessage getNotificationMessage();
  
  /**
   * Sets notification message
   * @param notification
   */
  NotificationContext setNotificationMessage(NotificationMessage notification);
  
  /**
   * Sets notification message list
   * @param notifications
   */
  void setNotificationMessages(List<NotificationMessage> notifications);
  
  /**
   * Gets notification message list
   * @return
   */
  List<NotificationMessage> getNotificationMessages();
  
  
  Exception getException();
  
  <T> T getException(Class<T> type);
  
  void setException(Throwable t);
  
  boolean isFailed();
  
  NotificationExecutor getNotificationExecutor();
  
  NotificationCommand makeCommand(NotificationKey key);
  
}

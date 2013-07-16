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
package org.exoplatform.commons.api.notification.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.UserNotificationSetting;



public interface NotificationService {
  
  void addNotificationServiceListener();

  void addSendNotificationListener(AbstractNotificationServiceListener listener);

  /**
   * Process notification message when have new a @NotificationMessage created.
   * 
   * @param message the new a @NotificationMessage
   */
  void processNotificationMessage(NotificationMessage  message) ;
  
  /**
   * Process the list notification message when have new list @NotificationMessage created.
   * 
   * @param messages
   */
  void processNotificationMessages(Collection<NotificationMessage> messages);

  /**
   * Save @NotificationMessage on the database
   * @param message
   */
  void saveNotificationMessage(NotificationMessage  message);
  
  /**
   * Get all @NotificationMessage by userId
   * @param userId
   * @param providerId TODO
   * @return
   */
  Map<String, List<NotificationMessage>> getNotificationMessagesByUser(UserNotificationSetting userSetting);

  NotificationMessage.SEND_TYPE getSendNotificationType(String userId);
}

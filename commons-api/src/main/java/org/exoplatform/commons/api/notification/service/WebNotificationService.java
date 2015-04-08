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

import java.util.List;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;

public interface WebNotificationService {
  /**
   * Creates the new notification message to the specified user.
   * The userId gets from the notification#getTo().
   * 
   * @param notification the notification
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  void save(NotificationInfo notification);

  /**
   * Marks the notification to be read by the userId
   * @param notificationId the Notification Id
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  void markRead(String notificationId);

  /**
   * Marks all notifications what belong to the user to be read.
   * 
   * 
   * @param userId the userId
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  void markAllRead(String userId);

  /**
   * Updates the notification's popover list status to be FALSE value
   * However it's still showing on View All page.
   * 
   * @param notificationId the Notification Id
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  void hidePopover(String notificationId);

  /**
   * Gets the notification list by the given filter.
   * 
   * The filter consist of these criteria:
   * + UserId
   * + isPopover TRUE/FALSE
   * + Read TRUE/FALSE
   * 
   * @param filter the filter condition
   * @param offset
   * @param limit
   * @return The notification list matched the given filter
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  List<String> get(WebNotificationFilter filter, int offset, int limit);

  /**
   * Removes the notification by the notificationId
   * 
   * @param notificationId
   * @return Returns TRUE if removing successfully Otherwise FALSE
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  boolean remove(String notificationId);
  
  /**
   * Gets the number on the badge by the specified user
   * @param userId
   * @return
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  int getNumberOnBadge(String userId);
  
  
  /**
   * @param userId
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  void resetNumberOnBadge(String userId);
}
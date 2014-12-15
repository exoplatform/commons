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
package org.exoplatform.commons.api.notification.service.storage;

import java.util.List;

import org.exoplatform.commons.api.notification.model.NotificationInfo;


public interface IntranetNotificationDataStorage {
  
  /**
   * Saves information of a notification.
   * 
   * @param notification The notification to be saved.
   * @throws Exception
   */
  void save(NotificationInfo notification) throws Exception;

  /**
   * 
   * @param userId
   * @param notificationId the NotificationInfo's id
   * @throws Exception
   */
  void saveRead(String userId, String notificationId) throws Exception;

  /**
   * @param userId
   * @throws Exception
   */
  void saveReadAll(String userId) throws Exception;

  /**
   * @param userId the user's id of owner NotificationInfo
   * @param offset The offset
   * @param limit The limit
   * @throws Exception
   */
  List<NotificationInfo> get(String userId, int offset, int limit) throws Exception;

  /**
   * @param userId
   * @param notificationId the NotificationInfo's id
   * @return the status removed or not
   * @throws Exception
   */
  boolean remove(String userId, String notificationId) throws Exception;

  /**
   * Remove the NotificationInfo live after X days
   * @param days 
   * @return the status removed or not
   * @throws Exception
   */
  boolean remove(int days) throws Exception;
}
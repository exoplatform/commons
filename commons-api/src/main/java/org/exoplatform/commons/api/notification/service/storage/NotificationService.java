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

import java.util.Collection;

import org.exoplatform.commons.api.notification.model.NotificationInfo;


public interface NotificationService {
  /**
   * Processes information when a notification message is created.
   * 
   * @param notification The notification message.
   */
  void process(NotificationInfo  notification) throws Exception;
  
  /**
   * Collects information of a digest message and sends it daily or weekly.
   *
   * @throws Exception
   */
  void processDigest() throws Exception;
  
  /**
   * Processes information when a list of notification messages are created.
   * 
   * @param notifications The list of notification messages.
   */
  void process(Collection<NotificationInfo> notifications) throws Exception;
  
}

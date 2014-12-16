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
package org.exoplatform.commons.notification.impl.service;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebFilter;
import org.exoplatform.commons.api.notification.service.WebNotificationService;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class WebNotificationServiceImpl implements WebNotificationService {
  private static final Log LOG = ExoLogger.getLogger(WebNotificationServiceImpl.class);
  private final WebNotificationStorage webStorage;
  public WebNotificationServiceImpl(WebNotificationStorage webStorage) {
    this.webStorage = webStorage;
  }

  @Override
  public void save(NotificationInfo notification) {
    webStorage.save(notification);
  }

  @Override
  public void markRead(String notificationId) {
    webStorage.markRead(notificationId);
  }

  @Override
  public void markReadAll(String userId) {
    webStorage.markReadAll(userId);
  }

  @Override
  public List<String> getNotificationContents(WebFilter filter) {
    List<String> messages = new ArrayList<String>();
    List<NotificationInfo> notificationInfos = webStorage.get(filter);
    //
    
    return messages;
  }

  @Override
  public void remove(String userId, String notificationId) {
    webStorage.remove(notificationId);
  }
}

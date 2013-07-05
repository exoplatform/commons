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
package org.exoplatform.commons.notification.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.exoplatform.commons.api.notification.NotificationDataStorage;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.service.NotificationService;

public class NotificationDataStorageImpl implements NotificationDataStorage {
  
  NotificationService              notificationService;

  Queue<NotificationMessage>       queue = new ConcurrentLinkedQueue<NotificationMessage>();

  public NotificationDataStorageImpl(NotificationService notificationService) {
    this.notificationService = notificationService;
  }
  
  public NotificationDataStorageImpl add(NotificationMessage notificationMessage) {
    queue.add(notificationMessage);
    notificationService.addNotificationServiceListener();
    return this;
  }

  public NotificationDataStorageImpl addAll(Collection<NotificationMessage> notificationMessages) {
    queue.addAll(notificationMessages);
    notificationService.addNotificationServiceListener();
    return this;
  }
  
  public int size() {
    return queue.size();
  }
  
  public Collection<NotificationMessage> emails() {
    Collection<NotificationMessage> messages = new ArrayList<NotificationMessage>(queue);
    queue.clear();
    return messages;
  }
}

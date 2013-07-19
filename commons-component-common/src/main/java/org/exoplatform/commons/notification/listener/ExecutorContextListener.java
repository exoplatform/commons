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
package org.exoplatform.commons.notification.listener;

import java.util.concurrent.Callable;

import org.exoplatform.commons.api.notification.service.storage.NotificationDataStorage;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.utils.CommonsUtils;

public class ExecutorContextListener implements Callable<Boolean>{
  
  private static ExecutorContextListener  instance;
  
  @Override
  public Boolean call() throws Exception {
    // get all notification
    try {
      NotificationDataStorage dataStorage = CommonsUtils.getService(NotificationDataStorage.class);

      NotificationService notificationService = CommonsUtils.getService(NotificationService.class);

      notificationService.processNotificationMessages(dataStorage.emails());

    } catch (Exception e) {
      return false;
    }

    return true;
  }
  
  public static ExecutorContextListener getInstance() {
    if (instance == null) {
      synchronized (ExecutorContextListener.class) {
        if (instance == null) {
          instance = new ExecutorContextListener();
        }
      }
    }

    return instance;
  }

}

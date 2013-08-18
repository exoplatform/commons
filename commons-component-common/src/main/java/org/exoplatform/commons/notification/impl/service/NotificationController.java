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
package org.exoplatform.commons.notification.impl.service;

import java.util.List;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.notification.service.storage.NotificationDataStorage;
import org.exoplatform.commons.notification.impl.service.process.NotificationDaily;
import org.exoplatform.commons.notification.impl.service.process.NotificationInstantly;
import org.exoplatform.commons.notification.impl.service.process.NotificationProcess;
import org.exoplatform.commons.notification.impl.service.process.NotificationWeekly;
import org.exoplatform.commons.utils.CommonsUtils;

public class NotificationController {

  private final NotificationProcess instantly;

  public static NotificationController DEFAULT = new NotificationController();
  
  private final UserSettingService notificationService;
  private final NotificationDataStorage storage;
  
  public NotificationController() {
    
    //initialize chain of responsibilities processing
    NotificationProcess daily = new NotificationDaily();
    this.instantly = new NotificationInstantly();
    
    //
    daily.setNext(new NotificationWeekly());
    this.instantly.setNext(daily);
    
    this.notificationService = CommonsUtils.getService(UserSettingService.class);
    this.storage = CommonsUtils.getService(NotificationDataStorage.class);
  }
  
  
  public void process(NotificationInfo notification) throws Exception {
    List<String> userIds = notification.getSendToUserIds();
    for (String userId : userIds) {
      UserSetting setting = notificationService.get(userId);
      this.instantly.process(setting, notification);
    }
    
    save(notification);
  }


  private void save(NotificationInfo notification) throws Exception {
    if (notification.getSendToDaily().length > 0 || 
        notification.getSendToWeekly().length > 0) {
      storage.save(notification);
    }
  }
}

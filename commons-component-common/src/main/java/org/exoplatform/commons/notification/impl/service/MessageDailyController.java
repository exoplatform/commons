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
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.notification.impl.service.process.MessageDaily;
import org.exoplatform.commons.notification.impl.service.process.MessageProcess;
import org.exoplatform.commons.notification.impl.service.process.MessageWeekly;
import org.exoplatform.commons.notification.impl.setting.NotificationPluginContainer;
import org.exoplatform.commons.utils.CommonsUtils;

public class MessageDailyController {
  
  private final MessageProcess daily;

  public static MessageDailyController DEFAULT = new MessageDailyController();
  
  private final NotificationPluginContainer pluginService;
  
  private final PluginSettingService providerService;
  
  public MessageDailyController() {
    
    //initialize chain of responsibilities processing
    this.daily = new MessageDaily();
    daily.setNext(new MessageWeekly());
    this.pluginService = CommonsUtils.getService(NotificationPluginContainer.class);
    this.providerService = CommonsUtils.getService(PluginSettingService.class);
  }
  
  
  public void process(List<NotificationInfo> notifications, UserSetting setting) {
    //AbstractNotificationPlugin plugin =  this.pluginService.getPlugin(key);
    //List<ProviderData> all = providerService.getAllProviders();
    
    List<String> activeProviders = providerService.getActivePluginIds();
    
    
    for(NotificationInfo notification : notifications) {
      //plugin.buildDigest(NotificationContextImpl.DEFAULT.setNotificationMessage(notification), out);
    }
  }
  
}

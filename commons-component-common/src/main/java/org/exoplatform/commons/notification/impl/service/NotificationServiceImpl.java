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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.NotificationKey;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.model.UserSetting.FREQUENCY;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.api.notification.service.QueueMessage;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.notification.service.storage.NotificationDataStorage;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.api.notification.service.template.DigestorService;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.mail.MailService;

public class NotificationServiceImpl extends AbstractService implements NotificationService {

  private final NotificationDataStorage storage;

  public NotificationServiceImpl(NotificationDataStorage storage) {
    this.storage = storage;
  }

  @Override
  public void process(NotificationInfo notification) throws Exception {
    String pluginId = notification.getKey().getId();
    // if the provider is not active, do nothing
    if (CommonsUtils.getService(PluginSettingService.class).isActive(pluginId) == false) {
      return;
    }
    //
    UserSettingService notificationService = CommonsUtils.getService(UserSettingService.class);
    List<String> userIds = notification.getSendToUserIds();
    if (notification.isSendAll()) {
      userIds = notificationService.getUserSettingByPlugin(pluginId);
    }

    List<String> userIdPendings = new ArrayList<String>();
    for (String userId : userIds) {
      UserSetting userSetting = notificationService.get(userId);
      //
      if (userSetting.isActive() == false) {
        continue;
      }
      // send instantly mail
      if (userSetting.isInInstantly(pluginId)) {
        sendInstantly(notification.clone().setTo(userId));
      }
      //
      if (userSetting.isActiveWithoutInstantly(pluginId)) {
        userIdPendings.add(userId);
        setValueSendbyFrequency(notification, userSetting, userId);
      }
    }

    if (userIdPendings.size() > 0) {
      notification.to(userIdPendings);
      storage.save(notification);
    }
  }
  
  /**
   * Process to send instantly mail
   * 
   * @param notification
   */
  private void sendInstantly(NotificationInfo notification) {

    NotificationContext nCtx = NotificationContextImpl.cloneInstance();
    AbstractNotificationPlugin plugin = nCtx.getPluginContainer().getPlugin(notification.getKey());
    if (plugin != null) {
      nCtx.setNotificationInfo(notification);
      MessageInfo info = plugin.buildMessage(nCtx);
      if (info != null) {
        CommonsUtils.getService(QueueMessage.class).put(info);
      }
    }
  }

  @Override
  public void process(Collection<NotificationInfo> messages) throws Exception {
    for (NotificationInfo message : messages) {
      process(message);
    }
  }
  
  private void setValueSendbyFrequency(NotificationInfo message,
                                             UserSetting userNotificationSetting,
                                             String userId) {
    if (message.isSendAll()) {
      return;
    }
    //
    String pluginId = message.getKey().getId();
    if (userNotificationSetting.isInDaily(pluginId)) {
      message.setSendToDaily(userId);
    }
    //
    if (userNotificationSetting.isInWeekly(pluginId)) {
      message.setSendToWeekly(userId);
    }
    
  }
  
  @Override
  public void processDigest() throws Exception {
    
    /**
     * TODO
     * 1. just implements for daily
     * 2. apply Strategy pattern and Factory Pattern
     * 3. Rename method as processDigest
     */
    UserSettingService userService = CommonsUtils.getService(UserSettingService.class);
    DigestorService digest = CommonsUtils.getService(DigestorService.class);
    MailService mailService = CommonsUtils.getService(MailService.class);
    int offset = 0;
    int limit = 20;
    
    List<UserSetting> userSettings = userService.getDaily(offset, limit);
    send(digest, mailService, userSettings, false);

    //
    List<UserSetting> usersDefaultSettings = userService.getDefaultDaily();
    send(digest, mailService, usersDefaultSettings, true);
    
    //Clear all stored message
    storage.removeMessageCallBack();
  }
  
  private void send(DigestorService digest, MailService mail, List<UserSetting> userSettings, boolean isDefault) {
    
    for (UserSetting userSetting : userSettings) {
      if (isDefault) {
        userSetting = getDefaultUserNotificationSetting(userSetting);
      }
      Map<NotificationKey, List<NotificationInfo>> notificationMessageMap = storage.getByUser(userSetting);

      if (notificationMessageMap.size() > 0) {
        MessageInfo messageInfo = digest.buildMessage(notificationMessageMap, userSetting);
        if (messageInfo != null) {
          //
          CommonsUtils.getService(QueueMessage.class).put(messageInfo);
        }
      }
    }
  }
  
  private UserSetting getDefaultUserNotificationSetting(UserSetting setting) {
    UserSetting notificationSetting = UserSetting.getInstance();
    PluginSettingService settingService = CommonsUtils.getService(PluginSettingService.class);
    List<String> activesProvider = settingService.getActivePluginIds();
    for (String string : activesProvider) {
      if(setting.isInWeekly(string)) {
        notificationSetting.addProvider(string, FREQUENCY.WEEKLY);
      } else if(setting.isInDaily(string)) {
        notificationSetting.addProvider(string, FREQUENCY.DAILY);
      }
    }

    return notificationSetting.setUserId(setting.getUserId()).setLastUpdateTime(setting.getLastUpdateTime());
  }
}

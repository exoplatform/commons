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
import org.exoplatform.commons.notification.NotificationConfiguration;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.mail.MailService;

public class NotificationServiceImpl extends AbstractService implements NotificationService {
  
  private final NotificationDataStorage storage;
  private static final String NEW_USER_PLUGIN_ID = "NewUserPlugin";

  public NotificationServiceImpl( NotificationDataStorage storage) {
    this.storage = storage;
  }

  @Override
  public void process(NotificationInfo notification) throws Exception {
    UserSettingService notificationService = CommonsUtils.getService(UserSettingService.class);
    List<String> userIds = notification.getSendToUserIds();
    List<String> userIdPendings = new ArrayList<String>();

    String providerId = notification.getKey().getId();
    //if the provider is not active, do nothing
    PluginSettingService settingService = CommonsUtils.getService(PluginSettingService.class);
    if (settingService.isActive(providerId) == false) return;
    //In case of NewUserPlugin, we process only the send instantly, and the daily or weekly when the job is called
    //TODO need to find better solution to handle this case
    if (NEW_USER_PLUGIN_ID.equals(providerId)) {
      //add mixin for new user
      UserSettingService userSettingService = CommonsUtils.getService(UserSettingService.class);
      userSettingService.addMixin(notification.getValueOwnerParameter("remoteId"));
      //Get all user has config instantly for new user plugin
      userIds = notificationService.getUserSettingByPlugin(providerId);
      storage.save(notification);
    }
    
    for (String userId : userIds) {
      UserSetting userSetting = notificationService.get(userId);
      
      if (! userSetting.isActive()) {
        continue;
      }
      //send instantly mail
      if (userSetting.isInInstantly(providerId)) {
        sendInstantly(notification.clone().setTo(userId));
      }
      //
      if(NEW_USER_PLUGIN_ID.equals(providerId) == false && userSetting.isActiveWithoutInstantly(providerId)){
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
    String providerId = message.getKey().getId();
    if (userNotificationSetting.isInDaily(providerId)) {
      message.setSendToDaily(userId);
    }
    
    if (userNotificationSetting.isInWeekly(providerId)) {
      message.setSendToWeekly(userId);
    }
    
  }
  
  @Override
  public Map<String, NotificationInfo> getNotificationMessagesByProviderId(String providerId, boolean isWeekend) {
    return storage.getNotificationMessagesByProviderId(providerId, isWeekend);
  }


  @Override
  public Map<NotificationKey, List<NotificationInfo>> getByUser(UserSetting userSetting) {
    return storage.getByUser(userSetting);
  }

  @Override
  public void processDaily() throws Exception {
    
    /**
     * TODO
     * 1. just implements for daily
     * 2. apply Strategy pattern and Factory Pattern
     * 3. Rename method as processDigest
     */
    UserSettingService userService = CommonsUtils.getService(UserSettingService.class);
    NotificationService notificationService = CommonsUtils.getService(NotificationService.class);
    DigestorService digest = CommonsUtils.getService(DigestorService.class);
    MailService mailService = CommonsUtils.getService(MailService.class);
    int offset = 0;
    int limit = 20;
    
    List<UserSetting> userSettings = userService.getDaily(offset, limit);
    send(digest, notificationService, mailService, userSettings, false);

    //
    List<UserSetting> usersDefaultSettings = userService.getDefaultDaily();
    send(digest, notificationService, mailService, usersDefaultSettings, true);
    
    //if today is friday, clear all stored message for the new user plugin
    if (isWeekend()) {
      notificationService.removeNotificationMessages(NEW_USER_PLUGIN_ID);
    }
  }
  
  @Override
  public void removeNotificationMessages(String pluginId) {
    storage.removeNotificationMessages(pluginId);
  }
  
  private void send(DigestorService digest, NotificationService notification, MailService mail, List<UserSetting> userSettings, boolean isDefault) {
    
    //get list of new user notification message follow by daily or weekly
    Map<String, NotificationInfo> newUserMessages = getNotificationMessagesByProviderId(NEW_USER_PLUGIN_ID, isWeekend());

    for (UserSetting userSetting : userSettings) {
      if (isDefault) {
        userSetting = getDefaultUserNotificationSetting(userSetting);
      }
      Map<NotificationKey, List<NotificationInfo>> notificationMessageMap = notification.getByUser(userSetting);
      
      NotificationInfo message = newUserMessages.get(userSetting.getUserId());
      //notify new user event only when the user has this config daily or weekly with today = friday
      if (newUserMessages.size() > 0 && (userSetting.isInDaily(NEW_USER_PLUGIN_ID) || (userSetting.isInWeekly(NEW_USER_PLUGIN_ID) && isWeekend()))) {
        List<NotificationInfo> messages = new ArrayList<NotificationInfo>(newUserMessages.values());

        //remove the current user of userSetting from list messages
        messages.remove(message);
        if (messages.size() > 0) {
          //set the information of the receiver for the first message
          NotificationInfo first = messages.get(0);
          first.setTo(userSetting.getUserId());
          messages.set(0, first);
          //put to map
          notificationMessageMap.put(NotificationKey.key(NEW_USER_PLUGIN_ID), messages);
        }
      }
      
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
  
  private boolean isWeekend() {
    NotificationConfiguration configuration= CommonsUtils.getService(NotificationConfiguration.class);
    return NotificationUtils.isWeekEnd(configuration.getDayOfWeekend());
  }
}

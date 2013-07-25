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

import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationMessage;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.model.UserSetting.FREQUENCY;
import org.exoplatform.commons.api.notification.plugin.DigestorService;
import org.exoplatform.commons.api.notification.plugin.NotificationKey;
import org.exoplatform.commons.api.notification.service.AbstractNotificationServiceListener;
import org.exoplatform.commons.api.notification.service.setting.ProviderSettingService;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.notification.service.storage.NotificationDataStorage;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.notification.NotificationConfiguration;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.impl.setting.NotificationPluginContainer;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;

public class NotificationServiceImpl extends AbstractService implements NotificationService {

  private static final Log LOG = ExoLogger.getLogger(NotificationServiceImpl.class);

  private List<AbstractNotificationServiceListener> messageListeners = new ArrayList<AbstractNotificationServiceListener>(2);

  private final NotificationDataStorage storage;
  private final NotificationPluginContainer pluginService;
  private final NotificationConfiguration configuration;

  public NotificationServiceImpl(NotificationPluginContainer pluginService, NotificationDataStorage storage, NotificationConfiguration configuration) {
    this.storage = storage;
    this.pluginService = pluginService;
    this.configuration = configuration;
  }

  @Override
  public void addSendNotificationListener(AbstractNotificationServiceListener messageListener) {
    messageListeners.add(messageListener);
  }

  private void processSendNotificationListener(NotificationMessage message) {
    for (AbstractNotificationServiceListener messageListener : messageListeners) {
      messageListener.processListener(message);
    }
  }

  @Override
  public void process(NotificationMessage notification) throws Exception {
    UserSettingService notificationService = CommonsUtils.getService(UserSettingService.class);
    List<String> userIds = notification.getSendToUserIds();
    List<String> userIdPendings = new ArrayList<String>();

    String providerId = notification.getKey().getId();
    for (String userId : userIds) {
      UserSetting userSetting = notificationService.get(userId);
      
      //
      if (userSetting.isInInstantly(providerId)) {
        processSendNotificationListener(notification.setTo(userId));
      }
      //
      if(userSetting.isActiveWithoutInstantly(providerId)){
        userIdPendings.add(userId);
        setValueSendbyFrequency(notification, userSetting, userId);
      }
    }

    if (userIdPendings.size() > 0) {
      notification.to(userIdPendings);
      storage.save(notification);
    }
  }

  @Override
  public void process(Collection<NotificationMessage> messages) throws Exception {
    for (NotificationMessage message : messages) {
      process(message);
    }
  }
  
  private void setValueSendbyFrequency(NotificationMessage message,
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
  public Map<NotificationKey, List<NotificationMessage>> getByUser(UserSetting userSetting) {
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
    
    for (UserSetting userSetting : userSettings) {
      Map<NotificationKey, List<NotificationMessage>> notificationMessageMap = notificationService.getByUser(userSetting);
      if (notificationMessageMap.size() > 0) {
        MessageInfo messageInfo = digest.buildMessage(notificationMessageMap, userSetting);
        if(messageInfo != null) {
          Message message_ = messageInfo.makeEmailNotification();
          mailService.sendMessage(message_);
        }
        
      }
    }
    
    List<UserSetting> usersDefaultSettings = userService.getDefaultDaily();
    for (UserSetting setting : usersDefaultSettings) {
      UserSetting userNotificationSetting = getDefaultUserNotificationSetting(setting);
      //
      Map<NotificationKey, List<NotificationMessage>> notificationMessageMap = notificationService.getByUser(userNotificationSetting);
      if (notificationMessageMap.size() > 0) {
        MessageInfo messageInfo = digest.buildMessage(notificationMessageMap, userNotificationSetting);
        if(messageInfo != null) {
          Message message_ = messageInfo.makeEmailNotification();
          mailService.sendMessage(message_);
        }
        
      }
    }
  }
  
  private UserSetting getDefaultUserNotificationSetting(UserSetting setting) {
    UserSetting notificationSetting = UserSetting.getInstance();
    ProviderSettingService settingService = CommonsUtils.getService(ProviderSettingService.class);
    List<String> activesProvider = settingService.getActiveProviderIds();
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

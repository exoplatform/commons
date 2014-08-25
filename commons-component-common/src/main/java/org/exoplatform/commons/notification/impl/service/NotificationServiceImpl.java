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
import java.util.Calendar;
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
import org.exoplatform.commons.notification.NotificationContextFactory;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;

public class NotificationServiceImpl extends AbstractService implements NotificationService {
  private static final Log         LOG              = ExoLogger.getLogger(NotificationServiceImpl.class);
  /** */
  private final NotificationDataStorage storage;
  /** */
  private final DigestorService digestorService;
  /** */
  private final UserSettingService userService;

  public NotificationServiceImpl(UserSettingService userService, DigestorService digestorService, NotificationDataStorage storage) {
    this.userService = userService;
    this.digestorService = digestorService;
    this.storage = storage;
  }
  
  @Override
  public void process(NotificationInfo notification) throws Exception {

    String pluginId = notification.getKey().getId();
    
    //create notification here
    if (NotificationContextFactory.getInstance().getStatisticsService().isStatisticsEnabled()) {
      NotificationContextFactory.getInstance().getStatisticsCollector().createNotificationInfoCount(pluginId);
    }
    // if the provider is not active, do nothing
    if (CommonsUtils.getService(PluginSettingService.class).isActive(pluginId) == false) {
      return;
    }
    //
    UserSettingService notificationService = CommonsUtils.getService(UserSettingService.class);
    List<String> userIds = notification.getSendToUserIds();
    //
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

    if (userIdPendings.size() > 0 || notification.isSendAll()) {
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
    final boolean stats = NotificationContextFactory.getInstance().getStatistics().isStatisticsEnabled();
    
    NotificationContext nCtx = NotificationContextImpl.cloneInstance();
    AbstractNotificationPlugin plugin = nCtx.getPluginContainer().getPlugin(notification.getKey());
    if (plugin != null) {
      nCtx.setNotificationInfo(notification);
      MessageInfo info = plugin.buildMessage(nCtx);
      
      if (info != null) {
        if (NotificationUtils.isValidEmailAddresses(info.getTo()) == true) {
          CommonsUtils.getService(QueueMessageImpl.class).sendMessage(info.makeEmailNotification());
        } else {
          LOG.warn(String.format("The email %s is not valid for sending notification", info.getTo()));
        }
        if (stats) {
          NotificationContextFactory.getInstance().getStatisticsCollector().createMessageInfoCount(info.getPluginId());
        }
      }
    }
  }

  @Override
  public void process(Collection<NotificationInfo> messages) throws Exception {
    for (NotificationInfo message : messages) {
      process(message);
    }
  }
  
  private void setValueSendbyFrequency(NotificationInfo message, UserSetting userNotificationSetting, String userId) {
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
  public void digest(NotificationContext notifContext) throws Exception {
    /**
     * 1. just implements for daily
     * 2. apply Strategy pattern and Factory Pattern
     */
    UserSetting defaultConfigPlugins = getDefaultUserSetting(notifContext.getPluginSettingService().getActivePluginIds());
    //process for users used setting
    /**
     * Tested with 5000 users:
     * 
     * + limit = 20 time lost: 58881ms user settings 128992ms default user settings.
     * + limit = 50 time lost: 44873ms user settings 70630ms default user settings.
     * + limit = 100 time lost: 26997ms user settings 60051ms default user settings.
    */
    List<UserSetting> sentUsers = new ArrayList<UserSetting>();
    long startTime = System.currentTimeMillis();
    int limit = 100;
    int offset = 0;
    while (true) {
      List<UserSetting> userDigestSettings = this.userService.getDigestSettingForAllUser(notifContext, offset, limit);
      if(userDigestSettings.size() == 0) {
        break;
      }
      send(notifContext, userDigestSettings);
      offset += limit;
      sentUsers.addAll(userDigestSettings);
    }
    LOG.debug("Time to run process users have settings: " + (System.currentTimeMillis() - startTime) + "ms.");
    long startTimeDefault = System.currentTimeMillis();
    //process for users used default setting
    offset = 0;
    while (true) {
      List<UserSetting> defaultMixinUsers = this.userService.getDigestDefaultSettingForAllUser(offset, limit);
      if (defaultMixinUsers.size() == 0) {
        break;
      }
      sendDefault(notifContext, defaultMixinUsers, defaultConfigPlugins);
      offset += limit;
      sentUsers.addAll(defaultMixinUsers);
    }
    //provided the sentUser for excluding to process sending mail
    //get list of user who has the isActivate = FALSE
    sentUsers.addAll(this.userService.getUserSettingWithDeactivate());
    //
    sendUserWithNoSetting(notifContext, defaultConfigPlugins, sentUsers);
    
    //Clear all stored message
    storage.removeMessageAfterSent();
    LOG.debug("Time to run process users used default settings: " + (System.currentTimeMillis() - startTimeDefault) + "ms.");
  }

  /**
   * Process these users who isn't existing any setting and default mixin type in the Setting.
   * Must use the Organization service to get these users and excluded sentUsers
   * 
   * @param context
   * @param defaultSetting
   * @param sentUsers
   * @throws Exception
   */
  private void sendUserWithNoSetting(NotificationContext context,
                                         UserSetting defaultSetting,
                                         List<UserSetting> sentUsers) throws Exception {
    
    OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
    ListAccess<User> allUsers = organizationService.getUserHandler().findAllUsers();
    int size = allUsers.getSize(), limit = 200;
    int index = 0, length = Math.min(limit, size);
    //only lazy adding mixin-type(defaultSetting) when the user's size > sent notification's size.
    if (size > sentUsers.size()) {
      List<User> addMixinUsers = new ArrayList<User>();
      List<UserSetting> usersDefaultSettings = new ArrayList<UserSetting>();

      while (index < size && length > 0) {
        usersDefaultSettings = new ArrayList<UserSetting>();
        //
        LOG.debug(String.format("Load from %s to %s, length %s", index, (index + length), length));
        User[] users = allUsers.load(index, length);
        if (users.length == 0) {
          break;
        }
        UserSetting userSetting;
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < users.length; i++) {
          userSetting = UserSetting.getInstance().setUserId(users[i].getUserName());
          if (!sentUsers.contains(userSetting)) {
            //
            cal.setTime(users[i].getCreatedDate());
            usersDefaultSettings.add(userSetting.setLastUpdateTime(cal));
            //
            addMixinUsers.add(users[i]);
          }
        }
        //
        sendDefault(context, usersDefaultSettings, defaultSetting);

        index += length;
        length = Math.min(limit, size - index);
      }

      LOG.debug("Done sent notification for " + addMixinUsers.size() + " users must addMixin.");
      //
      long startTime = System.currentTimeMillis();
      userService.addMixin(addMixinUsers.toArray(new User[addMixinUsers.size()]));
      LOG.debug("Done addMixin for " + addMixinUsers.size() + " users, time: " + (System.currentTimeMillis() - startTime) + "ms.");
    }
  }
  
  private void send(NotificationContext context, List<UserSetting> userSettings) {
    final boolean stats = NotificationContextFactory.getInstance().getStatistics().isStatisticsEnabled();
    
    for (UserSetting userSetting : userSettings) {
      if (NotificationUtils.isDeletedMember(userSetting.getUserId())) {
        continue;
      }
      
      Map<NotificationKey, List<NotificationInfo>> notificationMessageMap = storage.getByUser(context, userSetting);

      if (notificationMessageMap.size() > 0) {
        MessageInfo messageInfo = this.digestorService.buildMessage(context, notificationMessageMap, userSetting);
        if (messageInfo != null) {
          //
          CommonsUtils.getService(QueueMessage.class).put(messageInfo);
          
          if (stats) {
            NotificationContextFactory.getInstance().getStatisticsCollector().createMessageInfoCount(messageInfo.getPluginId());
            NotificationContextFactory.getInstance().getStatisticsCollector().putQueue(messageInfo.getPluginId());
          }
        }
      }
    }
  }
  
  private void sendDefault(NotificationContext context, List<UserSetting> userSettings, UserSetting defaultConfigPlugins) {
    final boolean stats = NotificationContextFactory.getInstance().getStatistics().isStatisticsEnabled();
    
    for (UserSetting userSetting : userSettings) {
      if (NotificationUtils.isDeletedMember(userSetting.getUserId())) {
        continue;
      }

      userSetting = defaultConfigPlugins.clone().setUserId(userSetting.getUserId()).setLastUpdateTime(userSetting.getLastUpdateTime());
      Map<NotificationKey, List<NotificationInfo>> notificationMessageMap = storage.getByUser(context, userSetting);

      if (notificationMessageMap.size() > 0) {
        MessageInfo messageInfo = this.digestorService.buildMessage(context, notificationMessageMap, userSetting);
        if (messageInfo != null) {
          //
          CommonsUtils.getService(QueueMessage.class).put(messageInfo);
          
          if (stats) {
            NotificationContextFactory.getInstance().getStatisticsCollector().createMessageInfoCount(messageInfo.getPluginId());
            NotificationContextFactory.getInstance().getStatisticsCollector().putQueue(messageInfo.getPluginId());
          }
        }
      }
    }
  }
  
  /**
   * The method uses to get the notification plugin's default setting.
   * If it had been changed by the administrator then the setting must be followed by admin's setting.
   * 
   * For example: 
   * 
   * 
   * @param activatedPluginsByAdminSetting The setting what set by administrator
   * @return
   */
  private UserSetting getDefaultUserSetting(List<String> activatedPluginsByAdminSetting) {
    UserSetting setting = UserSetting.getInstance();
    //default setting loaded from configuration xml file
    UserSetting defaultSetting = UserSetting.getDefaultInstance();
    for (String string : activatedPluginsByAdminSetting) {
      if (defaultSetting.isInWeekly(string)) {
        setting.addProvider(string, FREQUENCY.WEEKLY);
      } else if (defaultSetting.isInDaily(string)) {
        setting.addProvider(string, FREQUENCY.DAILY);
      }
    }

    return setting;
  }
}

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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.lifecycle.AbstractNotificationLifecycle;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.model.UserSetting.FREQUENCY;
import org.exoplatform.commons.api.notification.service.QueueMessage;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.notification.service.storage.MailNotificationStorage;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.api.notification.service.template.DigestorService;
import org.exoplatform.commons.notification.NotificationContextFactory;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.channel.MailChannel;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class NotificationServiceImpl extends AbstractService implements NotificationService {
  private static final Log                 LOG = ExoLogger.getLogger(NotificationServiceImpl.class);

  /** */
  private final MailNotificationStorage    storage;

  /** */
  private final DigestorService            digestorService;

  /** */
  private final UserSettingService         userService;

  /** */
  private final NotificationContextFactory notificationContextFactory;

  /** */
  private final ChannelManager             channelManager;

  public NotificationServiceImpl(ChannelManager channelManager,
                                 UserSettingService userService,
                                 DigestorService digestorService,
                                 MailNotificationStorage storage,
                                 NotificationContextFactory notificationContextFactory) {
    this.userService = userService;
    this.digestorService = digestorService;
    this.storage = storage;
    this.notificationContextFactory = notificationContextFactory;
    this.channelManager = channelManager;
  }

  @Override
  public void process(NotificationInfo notification) throws Exception {
    if (notification == null) {
      throw new IllegalArgumentException("Notification argument shouldn't be null");
    }
    String pluginId = notification.getKey().getId();

    // statistic metrics
    if (this.notificationContextFactory.getStatisticsService().isStatisticsEnabled()) {
      this.notificationContextFactory.getStatisticsCollector().createNotificationInfoCount(pluginId);
    }
    //
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(notification);
    //
    List<String> userIds = null;

    List<AbstractChannel> channels = channelManager.getChannels();
    for (AbstractChannel channel : channels) {
      if (!CommonsUtils.getService(PluginSettingService.class).isActive(channel.getId(), pluginId)) {
        continue;
      }

      if (notification.getSendToUserIds() == null || notification.getSendToUserIds().isEmpty()) {
        LOG.debug("Notification with id '{}' and parameters = '{}' not sent because receivers are empty",
                  notification.getId(),
                  notification.getOwnerParameter());
        userIds = Collections.emptyList();
      } else {
        userIds = notification.getSendToUserIds();
      }
      AbstractNotificationLifecycle lifecycle = channelManager.getLifecycle(ChannelKey.key(channel.getId()));
      lifecycle.process(ctx, userIds.toArray(new String[userIds.size()]));
    }
  }

  @Override
  public void process(Collection<NotificationInfo> messages) throws Exception {
    for (NotificationInfo message : messages) {
      if (message == null) {
        continue;
      }
      process(message);
    }
  }

  @Override
  public void digest(NotificationContext notifContext) throws Exception {
    UserSetting defaultConfigPlugins = getDefaultUserSetting(notifContext.getPluginSettingService()
                                                                         .getActivePluginIds(UserSetting.EMAIL_CHANNEL));
    // process for users used setting
    long startTime = System.currentTimeMillis();
    int limit = 100;
    int offset = 0;
    while (true) {
      List<UserSetting> userDigestSettings = this.userService.getDigestSettingForAllUser(notifContext, offset, limit);
      if (userDigestSettings.size() == 0) {
        break;
      }
      send(notifContext, userDigestSettings);
      offset += limit;
    }
    LOG.info("Time spent to send mail messages for users having personal settings: " + (System.currentTimeMillis() - startTime)
        + "ms.");
    startTime = System.currentTimeMillis();
    // process for users used default setting
    if (defaultConfigPlugins.getDailyPlugins().size() > 0 || defaultConfigPlugins.getWeeklyPlugins().size() > 0) {
      offset = 0;
      while (true) {
        List<UserSetting> usersWithDefaultSettings = this.userService.getDigestDefaultSettingForAllUser(offset, limit);
        if (usersWithDefaultSettings.size() == 0) {
          break;
        }
        sendDefault(notifContext, usersWithDefaultSettings, defaultConfigPlugins);
        offset += limit;
      }
    }

    // Clear all stored message
    storage.removeMessageAfterSent(notifContext);
    LOG.info("Time spent to send mail messages for users having default settings: " + (System.currentTimeMillis() - startTime)
        + "ms.");
  }

  private void send(NotificationContext context, List<UserSetting> userSettings) {
    final boolean stats = NotificationContextFactory.getInstance().getStatistics().isStatisticsEnabled();

    for (UserSetting userSetting : userSettings) {
      if (!userSetting.isChannelActive(MailChannel.ID) || !userSetting.isEnabled()
          || NotificationUtils.isDeletedMember(userSetting.getUserId())) {
        continue;
      }

      putMessageToQueue(context, userSetting, stats);
    }
  }

  private void sendDefault(NotificationContext context, List<UserSetting> userSettings, UserSetting defaultConfigPlugins) {
    final boolean stats = NotificationContextFactory.getInstance().getStatistics().isStatisticsEnabled();

    for (UserSetting userSetting : userSettings) {
      if (!userSetting.isEnabled() || NotificationUtils.isDeletedMember(userSetting.getUserId())) {
        continue;
      }

      userSetting = defaultConfigPlugins.clone()
                                        .setUserId(userSetting.getUserId())
                                        .setLastUpdateTime(userSetting.getLastUpdateTime());
      putMessageToQueue(context, userSetting, stats);
    }
  }

  private void putMessageToQueue(NotificationContext context, UserSetting userSetting, final boolean stats) {
    Map<PluginKey, List<NotificationInfo>> notificationMessageMap = storage.getByUser(context, userSetting);
    if (notificationMessageMap.size() > 0) {
      MessageInfo messageInfo = this.digestorService.buildMessage(context, notificationMessageMap, userSetting);
      if (messageInfo != null) {
        //
        try {
          CommonsUtils.getService(QueueMessage.class).put(messageInfo);

          if (stats) {
            NotificationContextFactory.getInstance().getStatisticsCollector().createMessageInfoCount(messageInfo.getPluginId());
            NotificationContextFactory.getInstance().getStatisticsCollector().putQueue(messageInfo.getPluginId());
          }
        } catch (Exception e) {
          LOG.error("An error occurred while putting message " + messageInfo.getId() + " to user " + messageInfo.getTo()
              + " in queue", e);
        }
      }
    }
  }

  /**
   * The method uses to get the notification plugin's default setting. If it had
   * been changed by the administrator then the setting must be followed by
   * admin's setting. For example:
   * 
   * @param activatedPluginsByAdminSetting The setting what set by administrator
   * @return
   */
  private UserSetting getDefaultUserSetting(List<String> activatedPluginsByAdminSetting) {
    UserSetting setting = UserSetting.getInstance();
    // default setting loaded from configuration xml file
    UserSetting defaultSetting = UserSetting.getDefaultInstance();
    for (String string : activatedPluginsByAdminSetting) {
      if (defaultSetting.isInWeekly(string)) {
        setting.addPlugin(string, FREQUENCY.WEEKLY);
      } else if (defaultSetting.isInDaily(string)) {
        setting.addPlugin(string, FREQUENCY.DAILY);
      }
    }

    return setting;
  }
}

/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.commons.notification.lifecycle;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.lifecycle.AbstractNotificationLifecycle;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.QueueMessage;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.notification.service.storage.MailNotificationStorage;
import org.exoplatform.commons.notification.NotificationContextFactory;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.channel.MailChannel;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 12, 2014  
 */
public class MailLifecycle extends AbstractNotificationLifecycle {
  private static final Log LOG = ExoLogger.getLogger(MailLifecycle.class);

  @Override
  public void process(NotificationContext ctx, String... userIds) {
    NotificationInfo notification = ctx.getNotificationInfo();
    String pluginId = notification.getKey().getId();
    UserSettingService userService = CommonsUtils.getService(UserSettingService.class);
    
    List<String> userIdPendings = new ArrayList<String>();
    for (String userId : userIds) {
      UserSetting userSetting = userService.get(userId);
      //check channel active for user & user enabled
      if (!userSetting.isEnabled() || !userSetting.isChannelActive(MailChannel.ID)) {
        continue;
      }
      // check plugin active for user
      if (userSetting.isActive(MailChannel.ID, pluginId)) {
        send(ctx.setNotificationInfo(notification.clone().setTo(userId)));
      }
      //handles the daily or weekly
      if (userSetting.isInDaily(pluginId) || userSetting.isInWeekly(pluginId)) {
        userIdPendings.add(userId);
        setValueSendbyFrequency(notification, userSetting, userId);
      }
    }

    if (userIdPendings.size() > 0 || notification.isSendAll()) {
      store(notification);
    }
  }
  
  /**
   * Sets the message to determine which user will be sent daily or weekly
   * 
   * @param msg
   * @param userSetting
   * @param userId
   */
  private void setValueSendbyFrequency(NotificationInfo msg, UserSetting userSetting, String userId) {
    if (msg.isSendAll()) {
      return;
    }
    //
    String pluginId = msg.getKey().getId();
    if (userSetting.isInDaily(pluginId)) {
      msg.setSendToDaily(userId);
    }
    //
    if (userSetting.isInWeekly(pluginId)) {
      msg.setSendToWeekly(userId);
    }
  }

  @Override
  public void process(NotificationContext ctx, String userId) {
    LOG.info("Mail Notification process user: " + userId);
  }
  
  @Override
  public void store(NotificationInfo notifInfo) {
    MailNotificationStorage storage = CommonsUtils.getService(MailNotificationStorage.class);
    try {
      storage.save(notifInfo);
    } catch (Exception e) {
      LOG.error("Error storing notification", e);
    }
  }
  
  @Override
  public void send(NotificationContext ctx) {
    final boolean stats = NotificationContextFactory.getInstance().getStatistics().isStatisticsEnabled();
    NotificationInfo notification = ctx.getNotificationInfo();
    
    AbstractTemplateBuilder builder = getChannel().getTemplateBuilder(notification.getKey());
    if (builder != null) {
      MessageInfo msg = builder.buildMessage(ctx);
      if (msg != null) {
        if (NotificationUtils.isValidEmailAddresses(msg.getTo()) == true) {
          try {
            CommonsUtils.getService(QueueMessage.class).sendMessage(msg);
          } catch (Exception e) {
            //error in sending message
            LOG.error("error in sending message with id = " + msg.getId(), e);
          }
        } else {
          LOG.warn(String.format("The email %s is not valid for sending notification", msg.getTo()));
        }
        if (stats) {
          NotificationContextFactory.getInstance().getStatisticsCollector().createMessageInfoCount(msg.getPluginId());
        }
      }
    }
  }

}

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

import java.util.Calendar;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.lifecycle.AbstractNotificationLifecycle;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.notification.channel.WebChannel;
import org.exoplatform.commons.notification.net.WebNotificationSender;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 12, 2014  
 */
public class WebLifecycle extends AbstractNotificationLifecycle {
  private static final Log LOG = ExoLogger.getLogger(WebLifecycle.class);

  @Override
  public void process(NotificationContext ctx, String... userIds) {
    NotificationInfo notification = ctx.getNotificationInfo();
    String pluginId = notification.getKey().getId();
    UserSettingService userService = CommonsUtils.getService(UserSettingService.class);
    
    for (String userId : userIds) {
      UserSetting userSetting = userService.get(userId);
      //check channel active for user
      if (!userSetting.isChannelActive(WebChannel.ID)) {
        continue;
      }
      
      if (userSetting.isActive(WebChannel.ID, pluginId)) {
        ctx.setWritingProcess(true);
        send(ctx.setNotificationInfo(notification.clone(true).setTo(userId)));
        if (notification.getId().equals(ctx.getNotificationInfo().getId())) {
          store(ctx.getNotificationInfo());
        } else {
          update(ctx.getNotificationInfo());
        }
      }
    }

  }

  @Override
  public void process(NotificationContext ctx, String userId) {
    LOG.info("Web Notification process user: " + userId);
  }
  
  @Override
  public void store(NotificationInfo notifInfo) {
    LOG.info("WEB:: Store the notification to db by Web channel.");
    notifInfo.with(NotificationMessageUtils.SHOW_POPOVER_PROPERTY.getKey(), "true")
             .with(NotificationMessageUtils.READ_PORPERTY.getKey(), "false");
    CommonsUtils.getService(WebNotificationStorage.class).save(notifInfo);
  }
  
  @Override
  public void update(NotificationInfo notifInfo) {
    LOG.info("WEB:: Update an existing notification to db by Web channel.");
    notifInfo.with(NotificationMessageUtils.SHOW_POPOVER_PROPERTY.getKey(), "true")
             .with(NotificationMessageUtils.READ_PORPERTY.getKey(), "false");
    CommonsUtils.getService(WebNotificationStorage.class).update(notifInfo);
  }
  
  @Override
  public void send(NotificationContext ctx) {
    LOG.info("WEB:: Send the message by Web channel.");
    getChannel().dispatch(ctx.getNotificationInfo().setLastModifiedDate(Calendar.getInstance()));
    try {
      MessageInfo msg = buildMessageInfo(ctx);
      if(msg != null) {
        NotificationInfo notification = ctx.getNotificationInfo();
        WebNotificationSender.sendJsonMessage(notification.getTo(), msg);
        notification.setTitle(msg.getBody());
      }
    } catch (Exception e) {
      LOG.error("Failed to connect with server :", e);
    }
  }

  /**
   * Builds the message inform from the notification context.
   * 
   * @param ctx
   * @return
   */
  private MessageInfo buildMessageInfo(NotificationContext ctx) {
    AbstractTemplateBuilder builder = getChannel().getTemplateBuilder(ctx.getNotificationInfo().getKey());
    return builder.buildMessage(ctx);
  }

}

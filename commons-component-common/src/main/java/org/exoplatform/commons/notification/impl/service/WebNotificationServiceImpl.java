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
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebFilter;
import org.exoplatform.commons.api.notification.service.WebNotificationService;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.notification.channel.WebChannel;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class WebNotificationServiceImpl implements WebNotificationService {
  private static final Log LOG = ExoLogger.getLogger(WebNotificationServiceImpl.class);
  private final WebNotificationStorage webStorage;
  public WebNotificationServiceImpl(WebNotificationStorage webStorage) {
    this.webStorage = webStorage;
  }

  @Override
  public void save(NotificationInfo notification) {
    webStorage.save(notification);
  }

  @Override
  public void markRead(String notificationId) {
    webStorage.markRead(notificationId);
  }

  @Override
  public void markReadAll(String userId) {
    webStorage.markReadAll(userId);
  }

  @Override
  public List<String> getNotificationContents(WebFilter filter) {
    List<String> messages = new ArrayList<String>();
    List<NotificationInfo> notificationInfos = webStorage.get(filter);
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    AbstractChannel channel = ctx.getChannelManager().getChannel(ChannelKey.key(WebChannel.ID));
    //
    for (NotificationInfo notification : notificationInfos) {
      boolean isText = true;
      try {
        AbstractTemplateBuilder builder = channel.getTemplateBuilder(notification.getKey());
        if(builder != null) {
          MessageInfo msg = builder.buildMessage(ctx.setNotificationInfo(notification));
          if(msg != null) {
            messages.add(msg.getBody());
            isText = false;
          }
        }
      } catch (Exception e) {
        LOG.error("Failed to build web notification: " + notification.getId(), e);
      }
      if (isText) {
        messages.add(notification.getTitle());
      }
    }
    return messages;
  }

  @Override
  public void remove(String notificationId) {
    webStorage.remove(notificationId);
  }

  @Override
  public void hidePopover(String notificationId) {
    webStorage.hidePopover(notificationId);
  }
}

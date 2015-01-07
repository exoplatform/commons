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
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.api.notification.service.WebNotificationService;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.notification.channel.WebChannel;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.notification.impl.service.storage.cache.model.WebNotifInfoCacheKey;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class WebNotificationServiceImpl implements WebNotificationService {
  /** logger */
  private static final Log LOG = ExoLogger.getLogger(WebNotificationServiceImpl.class);
  /** storage */
  private final WebNotificationStorage storage;
  
  public WebNotificationServiceImpl(WebNotificationStorage webStorage) {
    this.storage = webStorage;
  }

  @Override
  public void save(NotificationInfo notification) {
    storage.save(notification);
  }

  @Override
  public void markRead(String notificationId) {
    storage.markRead(notificationId);
  }

  @Override
  public void markAllRead(String userId) {
    storage.markAllRead(userId);
  }

  @Override
  public List<String> get(WebNotificationFilter filter, int offset, int limit) {
    List<String> result = new ArrayList<String>();
    List<NotificationInfo> gotList = storage.get(filter, offset, limit);
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    AbstractChannel channel = ctx.getChannelManager().getChannel(ChannelKey.key(WebChannel.ID));
    //
    for (NotificationInfo notification : gotList) {
      AbstractTemplateBuilder builder = channel.getTemplateBuilder(notification.getKey());
      if (builder != null) {
        MessageInfo msg = builder.buildMessage(ctx.setNotificationInfo(notification));
        result.add(msg.getBody());
        //if have any exception when template transformation
        // gets the default title
        if (ctx.isFailed()) {
          LOG.debug(ctx.getException().getMessage(), ctx.getException());
          result.add(notification.getTitle());
        }
      } else {
        result.add(notification.getTitle());
      }
    }
    return result;
  }

  @Override
  public boolean remove(String notificationId) {
    return storage.remove(notificationId);
  }

  @Override
  public void hidePopover(String notificationId) {
    storage.hidePopover(notificationId);
  }

  @Override
  public void resetNumberOnBadge(String userId) {
    storage.resetNumberOnBadge(userId);
  }

  @Override
  public int getNumberOnBadge(String userId) {
    return storage.getNumberOnBadge(userId);
  }
}

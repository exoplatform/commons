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
import org.exoplatform.commons.api.notification.lifecycle.AbstractNotificationLifecycle;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
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
    
  }

  @Override
  public void process(NotificationContext ctx, String userId) {
    LOG.info("Web Notification process user: " + userId);
    send(ctx, ctx.getNotificationInfo());
  }
  
  @Override
  public void store(NotificationInfo notifInfo) {
    LOG.info("store the notification to db for Web channel.");
  }
  
  @Override
  public void send(NotificationContext ctx, NotificationInfo notification) {
    LOG.info("send the message by Web channel.");
  }
  
//private void sendIntranetNotification(NotificationInfo notification) {
//NotificationContext nCtx = NotificationContextImpl.cloneInstance();
//AbstractNotificationPlugin plugin = nCtx.getPluginContainer().getPlugin(notification.getKey());
//if (plugin == null) {
//  return;
//}
//try {
//  notification.setLastModifiedDate(Calendar.getInstance());
//  notification.setId(new NotificationInfo().getId());
//  String message = dataStorage.buildUIMessage(notification);
//  WebSocketBootstrap.sendMessage(WebSocketServer.NOTIFICATION_WEB_IDENTIFIER, notification.getTo(),
//                                 new JsonObject().putString("message", message).encode());
//  //
//  dataStorage.save(notification);
//} catch (Exception e) {
//  LOG.error("Failed to connect with server : " + e, e.getMessage());
//}
//}
}

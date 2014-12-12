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
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

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
    for(String userId : userIds) {
      process(ctx, userId);
    }
  }

  @Override
  public void process(NotificationContext ctx, String userId) {
    LOG.info("Mail Notification process user: " + userId);
  }
  
  @Override
  public void store(NotificationInfo notifInfo) {
    LOG.info("store the notification to db for Mail channel.");
  }
  
  @Override
  public void send(MessageInfo msg) {
    LOG.info("send the message by Mail channel.");
  }

}

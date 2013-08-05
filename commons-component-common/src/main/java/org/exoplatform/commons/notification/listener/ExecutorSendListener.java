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
package org.exoplatform.commons.notification.listener;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationMessage;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;

public class ExecutorSendListener implements Callable<NotificationMessage> {
  private static final Log           LOG      = ExoLogger.getExoLogger(ExecutorSendListener.class);

  private static ExecutorSendListener instance;

  private Queue<NotificationMessage>  messages = new ConcurrentLinkedQueue<NotificationMessage>();
  
  @Override
  public NotificationMessage call() throws Exception {
    NotificationMessage message = ExecutorSendListener.getInstance().getNotificationMessage();
    if (message != null) {
      processSendEmailNotifcation(message);
    }
    return message;
  }

  public NotificationMessage getNotificationMessage() {
    return messages.poll();
  }

  private ExecutorSendListener setNotificationMessage(NotificationMessage message) {
    this.messages.add(message);
    return this;
  }

  private static ExecutorSendListener getInstance() {
    if (instance == null) {
      synchronized (ExecutorSendListener.class) {
        if (instance == null) {
          instance = new ExecutorSendListener();
        }
      }
    }
    return instance;
  }

  public static ExecutorSendListener getInstance(NotificationMessage message) {
    return getInstance().setNotificationMessage(message);
  }

  private void processSendEmailNotifcation(NotificationMessage message) {
    NotificationContext nCtx = NotificationContextImpl.DEFAULT;
    AbstractNotificationPlugin supportProvider = ((NotificationContextImpl) nCtx).getNotificationPluginContainer().getPlugin(message.getKey());
    if (supportProvider != null) {
      nCtx.setNotificationMessage(message);
      MessageInfo messageInfo = supportProvider.buildMessage(nCtx);

      if (messageInfo != null) {
        Message message_ = messageInfo.makeEmailNotification();


        try {
          MailService mailService = (MailService) PortalContainer.getInstance().getComponentInstanceOfType(MailService.class);
          mailService.sendMessage(message_);
          LOG.info("Successfully, to sent email notification to user: " + message_.getTo());
        } catch (Exception e) {
          LOG.error("Send email error!", e);
        }
      }
    }
  }
}

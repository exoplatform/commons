/*
 * Copyright (C) 2003-${year} eXo Platform SAS.
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.notification.impl.service;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.service.QueueMessage;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.MailService;

public class QueueMessageImpl implements QueueMessage {
  
  private static final Log LOG = ExoLogger.getExoLogger(QueueMessageImpl.class);
  
  private final static Queue<MessageInfo> messageQueue = new ConcurrentLinkedQueue<MessageInfo>();
  
  private int MAX_TO_SEND;
  
  private int DELAY_TIME;
  
  private int INITIAL_DELAY;
  
  private MailService mailService;
  
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  
  public QueueMessageImpl(InitParams params) {
    
    MAX_TO_SEND = Integer.parseInt(params.getValueParam("maxToSend").getValue());
    DELAY_TIME = Integer.parseInt(params.getValueParam("delayTime").getValue());
    INITIAL_DELAY = Integer.parseInt(params.getValueParam("initialDelay").getValue());
    
    this.mailService = CommonsUtils.getService(MailService.class);
    
    scheduler.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        send();
      }
    }, INITIAL_DELAY, DELAY_TIME, TimeUnit.SECONDS);
    
  }

  @Override
  public boolean put(MessageInfo message) {
    //
    if (message == null || message.getTo() == null || message.getTo().length() == 0) {
      return false;
    }
    //
    if (NotificationUtils.isValidEmailAddresses(message.getTo()) == false) {
      LOG.warn(String.format("The email %s is not valid for sending notification", message.getTo()));
      return false;
    }
    messageQueue.add(message);

    return true;
  }

  @Override
  public void send() {
    
    for (int i = 0; i < MAX_TO_SEND; i++) {
      try {
        
        if (messageQueue.isEmpty() == false) {
          MessageInfo mailMessage = messageQueue.poll();
          mailService.sendMessage(mailMessage.makeEmailNotification());
          LOG.debug("Sent notification to user "+ mailMessage.getTo());
        } else {
          break;
        }
        
      } catch (Exception e) {
        LOG.error("Failed to send notification.", e);
      }
    }
  }

}

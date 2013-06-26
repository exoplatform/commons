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
package org.exoplatform.commons.notification.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.UserNotificationSetting;
import org.exoplatform.commons.api.notification.service.NotificationService;
import org.exoplatform.commons.api.notification.service.NotificationServiceListener;
import org.exoplatform.commons.api.notification.service.UserNotificationService;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.listener.AbstractNotificationServiceListener;
import org.exoplatform.commons.notification.listener.NotificationServiceListenerImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

public class NotificationServiceImpl implements NotificationService, Startable {

  private static final Log                               LOG = ExoLogger.getLogger(NotificationServiceImpl.class);

  private List<AbstractNotificationServiceListener>        messageListeners = new ArrayList<AbstractNotificationServiceListener>(2);

  private NotificationServiceListener<NotificationContext> contextListener;
  
  private String                                           workspace;

  private int                                              MAX_SIZE = 1000;

  public NotificationServiceImpl(InitParams params) {
    this.contextListener = new NotificationServiceListenerImpl();
    this.workspace = params.getValueParam(NotificationUtils.WORKSPACE_PARAM).getValue();
    if (this.workspace == null) {
      this.workspace = NotificationUtils.DEFAULT_WORKSPACE_NAME;
    }
  }
  

  @Override
  public void start() {
    //
    createNotificationHomeNode();
  }

  @Override
  public void stop() {
    
  }

  private void createNotificationHomeNode() {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      Node homeNode = NotificationUtils.getSession(sProvider, workspace).getRootNode();
      if(NotificationUtils.NOTIFICATION_PARENT_PATH.equals(homeNode.getPath()) == false) {
        homeNode = homeNode.getNode(NotificationUtils.NOTIFICATION_PARENT_PATH);
      }

      if(homeNode.hasNode(NotificationUtils.NOTIFICATION_HOME_NODE) == false) {
        homeNode.addNode(NotificationUtils.NOTIFICATION_HOME_NODE, "ntf:notification");
        homeNode.getSession().save();
      }
    } catch (Exception e) {
      LOG.error("Can not creating the home node of notification setting", e);
    } finally {
      sProvider.close();
    }
  }
  
  private Node getNotificationHomeNode(SessionProvider sProvider) throws Exception {
    Node homeNode = NotificationUtils.getSession(sProvider, workspace).getRootNode();
    if (NotificationUtils.NOTIFICATION_PARENT_PATH.equals(homeNode.getPath()) == false) {
      homeNode = homeNode.getNode(NotificationUtils.NOTIFICATION_PARENT_PATH);
    }

    return homeNode.getNode(NotificationUtils.NOTIFICATION_HOME_NODE);
  }
  
  private Node getMessageHome(SessionProvider sProvider) throws Exception {
    Node homeNode = getNotificationHomeNode(sProvider);
    
    String lever1 = NotificationUtils.PREFIX_MESSAGE_HOME_NODE + String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    if(homeNode.hasNode(lever1)) {
      homeNode = homeNode.getNode(lever1);
      if(homeNode.getNodes().getSize() > MAX_SIZE) {
        String lever2 = NotificationUtils.PREFIX_MESSAGE_HOME_NODE + String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        if(homeNode.canAddMixin("mix:subMessageHome")) {
          homeNode.addMixin("mix:subMessageHome");
        }
        homeNode.addNode(lever2, "ntf:messageHome");
        homeNode.getSession().save();
        return homeNode.getNode(lever2);
      }
    } else {
      homeNode.addNode(lever1, "ntf:messageHome");
      homeNode.getSession().save();
    }
    
    return homeNode.getNode(lever1);
  }

  @Override
  public void addNotificationServiceListener(NotificationContext ctx) {
    contextListener.processListener(ctx);
  }

  @Override
  public void addSendNotificationListener(NotificationMessage message) {
    for (NotificationServiceListener<NotificationMessage> messageListener : messageListeners) {
      messageListener.processListener(message);
    }
  }

  @Override
  public void processNotificationMessage(NotificationMessage message) {
    UserNotificationService notificationService = CommonsUtils.getService(UserNotificationService.class);
    List<String> userIds = message.getSendToUserIds();
    List<String> userIdPendings = new ArrayList<String>();

    String providerId = message.getProviderType();
    for (String userId : userIds) {
      UserNotificationSetting userNotificationSetting = notificationService.getUserNotificationSetting(userId);
      //
      if (userNotificationSetting.isInInstantly(providerId)) {
        message.setSendToUserIds(Arrays.asList(userId));
        addSendNotificationListener(message);
      } 
      //
      if(userNotificationSetting.isActiveWithoutInstantly(providerId)){
        userIdPendings.add(userId);
        setValueSendbyFrequency(message, userNotificationSetting, userId);
      }
    }

    if (userIdPendings.size() > 0) {
      message.setSendToUserIds(userIdPendings);
      saveNotificationMessage(message);
    }
  }

  public void processNotificationMessages(Collection<NotificationMessage> messages) {
    for (NotificationMessage message : messages) {
      processNotificationMessage(message);
    }
  }
  
  private void setValueSendbyFrequency(NotificationMessage message,
                                             UserNotificationSetting userNotificationSetting,
                                             String userId) {
    String providerId = message.getProviderType();
    if (userNotificationSetting.isInDaily(providerId)) {
      message.setSendToDaily(userId);
    }
    if (userNotificationSetting.isInWeekly(providerId)) {
      message.setSendToWeekly(userId);
    }
    if (userNotificationSetting.isInMonthly(providerId)) {
      message.setSendToMonthly(userId);
    }
  }

  @Override
  public void saveNotificationMessage(NotificationMessage message) {
    SessionProvider sProvider = NotificationUtils.createSystemProvider();
    try {
      Node messageHomeNode = getMessageHome(sProvider);
      messageHomeNode.addNode(message.getId(), "ntf:message");
      
      messageHomeNode.getSession().save();
    } catch (Exception e) {
      LOG.error("Can not save the NotificationMessage", e);
    }
  }

  @Override
  public NotificationMessage getNotificationMessageByProviderType(String providerType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<NotificationMessage> getNotificationMessagesByUser(String userId) {
    // TODO Auto-generated method stub
    return null;
  }

}

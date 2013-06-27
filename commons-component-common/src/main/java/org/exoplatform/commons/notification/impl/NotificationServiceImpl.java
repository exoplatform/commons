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
import org.exoplatform.commons.notification.NotificationProperties;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.listener.AbstractNotificationServiceListener;
import org.exoplatform.commons.notification.listener.NotificationServiceListenerImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class NotificationServiceImpl implements NotificationService, NotificationProperties {

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
  
  private Node getMessageHome(Node parent, String nodeName) throws Exception {
    if (parent.hasNode(nodeName) == false) {
      Node messageHome = parent.addNode(nodeName, NTF_MESSAGE_HOME);
      if (messageHome.canAddMixin(MIX_SUB_MESSAGE_HOME)) {
        messageHome.addMixin(MIX_SUB_MESSAGE_HOME);
      }
      return messageHome;
    }
    return parent.getNode(nodeName);
  }
  
  private Node getMessageHome(SessionProvider sProvider) throws Exception {
    Node homeNode = NotificationUtils.getNotificationHomeNode(sProvider, workspace);
    
    return getMessageHome(homeNode, NotificationUtils.PREFIX_MESSAGE_HOME_NODE);
  }
  
  private Node getMessageHomeByDate(Node messageHome) throws Exception {
    
    String lever1 = NotificationUtils.PREFIX_MESSAGE_HOME_NODE + String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    if (messageHome.hasNode(lever1)) {
      messageHome = messageHome.getNode(lever1);
      //
      String lever2 = NotificationUtils.PREFIX_MESSAGE_HOME_NODE + String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
      if (messageHome.getNodes().getSize() > MAX_SIZE && messageHome.hasNode(lever2) == false) {
        messageHome = getMessageHome(messageHome, lever2);
      }
    } else {
      messageHome = getMessageHome(messageHome, lever1);
    }
    if (messageHome.isNew()) {
      messageHome.getSession().save();
    }
    return messageHome;
  }

  private Node getMessageHomeByProviderId(SessionProvider sProvider, String providerId) throws Exception {
    Node messageHome = getMessageHome(getMessageHome(sProvider), providerId);
    return getMessageHomeByDate(messageHome);
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
      Node messageHomeNode = getMessageHomeByProviderId(sProvider, message.getProviderType());
      messageHomeNode.addNode(message.getId(), NTF_MESSAGE);
      
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

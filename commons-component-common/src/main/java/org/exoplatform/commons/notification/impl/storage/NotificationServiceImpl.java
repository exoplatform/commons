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
package org.exoplatform.commons.notification.impl.storage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.UserNotificationSetting;
import org.exoplatform.commons.api.notification.service.AbstractNotificationServiceListener;
import org.exoplatform.commons.api.notification.service.NotificationServiceListener;
import org.exoplatform.commons.api.notification.service.setting.UserNotificationService;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.notification.AbstractService;
import org.exoplatform.commons.notification.NotificationConfiguration;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.listener.NotificationServiceListenerImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class NotificationServiceImpl extends AbstractService implements NotificationService {

  private static final Log                               LOG = ExoLogger.getLogger(NotificationServiceImpl.class);

  private List<AbstractNotificationServiceListener>        messageListeners = new ArrayList<AbstractNotificationServiceListener>(2);

  private NotificationServiceListener<Object> contextListener;
  
  private String                                           workspace;

  private int                                              MAX_SIZE = 1000;
  
  private NotificationConfiguration configuration;

  public NotificationServiceImpl(NotificationConfiguration configuration) {
    this.contextListener = new NotificationServiceListenerImpl();
    this.workspace = configuration.getWorkspace();
    this.configuration = configuration;
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
    Node homeNode = getNotificationHomeNode(sProvider, workspace);
    
    return getMessageHome(homeNode, PREFIX_MESSAGE_HOME_NODE);
  }
  
  private Node getMessageHomeByDate(Node messageHome) throws Exception {
    
    String lever1 = PREFIX_MESSAGE_HOME_NODE + String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    if (messageHome.hasNode(lever1)) {
      messageHome = messageHome.getNode(lever1);
      //
      String lever2 = PREFIX_MESSAGE_HOME_NODE + String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
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
  public void addNotificationServiceListener() {
    contextListener.processListener(null);
  }

  @Override
  public void addSendNotificationListener(AbstractNotificationServiceListener listener) {
    messageListeners.add((AbstractNotificationServiceListener)listener);
  }

  private void processSendNotificationListener(NotificationMessage message) {
    for (AbstractNotificationServiceListener messageListener : messageListeners) {
      messageListener.processListener(message);
    }
  }

  @Override
  public void processNotificationMessage(NotificationMessage message) {
    LOG.info("processNotificationMessage " + message.getId());
    UserNotificationService notificationService = CommonsUtils.getService(UserNotificationService.class);
    List<String> userIds = message.getSendToUserIds();
    List<String> userIdPendings = new ArrayList<String>();

    String providerId = message.getKey().getId();
    for (String userId : userIds) {
      UserNotificationSetting userNotificationSetting = notificationService.getUserNotificationSetting(userId);
      //
      if (userNotificationSetting.isInInstantly(providerId)) {
        LOG.info("call send inInstantly to user " + userId + " with message of provider type " + providerId);
        processSendNotificationListener(message.setTo(userId));
      }
      //
      if(userNotificationSetting.isActiveWithoutInstantly(providerId)){
        userIdPendings.add(userId);
        setValueSendbyFrequency(message, userNotificationSetting, userId);
      }
    }

    if (userIdPendings.size() > 0) {
      message.to(userIdPendings);
      saveNotificationMessage(message);
    }
  }

  public void processNotificationMessages(Collection<NotificationMessage> messages) {
    LOG.info("processNotificationMessages with " + messages.size() + " messages on new thread...");
    for (NotificationMessage message : messages) {
      processNotificationMessage(message);
    }
  }
  
  private void setValueSendbyFrequency(NotificationMessage message,
                                             UserNotificationSetting userNotificationSetting,
                                             String userId) {
    String providerId = message.getKey().getId();
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
    LOG.info("saveNotificationMessage to jcr " + message.toString());
    SessionProvider sProvider = CommonsUtils.getSystemSessionProvider();
    try {
      Node messageHomeNode = getMessageHomeByProviderId(sProvider, message.getKey().getId());
      Node messageNode = messageHomeNode.addNode(message.getId(), NTF_MESSAGE);
      messageNode.setProperty(NTF_FROM, message.getFrom());
      messageNode.setProperty(NTF_ORDER, message.getOrder());
      messageNode.setProperty(NTF_PROVIDER_TYPE, message.getKey().getId());
      messageNode.setProperty(NTF_OWNER_PARAMETER, message.getArrayOwnerParameter());
      messageNode.setProperty(NTF_SEND_TO_DAILY, message.getSendToDaily());
      messageNode.setProperty(NTF_SEND_TO_WEEKLY, message.getSendToWeekly());
      messageNode.setProperty(NTF_SEND_TO_MONTHLY, message.getSendToMonthly());

      if (messageHomeNode.isNew()) {
        messageHomeNode.getSession().save();
      } else {
        messageHomeNode.save();
      }
    } catch (Exception e) {
      LOG.error("Failed to save the NotificationMessage", e);
    }
  }
  
  private NotificationMessage getNotificationMessage(Node node) throws Exception {
    if(node == null) return null;
    NotificationMessage message = NotificationMessage.instance()
      .setFrom(node.getProperty(NTF_FROM).getString())
      .setOrder(Integer.valueOf(node.getProperty(NTF_ORDER).getString()))
      .key(node.getProperty(NTF_PROVIDER_TYPE).getString())
      .setOwnerParameter(node.getProperty(NTF_OWNER_PARAMETER).getValues())
      .setSendToDaily(NotificationUtils.valuesToArray(node.getProperty(NTF_SEND_TO_DAILY).getValues()))
      .setSendToWeekly(NotificationUtils.valuesToArray(node.getProperty(NTF_SEND_TO_WEEKLY).getValues()))
      .setSendToMonthly(NotificationUtils.valuesToArray(node.getProperty(NTF_SEND_TO_MONTHLY).getValues()))
      .setId(node.getName());
    
    return message;
  }

  private void removeNotificationMessage(Session session, List<String> removePaths) throws Exception {
    if (removePaths.size() > 0) {
      for (String string : removePaths) {
        LOG.info("Remove NotificationMessage " + string);
        session.getItem(string).remove();
      }
      session.save();
    }
  }

  private void removeProperty(Node node, String property, String userId) throws Exception {
    List<String> values = NotificationUtils.valuesToList(node.getProperty(property).getValues());
    LOG.info("Remove Property NotificationMessage " + property + " of user " + userId);
    if(values.contains(userId)) {
      values.remove(userId);
      node.setProperty(property, values.toArray(new String[values.size()]));
      node.save();
    }
  }
  
  private boolean isRemove(NotificationMessage message, String property) {
    if(property.equals(NTF_SEND_TO_DAILY) && message.getSendToDaily().length == 1){
      if(message.getSendToWeekly().length == 0 && message.getSendToMonthly().length == 0) {
        return true;
      }
    }
    if(property.equals(NTF_SEND_TO_WEEKLY) && message.getSendToWeekly().length == 1){
      if(message.getSendToDaily().length == 0 && message.getSendToMonthly().length == 0) {
        return true;
      }
    }
    if(property.equals(NTF_SEND_TO_MONTHLY) && message.getSendToMonthly().length == 1){
      if(message.getSendToWeekly().length == 0 && message.getSendToDaily().length == 0) {
        return true;
      }
    }
    return false;
  }

  private List<NotificationMessage> getNotificationMessages(SessionProvider sProvider, String providerId,
                                                            String property, String userId) throws Exception{
    List<NotificationMessage> messages = new ArrayList<NotificationMessage>();
    StringBuffer queryBuffer = new StringBuffer(JCR_ROOT);
    Node messageHomeNode = getMessageHome(getMessageHome(sProvider), providerId);
    Session session = messageHomeNode.getSession();
    queryBuffer.append(messageHomeNode.getPath()).append("//element(*,").append(NTF_MESSAGE).append(")")
               .append("[").append("@").append(property).append("='").append(userId).append("']  order by @")
               .append(NTF_ORDER).append(ASCENDING).append(", @").append("exo:dateCreated").append(DESCENDING);

    QueryManager qm = session.getWorkspace().getQueryManager();
    Query query = qm.createQuery(queryBuffer.toString(), Query.XPATH);
    NodeIterator iter = query.execute().getNodes();
    
    List<String> removePaths = new ArrayList<String>();
    while (iter.hasNext()) {
      Node node = iter.nextNode();
      NotificationMessage message = getNotificationMessage(node);
      messages.add(message.setTo(userId));
      if(isRemove(message, property)) {
        removePaths.add(node.getPath());
      } else {
        removeProperty(node, property, userId);
      }
    }
    
    //
    removeNotificationMessage(session, removePaths);

    return messages;
  }

  private static void strageMap(Map<String, List<NotificationMessage>> notificationData, String key, List<NotificationMessage> values) {
    if (notificationData.containsKey(key)) {
      List<NotificationMessage> messages = notificationData.get(key);
      for (NotificationMessage notificationMessage : values) {
        if (messages.size() == 0 || messages.contains(notificationMessage) == false) {
          messages.add(notificationMessage);
        }
      }
      //
      if(messages.size() > 0 ) {
        notificationData.put(key, messages);
      }
    } else if (values.size() > 0) {
      notificationData.put(key, values);
    }
  }

  @Override
  public Map<String, List<NotificationMessage>> getNotificationMessagesByUser(UserNotificationSetting userSetting) {
    LOG.info("Get all messages notification by user " +userSetting.getUserId() );
    long startTime = System.currentTimeMillis();
    
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    Map<String, List<NotificationMessage>> notificationData = new LinkedHashMap<String, List<NotificationMessage>>();
    try {
      //for daily
      for (String providerId : userSetting.getDailyProviders()) {
        LOG.info("Get NotificationMessage for daily... ");
        strageMap(notificationData, providerId, getNotificationMessages(sProvider, providerId, NTF_SEND_TO_DAILY, userSetting.getUserId()));
      }
      
      // for weekly
      if(NotificationUtils.isWeekEnd(configuration.getDayOfWeekend())) {
        LOG.info("Get NotificationMessage for weekly... ");
        for (String providerId : userSetting.getWeeklyProviders()) {
          strageMap(notificationData, providerId, getNotificationMessages(sProvider, providerId, NTF_SEND_TO_WEEKLY, userSetting.getUserId()));
        }
      }

    } catch (Exception e) {
      LOG.error("Failed to get the NotificationMessage by user: " + userSetting.getUserId(), e);
    } finally {
      sProvider.close();
    }
    
    LOG.info("And get all messages notification by user " + userSetting.getUserId() + " " + notificationData.size() + " .. " + (System.currentTimeMillis() - startTime) + " ms");
    return notificationData;
  }

}

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
package org.exoplatform.commons.notification.impl.service.storage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.exoplatform.commons.api.notification.model.NotificationKey;
import org.exoplatform.commons.api.notification.model.NotificationMessage;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.storage.NotificationDataStorage;
import org.exoplatform.commons.notification.NotificationConfiguration;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class NotificationDataStorageImpl extends AbstractService implements NotificationDataStorage {
  
  private static final Log LOG = ExoLogger.getLogger(NotificationDataStorageImpl.class);
  private String workspace;
  private NotificationConfiguration configuration = null;
  
  public NotificationDataStorageImpl(NotificationConfiguration configuration) {
    this.workspace = configuration.getWorkspace();
    this.configuration = configuration;
  }
  
  @Override
  public void createParentNodeOfPlugin(String pluginId) {
    SessionProvider sProvider = CommonsUtils.getSystemSessionProvider();
    try {
      Node node = getOrCreateMessageParent(sProvider, workspace, pluginId);
      sessionSave(node);
    } catch (Exception e) {
      LOG.error("Failed to create parent Node of plugin " + pluginId, e);
    }
  }

  @Override
  public void save(NotificationMessage message) throws Exception {
    LOG.info("saveNotificationMessage to jcr " + message.toString());
    SessionProvider sProvider = CommonsUtils.getSystemSessionProvider();
    try {
      Node messageHomeNode = getOrCreateMessageParent(sProvider, workspace, message.getKey().getId());
      Node messageNode = messageHomeNode.addNode(message.getId(), NTF_MESSAGE);
      messageNode.setProperty(NTF_FROM, message.getFrom());
      messageNode.setProperty(NTF_ORDER, message.getOrder());
      messageNode.setProperty(NTF_PROVIDER_TYPE, message.getKey().getId());
      messageNode.setProperty(NTF_OWNER_PARAMETER, message.getArrayOwnerParameter());
      messageNode.setProperty(NTF_SEND_TO_DAILY, message.getSendToDaily());
      messageNode.setProperty(NTF_SEND_TO_WEEKLY, message.getSendToWeekly());
      messageHomeNode.getSession().save();
    } catch (Exception e) {
      LOG.error("Failed to save the NotificationMessage", e);
    }
  }
 
  
  @Override
  public Map<NotificationKey, List<NotificationMessage>> getByUser(UserSetting setting) {
    LOG.info("Get all messages notification by user " + setting.getUserId());
    long startTime = System.currentTimeMillis();
    
    SessionProvider sProvider = CommonsUtils.getSystemSessionProvider();
    Map<NotificationKey, List<NotificationMessage>> notificationData = new LinkedHashMap<NotificationKey, List<NotificationMessage>>();
    try {
      //for daily
      LOG.info("Get NotificationMessage for daily... ");
      for (String providerId : setting.getDailyProviders()) {
        putMap(notificationData, NotificationKey.key(providerId), getNotificationMessages(sProvider, providerId, NTF_SEND_TO_DAILY, setting.getUserId()));
      }
      
      // for weekly
      if(NotificationUtils.isWeekEnd(configuration.getDayOfWeekend())) {
        LOG.info("Get NotificationMessage for weekly... ");
        for (String providerId : setting.getWeeklyProviders()) {
          putMap(notificationData, NotificationKey.key(providerId), getNotificationMessages(sProvider, providerId, NTF_SEND_TO_WEEKLY, setting.getUserId()));
        }
      }

    } catch (Exception e) {
      LOG.error("Failed to get the NotificationMessage by user: " + setting.getUserId(), e);
    }
    
    LOG.info("And get all messages notification by user " + setting.getUserId() + " " + notificationData.size() + " .. " + (System.currentTimeMillis() - startTime) + " ms");
    return notificationData;
  }
  
  private static void putMap(Map<NotificationKey, List<NotificationMessage>> notificationData, NotificationKey key, List<NotificationMessage> values) {
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
  
  private List<NotificationMessage> getNotificationMessages(SessionProvider sProvider, String providerId,
                                                            String property, String userId) throws Exception{
    List<NotificationMessage> messages = new ArrayList<NotificationMessage>();
    StringBuffer queryBuffer = new StringBuffer(JCR_ROOT);
    Node messageHomeNode = getOrCreateMessageParent(sProvider, workspace, providerId);
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
      NotificationMessage model = fillModel(node);
      messages.add(model.setTo(userId));
      if(isRemove(model, property)) {
        removePaths.add(node.getPath());
      } else {
        removeProperty(node, property, userId);
      }
    }
    
    //
    removeNotificationMessage(session, removePaths);

    return messages;
  }
  
  private NotificationMessage fillModel(Node node) throws Exception {
    if(node == null) return null;
    NotificationMessage message = NotificationMessage.instance()
      .setFrom(node.getProperty(NTF_FROM).getString())
      .setOrder(Integer.valueOf(node.getProperty(NTF_ORDER).getString()))
      .key(node.getProperty(NTF_PROVIDER_TYPE).getString())
      .setOwnerParameter(node.getProperty(NTF_OWNER_PARAMETER).getValues())
      .setSendToDaily(NotificationUtils.valuesToArray(node.getProperty(NTF_SEND_TO_DAILY).getValues()))
      .setSendToWeekly(NotificationUtils.valuesToArray(node.getProperty(NTF_SEND_TO_WEEKLY).getValues()))
      .setId(node.getName());
    
    return message;
  }
  
  private boolean isRemove(NotificationMessage message, String property) {
    if(property.equals(NTF_SEND_TO_DAILY) && message.getSendToDaily().length == 1){
      if(message.getSendToWeekly().length == 0) {
        return true;
      }
    }
    if(property.equals(NTF_SEND_TO_WEEKLY) && message.getSendToWeekly().length == 1){
      if(message.getSendToDaily().length == 0) {
        return true;
      }
    }
    
    return false;
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
  
  private void removeNotificationMessage(Session session, List<String> removePaths) throws Exception {
    if (removePaths.size() > 0) {
      for (String nodePath : removePaths) {
        LOG.info("Remove NotificationMessage " + nodePath);
        session.getItem(nodePath).remove();
      }
      session.save();
    }
  }
  
  
}

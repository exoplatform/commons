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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.NotificationKey;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.storage.NotificationDataStorage;
import org.exoplatform.commons.notification.NotificationConfiguration;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class NotificationDataStorageImpl extends AbstractService implements NotificationDataStorage{
  private static final Log         LOG              = ExoLogger.getLogger(NotificationDataStorageImpl.class);

  public static final String       REMOVE_ALL       = "removeAll";

  private String                    workspace;

  private NotificationConfiguration configuration    = null;

  private Map<String, Set<String>>  removeByCallBack = new ConcurrentHashMap<String, Set<String>>();

  public NotificationDataStorageImpl(NotificationConfiguration configuration) {
    this.workspace = configuration.getWorkspace();
    this.configuration = configuration;
  }

  @Override
  public void save(NotificationInfo message) throws Exception {
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
  public Map<NotificationKey, List<NotificationInfo>> getByUser(UserSetting setting) {
    LOG.info("Get all messages notification by user " + setting.getUserId());
    long startTime = System.currentTimeMillis();
    
    SessionProvider sProvider = CommonsUtils.getSystemSessionProvider();
    Map<NotificationKey, List<NotificationInfo>> notificationData = new LinkedHashMap<NotificationKey, List<NotificationInfo>>();
    try {
      //for daily
      LOG.info("Get NotificationMessage for daily... ");
      for (String pluginId : setting.getDailyProviders()) {
        putMap(notificationData, NotificationKey.key(pluginId), getNotificationMessages(sProvider, pluginId, NTF_SEND_TO_DAILY, setting.getUserId()));
      }
      
      // for weekly
      if(NotificationUtils.isWeekEnd(configuration.getDayOfWeekend())) {
        LOG.info("Get NotificationMessage for weekly... ");
        for (String pluginId : setting.getWeeklyProviders()) {
          putMap(notificationData, NotificationKey.key(pluginId), getNotificationMessages(sProvider, pluginId, NTF_SEND_TO_WEEKLY, setting.getUserId()));
        }
      }

    } catch (Exception e) {
      LOG.error("Failed to get the NotificationMessage by user: " + setting.getUserId(), e);
    }
    
    LOG.info("And get all messages notification by user " + setting.getUserId() + " " + notificationData.size() + " .. " + (System.currentTimeMillis() - startTime) + " ms");
    return notificationData;
  }

  private static void putMap(Map<NotificationKey, List<NotificationInfo>> notificationData, NotificationKey key, List<NotificationInfo> values) {
    if (notificationData.containsKey(key)) {
      List<NotificationInfo> messages = notificationData.get(key);
      for (NotificationInfo notificationMessage : values) {
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

  private Node getParentNodeByPlugin(SessionProvider sProvider, String property, String pluginId) throws Exception {
    if (NTF_SEND_TO_DAILY.equals(property)) {
      return getOrCreateMessageParent(sProvider, workspace, pluginId).getParent();
    }
    return getMessageNodeByPluginId(sProvider, workspace, pluginId);
  }

  private List<NotificationInfo> getNotificationMessages(SessionProvider sProvider, String pluginId,
                                                            String property, String userId) throws Exception{
    List<NotificationInfo> messages = new ArrayList<NotificationInfo>();
    StringBuffer queryBuffer = new StringBuffer(JCR_ROOT);
    Node messageHomeNode = getParentNodeByPlugin(sProvider, property, pluginId);
    queryBuffer.append(messageHomeNode.getPath()).append("//element(*,").append(NTF_MESSAGE).append(")")
               .append("[").append("(@").append(property).append("='").append(userId).append("' or @")
               .append(property).append("='").append(NotificationInfo.FOR_ALL_USER).append("') and (@")
               .append(NTF_FROM).append("!='").append(userId).append("')")
               .append("]  order by @").append(NTF_ORDER).append(ASCENDING)
               .append(", @").append("exo:dateCreated").append(DESCENDING);

    Session session = messageHomeNode.getSession();
    QueryManager qm = session.getWorkspace().getQueryManager();
    Query query = qm.createQuery(queryBuffer.toString(), Query.XPATH);
    NodeIterator iter = query.execute().getNodes();

    while (iter.hasNext()) {
      Node node = iter.nextNode();
      NotificationInfo model = fillModel(node);
      messages.add(model.setTo(userId));
      processRemove(session, model, property, node.getPath());
    }
    
    return messages;
  }
  
  private NotificationInfo fillModel(Node node) throws Exception {
    if(node == null) return null;
    NotificationInfo message = NotificationInfo.instance()
      .setFrom(node.getProperty(NTF_FROM).getString())
      .setOrder(Integer.valueOf(node.getProperty(NTF_ORDER).getString()))
      .key(node.getProperty(NTF_PROVIDER_TYPE).getString())
      .setOwnerParameter(node.getProperty(NTF_OWNER_PARAMETER).getValues())
      .setSendToDaily(NotificationUtils.valuesToArray(node.getProperty(NTF_SEND_TO_DAILY).getValues()))
      .setSendToWeekly(NotificationUtils.valuesToArray(node.getProperty(NTF_SEND_TO_WEEKLY).getValues()))
      .setId(node.getName());
    
    return message;
  }
  
  private Set<String> addValue(String property, String value) {
    Set<String> set = removeByCallBack.get(property);
    if (set == null) {
      set = new HashSet<String>();
    }
    set.add(value);
    return set;
  }

  private void processRemove(Session session, NotificationInfo message, 
                                String property, String path) throws Exception {
    boolean isRemove = false;
    if (message.isSendAll()) {
      isRemove = (property.equals(NTF_SEND_TO_WEEKLY));
    }
    if (isRemove == false && property.equals(NTF_SEND_TO_DAILY) && message.getSendToDaily().length == 1) {
      isRemove = (message.getSendToWeekly().length == 0);
    }
    if (isRemove == false && property.equals(NTF_SEND_TO_WEEKLY) && message.getSendToWeekly().length == 1) {
      isRemove = (message.getSendToDaily().length == 0);
    }
    //
    if (isRemove) {
      removeByCallBack.put(REMOVE_ALL, addValue(REMOVE_ALL, path));
    } else {
      removeProperty(session, path, property, message.getTo());
    }
  }
  
  private void removeProperty(Session session, String path, String property, String value) {
    try {
      Node node = (Node) session.getItem(path);
      List<String> values = NotificationUtils.valuesToList(node.getProperty(property).getValues());
      if (values.contains(value)) {
        values.remove(value);
        node.setProperty(property, values.toArray(new String[values.size()]));
        node.save();
      }
    } catch (Exception e) {
      LOG.info(String.format("Failed to remove property %s of value %s on node ", property, value));
    }
  }
  
  @Override
  public void removeMessageCallBack() throws Exception {
    SessionProvider sProvider = CommonsUtils.getSystemSessionProvider();
    try {
      Session session = getNotificationHomeNode(sProvider, workspace).getSession();
      // remove all
      Set<String> listRemoveAll = removeByCallBack.get(REMOVE_ALL);
      if (listRemoveAll != null && listRemoveAll.size() > 0) {
        for (String nodePath : listRemoveAll) {
          try {
            session.getItem(nodePath).remove();
            LOG.info("Remove NotificationMessage " + nodePath);
          } catch (Exception e) {
            LOG.warn("Failed to remove node of NotificationMessage " + nodePath, e);
          }
        }
        session.save();
      }
    } catch (Exception e) {
      LOG.warn("Failed to remove message after sent email notification", e);
    }
    removeByCallBack.clear();
  }

}

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
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

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
import org.exoplatform.commons.notification.NotificationContextFactory;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.impl.NotificationSessionManager;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class NotificationDataStorageImpl extends AbstractService implements NotificationDataStorage{
  private static final Log         LOG              = ExoLogger.getLogger(NotificationDataStorageImpl.class);

  public static final String       REMOVE_ALL       = "removeAll";

  private String                    workspace;

  private NotificationConfiguration configuration    = null;
  
  private final ReentrantLock lock = new ReentrantLock();

  private Map<String, Set<String>>  removeByCallBack = new ConcurrentHashMap<String, Set<String>>();

  public NotificationDataStorageImpl(NotificationConfiguration configuration) {
    this.workspace = configuration.getWorkspace();
    this.configuration = configuration;
  }

  @Override
  public void save(NotificationInfo message) throws Exception {
    SessionProvider sProvider = CommonsUtils.getSystemSessionProvider();
    final ReentrantLock localLock = lock;
    try {
      localLock.lock();
      Node messageHomeNode = getOrCreateMessageParent(sProvider, workspace, message.getKey().getId());
      Node messageNode = messageHomeNode.addNode(message.getId(), NTF_MESSAGE);
      messageNode.setProperty(NTF_FROM, message.getFrom());
      messageNode.setProperty(NTF_ORDER, message.getOrder());
      messageNode.setProperty(NTF_PROVIDER_TYPE, message.getKey().getId());
      messageNode.setProperty(NTF_OWNER_PARAMETER, message.getArrayOwnerParameter());
      messageNode.setProperty(NTF_SEND_TO_DAILY, message.getSendToDaily());
      messageNode.setProperty(NTF_SEND_TO_WEEKLY, message.getSendToWeekly());
      messageHomeNode.getSession().save();
      
      //record statistics insert entity
      if (NotificationContextFactory.getInstance().getStatistics().isStatisticsEnabled()) {
        NotificationContextFactory.getInstance().getStatisticsCollector().insertEntity(NTF_MESSAGE);
      }
      
    } catch (Exception e) {
      LOG.error("Failed to save the NotificationMessage", e);
    } finally {
      localLock.unlock();
    }
  }

  @Override
  public Map<NotificationKey, List<NotificationInfo>> getByUser(UserSetting setting) {
//    SessionProvider sProvider = SessionProvider.createSystemProvider();
    SessionProvider sProvider = NotificationSessionManager.createSystemProvider();
    Map<NotificationKey, List<NotificationInfo>> notificationData = new LinkedHashMap<NotificationKey, List<NotificationInfo>>();
    try {

      if (configuration.isSendWeekly() == false) {
        // for daily
        for (String pluginId : setting.getDailyProviders()) {
          putMap(notificationData, NotificationKey.key(pluginId), getNotificationMessages(sProvider, pluginId, NTF_SEND_TO_DAILY, setting.getUserId()));
        }
      } else {
        // for weekly
        for (String pluginId : setting.getWeeklyProviders()) {
          putMap(notificationData, NotificationKey.key(pluginId), getNotificationMessages(sProvider, pluginId, NTF_SEND_TO_WEEKLY, setting.getUserId()));
        }
      }

    } catch (Exception e) {
      LOG.error("Failed to get the NotificationMessage by user: " + setting.getUserId(), e);
    } finally {
//      sProvider.close();
    }

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
                                                            String property, String userId) throws Exception {
    List<NotificationInfo> messages = new ArrayList<NotificationInfo>();
    Node messageHomeNode = getParentNodeByPlugin(sProvider, property, pluginId);
    NodeIterator iter = getNotificationNodeMessages(messageHomeNode, property, userId);
    Session session = messageHomeNode.getSession();
    while (iter.hasNext()) {
      Node node = iter.nextNode();
      NotificationInfo model = fillModel(node);
      messages.add(model.setTo(userId));
      processRemove(session, model, property, node.getPath());
    }
    return messages;
  }

  private NodeIterator getNotificationNodeMessages(Node messageHomeNode, String property, String userId) throws Exception {
    final boolean stats = NotificationContextFactory.getInstance().getStatistics().isStatisticsEnabled();
    long startTime = 0;
    if ( stats ) startTime = System.currentTimeMillis();
    
    StringBuilder strQuery = new StringBuilder("SELECT * FROM ").append(NTF_MESSAGE).append(" WHERE ");
    
    if (NTF_SEND_TO_DAILY.equals(property)) {
      String dayName = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
      strQuery.append(" (jcr:path LIKE '").append(messageHomeNode.getPath()).append("/").append(DAY).append(dayName).append("/%'")
              .append(" AND NOT jcr:path LIKE '").append(messageHomeNode.getPath()).append("/").append(DAY).append(dayName).append("/%/%')");
    } else {
      strQuery.append(" jcr:path LIKE '").append(messageHomeNode.getPath()).append("/%'");
    }

    strQuery.append(" AND (").append(property).append("='").append(userId).append("'");
    if(NotificationInfo.FOR_ALL_USER.equals(userId) == false) {
      strQuery.append(" OR ").append(property).append("='").append(NotificationInfo.FOR_ALL_USER).append("') AND ")
              .append(NTF_FROM).append("<>'").append(userId).append("'");
      strQuery.append(" order by ").append(NTF_ORDER).append(ASCENDING).append(", exo:dateCreated").append(DESCENDING);
    } else {
      strQuery.append(")");
    }

    QueryManager qm = messageHomeNode.getSession().getWorkspace().getQueryManager();
    Query query = qm.createQuery(strQuery.toString(), Query.SQL);
    NodeIterator it = query.execute().getNodes();
    
    //record statistics insert entity
    if (stats) {
      NotificationContextFactory.getInstance().getStatisticsCollector().queryExecuted(strQuery.toString(), it.getSize(), System.currentTimeMillis() - startTime);
    }
    return it;
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
      if(isRemove == false) {
        removeByCallBack.put(property, addValue(property, path));
      }
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
    final boolean stats = NotificationContextFactory.getInstance().getStatistics().isStatisticsEnabled();
    
    try {
      Node node = (Node) session.getItem(path);
      List<String> values = NotificationUtils.valuesToList(node.getProperty(property).getValues());
      if (values.contains(value)) {
        values.remove(value);
        if (values.isEmpty()) {
          values.add("");
        }
        node.setProperty(property, values.toArray(new String[values.size()]));
        node.save();
        
        //record entity update here
        if (stats) {
          NotificationContextFactory.getInstance().getStatisticsCollector().updateEntity(NTF_MESSAGE);
        }
      }
    } catch (Exception e) {
      LOG.warn(String.format("Failed to remove property %s of value %s on node ", property, value));
    }
  }

  @Override
  public void removeMessageAfterSent() throws Exception {
    final boolean stats = NotificationContextFactory.getInstance().getStatistics().isStatisticsEnabled();
    
//    SessionProvider sProvider = SessionProvider.createSystemProvider();
    SessionProvider sProvider = NotificationSessionManager.createSystemProvider();
    try {
      Node notificationHome = getNotificationHomeNode(sProvider, workspace);
      Session session = notificationHome.getSession();
      // remove all
      Set<String> listPaths = removeByCallBack.get(REMOVE_ALL);
      removeByCallBack.remove(REMOVE_ALL);
      if (listPaths != null && listPaths.size() > 0) {
        for (String nodePath : listPaths) {
          try {
            session.getItem(nodePath).remove();
            
            //record entity delete here
            if (stats) {
              NotificationContextFactory.getInstance().getStatisticsCollector().deleteEntity(NTF_MESSAGE);
            }
            
            LOG.debug("Remove NotificationMessage " + nodePath);
          } catch (Exception e) {
            LOG.warn("Failed to remove node of NotificationMessage " + nodePath + "\n" + e.getMessage());
          }
        }
        session.save();
      }
      // remove property daily for case send all.
      listPaths = removeByCallBack.get(NTF_SEND_TO_DAILY);
      removeByCallBack.remove(NTF_SEND_TO_DAILY);
      if (listPaths != null && listPaths.size() > 0) {
        for (String nodePath : listPaths) {
          removeProperty(session, nodePath, NTF_SEND_TO_DAILY, NotificationInfo.FOR_ALL_USER);
        }
      }
      // remove node weekly for case send all.
      if (configuration.isSendWeekly()) {
        Node messageHomeNode = notificationHome.getNode(MESSAGE_HOME_NODE);
        NodeIterator iterator = getNotificationNodeMessages(messageHomeNode, NTF_SEND_TO_WEEKLY, NotificationInfo.FOR_ALL_USER);
        String nodePath;
        while (iterator.hasNext()) {
          Node node = iterator.nextNode();
          nodePath = node.getPath();
          node.remove();
          
          //record entity delete here
          if (stats) {
            NotificationContextFactory.getInstance().getStatisticsCollector().deleteEntity(NTF_MESSAGE);
          }
          LOG.debug("Remove NotificationMessage " + nodePath);
        }
        session.save();
      }
    } catch (Exception e) {
      LOG.warn("Failed to remove message after sent email notification", e);
    } finally {
//      sProvider.close();
    }
  }

}

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

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.storage.NotificationDataStorage;
import org.exoplatform.commons.notification.NotificationConfiguration;
import org.exoplatform.commons.notification.NotificationContextFactory;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.impl.NotificationSessionManager;
import org.exoplatform.commons.notification.job.NotificationJob;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class NotificationDataStorageImpl extends AbstractService implements NotificationDataStorage {
  private static final Log         LOG              = ExoLogger.getLogger(NotificationDataStorageImpl.class);

  private static final String       REMOVE_ALL       = "removeAll";
  
  private static final String       REMOVE_DAILY     = "removeDaily";

  private String                    workspace;

  private final ReentrantLock lock = new ReentrantLock();

  private Map<String, Set<String>>  removeByCallBack = new ConcurrentHashMap<String, Set<String>>();

  public NotificationDataStorageImpl(NotificationConfiguration configuration) {
    this.workspace = configuration.getWorkspace();
  }

  @Override
  public void save(NotificationInfo message) throws Exception {
    boolean created = NotificationSessionManager.createSystemProvider();
    SessionProvider sProvider = NotificationSessionManager.getSessionProvider();
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
      NotificationSessionManager.closeSessionProvider(created);
      localLock.unlock();
    }
  }
  
  @Override
  public Map<PluginKey, List<NotificationInfo>> getByUser(NotificationContext context, UserSetting setting) {
    boolean created =  NotificationSessionManager.createSystemProvider();
    SessionProvider sProvider =  NotificationSessionManager.getSessionProvider();
    
    Map<PluginKey, List<NotificationInfo>> notificationData = new LinkedHashMap<PluginKey, List<NotificationInfo>>();
    try {
      boolean isWeekly = context.value(NotificationJob.JOB_WEEKLY);
      if (isWeekly) {
        for (String pluginId : setting.getWeeklyPlugins()) {
          putMap(notificationData, PluginKey.key(pluginId), getWeeklyNotifs(sProvider, pluginId, setting.getUserId()));
        }
      }
      //
      boolean isDaily = context.value(NotificationJob.JOB_DAILY);
      if (isDaily) {
        for (String pluginId : setting.getDailyPlugins()) {
          putMap(notificationData, PluginKey.key(pluginId), getDailyNotifs(sProvider, context, pluginId, setting.getUserId()));
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to get the NotificationMessage by user: " + setting.getUserId(), e);
    } finally {
      NotificationSessionManager.closeSessionProvider(created);
    }

    return notificationData;
  }

  private static void putMap(Map<PluginKey, List<NotificationInfo>> notificationData, PluginKey key, List<NotificationInfo> values) {
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

  private List<NotificationInfo> getDailyNotifs(SessionProvider sProvider,
                                                NotificationContext context,
                                                String pluginId,
                                                String userId) throws Exception {
    
    List<NotificationInfo> messages = new ArrayList<NotificationInfo>();
    Node plugInDayNode = getParentNodeByDate(sProvider, workspace, context, pluginId);
    NodeIterator iter = getDailyNodes(plugInDayNode, userId);
    Session session = plugInDayNode.getSession();
    while (iter.hasNext()) {
      Node node = iter.nextNode();
      NotificationInfo model = fillModel(node);
      messages.add(model.setTo(userId));
      removeDaily(session, model, node.getPath());
    }
    return messages;
  }

  /**
   * Makes the node path for MessageHome node '/eXoNotification/messageHome/<pluginId>/<DAY_OF_MONTH>/'
   * 
   * @param sProvider
   * @param workspace
   * @param context keeping the day if in the daily job context
   * @param pluginId
   * @return
   * @throws Exception
   */
  private Node getParentNodeByDate(SessionProvider sProvider,
                                 String workspace,
                                 NotificationContext context,
                                 String pluginId) throws Exception {
    
    Node providerNode = getMessageNodeByPluginId(sProvider, workspace, pluginId);
    String dayName = context.value(NotificationJob.DAY_OF_JOB);
    return getOrCreateMessageNode(providerNode, DAY + dayName);
  }

  private List<NotificationInfo> getWeeklyNotifs(SessionProvider sProvider,
                                                 String pluginId,
                                                 String userId) throws Exception {
    List<NotificationInfo> messages = new ArrayList<NotificationInfo>();
    Node messageHomeNode = getMessageNodeByPluginId(sProvider, workspace, pluginId);
    NodeIterator iter = getWeeklyNodes(messageHomeNode, userId);
    Session session = messageHomeNode.getSession();
    while (iter.hasNext()) {
      Node node = iter.nextNode();
      NotificationInfo model = fillModel(node);
      messages.add(model.setTo(userId));
      removeWeekly(session, model, node.getPath());
    }
    return messages;
  }

  private NodeIterator getWeeklyNodes(Node messageHomeNode, String userId) throws Exception {
    final boolean stats = NotificationContextFactory.getInstance().getStatistics().isStatisticsEnabled();
    long startTime = 0;
    if ( stats ) startTime = System.currentTimeMillis();
    //
    userId = userId.replace("'", "''");
    StringBuilder strQuery = new StringBuilder("SELECT * FROM ").append(NTF_MESSAGE).append(" WHERE ");
    strQuery.append(" jcr:path LIKE '").append(messageHomeNode.getPath()).append("/%'");
    strQuery.append(" AND (").append(NTF_SEND_TO_WEEKLY).append("='").append(userId).append("'");
    strQuery.append(" OR ").append(NTF_SEND_TO_WEEKLY).append("='").append(NotificationInfo.FOR_ALL_USER)
            .append("') AND ").append(NTF_FROM).append("<>'").append(userId).append("'");
    strQuery.append(" order by ").append(NTF_ORDER).append(ASCENDING).append(", exo:dateCreated").append(DESCENDING);

    QueryManager qm = messageHomeNode.getSession().getWorkspace().getQueryManager();
    Query query = qm.createQuery(strQuery.toString(), Query.SQL);
    NodeIterator it = query.execute().getNodes();
    
    //record statistics insert entity
    if (stats) {
      NotificationContextFactory.getInstance().getStatisticsCollector().queryExecuted(strQuery.toString(), it.getSize(), System.currentTimeMillis() - startTime);
    }
    return it;
  }
  
  private NodeIterator getDailyNodes(Node pluginDayNode, String userId) throws Exception {
    final boolean stats = NotificationContextFactory.getInstance().getStatistics().isStatisticsEnabled();
    long startTime = 0;
    if ( stats ) startTime = System.currentTimeMillis();
    //
    userId = userId.replace("'", "''");
    
    StringBuilder strQuery = new StringBuilder("SELECT * FROM ").append(NTF_MESSAGE).append(" WHERE ");
    strQuery.append(" (jcr:path LIKE '").append(pluginDayNode.getPath()).append("/%'")
              .append(" AND NOT jcr:path LIKE '").append(pluginDayNode.getPath()).append("/%/%')");
    strQuery.append(" AND (").append(NTF_SEND_TO_DAILY).append("='").append(userId).append("'");
    strQuery.append(" OR ").append(NTF_SEND_TO_DAILY).append("='").append(NotificationInfo.FOR_ALL_USER)
              .append("') AND ").append(NTF_FROM).append("<>'").append(userId).append("'");
    strQuery.append(" order by ").append(NTF_ORDER).append(ASCENDING).append(", exo:dateCreated").append(DESCENDING);

    QueryManager qm = pluginDayNode.getSession().getWorkspace().getQueryManager();
    Query query = qm.createQuery(strQuery.toString(), Query.SQL);
    NodeIterator it = query.execute().getNodes();
    
    if (stats) {
      NotificationContextFactory.getInstance().getStatisticsCollector().queryExecuted(strQuery.toString(), it.getSize(), System.currentTimeMillis() - startTime);
    }
    return it;
  }

  private NotificationInfo fillModel(Node node) throws Exception {
    if(node == null) return null;
    if(!node.hasProperty(EXO_LAST_MODIFIED_DATE)) {
      if(node.isNodeType(EXO_MODIFY)) {
        node.setProperty(EXO_LAST_MODIFIED_DATE, Calendar.getInstance());
        node.save();
      }
      else if(node.canAddMixin(EXO_MODIFY)) {
        node.addMixin(EXO_MODIFY);
        node.setProperty(EXO_LAST_MODIFIED_DATE, Calendar.getInstance());
        node.save();
      }
      else {
        LOG.warn("Cannot add mixin to node '{}'.", node.getPath());
      }
    }
    NotificationInfo message = NotificationInfo.instance()
      .setFrom(node.getProperty(NTF_FROM).getString())
      .setOrder(Integer.valueOf(node.getProperty(NTF_ORDER).getString()))
      .key(node.getProperty(NTF_PROVIDER_TYPE).getString())
      .setOwnerParameter(node.getProperty(NTF_OWNER_PARAMETER).getValues())
      .setSendToDaily(NotificationUtils.valuesToArray(node.getProperty(NTF_SEND_TO_DAILY).getValues()))
      .setSendToWeekly(NotificationUtils.valuesToArray(node.getProperty(NTF_SEND_TO_WEEKLY).getValues()))
      .setLastModifiedDate(node.getProperty(EXO_LAST_MODIFIED_DATE).getDate())
      .setId(node.getName());

    return message;
  }

  private void putRemoveMap(String key, String value) {
    Set<String> set = removeByCallBack.get(key);
    if (set == null) {
      set = new HashSet<String>();
      removeByCallBack.put(key, set);
    }
    set.add(value);
  }

  /**
   * In the case if the notification plug-in allows to impact all of user.
   * In the case Daily, the notifiation_send_to_daily will be remove the sendAll value and still keep it for weekly
   * 
   * @param session
   * @param message
   * @param path
   * @throws Exception
   */
  private void removeDaily(Session session, NotificationInfo message, String path) throws Exception {
    if (message.getSendToDaily().length == 1 && message.getSendToWeekly().length == 0) {
      putRemoveMap(REMOVE_ALL, path);
    } if (message.getSendToDaily().length > 0 &&  NotificationInfo.FOR_ALL_USER.equals(message.getSendToDaily()[0])) {
      putRemoveMap(REMOVE_DAILY, path);
    } else {
      removeProperty(session, path, NTF_SEND_TO_DAILY, message.getTo());
    }
  }

  private void removeWeekly(Session session, NotificationInfo message, String path) throws Exception {
    if (message.isSendAll() || message.getSendToWeekly().length == 1) {
      putRemoveMap(REMOVE_ALL, path);
    } else {
      removeProperty(session, path, NTF_SEND_TO_WEEKLY, message.getTo());
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
    boolean created =  NotificationSessionManager.createSystemProvider();
    SessionProvider sProvider =  NotificationSessionManager.getSessionProvider();
    
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
            LOG.debug("Remove NotificationMessage " + nodePath, e);
          }
        }
        session.save();
      }
      
      listPaths = removeByCallBack.get(REMOVE_DAILY);
      if (listPaths != null && listPaths.size() > 0) {
        for (String nodePath : listPaths) {
          try {
            Item item = session.getItem(nodePath);
            if (item.isNode()) {
              Node node = (Node) item;
              node.setProperty(NTF_SEND_TO_DAILY, new String[] { "" });
            }
            LOG.debug("Remove SendToDaily property " + nodePath);
          } catch (Exception e) {
            LOG.warn("Failed to remove SendToDaily property of " + nodePath + "\n" + e.getMessage());
            LOG.debug("Remove SendToDaily property " + nodePath, e);
          }
        }
        session.save();
      }
    } catch (Exception e) {
      LOG.warn("Failed to remove message after sent email notification", e);
    } finally {
      NotificationSessionManager.closeSessionProvider(created);
    }
  }

}

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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.json.JSONObject;
import org.picocontainer.Startable;

import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.service.QueueMessage;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.notification.NotificationConfiguration;
import org.exoplatform.commons.notification.NotificationContextFactory;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.impl.NotificationSessionManager;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.StringCommonUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.management.annotations.ManagedBy;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.MailService;

@ManagedBy(SendEmailService.class)
public class QueueMessageImpl extends AbstractService implements QueueMessage, Startable {
  private static final Log          LOG                 = ExoLogger.getExoLogger(QueueMessageImpl.class);

  private static final String       MAX_TO_SEND_SYS_KEY = "conf.notification.service.QueueMessage.numberOfMailPerBatch";

  private static final String       MAX_TO_SEND_KEY     = "numberOfMailPerBatch";

  private static final int          MAX_TO_SEND_DEFAULT  = 20;

  private boolean                   enabled             = true;

  private int                       maxToSend;

  /** .. */
  private MailService               mailService;

  private ListenerService           listenerService;

  /** .. */
  private NotificationConfiguration notificationConfiguration;

  /** The lock protecting all mutators */
  transient final ReentrantLock     lock                = new ReentrantLock();

  /** using the set to keep the messages. */
  private Set<MessageInfo>          messages            = Collections.synchronizedSet(new HashSet<MessageInfo>());

  /** .. */
  private ThreadLocal<Set<String>>  idsRemovingLocal    = new ThreadLocal<Set<String>>();

  public QueueMessageImpl(NotificationConfiguration notificationConfiguration,
                          MailService mailService,
                          ListenerService listenerService,
                          SettingService settingService,
                          InitParams params) {
    this.notificationConfiguration = notificationConfiguration;
    this.listenerService = listenerService;
    this.mailService = mailService;

    maxToSend = NotificationUtils.getSystemValue(params, MAX_TO_SEND_SYS_KEY, MAX_TO_SEND_KEY, MAX_TO_SEND_DEFAULT);
  }

  @Override
  public boolean put(MessageInfo message) throws Exception {
    final boolean stats = NotificationContextFactory.getInstance().getStatistics().isStatisticsEnabled();
    //
    if (message == null || message.getTo() == null || message.getTo().length() == 0) {
      return false;
    }
    //
    if (NotificationUtils.isValidEmailAddresses(message.getTo()) == false) {
      LOG.warn(String.format("The email %s is not valid for sending notification", message.getTo()));
      return false;
    }
    //
    if (stats) {
      LOG.info("Tenant Name:: " + CommonsUtils.getRepository().getConfiguration().getName());
      LOG.info("Message::From: " + message.getFrom() + " To: " + message.getTo() + " body: " + message.getBody());
    }
    saveMessageInfo(message);
    //
    listenerService.broadcast(new Event<QueueMessage, String>(MESSAGE_ADDED_IN_QUEUE, this, message.getId()));
    return true;
  }

  @Override
  public void send() throws Exception {
    final boolean stats = NotificationContextFactory.getInstance().getStatistics().isStatisticsEnabled();
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      //
      load(sProvider);
      if (idsRemovingLocal.get() == null) {
        idsRemovingLocal.set(new HashSet<String>());
      }
      //
      if (messages.size() > 0) {
        LOG.info(messages.size() + " message(s) will be sent.");
      }
      
      for (MessageInfo messageInfo : messages) {
        if (messageInfo != null && !idsRemovingLocal.get().contains(messageInfo.getId())
            && sendMessage(messageInfo)) {
          
          LOG.debug("Message sent to user: " + messageInfo.getTo());
          //
          idsRemovingLocal.get().add(messageInfo.getId());
          if (stats) {
            NotificationContextFactory.getInstance().getStatisticsCollector().pollQueue(messageInfo.getPluginId());
          }
        }
      }
    } catch (Exception e) {
      LOG.warn("Failed to send message.");
      LOG.debug(e.getMessage(), e);
    } finally {
      sProvider.close();
      removeMessageInfo();
    }
  }

  /**
   * Loading the messageInfo as buffer with Limit
   * and sinceTime
   * @param sProvider
   */
  private void load(SessionProvider sProvider) {
    try {
      NodeIterator iterator = getMessageInfoNodes(sProvider);
      while (iterator.hasNext()) {
        Node node = iterator.nextNode();
        MessageInfo messageInfo = getMessageInfo(node);
        messageInfo.setId(node.getUUID());
        messages.add(messageInfo);

      }
    } catch (Exception e) {
      LOG.warn("Failed to load message.");
      LOG.debug(e.getMessage(), e);
    }
  }

  private void saveMessageInfo(MessageInfo message) {
    final ReentrantLock lock = this.lock;
    lock.lock();
    
    boolean created =  NotificationSessionManager.createSystemProvider();
    SessionProvider sProvider =  NotificationSessionManager.getSessionProvider();
    try {
      message.setCreatedTime(System.currentTimeMillis());
      Node messageInfoHome = getMessageInfoHomeNode(sProvider, notificationConfiguration.getWorkspace());
      Node messageInfoNode = messageInfoHome.addNode(String.valueOf(message.getCreatedTime()), NTF_MESSAGE_INFO);
      if (messageInfoNode.canAddMixin("mix:referenceable")) {
        messageInfoNode.addMixin("mix:referenceable");
      }

      //
      saveData(messageInfoNode, StringCommonUtils.compress(message.toJSON()));
      sessionSave(messageInfoHome);

    } catch (Exception e) {
      LOG.warn("Failed to save message.");
      LOG.debug(e.getMessage() + message.toJSON(), e);
    } finally {
      NotificationSessionManager.closeSessionProvider(created);
      lock.unlock();
    }
  }

  private void removeMessageInfo() {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    final ReentrantLock lock = this.lock;
    List<String> ids = new ArrayList<String>(idsRemovingLocal.get()) ;
    try {
      lock.lock();
      Session session = getSession(sProvider, notificationConfiguration.getWorkspace());
      for (String messageId : ids) {
        session.getNodeByUUID(messageId).remove();
        //
        listenerService.broadcast(new Event<QueueMessage, String>(MESSAGE_DELETED_FROM_QUEUE, this, messageId));
        LOG.debug("Removing messageId: " + messageId);
      }
      session.save();
    } catch (Exception e) {
      LOG.warn("Failed to remove message.");
      LOG.debug(e.getMessage(), e);
    } finally {
      messages.clear();
      idsRemovingLocal.get().removeAll(ids);
      lock.unlock();
      sProvider.close();
    }
  }

  private NodeIterator getMessageInfoNodes(SessionProvider sProvider) {
    try {
      Node messageInfoHome = getMessageInfoHomeNode(sProvider, notificationConfiguration.getWorkspace());
      QueryManager qm = messageInfoHome.getSession().getWorkspace().getQueryManager();
      StringBuilder sqlQuery = new StringBuilder();
      sqlQuery.append("SELECT * FROM ").append(NTF_MESSAGE_INFO)
              .append(" WHERE jcr:path LIKE '").append(messageInfoHome.getPath()).append("/%' AND NOT jcr:path LIKE '")
              .append(messageInfoHome.getPath()).append("/%/%'")
              .append(" ORDER BY exo:name");
      QueryImpl query = (QueryImpl) qm.createQuery(sqlQuery.toString(), Query.SQL);
      query.setOffset(0);
      query.setLimit(maxToSend);
      QueryResult result = query.execute();
      return result.getNodes();
    } catch (Exception e) {
      LOG.warn("Failed to get message from node.");
      LOG.debug(e.getMessage(), e);
    }
    return null;
  }

  public MessageInfo getMessageInfo(Node messageInfoNode) {
    try {
      String messageJson = getDataJson(messageInfoNode);
      JSONObject object = new JSONObject(messageJson);
      MessageInfo info = new MessageInfo();
      info.pluginId(object.optString("pluginId"))
          .from(object.getString("from"))
          .to(object.getString("to"))
          .subject(object.getString("subject"))
          .body(object.getString("body"))
          .footer(object.optString("footer"))
          .setCreatedTime(object.getLong("createdTime"));
      //
      return info;
    } catch (Exception e) {
      LOG.warn("Failed to map message between node and model.");
      LOG.debug(e.getMessage(), e);
    }
    return null;
  }

  @Override
  public boolean sendMessage(MessageInfo message) throws Exception {
    if (message == null) {
      throw new IllegalArgumentException("Message is null");
    }
    if (this.enabled) {
      try {
        //ensure the message is valid
        if (message.getFrom() == null) {
          return false;
        }
        mailService.sendMessage(message.makeEmailNotification());
        return true;
      } catch (Exception e) {
        LOG.error("Error while sending a message - Cause : " + e.getMessage(), e);
        return false;
      }
    }
    //
    listenerService.broadcast(new Event<QueueMessage, String>(MESSAGE_SENT_FROM_QUEUE, this, message.getId()));
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void enable(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() {
  }

  private void saveData(Node node, InputStream is) throws Exception {
    Node fileNode = node.addNode("datajson", "nt:file");
    Node nodeContent = fileNode.addNode("jcr:content", "nt:resource");
    //
    nodeContent.setProperty("jcr:mimeType", "application/x-gzip");
    nodeContent.setProperty("jcr:data", is);
    nodeContent.setProperty("jcr:lastModified", Calendar.getInstance().getTimeInMillis());
  }

  private String getDataJson(Node node) throws Exception {
    Node fileNode = node.getNode("datajson");
    Node nodeContent = fileNode.getNode("jcr:content");
    InputStream stream = nodeContent.getProperty("jcr:data").getStream();
    return StringCommonUtils.decompress(stream);
  }



  public void removeAll() {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    int t = 0, j = 0;
    String pli="";
    try {
      Session session = getSession(sProvider, notificationConfiguration.getWorkspace());
      Node root = session.getRootNode();
      //
      LOG.debug("Removing messages: ");
      if (root.hasNode("eXoNotification/messageInfoHome")) {
        NodeIterator it = root.getNode("eXoNotification/messageInfoHome").getNodes();
        //
        removeNodes(session, it);
      }
      LOG.debug("Done to removed messages! ");
      //
      LOG.debug("Removing notification info... ");
      NodeIterator it = root.getNode("eXoNotification/messageHome").getNodes();
      List<String> pluginPaths = new ArrayList<String>();
      while (it.hasNext()) {
        pluginPaths.add(it.nextNode().getPath());
      }
      session.logout();
      for (String string : pluginPaths) {
        pli = string;
        LOG.debug("Remove notification info on plugin: " + pli);
        //
        session = getSession(sProvider, notificationConfiguration.getWorkspace());
        it = ((Node) session.getItem(string)).getNodes();
        while (it.hasNext()) {
          NodeIterator hIter = it.nextNode().getNodes();
          j = removeNodes(session, hIter);
          t += j;
        }
        LOG.debug("Removed " + j + " nodes info on plugin: " + pli);
        session.logout();
      }

    } catch (Exception e) {
      LOG.debug("Removed " + j + " nodes info on plugin: " + pli);
      LOG.debug("Removed all " + t + " nodes.");
      LOG.debug("Failed to remove all data of feature notification." + e.getMessage());
    } finally {
      sProvider.close();
    }
  }
  
  private int removeNodes(Session session, NodeIterator it) throws Exception {
    int i = 0, size = Integer.valueOf(System.getProperty("sizePersiter", "200"));
    LOG.debug("Starting to remove nodes...");
    while (it.hasNext()) {
      it.nextNode().remove();
      ++i;
      if (i % size == 0) {
        session.save();
      }
    }
    session.save();
    LOG.debug(String.format("Done to removed %s nodes", i));
    return i;
  }
}

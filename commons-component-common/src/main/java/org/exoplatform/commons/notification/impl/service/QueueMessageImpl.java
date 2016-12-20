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
import java.util.GregorianCalendar;
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

import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.service.QueueMessage;
import org.exoplatform.commons.notification.NotificationConfiguration;
import org.exoplatform.commons.notification.NotificationContextFactory;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.impl.NotificationSessionManager;
import org.exoplatform.commons.notification.job.SendEmailNotificationJob;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.StringCommonUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.management.annotations.ManagedBy;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.PeriodInfo;
import org.json.JSONObject;
import org.picocontainer.Startable;
import org.quartz.JobDataMap;

@ManagedBy(SendEmailService.class)
public class QueueMessageImpl extends AbstractService implements QueueMessage, Startable {
  private static final Log               LOG                   = ExoLogger.getExoLogger(QueueMessageImpl.class);

  private static final String            MAX_TO_SEND_SYS_KEY   = "conf.notification.service.QueueMessage.numberOfMailPerBatch";
  private static final String            MAX_TO_SEND_KEY       = "numberOfMailPerBatch";
  private static final String            DELAY_TIME_SYS_KEY    = "conf.notification.service.QueueMessage.period";
  private static final String            DELAY_TIME_KEY        = "period";
  private static final String            CACHE_REPO_NAME       = "repositoryName";
  private static int                     LIMIT                 = 20;

  private int                            MAX_TO_SEND;
  private long                           DELAY_TIME;
  /** .. */
  private SendEmailService               sendEmailService;
  /** .. */
  private MailService                    mailService;
  /** .. */
  private NotificationConfiguration      configuration;
  /** The lock protecting all mutators */
  transient final ReentrantLock lock = new ReentrantLock();
  /** using the set to keep the messages. */
  private Set<MessageInfo> messages = Collections.synchronizedSet(new HashSet<MessageInfo>());
  /** .. */
  private ThreadLocal<Set<String>> idsRemovingLocal = new ThreadLocal<Set<String>>();
  
  public QueueMessageImpl(InitParams params) {
    this.configuration = CommonsUtils.getService(NotificationConfiguration.class);
    this.mailService = CommonsUtils.getService(MailService.class);

    MAX_TO_SEND = NotificationUtils.getSystemValue(params, MAX_TO_SEND_SYS_KEY, MAX_TO_SEND_KEY, 20);
    DELAY_TIME = NotificationUtils.getSystemValue(params, DELAY_TIME_SYS_KEY, DELAY_TIME_KEY, 120) * 1000;
  }
  
  public void setManagementView(SendEmailService managementView) {
    this.sendEmailService = managementView;
  }

  public void makeJob(int limit, long interval) {
    if (interval > 0) {
      LIMIT = limit;
      //
      JobSchedulerService schedulerService = CommonsUtils.getService(JobSchedulerService.class);
      Calendar cal = new GregorianCalendar();
      //
      try {
        PeriodInfo periodInfo = new PeriodInfo(cal.getTime(), null, -1, interval);
        JobInfo info = new JobInfo("SendEmailNotificationJob", "Notification", SendEmailNotificationJob.class);
        info.setDescription("Send email notification job.");
        //
        schedulerService.removeJob(info);

        JobDataMap jdatamap = new JobDataMap();
        jdatamap.put(CACHE_REPO_NAME, CommonsUtils.getRepository().getConfiguration().getName());
        //
        schedulerService.addPeriodJob(info, periodInfo, jdatamap);
        LOG.debug("Job executes interval: " + interval);
      } catch (Exception e) {
        LOG.warn("Failed at makeJob().");
        LOG.debug(e.getMessage(), e);
      }
    }
  }

  public void resetDefaultConfigJob() {
    makeJob(MAX_TO_SEND, DELAY_TIME);
  }

  @Override
  public void start() {
    
    //
    resetDefaultConfigJob();
    //
    sendEmailService.registerManager(this);
  }

  @Override
  public void stop() {
  }

  @Override
  public boolean put(MessageInfo message) {
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
    sendEmailService.addCurrentCapacity();
    return true;
  }

  @Override
  public void send() {
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
            && sendMessage(messageInfo.makeEmailNotification())) {
          
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
      Node messageInfoHome = getMessageInfoHomeNode(sProvider, configuration.getWorkspace());
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
      Session session = getSession(sProvider, configuration.getWorkspace());
      for (String messageId : ids) {
        session.getNodeByUUID(messageId).remove();
        //
        sendEmailService.removeCurrentCapacity();
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
      Node messageInfoHome = getMessageInfoHomeNode(sProvider, configuration.getWorkspace());
      QueryManager qm = messageInfoHome.getSession().getWorkspace().getQueryManager();
      StringBuilder sqlQuery = new StringBuilder();
      sqlQuery.append("SELECT * FROM ").append(NTF_MESSAGE_INFO)
              .append(" WHERE jcr:path LIKE '").append(messageInfoHome.getPath()).append("/%' AND NOT jcr:path LIKE '")
              .append(messageInfoHome.getPath()).append("/%/%'")
              .append(" ORDER BY exo:name");
      QueryImpl query = (QueryImpl) qm.createQuery(sqlQuery.toString(), Query.SQL);
      query.setOffset(0);
      query.setLimit(LIMIT);
      QueryResult result = query.execute();
      return result.getNodes();
    } catch (Exception e) {
      LOG.warn("Failed to get message from node.");
      LOG.debug(e.getMessage(), e);
    }
    return null;
  }

  private MessageInfo getMessageInfo(Node messageInfoNode) {
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

  public boolean sendMessage(Message message) {
    if (sendEmailService.isOn() == false) {
      try {
        //ensure the message is valid
        if (message.getFrom() == null) {
          return false;
        }
        mailService.sendMessage(message);
        return true;
      } catch (Exception e) {
        LOG.error("Error while sending a message - Cause : " + e.getMessage(), e);
        return false;
      }
    } else {
      sendEmailService.counter();
    }
    // if service is off, removed message.
    return true;
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



  public String removeAll() {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    int t = 0, j = 0;
    String pli="";
    try {
      Session session = getSession(sProvider, configuration.getWorkspace());
      Node root = session.getRootNode();
      //
      LOG.trace("Removing messages: ");
      if (root.hasNode("eXoNotification/messageInfoHome")) {
        NodeIterator it = root.getNode("eXoNotification/messageInfoHome").getNodes();
        //
        removeNodes(session, it);
      }
      LOG.trace("Done to removed messages! ");
      //
      LOG.trace("Removing notification info... ");
      NodeIterator it = root.getNode("eXoNotification/messageHome").getNodes();
      List<String> pluginPaths = new ArrayList<String>();
      while (it.hasNext()) {
        pluginPaths.add(it.nextNode().getPath());
      }
      session.logout();
      for (String string : pluginPaths) {
        pli = string;
        LOG.trace("Remove notification info on plugin: " + pli);
        //
        session = getSession(sProvider, configuration.getWorkspace());
        it = ((Node) session.getItem(string)).getNodes();
        while (it.hasNext()) {
          NodeIterator hIter = it.nextNode().getNodes();
          j = removeNodes(session, hIter);
          t += j;
        }
        LOG.trace("Removed " + j + " nodes info on plugin: " + pli);
        session.logout();
      }

      return "Done to removed " + t + " nodes!";
    } catch (Exception e) {
      LOG.trace("Removed " + j + " nodes info on plugin: " + pli);
      LOG.trace("Removed all " + t + " nodes.");
      LOG.debug("Failed to remove all data of feature notification." + e.getMessage());
    } finally {
      sProvider.close();
    }
    return "Failed to remove all. Please, try again !";
  }
  
  private int removeNodes(Session session, NodeIterator it) throws Exception {
    int i = 0, size = Integer.valueOf(System.getProperty("sizePersiter", "200"));
    LOG.trace("Starting to remove nodes...");
    while (it.hasNext()) {
      it.nextNode().remove();
      ++i;
      if (i % size == 0) {
        session.save();
      }
    }
    session.save();
    LOG.trace(String.format("Done to removed %s nodes", i));
    return i;
  }
  
  public String removeUsersSetting() {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    int t = 0;
    try {
      Session session = getSession(sProvider, configuration.getWorkspace());
      Node root = session.getRootNode();
      LOG.trace("Removing all user settings: ");
      if (root.hasNode("settings/user")) {
        NodeIterator it = root.getNode("settings/user").getNodes();
        //
        t = removeNodes(session, it);
      }
      return "Done to removed " + t + " users!";
    } catch (Exception e) {
      LOG.trace("Removed all " + t + " nodes.");
      LOG.debug("Failed to remove all data of feature notification." + e.getMessage());
    } finally {
      sProvider.close();
    }
    return "Failed to remove all. Please, try again !";
  }
}

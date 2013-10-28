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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.service.QueueMessage;
import org.exoplatform.commons.notification.NotificationConfiguration;
import org.exoplatform.commons.notification.NotificationContextFactory;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.job.SendEmailNotificationJob;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.management.annotations.ManagedBy;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
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
  private static final int               BUFFER_SIZE           = 32;

  private int                            MAX_TO_SEND;
  private long                           DELAY_TIME;
  
  ScheduledFuture<?>                      future                = null;

  private SendEmailService               sendEmailService;
  private MailService                    mailService;
  private NotificationConfiguration      configuration;
  
  public QueueMessageImpl(InitParams params) {
    this.configuration = CommonsUtils.getService(NotificationConfiguration.class);
    this.mailService = CommonsUtils.getService(MailService.class);

    MAX_TO_SEND = NotificationUtils.getSystemValue(params, MAX_TO_SEND_SYS_KEY, MAX_TO_SEND_KEY, 50);
    DELAY_TIME = NotificationUtils.getSystemValue(params, DELAY_TIME_SYS_KEY, DELAY_TIME_KEY, 120) * 1000;
  }
  
  public void setManagementView(SendEmailService managementView) {
    this.sendEmailService = managementView;
  }

  public void makeJob(long interval) {
    LOG.info("makeJob: " + interval);
    if (interval > 0) {
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
      } catch (Exception e) {
        LOG.warn("Failed to add send email notification jobs ", e);
      }
    }
    if (Long.valueOf(DELAY_TIME) != interval) {
      MAX_TO_SEND = 1;
    }
  }

  @Override
  public void start() {
    //
    makeJob(DELAY_TIME);
    //
    sendEmailService.registerManager(this);
  }

  @Override
  public void stop() {
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
    //
    saveMessageInfo(message);
    //
    sendEmailService.addCurrentCapacity();
    return true;
  }

  @Override
  public void send() {
    final boolean stats = NotificationContextFactory.getInstance().getStatistics().isStatisticsEnabled();
    NodeIterator iterator = getMessageInfoNodes();
    long size = 0;
    List<String> msgInfoRemove = new ArrayList<String>();
    if (iterator != null && (size = iterator.getSize()) > 0) {

      for (int i = 0; i < MAX_TO_SEND && i < size; i++) {
        MessageInfo messageInfo = getMessageInfo(iterator.nextNode());
        if (messageInfo != null && sendMessage(messageInfo.makeEmailNotification()) == true) {
          msgInfoRemove.add(messageInfo.getId());
          if (stats) {
            NotificationContextFactory.getInstance().getStatisticsCollector().pollQueue(messageInfo.getPluginId());
          }
        }
      }
      //
      for (String messageId : msgInfoRemove) {
        removeMessageInfo(messageId);
        //
        sendEmailService.removeCurrentCapacity();
      }
    }
  }

  private void saveMessageInfo(MessageInfo message) {
    SessionProvider sProvider = getSystemProvider();
    try {
      Node messageInfoHome = getMessageInfoHomeNode(sProvider, configuration.getWorkspace());
      Node messageInfoNode = messageInfoHome.addNode(message.getId(), NTF_MESSAGE_INFO);
      //
      saveData(messageInfoNode, compress(message.toJSON()));

      sessionSave(messageInfoHome);
    } catch (Exception e) {
      LOG.warn("Failed to storage MessageInfo: " + message.toJSON(), e );
    }
  }

  private void removeMessageInfo(String messageId) {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      Node messageInfoHome = getMessageInfoHomeNode(sProvider, configuration.getWorkspace());
      messageInfoHome.getNode(messageId).remove();
      sessionSave(messageInfoHome);
      LOG.debug("remove MessageInfo " + messageId);
    } catch (Exception e) {
      LOG.error("Failed to remove MessageInfo " + messageId, e);
    } finally {
      sProvider.close();
    }
  }

  private NodeIterator getMessageInfoNodes() {
    SessionProvider sProvider = getSystemProvider();
    try {
      Node messageInfoHome = getMessageInfoHomeNode(sProvider, configuration.getWorkspace());
      return messageInfoHome.getNodes();
    } catch (Exception e) {
      LOG.error("Failed to getMessageInfos", e);
    }
    return null;
  }

  private MessageInfo getMessageInfo(Node messageInfoNode) {
    try {
      String messageJson = getDataJson(messageInfoNode);
      JSONObject object = new JSONObject(messageJson);
      MessageInfo info = new MessageInfo();
      info.setId(object.getString("id"))
          .pluginId(object.optString("pluginId"))
          .from(object.getString("from"))
          .to(object.getString("to"))
          .subject(object.getString("subject"))
          .body(object.getString("body"))
          .footer(object.optString("footer"));
      //
      return info;
    } catch (Exception e) {
      LOG.warn("Failed to set back MessageInfo: ", e);
    }
    return null;
  }

  private boolean sendMessage(Message message) {
    if (sendEmailService.isOn()) {
      try {
        mailService.sendMessage(message);
        sendEmailService.counter();
        return true;
      } catch (Exception e) {
        LOG.error("Failed to send notification.", e);
        return false;
      }
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
    return decompress(stream);
  }

  public static InputStream compress(String string) throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
    GZIPOutputStream gos = new GZIPOutputStream(os);
    gos.write(string.getBytes());
    gos.close();
    byte[] compressed = os.toByteArray();
    os.close();
    return new ByteArrayInputStream(compressed);
  }

  public static String decompress(InputStream is) throws IOException {
    GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
    StringBuilder string = new StringBuilder();
    byte[] data = new byte[BUFFER_SIZE];
    int bytesRead;
    while ((bytesRead = gis.read(data)) != -1) {
      string.append(new String(data, 0, bytesRead));
    }
    gis.close();
    is.close();
    return string.toString();
  }

}

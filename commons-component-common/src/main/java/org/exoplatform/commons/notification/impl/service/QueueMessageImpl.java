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
import java.util.Calendar;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.service.QueueMessage;
import org.exoplatform.commons.notification.NotificationConfiguration;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.MailService;
import org.json.JSONObject;
import org.picocontainer.Startable;

public class QueueMessageImpl extends AbstractService implements QueueMessage, Startable {
  private static final Log               LOG                   = ExoLogger.getExoLogger(QueueMessageImpl.class);

  private static final String            MAX_TO_SEND_SYS_KEY   = "conf.notification.service.QueueMessage.numberOfMailPerBatch";
  private static final String            MAX_TO_SEND_KEY       = "numberOfMailPerBatch";
  private static final String            DELAY_TIME_SYS_KEY    = "conf.notification.service.QueueMessage.period";
  private static final String            DELAY_TIME_KEY        = "period";
  private static final String            INITIAL_DELAY_SYS_KEY = "conf.notification.service.QueueMessage.initialDelay";
  private static final String            INITIAL_DELAY_KEY     = "initialDelay";
  private static final int               BUFFER_SIZE           = 32;

  private int                            MAX_TO_SEND;
  private int                            DELAY_TIME;
  private int                            INITIAL_DELAY;

  private MailService                    mailService;
  private NotificationConfiguration      configuration;

  private final Queue<MessageInfo> messageQueue = new ConcurrentLinkedQueue<MessageInfo>();
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  
  public QueueMessageImpl(InitParams params) {
    this.mailService = CommonsUtils.getService(MailService.class);
    this.configuration = CommonsUtils.getService(NotificationConfiguration.class);

    MAX_TO_SEND = NotificationUtils.getSystemValue(params, MAX_TO_SEND_SYS_KEY, MAX_TO_SEND_KEY, 50);
    DELAY_TIME = NotificationUtils.getSystemValue(params, DELAY_TIME_SYS_KEY, DELAY_TIME_KEY, 120);
    INITIAL_DELAY = NotificationUtils.getSystemValue(params, INITIAL_DELAY_SYS_KEY, INITIAL_DELAY_KEY, 60);

    scheduler.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        send();
      }
    }, INITIAL_DELAY, DELAY_TIME, TimeUnit.SECONDS);

  }

  @Override
  public void start() {
    //
    setBackMessageInfos();
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
    messageQueue.add(message);
    //
    saveMessageInfo(message);
    return true;
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

  private void setBackMessageInfos() {
    SessionProvider sProvider = getSystemProvider();
    try {
      Node messageInfoHome = getMessageInfoHomeNode(sProvider, configuration.getWorkspace());
      NodeIterator iter = messageInfoHome.getNodes();
      MessageInfo info;
      while (iter.hasNext()) {

        Node messageInfoNode = iter.nextNode();
        try {
          String messageJson = getDataJson(messageInfoNode);
          JSONObject object = new JSONObject(messageJson);
          info = new MessageInfo();
          info.setId(object.getString("id"))
              .pluginId(object.optString("pluginId"))
              .from(object.getString("from"))
              .to(object.getString("to"))
              .subject(object.getString("subject"))
              .body(object.getString("body"))
              .footer(object.optString("footer"));
          //
          messageQueue.add(info);
          LOG.debug("Set back MessageInfo after stop server " + info.getId());
        } catch (Exception e) {
          LOG.warn("Failed to set back MessageInfo " + messageInfoNode.getName(), e);
        }
      }
    } catch (Exception e) {
      LOG.warn("Failed to set back MessageInfo from database ", e);
    }
  }

  private void removeMessageInfo(MessageInfo message) {
    SessionProvider sProvider = getSystemProvider();
    try {
      Node messageInfoHome = getMessageInfoHomeNode(sProvider, configuration.getWorkspace());
      messageInfoHome.getNode(message.getId()).remove();
      sessionSave(messageInfoHome);
      LOG.debug("remove MessageInfo " + message.getId());
    } catch (Exception e) {
      LOG.error("Failed to remove MessageInfo " + message.getId(), e);
    }
  }

  @Override
  public void send() {

    for (int i = 0; i < MAX_TO_SEND; i++) {
      if (messageQueue.isEmpty() == false) {
        try {
          MessageInfo messageInfo = messageQueue.poll();
          mailService.sendMessage(messageInfo.makeEmailNotification());
          LOG.debug("\nSent notification to user " + messageInfo.getTo());
          removeMessageInfo(messageInfo);
        } catch (Exception e) {
          LOG.error("Failed to send notification.", e);
        }
      } else {
        break;
      }
    }
  }

  private void saveData(Node node, InputStream is) throws Exception {
    Node fileNode = node.addNode("datajson", "nt:file");
    Node nodeContent = fileNode.addNode("jcr:content", "nt:resource");
    //
    nodeContent.setProperty("jcr:mimeType", "application/gzip");
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
    ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
    gos.close();
    os.close();
    return is;
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

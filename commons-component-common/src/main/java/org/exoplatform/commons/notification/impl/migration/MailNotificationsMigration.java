package org.exoplatform.commons.notification.impl.migration;

import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.cluster.StartableClusterAware;
import org.exoplatform.commons.notification.impl.jpa.email.JPANotificationDataStorage;
import org.exoplatform.commons.notification.impl.jpa.email.JPAQueueMessageImpl;
import org.exoplatform.commons.notification.impl.service.storage.NotificationDataStorageImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.RDBMSMigrationUtils;
import org.exoplatform.commons.utils.StringCommonUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.json.JSONObject;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletContext;
import java.io.InputStream;
import java.util.concurrent.Callable;

/**
 * Created by exo on 4/21/17.
 */
public class MailNotificationsMigration implements StartableClusterAware {

  private static final Log LOG = ExoLogger.getLogger(MailNotificationsMigration.class);

  //JPA Storage
  private JPANotificationDataStorage jpaNotificationDataStorage;
  private JPAQueueMessageImpl jpaQueueMessage;

  //JCR storage
  private NotificationDataStorageImpl jcrNotificationDataStorage;

  private Session session;
  private NodeHierarchyCreator nodeHierarchyCreator;
  private JobSchedulerService schedulerService;

  private static Boolean isMailNotifsMigrated = false;

  public MailNotificationsMigration( NotificationDataStorageImpl jcrNotificationDataStorage, JPANotificationDataStorage jpaNotificationDataStorage,
                                     JPAQueueMessageImpl jpaQueueMessage) {
    this.jpaNotificationDataStorage = jpaNotificationDataStorage;
    this.jpaQueueMessage = jpaQueueMessage;
    this.jcrNotificationDataStorage = jcrNotificationDataStorage;

    schedulerService = CommonsUtils.getService(JobSchedulerService.class);
  }

  @Override
  public void start() {
    PortalContainer container = PortalContainer.getInstance();
    nodeHierarchyCreator = (NodeHierarchyCreator) container.getComponentInstanceOfType(NodeHierarchyCreator.class);
    RepositoryService repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    try {
      session = repositoryService.getRepository("repository").getSystemSession("portal-system");
    } catch (Exception e) {
      LOG.error("Error while getting Notification nodes for Notifications migration - Cause : " + e.getMessage(), e);
      return;
    }
    if (!hasMailNotifDataToMigrate()) {
      LOG.info("No mail notification data to migrate from JCR to RDBMS");
      return;
    }
    //migration of mail notifications data from JCR to RDBMS is done as a background task
    PortalContainer.addInitTask(PortalContainer.getInstance().getPortalContext(), new RootContainer.PortalContainerPostInitTask() {
      @Override
      public void execute(ServletContext context, PortalContainer portalContainer) {
        RDBMSMigrationUtils.getExecutorService().submit(new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            try {
              // pause job of sending digest mails
              schedulerService.pauseJob("NotificationDailyJob", "Notification");
              schedulerService.pauseJob("NotificationWeeklyJob", "Notification");
              ExoContainerContext.setCurrentContainer(PortalContainer.getInstance());
              LOG.info("=== Start migration of Mail Notifications data from JCR");
              long startTime = System.currentTimeMillis();
              migrateMailNotifData();
              isMailNotifsMigrated = true;
              long endTime = System.currentTimeMillis();
              LOG.info("=== Migration of Mail Notification data done in " + (endTime - startTime) + " ms");
            } catch (Exception e) {
              LOG.error("Error while migrating Mail Notification data from JCR to RDBMS - Cause : " + e.getMessage(), e);
              isMailNotifsMigrated = false;
            }
            try {
              if (isMailNotifsMigrated) {
                LOG.info("=== Start cleaning Mail notifications data from JCR");
                long startTime = System.currentTimeMillis();
                deleteJcrMailNotifications();
                long endTime = System.currentTimeMillis();
                LOG.info("=== Mail notifications JCR data cleaning due to RDBMS migration done in " + (endTime - startTime) + " ms");
              }
            } catch (Exception e) {
              LOG.error("Error while cleaning Mail notifications JCR data to RDBMS - Cause : " + e.getMessage(), e);
            } finally {
              schedulerService.resumeJob("NotificationDailyJob", "Notification");
              schedulerService.resumeJob("NotificationWeeklyJob", "Notification");
            }
            return null;
          }
        });
      }
    });

    if (!hasQueueMessagesDataToMigrate()) {
      LOG.info("No queue messages data to migrate from JCR to RDBMS");
      return;
    }
    //migration of queue messages data from JCR to RDBMS is done as a background task
    PortalContainer.addInitTask(PortalContainer.getInstance().getPortalContext(), new RootContainer.PortalContainerPostInitTask() {
      @Override
      public void execute(ServletContext context, PortalContainer portalContainer) {
        RDBMSMigrationUtils.getExecutorService().submit(new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            try {
              ExoContainerContext.setCurrentContainer(PortalContainer.getInstance());
              LOG.info("=== Start migration of Mail messages stored in the queue from JCR");
              long startTime = System.currentTimeMillis();
              migrateQueueMessages();
              long endTime = System.currentTimeMillis();
              LOG.info("=== Migration of Mail messages data done in " + (endTime - startTime) + " ms");
            } catch (Exception e) {
              LOG.error("Error while migrating Mail messages data from JCR to RDBMS - Cause : " + e.getMessage(), e);
            }
            try {
              LOG.info("=== Start cleaning Mail messages data from JCR");
              long startTime = System.currentTimeMillis();
              deleteJcrMailMessages();
              long endTime = System.currentTimeMillis();
              LOG.info("=== Mail messages JCR data cleaning due to RDBMS migration done in " + (endTime - startTime) + " ms");

            } catch (Exception e) {
              LOG.error("Error while cleaning Mail messages JCR data to RDBMS - Cause : " + e.getMessage(), e);
            }
            return null;
          }
        });
      }
    });
  }

  private void deleteJcrMailMessages() throws RepositoryException {
    ((Node)session.getItem(nodeHierarchyCreator.getJcrPath("eXoNotification"))).getNode("messageInfoHome").remove();
    session.save();
  }

  private void deleteJcrMailNotifications() throws RepositoryException {
    NodeIterator pluginNodesIterator = getMailNotificationNodes();
    while (pluginNodesIterator.hasNext()) {
      Node pluginNode = pluginNodesIterator.nextNode();
      NodeIterator dayNodesIterator = pluginNode.getNodes();
      while (dayNodesIterator.hasNext()) {
        Node dayNode = dayNodesIterator.nextNode();
        NodeIterator notifNodes = dayNode.getNodes();
        LOG.info("    Removing JCR mail notifications for plugin: " + pluginNode.getName() + " - day: " + dayNode.getName());
        int i = 0;
        while (notifNodes.hasNext()) {
          i++;
          notifNodes.nextNode().remove();
          if(i%100 == 0){
            session.save();
          }
        }
        if (i > 0) {
          session.save();
          LOG.info("=== done removed " + i + " mail notifications from JCR for plugin: " + pluginNode.getName());
        }
      }
      pluginNode.remove();
      session.save();
    }
    ((Node)session.getItem(nodeHierarchyCreator.getJcrPath("eXoNotification"))).getNode("messageHome").remove();
    session.save();
  }

  private void migrateQueueMessages() throws RepositoryException, RepositoryConfigurationException {
    NodeIterator iterator = getMessageInfoNodes();
    while (iterator.hasNext()) {
      Node node = iterator.nextNode();
      jpaQueueMessage.put(getMessageInfo(node));
    }
  }

  private MessageInfo getMessageInfo(Node node) {
    try {
      String messageJson = getDataJson(node);
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

  private String getDataJson(Node node) throws Exception {
    Node fileNode = node.getNode("datajson");
    Node nodeContent = fileNode.getNode("jcr:content");
    InputStream stream = nodeContent.getProperty("jcr:data").getStream();
    return StringCommonUtils.decompress(stream);
  }

  private NodeIterator getMessageInfoNodes() {
    try {
      return ((Node) session.getItem(nodeHierarchyCreator.getJcrPath("eXoNotification"))).getNode("messageInfoHome").getNodes();
    } catch (Exception e) {
      LOG.error("Error while getting MessageInfo nodes - Cause : " + e.getMessage(), e);
      return null;
    }
  }

  private NodeIterator getMailNotificationNodes() {
    try {
      return ((Node)session.getItem(nodeHierarchyCreator.getJcrPath("eXoNotification"))).getNode("messageHome").getNodes();
    } catch (Exception e) {
      LOG.error("Error while getting Mail notification nodes - Cause : " + e.getMessage(), e);
      return null;
    }
  }

  private void migrateMailNotifData() throws RepositoryException, RepositoryConfigurationException {
    NodeIterator pluginNodesIterator = getMailNotificationNodes();
    while (pluginNodesIterator.hasNext()) {
      Node pluginNode = pluginNodesIterator.nextNode();
      NodeIterator dayNodesIterator = pluginNode.getNodes();
      while (dayNodesIterator.hasNext()) {
        Node dayNode = dayNodesIterator.nextNode();
        NodeIterator notifNodes = dayNode.getNodes();
        LOG.info("    Progression mail notifications migration for plugin: " + pluginNode.getName() + " - day: " + dayNode.getName());
        while (notifNodes.hasNext()) {
          migrateMailNotifNodeToRDBMS(notifNodes.nextNode());
        }
      }
    }
  }

  private void migrateMailNotifNodeToRDBMS(Node node) {
    try {
      jpaNotificationDataStorage.save(jcrNotificationDataStorage.fillModel(node));
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }

  private boolean hasMailNotifDataToMigrate() {
    return (getMailNotificationNodes() != null && getMailNotificationNodes().hasNext());
  }

  private boolean hasQueueMessagesDataToMigrate() {
    return (getMessageInfoNodes() != null && getMessageInfoNodes().hasNext());
  }

  @Override
  public boolean isDone() {
    return false;
  }
}

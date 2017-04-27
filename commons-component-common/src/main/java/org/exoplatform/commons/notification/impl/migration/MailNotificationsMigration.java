package org.exoplatform.commons.notification.impl.migration;

import org.exoplatform.commons.api.notification.service.QueueMessage;
import org.exoplatform.commons.cluster.StartableClusterAware;
import org.exoplatform.commons.notification.impl.jpa.email.JPANotificationDataStorage;
import org.exoplatform.commons.notification.impl.jpa.email.JPAQueueMessageImpl;
import org.exoplatform.commons.notification.impl.service.QueueMessageImpl;
import org.exoplatform.commons.notification.impl.service.storage.NotificationDataStorageImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.jgroups.util.DefaultThreadFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
  private QueueMessage jcrQueueMessage;

  private ExecutorService executorServiceMail;
  private ExecutorService executorServiceQueue;
  private Session session;
  private NodeHierarchyCreator nodeHierarchyCreator;
  private JobSchedulerService schedulerService;

  private static Boolean isMailNotifsMigrated = false;

  public MailNotificationsMigration( NotificationDataStorageImpl jcrNotificationDataStorage, JPANotificationDataStorage jpaNotificationDataStorage,
                                     JPAQueueMessageImpl jpaQueueMessage, QueueMessage jcrQueueMessage) {
    this.jpaNotificationDataStorage = jpaNotificationDataStorage;
    this.jpaQueueMessage = jpaQueueMessage;
    this.jcrNotificationDataStorage = jcrNotificationDataStorage;
    this.jcrQueueMessage = jcrQueueMessage;

    schedulerService = CommonsUtils.getService(JobSchedulerService.class);

    this.executorServiceMail = Executors.newSingleThreadExecutor(new DefaultThreadFactory("MAIL-NOTIFICATIONS-MIGRATION-RDBMS", false, false));
    this.executorServiceQueue = Executors.newSingleThreadExecutor(new DefaultThreadFactory("QUEUE-MESSAGES-MIGRATION-RDBMS", false, false));
  }

  public ExecutorService getExecutorServiceMail() {
    return executorServiceMail;
  }

  public void setExecutorServiceMail(ExecutorService executorServiceMail) {
    this.executorServiceMail = executorServiceMail;
  }

  public ExecutorService getExecutorServiceQueue() {
    return executorServiceQueue;
  }

  public void setExecutorServiceQueue(ExecutorService executorServiceQueue) {
    this.executorServiceQueue = executorServiceQueue;
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
    getExecutorServiceMail().submit(new Callable<Void>() {
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
          schedulerService.resumeJob("NotificationDailyJob", "Notification");
          schedulerService.resumeJob("NotificationWeeklyJob", "Notification");
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
        }
        return null;
      }
    });

    if (!hasQueueMessagesDataToMigrate()) {
      LOG.info("No queue messages data to migrate from JCR to RDBMS");
      return;
    }
    //migration of queue messages data from JCR to RDBMS is done as a background task
    getExecutorServiceQueue().submit(new Callable<Void>() {
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

  private void deleteJcrMailMessages() throws RepositoryException {
    ((Node)session.getItem(nodeHierarchyCreator.getJcrPath("eXoNotification"))).getNode("messageInfoHome").remove();
    session.save();
  }

  private void deleteJcrMailNotifications() throws RepositoryException {
    ((Node)session.getItem(nodeHierarchyCreator.getJcrPath("eXoNotification"))).getNode("messageHome").remove();
    session.save();
  }

  private void migrateQueueMessages() throws RepositoryException, RepositoryConfigurationException {
    NodeIterator iterator = getMessageInfoNodes();
    while (iterator.hasNext()) {
      Node node = iterator.nextNode();
      jpaQueueMessage.put(((QueueMessageImpl)jcrQueueMessage).getMessageInfo(node));
    }
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

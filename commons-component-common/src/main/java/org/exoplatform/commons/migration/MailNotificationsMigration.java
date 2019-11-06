package org.exoplatform.commons.migration;

import java.io.InputStream;
import java.util.concurrent.Callable;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.notification.NotificationConfiguration;
import org.exoplatform.commons.notification.impl.jpa.email.JPAMailNotificationStorage;
import org.exoplatform.commons.notification.impl.jpa.email.JPAQueueMessageImpl;
import org.exoplatform.commons.notification.impl.service.storage.MailNotificationStorageImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.RDBMSMigrationUtils;
import org.exoplatform.commons.utils.StringCommonUtils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.transaction.TransactionService;

public class MailNotificationsMigration {
  //scope of mail notification migration status
  public static final String MAIL_NOTIFICATION_MIGRATION_DONE_KEY = "MAIL_NOTIFICATION_MIGRATION_DONE";
  //status of mail notifications migration (true if migration completed successfully)
  public static final String MAIL_NOTIFICATION_RDBMS_MIGRATION_DONE = "MAIL_NOTIFICATION_RDBMS_MIGRATION_DONE";
  //status of mail notifications cleanup from JCR (true if cleanup is completed successfully)
  public static final String MAIL_NOTIFICATION_RDBMS_CLEANUP_DONE = "MAIL_NOTIFICATION_RDBMS_CLEANUP_DONE";

  private static final Log LOG = ExoLogger.getLogger(MailNotificationsMigration.class);

  //JPA Storage
  private JPAMailNotificationStorage jpaMailNotificationStorage;
  private JPAQueueMessageImpl jpaQueueMessage;

  //JCR storage
  private MailNotificationStorageImpl jcrNotificationDataStorage;

  private String jcrWorkspace;

  private Session session;
  private NodeHierarchyCreator nodeHierarchyCreator;
  private JobSchedulerService schedulerService;
  private RepositoryService repositoryService;
  private SettingService settingService;
  private TransactionService transactionService;

  public MailNotificationsMigration(MailNotificationStorageImpl jcrNotificationDataStorage,
                                    JPAMailNotificationStorage jpaMailNotificationStorage,
                                    JobSchedulerService schedulerService,
                                    SettingService settingService,
                                    NotificationConfiguration notificationConfiguration,
                                    RepositoryService repositoryService,
                                    TransactionService transactionService,
                                    NodeHierarchyCreator nodeHierarchyCreator) {
    this.jpaMailNotificationStorage = jpaMailNotificationStorage;
    this.jcrNotificationDataStorage = jcrNotificationDataStorage;
    this.nodeHierarchyCreator = nodeHierarchyCreator;
    this.schedulerService = schedulerService;
    this.repositoryService = repositoryService;
    this.settingService = settingService;
    this.transactionService = transactionService;
    this.jcrWorkspace = notificationConfiguration.getWorkspace();

    this.jpaQueueMessage = CommonsUtils.getService(JPAQueueMessageImpl.class);
  }

  private Session getJCRSession() {
    Session jcrSession = null;
    try {
      jcrSession = repositoryService.getCurrentRepository().getSystemSession(jcrWorkspace);
      if (jcrSession instanceof SessionImpl) {
        ((SessionImpl) jcrSession).setTimeout(JPAAsynMigrationService.ONE_DAY_IN_MS);
      }
      transactionService.setTransactionTimeout(JPAAsynMigrationService.ONE_DAY_IN_SECONDS);
    } catch (Exception e) {
      LOG.error("Error while getting Notification nodes for Notifications migration - Cause : " + e.getMessage(), e);
    }
    finally {
      if (jcrSession != null) {
        jcrSession.logout();
      }
    }
    return jcrSession;
  }

  public void migrate() {
    session = getJCRSession();
    //migration of mail notifications data from JCR to RDBMS is done as a background task
    PortalContainer.addInitTask(PortalContainer.getInstance().getPortalContext(), new RootContainer.PortalContainerPostInitTask() {
      @Override
      public void execute(ServletContext context, PortalContainer portalContainer) {
        RDBMSMigrationUtils.getExecutorService().submit(new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            if (!isMailNotifMigrationDone()) {
              try {
                // pause job of sending digest mails
                schedulerService.pauseJob("NotificationDailyJob", "Notification");
                schedulerService.pauseJob("NotificationWeeklyJob", "Notification");
                ExoContainerContext.setCurrentContainer(PortalContainer.getInstance());
                LOG.info("=== Start migration of Mail Notifications data from JCR");
                long startTime = System.currentTimeMillis();
                migrateMailNotifData();
                setMailNotifMigrationDone();
                long endTime = System.currentTimeMillis();
                LOG.info("=== Migration of Mail Notification data done in " + (endTime - startTime) + " ms");
              } catch (Exception e) {
                LOG.error("Error while migrating Mail Notification data from JCR to RDBMS - Cause : " + e.getMessage(), e);
              } finally {
                schedulerService.resumeJob("NotificationDailyJob", "Notification");
                schedulerService.resumeJob("NotificationWeeklyJob", "Notification");
              }
            } else {
              LOG.info("No mail notification data to migrate from JCR to RDBMS");
            }
            cleanupMailNotifications();
            return null;
          }
        });
      }
    });

    //migration of queue messages data from JCR to RDBMS is done as a background task
    PortalContainer.addInitTask(PortalContainer.getInstance().getPortalContext(), new RootContainer.PortalContainerPostInitTask() {
      @Override
      public void execute(ServletContext context, PortalContainer portalContainer) {
        RDBMSMigrationUtils.getExecutorService().submit(new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            ExoContainerContext.setCurrentContainer(PortalContainer.getInstance());
            if (hasQueueMessagesDataToMigrate()) {
              try {
                LOG.info("=== Start migration of Mail messages stored in the queue from JCR");
                long startTime = System.currentTimeMillis();
                migrateQueueMessages();
                long endTime = System.currentTimeMillis();
                LOG.info("=== Migration of Mail messages data done in " + (endTime - startTime) + " ms");
              } catch (Exception e) {
                LOG.error("Error while migrating Mail messages data from JCR to RDBMS - Cause : " + e.getMessage(), e);
              }
            } else {
              LOG.info("No queue messages data to migrate from JCR to RDBMS");
            }
            cleanupQueue();
            return null;
          }
        });
      }
    });
  }

  public void cleanupMailNotifications() {
    //migration of mail notifications data from JCR to RDBMS is done as a background task
    RDBMSMigrationUtils.getExecutorService().submit(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        RequestLifeCycle.begin(PortalContainer.getInstance());
        if (isMailNotifMigrationDone() && !isMailNotifCleanupDone()) {
          // pause job of sending digest mails
          schedulerService.pauseJob("NotificationDailyJob", "Notification");
          schedulerService.pauseJob("NotificationWeeklyJob", "Notification");
          try {
              LOG.info("=== Start cleaning Mail notifications data from JCR");
              long startTime = System.currentTimeMillis();
              deleteJcrMailNotifications();
              setMailNotifCleanupDone();
              long endTime = System.currentTimeMillis();
              LOG.info("=== Mail notifications JCR data cleaning due to RDBMS migration done in " + (endTime - startTime) + " ms");
          } catch (Exception e) {
            LOG.error("Error while cleaning Mail notifications JCR data", e);
          } finally {
            RequestLifeCycle.end();
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
        }
        return null;
      }
    });
  }

  public void cleanupQueue() {
    //migration of mail notifications data from JCR to RDBMS is done as a background task
    RDBMSMigrationUtils.getExecutorService().submit(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        RequestLifeCycle.begin(PortalContainer.getInstance());
        if (isMailNotifMigrationDone() && !isMailNotifCleanupDone()) {
          // pause job of sending digest mails
          schedulerService.pauseJob("NotificationDailyJob", "Notification");
          schedulerService.pauseJob("NotificationWeeklyJob", "Notification");
          try {
            LOG.info("=== Start cleaning Mail messages data from JCR");
            long startTime = System.currentTimeMillis();
            deleteJcrMailMessages();
            long endTime = System.currentTimeMillis();
            LOG.info("=== Mail messages JCR data cleaning due to RDBMS migration done in " + (endTime - startTime) + " ms");

          } catch (Exception e) {
            LOG.error("Error while cleaning Mail messages JCR data to RDBMS - Cause : " + e.getMessage(), e);
          }
        }
        return null;
      }
    });
  }

  private void setMailNotifMigrationDone() {
    settingService.set(Context.GLOBAL, Scope.APPLICATION.id(MAIL_NOTIFICATION_MIGRATION_DONE_KEY), MAIL_NOTIFICATION_RDBMS_MIGRATION_DONE, SettingValue.create("true"));
  }

  private void setMailNotifCleanupDone() {
    settingService.set(Context.GLOBAL, Scope.APPLICATION.id(MAIL_NOTIFICATION_MIGRATION_DONE_KEY), MAIL_NOTIFICATION_RDBMS_CLEANUP_DONE, SettingValue.create("true"));
  }

  private boolean isMailNotifMigrationDone() {
    SettingValue<?> setting = settingService.get(Context.GLOBAL, Scope.APPLICATION.id(MAIL_NOTIFICATION_MIGRATION_DONE_KEY), MAIL_NOTIFICATION_RDBMS_MIGRATION_DONE);
    return (setting != null && setting.getValue().equals("true"));
  }

  private boolean isMailNotifCleanupDone() {
    SettingValue<?> setting = settingService.get(Context.GLOBAL, Scope.APPLICATION.id(MAIL_NOTIFICATION_MIGRATION_DONE_KEY), MAIL_NOTIFICATION_RDBMS_CLEANUP_DONE);
    return (setting != null && setting.getValue().equals("true"));
  }

  private void deleteJcrMailMessages() throws RepositoryException {
    Node parentMsgHome = getNode("eXoNotification", "messageInfoHome");
    if (parentMsgHome != null) {
      parentMsgHome.remove();
      session.save();
    }
  }

  private void deleteJcrMailNotifications() throws RepositoryException {
    ExoContainer currentContainer = ExoContainerContext.getCurrentContainer();

    NodeIterator pluginNodesIterator = getMailNotificationNodes();
    if(pluginNodesIterator != null) {
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
            if (i % 100 == 0) {
              session.save();
              RequestLifeCycle.end();
              RequestLifeCycle.begin(currentContainer);
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
    }
    Node parentMsgHome = getNode("eXoNotification", "messageHome");
    if (parentMsgHome != null) {
      parentMsgHome.remove();
      session.save();
    }
  }

  private void migrateQueueMessages() throws Exception {
    NodeIterator iterator = getMessageInfoNodes();
    while (iterator.hasNext()) {
      Node node = iterator.nextNode();
      jpaQueueMessage.put(getMessageInfo(node));
    }
  }

  private MessageInfo getMessageInfo(Node node) {
    String path = null;
    try {
      path = node.getPath();

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
      LOG.warn("Failed to map message from node " + path + "", e);
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
    return getSubNodes("eXoNotification", "messageInfoHome");
  }

  private NodeIterator getMailNotificationNodes() {
    return getSubNodes("eXoNotification", "messageHome");
  }

  private NodeIterator getSubNodes(String jcrPathAlias, String relPath) {
    Node parentNode = getNode(jcrPathAlias, relPath);
    if (parentNode != null) {
      try {
        return parentNode.getNodes();
      } catch (RepositoryException e) {
        LOG.error("Error while getting sub nodes with path '" + relPath + "' from node with alias'" + jcrPathAlias + "'", e);
      }
    }
    return null;
  }

  private Node getNode(String jcrPathAlias, String relPath) {
    Node parentNode = null;
    try {
      String eXoNotificationJCRPath = nodeHierarchyCreator.getJcrPath(jcrPathAlias);
      if (StringUtils.isNotBlank(eXoNotificationJCRPath) && session.itemExists(eXoNotificationJCRPath)) {
        Node msgInfoHome = (Node) session.getItem(eXoNotificationJCRPath);
        if (msgInfoHome.hasNode(relPath)) {
          parentNode = msgInfoHome.getNode(relPath);
        }
      }
    } catch (Exception e) {
      LOG.error("Error while getting Path '" + relPath + "' from node with alias'" + jcrPathAlias + "'", e);
    }
    return parentNode;
  }

  private void migrateMailNotifData() throws RepositoryException, RepositoryConfigurationException {
    NodeIterator pluginNodesIterator = getMailNotificationNodes();
    if(pluginNodesIterator != null) {
      while (pluginNodesIterator.hasNext()) {
        Node pluginNode = pluginNodesIterator.nextNode();
        NodeIterator dayNodesIterator = pluginNode.getNodes();
        while (dayNodesIterator.hasNext()) {
          Node dayNode = dayNodesIterator.nextNode();
          NodeIterator notifNodes = dayNode.getNodes();
          if (notifNodes.getSize() > 0) {
            LOG.info("    Progression mail notifications migration for plugin: " + pluginNode.getName() + " - day: "
                    + dayNode.getName());
            while (notifNodes.hasNext()) {
              migrateMailNotifNodeToRDBMS(notifNodes.nextNode());
            }
          }
        }
      }
    }
  }

  private void migrateMailNotifNodeToRDBMS(Node node) {
    try {
      jpaMailNotificationStorage.save(jcrNotificationDataStorage.fillModel(node));
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }

  private boolean hasQueueMessagesDataToMigrate() {
    return (getMessageInfoNodes() != null && getMessageInfoNodes().hasNext());
  }
}

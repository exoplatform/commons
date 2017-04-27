package org.exoplatform.commons.notification.impl.migration;

import org.exoplatform.commons.cluster.StartableClusterAware;
import org.exoplatform.commons.notification.impl.jpa.web.JPAWebNotificationStorage;
import org.exoplatform.commons.notification.impl.service.storage.WebNotificationStorageImpl;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.jgroups.util.DefaultThreadFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.exoplatform.commons.utils.CommonsUtils.getService;

/**
 * Created by exo on 5/4/17.
 */
public class WebNotificationsMigration implements StartableClusterAware {

  private static final Log LOG = ExoLogger.getLogger(WebNotificationsMigration.class);

  //JPA Storage
  private JPAWebNotificationStorage jpaWebNotificationStorage;

  //JCR storage
  private WebNotificationStorageImpl jcrWebNotificationStorage;

  private ExecutorService executorServiceWeb;
  private SessionProvider sProvider;
  private NodeHierarchyCreator nodeHierarchyCreator;
  private OrganizationService organizationService;

  private static Boolean isWebNotifsMigrated = false;
  private static List<String> users = new LinkedList<String>();

  public WebNotificationsMigration(JPAWebNotificationStorage jpaWebNotificationStorage, WebNotificationStorageImpl jcrWebNotificationStorage) {
    this.jpaWebNotificationStorage = jpaWebNotificationStorage;
    this.jcrWebNotificationStorage = jcrWebNotificationStorage;

    this.organizationService = getService(OrganizationService.class);
    this.executorServiceWeb = Executors.newSingleThreadExecutor(new DefaultThreadFactory("WEB-NOTIFICATIONS-MIGRATION-RDBMS", false, false));
  }

  public ExecutorService getExecutorServiceWeb() {
    return executorServiceWeb;
  }

  public void setExecutorServiceWeb(ExecutorService executorServiceWeb) {
    this.executorServiceWeb = executorServiceWeb;
  }

  @Override
  public void start() {
    PortalContainer container = PortalContainer.getInstance();
    nodeHierarchyCreator = (NodeHierarchyCreator) container.getComponentInstanceOfType(NodeHierarchyCreator.class);
    try {
      sProvider = SessionProvider.createSystemProvider();
      ListAccess<User> list = organizationService.getUserHandler().findAllUsers();
      for (User user : list.load(0, list.getSize())) {
        users.add(user.getUserName());
      }
    } catch (Exception e) {
      LOG.error("Error while getting Notification nodes for Notifications migration - Cause : " + e.getMessage(), e);
      return;
    }
    if (!hasWebNotifDataToMigrate()) {
      LOG.info("No Web notification data to migrate from JCR to RDBMS");
      return;
    }
    //migration of mail notifications data from JCR to RDBMS is done as a background task
    getExecutorServiceWeb().submit(new Callable<Void>() {
      @Override
      public Void call() {
        try {
          ExoContainerContext.setCurrentContainer(PortalContainer.getInstance());
          LOG.info("=== Start migration of Web Notifications data from JCR");
          long startTime = System.currentTimeMillis();
          for (String userId : users) {
            migrateWebNotifDataOfUser(nodeHierarchyCreator.getUserApplicationNode(sProvider, userId));
          }
          long endTime = System.currentTimeMillis();
          LOG.info("=== Migration of Web Notification data done in " + (endTime - startTime) + " ms");
          isWebNotifsMigrated = true;
        } catch (Exception e) {
          LOG.error("Error while migrating Web Notification data from JCR to RDBMS - Cause : " + e.getMessage(), e);
          isWebNotifsMigrated = false;
        }
        try {
          if (isWebNotifsMigrated) {
            LOG.info("=== Start cleaning Web notifications data from JCR");
            long startTime = System.currentTimeMillis();
            deleteJcrWebNotifications();
            long endTime = System.currentTimeMillis();
            LOG.info("=== Web notifications JCR data cleaning due to RDBMS migration done in " + (endTime - startTime) + " ms");
          }
        } catch (Exception e) {
          LOG.error("Error while cleaning Web notifications JCR data to RDBMS - Cause : " + e.getMessage(), e);
        }
        return null;
      }
    });
  }

  private void migrateWebNotifDataOfUser(Node userAppNode) throws Exception {
    NodeIterator dateIterator = userAppNode.getNode("notifications").getNode("web").getNodes();
    while (dateIterator.hasNext()) {
      NodeIterator notifIterator = dateIterator.nextNode().getNodes();
      while (notifIterator.hasNext()) {
        migrateWebNotifNodeToRDBMS(notifIterator.nextNode());
      }
    }
  }

  private void migrateWebNotifNodeToRDBMS(Node node) throws Exception {
      jpaWebNotificationStorage.save(jcrWebNotificationStorage.fillModel(node));
  }

  private void deleteJcrWebNotifications() throws Exception {
    for (String userId : users) {
        Node node = nodeHierarchyCreator.getUserApplicationNode(sProvider, userId).getNode("notifications").getNode("web");
        node.remove();
        node.getSession().save();
    }
  }

  private boolean hasWebNotifDataToMigrate() {
    for (String userId : users) {
      try {
        Node node = nodeHierarchyCreator.getUserApplicationNode(sProvider, userId).getNode("notifications");
        if (node.hasNode("web")) {
          return true;
        }
      } catch (PathNotFoundException e) {
        return false;
      } catch (Exception e) {
        LOG.error("Error while verifying if web notification nodes exist in JCR - Cause : " + e.getMessage(), e);
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isDone() {
    return false;
  }

}

package org.exoplatform.commons.notification.impl.migration;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.cluster.StartableClusterAware;
import org.exoplatform.commons.notification.impl.jpa.web.JPAWebNotificationStorage;
import org.exoplatform.commons.notification.impl.service.storage.WebNotificationStorageImpl;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.RDBMSMigrationUtils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.servlet.ServletContext;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

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

  private SessionProvider sProvider;
  private NodeHierarchyCreator nodeHierarchyCreator;
  private OrganizationService organizationService;
  private SettingService settingService;
  private static List<String> allUsers = new LinkedList<String>();
  private static List<String> nonRemovedWebNotifs = new LinkedList<String>();
  private static List<String> nonMigratedWebNotifs = new LinkedList<String>();
  //scope of user web notification migration
  public static final String WEB_NOTIFICATION_MIGRATION_USER_KEY = "WEB_NOTIFICATION_MIGRATION_USER";
  //scope of web notification migration status
  public static final String WEB_NOTIFICATION_MIGRATION_DONE_KEY = "WEB_NOTIFICATION_MIGRATION_DONE";
  //status of web notifications migration (true if migration completed successfully)
  public static final String WEB_NOTIFICATION_RDBMS_MIGRATION_DONE = "WEB_NOTIFICATION_RDBMS_MIGRATION_DONE";
  //status of web notifications cleanup from JCR (true if cleanup is completed successfully)
  public static final String WEB_NOTIFICATION_RDBMS_CLEANUP_DONE = "WEB_NOTIFICATION_RDBMS_CLEANUP_DONE";


  public WebNotificationsMigration(JPAWebNotificationStorage jpaWebNotificationStorage, WebNotificationStorageImpl jcrWebNotificationStorage) {
    this.jpaWebNotificationStorage = jpaWebNotificationStorage;
    this.jcrWebNotificationStorage = jcrWebNotificationStorage;

    this.organizationService = getService(OrganizationService.class);
    this.settingService = getService(SettingService.class);
  }

  @Override
  public void start() {
    PortalContainer container = PortalContainer.getInstance();
    nodeHierarchyCreator = (NodeHierarchyCreator) container.getComponentInstanceOfType(NodeHierarchyCreator.class);
    try {
      sProvider = SessionProvider.createSystemProvider();
    } catch (Exception e) {
      LOG.error("Error while getting Notification nodes for Notifications migration - Cause : " + e.getMessage(), e);
      return;
    }
    //migration of web notifications data from JCR to RDBMS is done as a background task
    PortalContainer.addInitTask(PortalContainer.getInstance().getPortalContext(), new RootContainer.PortalContainerPostInitTask() {
      @Override
      public void execute(ServletContext context, PortalContainer portalContainer) {
        RDBMSMigrationUtils.getExecutorService().submit(new Callable<Void>() {
          @Override
          public Void call() {
            if (!isWebNotifMigrationDone()) {
              int pageSize = 20;
              int current = 0;
              try {
                ListAccess<User> allUsersListAccess = organizationService.getUserHandler().findAllUsers();
                ExoContainerContext.setCurrentContainer(PortalContainer.getInstance());
                int totalUsers = allUsersListAccess.getSize();
                LOG.info("    Number of users = " + totalUsers);
                User[] users;
                LOG.info("=== Start migration of Web Notifications data from JCR");
                ExoContainer currentContainer = ExoContainerContext.getCurrentContainer();
                RequestLifeCycle.begin(currentContainer);
                long startTime = System.currentTimeMillis();
                do {
                  LOG.info("    Progression of users web notifications migration : " + current + "/" + totalUsers);
                  if (current + pageSize > totalUsers) {
                    pageSize = totalUsers - current;
                  }
                  users = allUsersListAccess.load(current, pageSize);
                  for (User user : users) {
                    RequestLifeCycle.end();
                    RequestLifeCycle.begin(currentContainer);
                    String userName = user.getUserName();
                    if (!hasWebNotifDataToMigrate(userName) || isWebNotifMigrated(userName)) {
                      LOG.info("No Web notification data to migrate from JCR to RDBMS for user: " + userName);
                      continue;
                    }
                    try {
                      migrateWebNotifDataOfUser(nodeHierarchyCreator.getUserApplicationNode(sProvider, userName));
                      allUsers.add(userName);
                      settingService.set(Context.USER.id(userName), Scope.APPLICATION.id(WEB_NOTIFICATION_MIGRATION_USER_KEY), WEB_NOTIFICATION_RDBMS_MIGRATION_DONE, SettingValue.create("true"));
                    } catch (Exception e) {
                      settingService.set(Context.USER.id(userName), Scope.APPLICATION.id(WEB_NOTIFICATION_MIGRATION_USER_KEY), WEB_NOTIFICATION_RDBMS_MIGRATION_DONE, SettingValue.create("false"));
                    }
                  }
                  current += users.length;
                } while (users != null && users.length > 0);
                long endTime = System.currentTimeMillis();
                LOG.info("=== Migration of Web Notification data done in " + (endTime - startTime) + " ms");
              } catch (Exception e) {
                LOG.error("Error while migrating Web Notification data from JCR to RDBMS - Cause : " + e.getMessage(), e);
              } finally {
                RequestLifeCycle.end();
              }
              settingService.set(Context.GLOBAL, Scope.APPLICATION.id(WEB_NOTIFICATION_MIGRATION_DONE_KEY), WEB_NOTIFICATION_RDBMS_MIGRATION_DONE, SettingValue.create("true"));
            } else {
              LOG.info("No web notifications data to migrate from JCR to RDBMS");
            }
            if (!isWebNotifCleanupDone()) {
              deleteJcrWebNotifications();
            }
            return null;
          }
        });
      }
    });
  }

  private boolean isWebNotifMigrated(String userName) {
    SettingValue<?> setting = settingService.get(Context.USER.id(userName), Scope.APPLICATION.id(WEB_NOTIFICATION_MIGRATION_USER_KEY), WEB_NOTIFICATION_RDBMS_MIGRATION_DONE);
    return (setting != null && setting.getValue().equals("true"));
  }

  private boolean isWebNotifMigrationDone() {
    SettingValue<?> setting = settingService.get(Context.GLOBAL, Scope.APPLICATION.id(WEB_NOTIFICATION_MIGRATION_DONE_KEY), WEB_NOTIFICATION_RDBMS_MIGRATION_DONE);
    return (setting != null && setting.getValue().equals("true"));
  }

  private boolean isWebNotifCleanupDone() {
    SettingValue<?> setting = settingService.get(Context.GLOBAL, Scope.APPLICATION.id(WEB_NOTIFICATION_MIGRATION_DONE_KEY), WEB_NOTIFICATION_RDBMS_CLEANUP_DONE);
    return (setting != null && setting.getValue().equals("true"));
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

  private void deleteJcrWebNotifications() {
    LOG.info("=== Start Cleaning Web Notifications data from JCR");
    long startTime = System.currentTimeMillis();
    for (String userId : allUsers) {
      if (isWebNotifMigrated(userId)) {
        try {
          Node node = nodeHierarchyCreator.getUserApplicationNode(sProvider, userId).getNode("notifications").getNode("web");
          node.remove();
          node.getSession().save();
          settingService.remove(Context.USER.id(userId), Scope.APPLICATION.id(WEB_NOTIFICATION_MIGRATION_USER_KEY));
        } catch (Exception e) {
          nonRemovedWebNotifs.add(userId);
          LOG.error("Error while cleaning Web notifications JCR data to RDBMS of user: " + userId + " - Cause : " + e.getMessage(), e);
        }
      } else {
        nonMigratedWebNotifs.add(userId);
      }
    }
    LOG.info(" === Web Notifications Migration from JCR to RDBBMS report: \n"
           + "           - " + nonMigratedWebNotifs.size() + " Web Notifications nodes are not migrated to RDBMS \n"
           + "           - " + nonRemovedWebNotifs.size() + " Web Notifications nodes are migrated but not removed from JCR");
    long endTime = System.currentTimeMillis();
    LOG.info("=== Web notifications JCR data cleaning due to RDBMS migration done in " + (endTime - startTime) + " ms");
    settingService.set(Context.GLOBAL, Scope.APPLICATION.id(WEB_NOTIFICATION_MIGRATION_DONE_KEY), WEB_NOTIFICATION_RDBMS_CLEANUP_DONE, SettingValue.create("true"));
  }

  private boolean hasWebNotifDataToMigrate(String userName) {
    try {
      Node node = nodeHierarchyCreator.getUserApplicationNode(sProvider, userName).getNode("notifications");
      if (node.hasNode("web")) {
        return true;
      }
    } catch (PathNotFoundException e) {
      return false;
    } catch (Exception e) {
      LOG.error("Error while verifying if web notification nodes exist in JCR - Cause : " + e.getMessage(), e);
      return true;
    }
    return false;
  }

  @Override
  public boolean isDone() {
    return false;
  }

}

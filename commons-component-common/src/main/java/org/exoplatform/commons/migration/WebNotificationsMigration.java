package org.exoplatform.commons.migration;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.servlet.ServletContext;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
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

public class WebNotificationsMigration {

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


  public WebNotificationsMigration(JPAWebNotificationStorage jpaWebNotificationStorage,
                                   NodeHierarchyCreator nodeHierarchyCreator,
                                   OrganizationService organizationService,
                                   SettingService settingService,
                                   WebNotificationStorageImpl jcrWebNotificationStorage) {
    this.jpaWebNotificationStorage = jpaWebNotificationStorage;
    this.jcrWebNotificationStorage = jcrWebNotificationStorage;
    this.organizationService = organizationService;
    this.settingService = settingService;
    this.nodeHierarchyCreator = nodeHierarchyCreator;
  }

  public void migrate() {
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
                  int migratedUsersCount = current + 1;
                  for (User user : users) {
                    RequestLifeCycle.end();
                    RequestLifeCycle.begin(currentContainer);
                    String userName = user.getUserName();
                    if (!hasWebNotifDataToMigrate(userName) || isWebNotifMigrated(userName)) {
                      int progression = (int)((migratedUsersCount++ * 100) / totalUsers);
                      LOG.info("Web notification migration - progression = ({}%), username={}, migrated Web Notifications Count = {}", progression, userName, 0);
                      continue;
                    }
                    try {
                      long notificationsCount = migrateWebNotifDataOfUser(nodeHierarchyCreator.getUserApplicationNode(sProvider, userName));
                      int progression = (int)((migratedUsersCount++ * 100) / totalUsers);
                      LOG.info("Web notification migration - progression = ({}%), username={}, migrated Web Notifications Count = {}", progression, userName, notificationsCount);
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
            return null;
          }
        });
      }
    });
  }

  public void cleanup() {
    // Proceed to delete JCR data after migration is finished
    PortalContainer.addInitTask(PortalContainer.getInstance().getPortalContext(), new RootContainer.PortalContainerPostInitTask() {
      @Override
      public void execute(ServletContext context, PortalContainer portalContainer) {
        RDBMSMigrationUtils.getExecutorService().submit(new Callable<Void>() {
          @Override
          public Void call() {
            PortalContainer currentContainer = PortalContainer.getInstance();
            ExoContainerContext.setCurrentContainer(currentContainer);
            RequestLifeCycle.begin(currentContainer);
            try {
              if (isWebNotifMigrationDone() && !isWebNotifCleanupDone()) {
                deleteJcrWebNotifications();
              }
            } catch (Exception e) {
              LOG.error("Error while cleaning Web Notifications data from JCR", e);
            } finally {
              RequestLifeCycle.end();
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

  private long migrateWebNotifDataOfUser(Node userAppNode) throws Exception {
    NodeIterator dateIterator = userAppNode.getNode("notifications").getNode("web").getNodes();
    long notificationsCount = dateIterator.getSize();
    while (dateIterator.hasNext()) {
      NodeIterator notifIterator = dateIterator.nextNode().getNodes();
      while (notifIterator.hasNext()) {
        migrateWebNotifNodeToRDBMS(notifIterator.nextNode());
      }
    }
    return notificationsCount;
  }

  private void migrateWebNotifNodeToRDBMS(Node node) throws Exception {
      jpaWebNotificationStorage.save(jcrWebNotificationStorage.fillModel(node));
  }

  private void deleteJcrWebNotifications() {
    LOG.info("=== Start Cleaning Web Notifications data from JCR");
    long startTime = System.currentTimeMillis();
    int i = 0;
    int totalSize = allUsers.size();
    for (String userId : allUsers) {
      i++;
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
      if (i % 100 == 0) {
        LOG.info("Web Notifications JCR cleanup - progression = {}/{}", i, totalSize);
      }
    }
    LOG.info(" === Web Notifications Migration from JCR to RDBBMS report:");
    LOG.info("           - " + nonMigratedWebNotifs.size() + " Web Notifications nodes are not migrated to RDBMS");
    LOG.info("           - " + nonRemovedWebNotifs.size() + " Web Notifications nodes are migrated but not removed from JCR");
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
}

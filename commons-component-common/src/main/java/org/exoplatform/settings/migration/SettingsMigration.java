package org.exoplatform.settings.migration;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.commons.chromattic.SessionContext;
import org.exoplatform.commons.cluster.StartableClusterAware;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.RDBMSMigrationUtils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.settings.chromattic.ScopeEntity;
import org.exoplatform.settings.chromattic.SimpleContextEntity;
import org.exoplatform.settings.chromattic.SubContextEntity;
import org.exoplatform.settings.chromattic.SynchronizationTask;
import org.exoplatform.settings.jpa.JPASettingServiceImpl;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.exoplatform.commons.utils.CommonsUtils.getService;

/**
 * Created by exo on 4/18/17.
 */
public class SettingsMigration implements StartableClusterAware {

  private static final Log LOG = ExoLogger.getLogger(SettingsMigration.class);

  //Service
  private JPASettingServiceImpl jpaSettingService;
  private ChromatticLifeCycle chromatticLifeCycle;
  private OrganizationService organizationService;

  private static List<String> allUsers = new LinkedList<String>();
  private static List<String> errorUserSettings = new LinkedList<String>();
  private static List<String> errorGlobalSettings = new LinkedList<String>();
  //scope of user settings migration
  public static final String SETTINGS_MIGRATION_USER_KEY = "SETTINGS_MIGRATION_USER";
  //status of jcr user settings data (true if jcr user settings data is migrated)
  public static final String SETTINGS_JCR_DATA_USER_MIGRATED_KEY = "SETTINGS_JCR_DATA_USER_MIGRATED";
  //status of settings migration (true if migration completed successfully)
  public static final String SETTINGS_RDBMS_MIGRATION_DONE = "SETTINGS_RDBMS_MIGRATION_DONE";


  public SettingsMigration(JPASettingServiceImpl jpaSettingService) {
    this.jpaSettingService = jpaSettingService;
    ChromatticManager chromatticManager = PortalContainer.getInstance().getComponentInstanceOfType(ChromatticManager.class);
    chromatticLifeCycle = (ChromatticLifeCycle) chromatticManager.getLifeCycle("setting");
    this.organizationService = getService(OrganizationService.class);
  }

  @Override
  public void start() {

    //First check to see if the JCR still contains settings data. If not, migration is skipped
    if (!hasGlobalSettingsToMigrate()) {
      LOG.info("No global settings data to migrate from JCR to RDBMS");
    } else {
      migrateGlobalSettings();
      //Deletion of settings data in JCR is done as a background task
      RDBMSMigrationUtils.getExecutorService().submit(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          LOG.info("=== Start cleaning Global Settings data from JCR");
          long startTime = System.currentTimeMillis();
          deleteJcrGlobalSettings();
          long endTime = System.currentTimeMillis();
          LOG.info("=== Global Settings JCR data cleaning due to RDBMS migration done in " + (endTime - startTime) + " ms");
          return null;
        }
      });
    }
    if (!hasUserSettingsToMigrate()) {
      LOG.info("No user settings data to migrate from JCR to RDBMS");
    } else {
      migrateUserSettings();
    }
    if (getJCRUserSettingsToRemove() != null) {
      RDBMSMigrationUtils.getExecutorService().submit(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          LOG.info("=== Start cleaning User Settings data from JCR");
          long startTime = System.currentTimeMillis();
          deleteJcrUserSettings();
          long endTime = System.currentTimeMillis();
          LOG.info("=== User Settings JCR data cleaning due to RDBMS migration done in " + (endTime - startTime) + " ms");
          if (chromatticLifeCycle.getManager().getSynchronization() != null) {
            chromatticLifeCycle.getManager().endRequest(true);
          }
          return null;
        }
      });
    }
  }

  private Boolean deleteJcrGlobalSettings() {
    return new SynchronizationTask<Boolean>() {
      @Override
      protected Boolean execute(SessionContext ctx) {
        try {
          for (String scope : getGlobalSettings()) {
            if (!errorGlobalSettings.contains(scope)) {
              deleteGlobalSettings(scope);
            }
          }
          return true;
        } catch (Exception e) {
          return false;
        }
      }
    }.executeWith(chromatticLifeCycle);
  }

  private Boolean deleteJcrUserSettings() {
    return new SynchronizationTask<Boolean>() {
      @Override
      protected Boolean execute(SessionContext ctx) {
        try {
          for (String user : getJCRUserSettingsToRemove()) {
            if (!errorUserSettings.contains(user) && isSettingsMigrated(user)) {
              LOG.info(" removing JCR settings of user: " + user);
              if (deleteUserSettings(user)) {
                jpaSettingService.remove(Context.USER.id(user), Scope.APPLICATION.id(SETTINGS_MIGRATION_USER_KEY), SETTINGS_RDBMS_MIGRATION_DONE);
                LOG.info(" done removing JCR settings of user: " + user);
              }
            }
          }
          return true;
        } catch (Exception e) {
          return false;
        }
      }
    }.executeWith(chromatticLifeCycle);
  }

  private Boolean migrateGlobalSettings() {
    return new SynchronizationTask<Boolean>() {
      @Override
      protected Boolean execute(SessionContext ctx) {
        LOG.info("=== Start migration of Global Settings data from JCR to RDBMS");
        long startTime = System.currentTimeMillis();
        for (String scope : getGlobalSettings()) {
          migrateGlobalSettingsOfGlobalScopes(scope);
          migrateGlobalSettingsOfGlobalSpecificScopes(scope);
        }
        long endTime = System.currentTimeMillis();
        LOG.info("Global Settings data migrated in " + (endTime - startTime) + " ms");
        return true;
      }
    }.executeWith(chromatticLifeCycle);
  }

  private Boolean migrateUserSettings() {
    int pageSize = 20;
    int current = 0;
    try {
      ListAccess<User> allUsersListAccess = organizationService.getUserHandler().findAllUsers();
      ExoContainerContext.setCurrentContainer(PortalContainer.getInstance());
      int totalUsers = allUsersListAccess.getSize();
      LOG.info("    Number of users = " + totalUsers);
      User[] users;
      LOG.info("=== Start migration of User Settings data from JCR");
      ExoContainer currentContainer = ExoContainerContext.getCurrentContainer();
      RequestLifeCycle.begin(currentContainer);
      long startTime = System.currentTimeMillis();
      do {
        LOG.info("    Progression of users settings migration : " + current + "/" + totalUsers);
        if (current + pageSize > totalUsers) {
          pageSize = totalUsers - current;
        }
        users = allUsersListAccess.load(current, pageSize);
        for (User user : users) {
          RequestLifeCycle.end();
          RequestLifeCycle.begin(currentContainer);
          String userName = user.getUserName();
          if (!isSettingsMigrated(userName)) {
            for (String userScope : getUserScopesSettings(userName)) {
              migrateUserSettingsOfGlobalScope(userName, userScope);
              migrateUserSettingsOfSpecificScope(userName, userScope);
            }
            allUsers.add(userName);
            if (errorUserSettings.contains(userName)) {
              jpaSettingService.set(Context.USER.id(userName), Scope.APPLICATION.id(SETTINGS_MIGRATION_USER_KEY), SETTINGS_RDBMS_MIGRATION_DONE, SettingValue.create("false"));
            } else {
              jpaSettingService.set(Context.USER.id(userName), Scope.APPLICATION.id(SETTINGS_MIGRATION_USER_KEY), SETTINGS_RDBMS_MIGRATION_DONE, SettingValue.create("true"));
            }
          }
        }
        current += users.length;
      } while(users != null && users.length > 0);
      long endTime = System.currentTimeMillis();
      LOG.info("User Settings data migrated in " + (endTime - startTime) + " ms");
      if (!errorUserSettings.isEmpty()) {
        jpaSettingService.set(Context.GLOBAL, Scope.APPLICATION.id(SETTINGS_MIGRATION_USER_KEY), SETTINGS_JCR_DATA_USER_MIGRATED_KEY, SettingValue.create("true"));
      } else {
        jpaSettingService.set(Context.GLOBAL, Scope.APPLICATION.id(SETTINGS_MIGRATION_USER_KEY), SETTINGS_JCR_DATA_USER_MIGRATED_KEY, SettingValue.create("false"));
      }
      return true;
    } catch (Exception e) {
      LOG.error("Error while migrating user settings data from JCR to RDBMS - Cause : " + e.getMessage(), e);
      return false;
    } finally {
      RequestLifeCycle.end();
      if (chromatticLifeCycle.getManager().getSynchronization() != null) {
        chromatticLifeCycle.getManager().endRequest(true);
      }
    }
  }

  private boolean isSettingsMigrated(String userName) {
    SettingValue<?> setting = jpaSettingService.get(Context.USER.id(userName), Scope.APPLICATION.id(SETTINGS_MIGRATION_USER_KEY), SETTINGS_RDBMS_MIGRATION_DONE);
    return (setting != null && setting.getValue().equals("true"));
  }

  private Set<String> getUserScopesSettings(String user) {
    return new SynchronizationTask<Set<String>>() {
      @Override
      protected Set<String> execute(SessionContext ctx) {
        SimpleContextEntity userSettings = ctx.getSession().findByPath(SimpleContextEntity.class, "settings/user/" + user);
        return (userSettings == null ? new HashSet<String>() : userSettings.getScopes().keySet());
      }
    }.executeWith(chromatticLifeCycle);
  }

  private ScopeEntity getSpecificScope(String scope) {
    return new SynchronizationTask<ScopeEntity>() {
      @Override
      protected ScopeEntity execute(SessionContext ctx) {
        ScopeEntity globalSettings = ctx.getSession().findByPath(ScopeEntity.class, "settings/global/" + scope);
        return globalSettings;
      }
    }.executeWith(chromatticLifeCycle);
  }

  @ExoTransactional
  private Boolean migrateGlobalSettingsOfGlobalSpecificScopes(String scope) {
    ScopeEntity scopeEntity = getSpecificScope(scope);
    for (String instance : scopeEntity.getInstances().keySet()) {
      Scope specificScope = getScope(scope, instance);
      for (String key : scopeEntity.getInstance(instance).getProperties().keySet()) {
        try {
          jpaSettingService.set(Context.GLOBAL, specificScope, key, new SettingValue<>(scopeEntity.getInstance(instance).getValue(key)));
        } catch (Exception e) {
          errorGlobalSettings.add(scope);
          LOG.error("Cannot migrate Global Settings data of specific scope: "+scope+" - cause: " +e.getMessage(), e);
          continue;
        }
      }
    }
    return true;
  }

  private Boolean deleteGlobalSettings(String scope) {
    return new SynchronizationTask<Boolean>() {
      @Override
      protected Boolean execute(SessionContext ctx) {
        try {
          ctx.getSession().remove(ctx.getSession().findByPath(ScopeEntity.class, "settings/global/" + scope));
          ctx.getSession().save();
          return true;
        } catch (Exception e) {
          LOG.error("Cannot remove JCR settings of scope: " + scope + " - cause: " + e.getCause(), e);
          return false;
        }
      }
    }.executeWith(chromatticLifeCycle);
  }

  private Boolean deleteUserSettings(String user) {
    return new SynchronizationTask<Boolean>() {
      @Override
      protected Boolean execute(SessionContext ctx) {
        try {
          ctx.getSession().remove(ctx.getSession().findByPath(SimpleContextEntity.class, "settings/user/" + user));
          ctx.getSession().save();
          return true;
        } catch (Exception e) {
          LOG.error("Cannot remove JCR settings of user: " + user + " - cause: " + e.getCause(), e);
          return false;
        }
      }
    }.executeWith(chromatticLifeCycle);
  }

  private ScopeEntity getScopeOfUser(String user, String scope) {
    return new SynchronizationTask<ScopeEntity>() {
      @Override
      protected ScopeEntity execute(SessionContext ctx) {
        ScopeEntity globalSettings = ctx.getSession().findByPath(ScopeEntity.class, "settings/user/" + user + "/" + scope);
        return globalSettings;
      }
    }.executeWith(chromatticLifeCycle);
  }

  @ExoTransactional
  private Boolean migrateUserSettingsOfSpecificScope(String user, String scope) {
    ScopeEntity scopeEntity = getScopeOfUser(user, scope);
    if (scopeEntity != null) {
      for (String instance : scopeEntity.getInstances().keySet()) {
        Scope specificScope = getScope(scope, instance);
        for (String key : scopeEntity.getInstance(instance).getProperties().keySet()) {
          try {
            jpaSettingService.set(Context.USER.id(user), specificScope, key, new SettingValue<>(scopeEntity.getInstance(instance).getValue(key)));
          } catch (Exception e) {
            errorUserSettings.add(user);
            LOG.error("Cannot migrate User Settings data of user: " + user + " and scope: " + scope + " - cause: " + e.getMessage(), e);
            continue;
          }
        }
      }
    }
    return true;
  }

  private Boolean hasGlobalSettingsToMigrate() {
    return new SynchronizationTask<Boolean>() {
      @Override
      protected Boolean execute(SessionContext ctx) {
        SimpleContextEntity settings = ctx.getSession().findByPath(SimpleContextEntity.class, "settings/global");
        return (settings!=null && settings.getScopes().size() > 0);
      }
    }.executeWith(chromatticLifeCycle);
  }

  private Boolean hasUserSettingsToMigrate() {
    try {
      SettingValue<?> setting = jpaSettingService.get(Context.GLOBAL, Scope.APPLICATION.id(SETTINGS_MIGRATION_USER_KEY), SETTINGS_JCR_DATA_USER_MIGRATED_KEY);
      if (setting != null) {
        return setting.getValue().equals("true");
      } else {
        return true;
      }
    } catch (Exception e) {
      LOG.error("Error when defining if there is user settings to migrate in jcr - cause: " + e.getMessage(), e);
      return false;
    }
  }

  private Set<String> getJCRUserSettingsToRemove() {
    return new SynchronizationTask<Set<String>>() {
      @Override
      protected Set<String> execute(SessionContext ctx) {
        SubContextEntity settings = ctx.getSession().findByPath(SubContextEntity.class, "settings/user");
        return (settings!=null ? settings.getContexts().keySet() : null);
      }
    }.executeWith(chromatticLifeCycle);
  }

  @Override
  public boolean isDone() {
    return false;
  }

  public Set<String> getGlobalSettings() {

    return new SynchronizationTask<Set<String>>() {
      @Override
      protected Set<String> execute(SessionContext ctx) {
        SimpleContextEntity globalSettings = ctx.getSession().findByPath(SimpleContextEntity.class, "settings/global");
        if (globalSettings != null) {
          return globalSettings.getScopes().keySet();
        } else {
          return new HashSet<String>();
        }
      }
    }.executeWith(chromatticLifeCycle);
  }

  public ScopeEntity getGlobalScope(String scope) {
    return new SynchronizationTask<ScopeEntity>() {
      @Override
      protected ScopeEntity execute(SessionContext ctx) {
        ScopeEntity globalSettings = ctx.getSession().findByPath(ScopeEntity.class, "settings/global/" + scope);
        return globalSettings;
      }
    }.executeWith(chromatticLifeCycle);
  }

  @ExoTransactional
  public Boolean migrateGlobalSettingsOfGlobalScopes(String scope) {
    ScopeEntity scopeEntity = getGlobalScope(scope);
    Scope globalScope = getScope(scope, null);
    for (String key : scopeEntity.getProperties().keySet()) {
      try {
        jpaSettingService.set(Context.GLOBAL, globalScope, key, new SettingValue<>(scopeEntity.getValue(key)));
      } catch (Exception e) {
        errorGlobalSettings.add(scope);
        LOG.error("Cannot migrate Global Settings data of scope: "+scope+" - cause: " +e.getMessage(), e);
        continue;
      }
    }
    return true;
  }

  public ScopeEntity getGlobalScopeOfUser(String user, String scope) {
    return new SynchronizationTask<ScopeEntity>() {
      @Override
      protected ScopeEntity execute(SessionContext ctx) {
        ScopeEntity globalSettings = ctx.getSession().findByPath(ScopeEntity.class, "settings/user/" + user + "/" + scope);
        return globalSettings;
      }
    }.executeWith(chromatticLifeCycle);
  }

  @ExoTransactional
  public Boolean migrateUserSettingsOfGlobalScope(String user, String scope) {
    ScopeEntity scopeEntity = getGlobalScopeOfUser(user, scope);
    Scope globalScope = getScope(scope, null);
    if (scopeEntity != null) {
      for (String key : scopeEntity.getProperties().keySet()) {
        try {
          jpaSettingService.set(Context.USER.id(user), globalScope, key, new SettingValue<>(scopeEntity.getValue(key)));
        } catch (Exception e) {
          errorUserSettings.add(user);
          LOG.error("Cannot migrate User Settings data of user: " + user + " and scope: " + scope + " - cause: " + e.getMessage(), e);
          continue;
        }
      }
    }
    return true;
  }

  private Scope getScope(String scope, String id) {
    switch (scope) {
      case "windows":
        return Scope.WINDOWS.id(id);
      case "page":
        return Scope.PAGE.id(id);
      case "space":
        return Scope.SPACE.id(id);
      case "site":
        return Scope.SITE.id(id);
      case "portal":
        return Scope.PORTAL.id(id);
      case "application":
        return Scope.APPLICATION.id(id);
      case "global":
        return Scope.GLOBAL.id(id);
    }
    return null;
  }
}

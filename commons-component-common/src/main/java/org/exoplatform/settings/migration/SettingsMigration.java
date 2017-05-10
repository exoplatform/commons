package org.exoplatform.settings.migration;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.commons.chromattic.SessionContext;
import org.exoplatform.commons.cluster.StartableClusterAware;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.settings.chromattic.*;
import org.exoplatform.settings.jpa.JPASettingServiceImpl;
import org.jgroups.util.DefaultThreadFactory;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by exo on 4/18/17.
 */
public class SettingsMigration implements StartableClusterAware {

  private static final Log LOG = ExoLogger.getLogger(SettingsMigration.class);

  //Service
  private JPASettingServiceImpl jpaSettingService;
  private ChromatticLifeCycle chromatticLifeCycle;
  private ExecutorService executorService;



  public SettingsMigration(JPASettingServiceImpl jpaSettingService) {
    this.jpaSettingService = jpaSettingService;
    ChromatticManager chromatticManager = PortalContainer.getInstance().getComponentInstanceOfType(ChromatticManager.class);
    chromatticLifeCycle = (ChromatticLifeCycle) chromatticManager.getLifeCycle("setting");
    this.executorService = Executors.newSingleThreadExecutor(new DefaultThreadFactory("SETTINGS-MIGRATION-RDBMS", false, false));
  }

  public ExecutorService getExecutorService() {
    return executorService;
  }

  public void setExecutorService(ExecutorService executorService) {
    this.executorService = executorService;
  }

  @Override
  public void start() {

    //First check to see if the JCR still contains settings data. If not, migration is skipped
    if (!hasDataToMigrate()) {
      LOG.info("No settings data to migrate from JCR to RDBMS");
      return;
    }
    if(migrateAllSettings()) {
      //Deletion of settings data in JCR is done as a background task
      getExecutorService().submit(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          LOG.info("=== Start cleaning Settings data from JCR");
          long startTime = System.currentTimeMillis();
          deleteJcrSettings();
          long endTime = System.currentTimeMillis();
          LOG.info("=== Settings JCR data cleaning due to RDBMS migration done in " + (endTime - startTime) + " ms");
          return null;
        }
      });
    }
  }

  private Boolean deleteJcrSettings() {
    return new SynchronizationTask<Boolean>() {
      @Override
      protected Boolean execute(SessionContext ctx) {
        try {
          for (String scope : getGlobalSettings()) {
            deleteGlobalSettings(scope);
          }
          for (String user : getUserSettings()) {
            deleteUserSettings(user);
          }
          return true;
        } catch (Exception e) {
          return false;
        }
      }
    }.executeWith(chromatticLifeCycle);
  }

  private Boolean migrateAllSettings() {
    return new SynchronizationTask<Boolean>() {
      @Override
      protected Boolean execute(SessionContext ctx) {
        try {
          LOG.info("=== Start migration of Settings data from JCR to RDBMS");
          for (String scope : getGlobalSettings()) {
            migrateGlobalSettingsOfGlobalScopes(scope);
            migrateGlobalSettingsOfGlobalSpecificScopes(scope);
          }
          for (String user : getUserSettings()) {
            for (String userScope : getUserScopesSettings(user)) {
              migrateUserSettingsOfGlobalScope(user, userScope);
              migrateUserSettingsOfSpecificScope(user, userScope);
            }
          }
          LOG.info("Settings data migrated");
          return true;
        } catch (Exception e) {
          LOG.error("Cannot migrate Settings data - cause: " +e.getMessage(), e);
          return false;
        }
      }
    }.executeWith(chromatticLifeCycle);
  }


  private Set<String> getUserScopesSettings(String user) {
    return new SynchronizationTask<Set<String>>() {
      @Override
      protected Set<String> execute(SessionContext ctx) {
        SimpleContextEntity userSettings = ctx.getSession().findByPath(SimpleContextEntity.class, "settings/user/" + user);
        return userSettings.getScopes().keySet();
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
      for (String key : scopeEntity.getInstance(instance).getProperties().keySet()) {
        jpaSettingService.set(Context.GLOBAL, getScope(scope, instance), key, new SettingValue<>(scopeEntity.getInstance(instance).getValue(key)));
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
    for (String instance : scopeEntity.getInstances().keySet()) {
      for (String key : scopeEntity.getInstance(instance).getProperties().keySet()) {
        jpaSettingService.set(Context.USER.id(user), getScope(scope, instance), key, new SettingValue<>(scopeEntity.getInstance(instance).getValue(key)));
      }
    }
    return true;
  }

  private Boolean hasDataToMigrate() {
    return new SynchronizationTask<Boolean>() {
      @Override
      protected Boolean execute(SessionContext ctx) {
        SimpleContextEntity settings = ctx.getSession().findByPath(SimpleContextEntity.class, "settings/global");
        SubContextEntity userSettings = ctx.getSession().findByPath(SubContextEntity.class, "settings/user");
        return ((userSettings==null && settings==null)
            ||((userSettings.getContexts().size() > 0) && (settings.getScopes().size() > 0)));
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
        return globalSettings.getScopes().keySet();
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
    for (String key : scopeEntity.getProperties().keySet()) {
      jpaSettingService.set(Context.GLOBAL, getScope(scope, null), key, new SettingValue<>(scopeEntity.getValue(key)));
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
    for (String key : scopeEntity.getProperties().keySet()) {
      jpaSettingService.set(Context.USER.id(user), getScope(scope, null), key, new SettingValue<>(scopeEntity.getValue(key)));
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

  public Set<String> getUserSettings() {
    return new SynchronizationTask<Set<String>>() {
      @Override
      protected Set<String> execute(SessionContext ctx) {
        SubContextEntity userSettings = ctx.getSession().findByPath(SubContextEntity.class, "settings/user");
        return userSettings.getContexts().keySet();
      }
    }.executeWith(chromatticLifeCycle);
  }
}

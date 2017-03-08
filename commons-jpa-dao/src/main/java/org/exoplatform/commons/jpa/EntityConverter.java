package org.exoplatform.commons.jpa;

import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.jpa.entity.ContextEntity;
import org.exoplatform.commons.jpa.entity.ScopeEntity;
import org.exoplatform.commons.jpa.entity.SettingsEntity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import static org.exoplatform.commons.api.settings.data.Context.GLOBAL;
import static org.exoplatform.commons.api.settings.data.Context.USER;
import static org.exoplatform.commons.api.settings.data.Scope.*;

/**
 * Created by exo on 3/8/17.
 */
public class EntityConverter {
  private static final Log LOG = ExoLogger.getLogger(EntityConverter.class);

  public static ContextEntity convertContextToContextEntity(Context context) {
    ContextEntity contextEntity = null;
    if (context != null) {
      contextEntity = new ContextEntity();
      contextEntity.setName(context.getId());
      switch (context) {
        case GLOBAL:
          contextEntity.setType(GLOBAL.toString());
        case USER:
          contextEntity.setType(USER.toString());
      }
    }
    return contextEntity;
  }

  public static ScopeEntity convertScopeToScopeEntity(Scope scope) {
    ScopeEntity scopeEntity = null;
    if (scope != null) {
      scopeEntity = new ScopeEntity();
      scopeEntity.setName(scope.getId());
      switch (scope) {
        case WINDOWS:
          scopeEntity.setType(WINDOWS.toString());
        case PAGE:
          scopeEntity.setType(PAGE.toString());
        case SPACE:
          scopeEntity.setType(SPACE.toString());
        case SITE:
          scopeEntity.setType(SITE.toString());
        case PORTAL:
          scopeEntity.setType(PORTAL.toString());
        case APPLICATION:
          scopeEntity.setType(APPLICATION.toString());
        case GLOBAL:
          scopeEntity.setType(GLOBAL.toString());
      }
    }
    return scopeEntity;
  }

  public static SettingsEntity convertSettingsToSettingsEntity(Context context, Scope scope, String key, SettingValue<?> value) {
    SettingsEntity settingsEntity = null;
    if (key != null) {
      settingsEntity = new SettingsEntity();
      settingsEntity.setContext(convertContextToContextEntity(context));
      settingsEntity.setScope(convertScopeToScopeEntity(scope));
      settingsEntity.setName(key);
      settingsEntity.setValue(value.toString());
    }
    return settingsEntity;
  }
}

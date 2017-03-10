package org.exoplatform.settings.jpa;

import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.settings.jpa.entity.ContextEntity;
import org.exoplatform.settings.jpa.entity.ScopeEntity;
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
          contextEntity.setName(GLOBAL.toString());
          break;
        case USER:
          contextEntity.setType(USER.toString());
          break;
      }
    }
    return contextEntity;
  }

  public static ScopeEntity convertScopeToScopeEntity(Scope scope) {
    ScopeEntity scopeEntity = null;
    if (scope != null) {
      scopeEntity = new ScopeEntity();
//      scopeEntity.setName(scope.getId());
      switch (scope) {
        case WINDOWS:
          scopeEntity.setType(WINDOWS.toString());
          break;
        case PAGE:
          scopeEntity.setType(PAGE.toString());
          break;
        case SPACE:
          scopeEntity.setType(SPACE.toString());
          break;
        case SITE:
          scopeEntity.setType(SITE.toString());
          break;
        case PORTAL:
          scopeEntity.setType(PORTAL.toString());
          break;
        case APPLICATION:
          scopeEntity.setType(APPLICATION.toString());
          break;
        case GLOBAL:
          scopeEntity.setType(GLOBAL.toString());
          break;
      }
      if (scope.getId() != null) {
        scopeEntity.setName(scope.getId());
      } else {
        scopeEntity.setName(scopeEntity.getType());
      }
    }
    return scopeEntity;
  }
}

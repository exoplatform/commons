package org.exoplatform.settings.jpa;

import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.settings.jpa.entity.ContextEntity;
import org.exoplatform.settings.jpa.entity.ScopeEntity;

import static org.exoplatform.commons.api.settings.data.Context.GLOBAL;
import static org.exoplatform.commons.api.settings.data.Context.USER;

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
      scopeEntity.setType(scope.toString());
      if (scope.getId() != null) {
        scopeEntity.setName(scope.getId());
      } else {
        scopeEntity.setName(scopeEntity.getType());
      }
    }
    return scopeEntity;
  }
}

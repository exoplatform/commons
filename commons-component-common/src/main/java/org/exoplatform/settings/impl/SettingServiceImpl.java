package org.exoplatform.settings.impl;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.*;
import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.commons.chromattic.SessionContext;
import org.exoplatform.commons.event.impl.EventManagerImpl;
import org.exoplatform.services.listener.Event;
import org.exoplatform.settings.chromattic.*;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @LevelAPI Experimental
 */
public class SettingServiceImpl implements SettingService {

  private final ChromatticLifeCycle chromatticLifeCycle;

  private final EventManagerImpl<SettingServiceImpl, SettingData> eventManager;
  
  /**
   * Create setting service object
   * @param eventManager event manager component
   * @throws NullPointerException
   * @LevelAPI Experimental
   */
  public SettingServiceImpl(EventManagerImpl<SettingServiceImpl, SettingData> eventManager, ChromatticManager chromatticManager) throws NullPointerException {
      chromatticLifeCycle = (ChromatticLifeCycle) chromatticManager.getLifeCycle("setting");
      if (chromatticLifeCycle == null) {
          throw new NullPointerException("Lifecycle Setting null");
      }
      this.eventManager = eventManager;
  }

  public void set(final Context context, final Scope scope, final String key, final SettingValue<?> value) {

    new SynchronizationTask<Object>() {
      @Override
      protected Object execute(SessionContext ctx) {

        ScopeEntity scopeEntity = getScope(context, scope);
        if (scopeEntity == null) {
          scopeEntity = createScope(context, scope);
        }
        scopeEntity.setValue(key, value.getValue());
//        ctx.getSession().save();
        return null;
      }
    }.executeWith(chromatticLifeCycle);
      SettingData data = new SettingData (EventType.SETTING_SET,new SettingKey(context, scope, key),value);
        eventManager.broadcastEvent(new Event<SettingServiceImpl,SettingData>(data.getEventType().toString(),this,data));
    }
    
  public SettingValue<?> get(final Context c, final Scope s, final String key) {
    Object got = new SynchronizationTask<Object>() {
      @Override
      protected Object execute(SessionContext ctx) {

        ScopeEntity scope = getScope(c, s);
        if (scope == null) {
          return null; // Property doesn't exist
        } else {
          return scope.getValue(key);
        }
      }
    }.executeWith(chromatticLifeCycle);

    if (got instanceof Long) {
      return SettingValue.create((Long) got);
    } else if (got instanceof String) {
      return SettingValue.create((String) got);
    }
    else if (got instanceof Double) {
      return SettingValue.create((Double) got);
    }
    else if (got instanceof Boolean) {
      return SettingValue.create((Boolean) got);
    }
    return null;
  }

// TODO
  // ** for migration service of settings **
//  public List<String> getAllSettings() {
//    return new SynchronizationTask<List<String>>() {
//      @Override
//      protected List<String> execute(SessionContext ctx) {
//
//        List<String> settings = new LinkedList<String>();
//        SimpleContextEntity contextEntity = null;
//        SettingsRoot settingsRoot = ctx.getSession().findByPath(SettingsRoot.class, "settings");
//        for (ContextEntity contextEntity1 : settingsRoot.getContexts().values()) {
//          if (contextEntity1 instanceof SimpleContextEntity) { // Global Settings
//            for (ScopeEntity s : ((SimpleContextEntity) contextEntity1).getScopes().values()) {
//              for (String setting : s.getProperties().keySet()) {
//                settings.add( )
//              }
//            }
//
//          }
//
//        }
//        switch(c) {
//          case GLOBAL:
//            contextEntity = (SimpleContextEntity) settingsRoot.getContext(c.name().toLowerCase());
//            break;
//          case USER:
//            SubContextEntity userContextEntity = (SubContextEntity) settingsRoot.getContext(c.name().toLowerCase());
//            contextEntity = userContextEntity.getContext(c.getId());
//            break;
//        }
//        ScopeEntity scopeEntity = contextEntity.getScope(s.name().toLowerCase());
//        if (scopeEntity == null) {
//          scopeEntity = ctx.getSession().insert(contextEntity, ScopeEntity.class, s.name().toLowerCase());
//        }
//
//        if (s.getId() == null) {
//          return scopeEntity;
//        } else {
//          ScopeEntity scopeInstanceEntity = scopeEntity.getInstance(s.getId());
//          if (scopeInstanceEntity == null) {
//            return ctx.getSession().insert(scopeEntity, ScopeEntity.class, s.getId());
//          }
//        }
//
//        return null;
//      }
//    }.executeWith(chromatticLifeCycle);
//
//    if (got instanceof Long) {
//      return SettingValue.create((Long) got);
//    } else if (got instanceof String) {
//      return SettingValue.create((String) got);
//    }
//    else if (got instanceof Double) {
//      return SettingValue.create((Double) got);
//    }
//    else if (got instanceof Boolean) {
//      return SettingValue.create((Boolean) got);
//    }
//    return null;
//  }
//
//
//  private Map<String,ContextEntity> getAllContexts() {
//    return new SynchronizationTask<Map<String,ContextEntity>>() {
//      @Override
//      protected Map<String, ContextEntity> execute(SessionContext ctx) {
//        return ctx.getSession().findByPath(SettingsRoot.class,"settings").getContexts();
//      }
//    }.executeWith(chromatticLifeCycle);
//  }
//
//  private List<String> getAllGlobalSettings() {
//    return new SynchronizationTask<List<String>>() {
//      @Override
//      protected List<String> execute(SessionContext ctx) {
//        // Root
//        SettingsRoot settings = ctx.getSession().findByPath(SettingsRoot.class, "settings");
//        SimpleContextEntity contextEntity = (SimpleContextEntity) settings.getContext(Context.GLOBAL.name());
//        for (ScopeEntity scopeEntity : contextEntity.getScopes().values()) {
//
//        }
////        return (List<ScopeEntity>) contextEntity.getScopes().values();
//      }
//    }.executeWith(chromatticLifeCycle);
//  }

  private ScopeEntity createScope(final Context c, final Scope s) {

    return new SynchronizationTask<ScopeEntity>() {
      @Override
      protected ScopeEntity execute(SessionContext ctx) {

        // Root
        SettingsRoot settings = ctx.getSession().findByPath(SettingsRoot.class, "settings");
        if (settings == null) {
          settings = ctx.getSession().insert(SettingsRoot.class, "settings");
        }
        // Context
        SimpleContextEntity contextEntity = null;
        switch(c) {
          case GLOBAL:
            contextEntity = (SimpleContextEntity) settings.getContext(c.name().toLowerCase());
            if (contextEntity == null) {
              contextEntity = ctx.getSession().insert(settings, SimpleContextEntity.class, c.name().toLowerCase());
            }
            break;

          case USER:
            SubContextEntity userContextEntity = (SubContextEntity) settings.getContext(c.name().toLowerCase());
            if (userContextEntity == null) {
              userContextEntity = ctx.getSession().insert(settings, SubContextEntity.class, c.name().toLowerCase());
            }
            contextEntity = userContextEntity.getContext(c.getId());
            if (contextEntity == null) {
              contextEntity = ctx.getSession().insert(userContextEntity, SimpleContextEntity.class, c.getId());
            }
            break;
        }

        // Scope
        ScopeEntity scopeEntity = contextEntity.getScope(s.name().toLowerCase());
        if (scopeEntity == null) {
          scopeEntity = ctx.getSession().insert(contextEntity, ScopeEntity.class, s.name().toLowerCase());
        }

        if (s.getId() == null) {
          return scopeEntity;
        } else {
          ScopeEntity scopeInstanceEntity = scopeEntity.getInstance(s.getId());
          if (scopeInstanceEntity == null) {
            return ctx.getSession().insert(scopeEntity, ScopeEntity.class, s.getId());
          }
        }

        return null;

      }
    }.executeWith(chromatticLifeCycle);

  }


  private ScopeEntity getScope(final Context c, final Scope s) {

    return new SynchronizationTask<ScopeEntity>() {
      @Override
      protected ScopeEntity execute(SessionContext ctx) {
        return ctx.getSession().findByPath(ScopeEntity.class,Tools.buildScopePath(c, s)); 
      }
    }.executeWith(chromatticLifeCycle);
  }
  
  private ContextEntity getContext(final Context context) {

    return new SynchronizationTask<ContextEntity>() {
      @Override
      protected ContextEntity execute(SessionContext ctx) {
        return ctx.getSession().findByPath(ContextEntity.class,Tools.buildContextPath(context)); 
      }
    }.executeWith(chromatticLifeCycle);
  }

  

  @Override
  public void remove(final Context c, final Scope s, final String key) {
     new SynchronizationTask<Object>() {
      @Override
      protected Object execute(SessionContext ctx) {
        ScopeEntity scope = getScope(c, s);
        if (scope == null) {
          return null; // Property doesn't exist
        } else {
          return scope.removeValue(key);
        }
      }
    }.executeWith(chromatticLifeCycle);
      SettingData data = new SettingData(EventType.SETTING_REMOVE_KEY,  new SettingKey(c, s, key));
      eventManager.broadcastEvent(new Event<SettingServiceImpl,SettingData>(data.getEventType().toString(),this,data));
    
  }
  
  public void remove(final Context context, final Scope scope) {
    if (Scope.GLOBAL.equals(scope)) {
      throw new IllegalArgumentException("The context type or Scope Type must be not GLOBAL");
    }
    if (scope.getId() == null) {
      throw new IllegalArgumentException("The id property of your scope parameter  must be not null");
    }
	  
     new SynchronizationTask<Object>() {
      @Override
      protected Object execute(SessionContext ctx) {
        ScopeEntity scopeEntity = getScope(context, scope);
        if (scopeEntity != null) {
          scopeEntity.remove();
        }
        return null;
      }
    }.executeWith(chromatticLifeCycle);
    
      SettingData data = new SettingData(EventType.SETTING_REMOVE_SCOPE,new SettingScope(context, scope));
        eventManager.broadcastEvent(new Event<SettingServiceImpl,SettingData>(data.getEventType().toString(),this,data));
  }
  
  public void remove(final Context context) {
    if (Context.GLOBAL.equals(context)) {
      throw new IllegalArgumentException("The context type must be not GLOBAL");
    }
    if (context.getId() == null) {
      throw new IllegalArgumentException("The id property of your context parameter must be not null");
    }
    new SynchronizationTask<Object>() {
      @Override
      protected Object execute(SessionContext ctx) {
        ContextEntity contextEntity = getContext(context);
        if (contextEntity != null) {
          contextEntity.remove();
        }
        return null;
      }
    }.executeWith(chromatticLifeCycle);
      SettingData data = new SettingData(EventType.SETTING_REMOVE_CONTEXT,  new SettingContext(context));
      eventManager.broadcastEvent(new Event<SettingServiceImpl,SettingData>(data.getEventType().toString(),this,data));
        // TODO Auto-generated catch block
    
    
  }

  public boolean startSynchronization() {
    if (chromatticLifeCycle.getManager().getSynchronization() == null) {
      chromatticLifeCycle.getManager().beginRequest();
      return true;
    }
    return false;
  }

  public void stopSynchronization(boolean requestClose) {
    if (requestClose) {
      chromatticLifeCycle.getManager().endRequest(true);
    }
  }

}

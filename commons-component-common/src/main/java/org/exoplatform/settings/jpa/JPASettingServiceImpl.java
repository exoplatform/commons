/*
 *
 *  * Copyright (C) 2003-2017 eXo Platform SAS.
 *  *
 *  * This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License
 *  as published by the Free Software Foundation; either version 3
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see<http://www.gnu.org/licenses/>.
 *
 */

package org.exoplatform.settings.jpa;

import static org.exoplatform.settings.jpa.EntityConverter.convertContextToContextEntity;
import static org.exoplatform.settings.jpa.EntityConverter.convertScopeToScopeEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.persistence.DataInitializer;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.EventType;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.api.settings.data.SettingContext;
import org.exoplatform.commons.api.settings.data.SettingData;
import org.exoplatform.commons.api.settings.data.SettingKey;
import org.exoplatform.commons.api.settings.data.SettingScope;
import org.exoplatform.commons.event.impl.EventManagerImpl;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.settings.jpa.dao.SettingContextDAO;
import org.exoplatform.settings.jpa.dao.SettingScopeDAO;
import org.exoplatform.settings.jpa.dao.SettingsDAO;
import org.exoplatform.settings.jpa.entity.ContextEntity;
import org.exoplatform.settings.jpa.entity.ScopeEntity;
import org.exoplatform.settings.jpa.entity.SettingsEntity;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Mar
 * 07, 2017
 */
public class JPASettingServiceImpl implements SettingService {

  private static final Log                                           LOG = ExoLogger.getLogger(JPASettingServiceImpl.class);

  private SettingsDAO                                                settingsDAO;

  private SettingContextDAO                                          settingContextDAO;

  private SettingScopeDAO                                            settingScopeDAO;

  private final EventManagerImpl<JPASettingServiceImpl, SettingData> eventManager;

  /**
   * JPASettingServiceImpl must depend on DataInitializer to make sure data
   * structure is created before initializing it
   */
  public JPASettingServiceImpl(SettingsDAO settingsDAO,
                               SettingContextDAO settingContextDAO,
                               SettingScopeDAO settingScopeDAO,
                               EventManagerImpl<JPASettingServiceImpl, SettingData> eventManager,
                               DataInitializer dataInitializer) {
    this.settingsDAO = settingsDAO;
    this.settingContextDAO = settingContextDAO;
    this.settingScopeDAO = settingScopeDAO;
    this.eventManager = eventManager;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @ExoTransactional
  public void set(Context context, Scope scope, String key, SettingValue<?> value) {
    validateContextArgument(context);
    validateScopeArgument(scope);
    validateArgumentNullability(key, "setting name is null");
    validateArgumentNullability(value, "setting value is null");

    try {
      if (context == null) {
        LOG.warn("Context is null, can't save setting key={}, value={}", key, value.getValue());
        return;
      }
      if (scope == null) {
        scope = Scope.GLOBAL.id(null);
      }
      LOG.debug("=== setting save, contextType={}, contextName={}, scopeType={} and scopeName={} and key={} and value={}",
                context.getName(),
                context.getId(),
                scope.getName(),
                scope.getId(),
                key,
                value.getValue());
      ContextEntity contextEntity = settingContextDAO.getContextByTypeAndName(context.getName(), context.getId());
      boolean contextAndScopeFound = true;
      if (contextEntity == null) {
        contextAndScopeFound = false;
        contextEntity = settingContextDAO.create(convertContextToContextEntity(context));
      }
      ScopeEntity scopeEntity = settingScopeDAO.getScopeByTypeAndName(scope.getName(), scope.getId());
      if (scopeEntity == null) {
        contextAndScopeFound = false;
        scopeEntity = convertScopeToScopeEntity(scope);
        scopeEntity = settingScopeDAO.create(scopeEntity);
      }

      SettingsEntity settingsEntity = null;
      if (contextAndScopeFound) {
        // If context or scope not found, the setting doesn't exist consequently
        // So no need to request database for the setting
        settingsEntity = settingsDAO.getSettingByContextAndScopeAndKey(context.getName(),
                                                                       context.getId(),
                                                                       scope.getName(),
                                                                       scope.getId(),
                                                                       key);
      }
      if (settingsEntity == null) {
        settingsDAO.create(new SettingsEntity().setScope(scopeEntity)
                                               .setContext(contextEntity)
                                               .setName(key)
                                               .setValue(value.getValue().toString()));
      } else {
        settingsEntity.setValue(value.getValue().toString());
        settingsDAO.update(settingsEntity);
      }

      // broadcast event
      SettingData data = new SettingData(EventType.SETTING_SET, new SettingKey(context, scope, key), value);
      eventManager.broadcastEvent(new Event<JPASettingServiceImpl, SettingData>(data.getEventType().toString(), this, data));
    } catch (Exception e) {
      LOG.error("An error occurred while saving setting: contextType=" + context.getName() + ", contextName=" + context.getId()
          + ", scopeType=" + scope.getName() + " and scopeName=" + scope.getId() + " and key=" + key + " and value="
          + value.getValue(), e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @ExoTransactional
  public void remove(Context context, Scope scope, String key) {
    validateContextArgument(context);
    validateScopeArgument(scope);
    validateArgumentNullability(key, "setting name is null");

    SettingsEntity setting = settingsDAO.getSettingByContextAndScopeAndKey(context.getName(),
                                                                           context.getId(),
                                                                           scope.getName(),
                                                                           scope.getId(),
                                                                           key);
    if (setting != null) {
      settingsDAO.delete(setting);
      SettingData data = new SettingData(EventType.SETTING_REMOVE_KEY, new SettingKey(context, scope, key));
      eventManager.broadcastEvent(new Event<JPASettingServiceImpl, SettingData>(data.getEventType().toString(), this, data));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @ExoTransactional
  public void remove(Context context, Scope scope) {
    validateContextArgument(context);
    validateScopeArgument(scope);

    List<SettingsEntity> settings = settingsDAO.getSettingsByContextAndScope(context.getName(),
                                                                             context.getId(),
                                                                             scope.getName(),
                                                                             scope.getId());
    settingsDAO.deleteAll(settings);
    SettingData data = new SettingData(EventType.SETTING_REMOVE_SCOPE, new SettingScope(context, scope));
    eventManager.broadcastEvent(new Event<JPASettingServiceImpl, SettingData>(data.getEventType().toString(), this, data));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @ExoTransactional
  public void remove(Context context) {
    validateContextArgument(context);

    List<SettingsEntity> settings = settingsDAO.getSettingsByContextTypeAndName(context.getName(), context.getId());
    settingsDAO.deleteAll(settings);
    SettingData data = new SettingData(EventType.SETTING_REMOVE_CONTEXT, new SettingContext(context));
    eventManager.broadcastEvent(new Event<JPASettingServiceImpl, SettingData>(data.getEventType().toString(), this, data));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @ExoTransactional
  public SettingValue<?> get(Context context, Scope scope, String key) {
    validateContextArgument(context);
    validateScopeArgument(scope);
    validateArgumentNullability(key, "setting name is null");

    SettingsEntity setting = settingsDAO.getSettingByContextAndScopeAndKey(context.getName(),
                                                                           context.getId(),
                                                                           scope.getName(),
                                                                           scope.getId(),
                                                                           key);
    if (setting == null) {
      return null; // Property doesn't exist
    } else {
      return SettingValue.create((String) setting.getValue());
    }
  }

  /**
   * Saves a {@link Context} on database
   * 
   * @param context context to save
   */
  @ExoTransactional
  public void save(Context context) {
    validateContextArgument(context);

    ContextEntity contextEntity = settingContextDAO.getContextByTypeAndName(context.getName(), context.getId());
    if (contextEntity == null) {
      contextEntity = new ContextEntity();
      contextEntity.setType(context.getName());
      contextEntity.setName(context.getId());
      settingContextDAO.create(contextEntity);
    }
  }

  /**
   * Get settings related to a scope and a context
   * 
   * @param context {@link Context} used to search settings
   * @return {@link Map} of settings with key = setting name and as value =
   *         corresponding {@link SettingValue}
   */
  @ExoTransactional
  public Map<Scope, Map<String, SettingValue<String>>> getSettingsByContext(Context context) {
    validateContextArgument(context);

    Collection<SettingsEntity> settings = settingsDAO.getSettingsByContextTypeAndName(context.getName(), context.getId());
    if (settings == null || settings.isEmpty()) {
      return Collections.emptyMap();
    } else {
      Map<Scope, Map<String, SettingValue<String>>> settingsByScopeByName =
                                                                          settings.stream()
                                                                                  .collect(Collectors.groupingBy(setting -> new Scope(setting.getScope()
                                                                                                                                             .getType(),
                                                                                                                                      setting.getScope()
                                                                                                                                             .getName()),
                                                                                                                 Collectors.toMap(setting -> setting.getName(),
                                                                                                                                  setting -> SettingValue.create((String) setting.getValue()))));
      return settingsByScopeByName;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long countContextsByType(String contextType) {
    validateArgumentNullability(contextType, "context type is null");
    return settingContextDAO.countContextsByType(contextType);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getContextNamesByType(String contextType, int offset, int limit) {
    validateArgumentNullability(contextType, "context type is null");
    return settingContextDAO.getContextNamesByType(contextType, offset, limit);
  }

  /**
   * Gets a list of names of contexts of a chosen type that doesn't have
   * settings associated to a dedicated scope
   * 
   * @param contextType type of context used in filter
   * @param scopeType type of scope used in filter
   * @param scopeName name of scope used in filter
   * @param offset search query offset
   * @param limit search query limit
   * @return a {@link Set} of {@link String} for filtered context names
   */
  @ExoTransactional
  public Set<String> getEmptyContextsByScopeAndContextType(String contextType,
                                                           String scopeType,
                                                           String scopeName,
                                                           int offset,
                                                           int limit) {
    validateArgumentNullability(contextType, "context type is null");
    validateArgumentNullability(scopeType, "scope type is null");

    List<ContextEntity> emptyContexts = settingContextDAO.getEmptyContextsByScopeAndContextType(contextType,
                                                                                                scopeType,
                                                                                                scopeName,
                                                                                                offset,
                                                                                                limit);
    return emptyContexts.stream().map(context -> context.getName()).collect(Collectors.toSet());
  }

  /**
   * Gets a list of names of contexts of a chosen type that have a setting
   * associated to a dedicated scope
   * 
   * @param contextType type of context used in filter
   * @param scopeType type of scope used in filter
   * @param scopeName name of scope used in filter
   * @param settingName name of setting used in filter
   * @param offset search query offset
   * @param limit search query limit
   * @return a {@link List} of {@link String} for filtered context names
   */
  @ExoTransactional
  public List<Context> getContextsByTypeAndScopeAndSettingName(String contextType,
                                                               String scopeType,
                                                               String scopeName,
                                                               String settingName,
                                                               int offset,
                                                               int limit) {
    validateArgumentNullability(contextType, "context type is null");
    validateArgumentNullability(scopeType, "scope type is null");
    validateArgumentNullability(settingName, "setting name is null");

    List<ContextEntity> contexts =
                                 settingContextDAO.getContextsByTypeAndSettingNameAndScope(contextType,
                                                                                           scopeType,
                                                                                           scopeName,
                                                                                           settingName,
                                                                                           offset,
                                                                                           limit);
    return contexts.stream().map(context -> new Context(context.getType(), context.getName())).collect(Collectors.toList());
  }

  @ExoTransactional
  public long countSettingsByNameAndValueAndScope(Scope scope, String key, String value) {
    validateScopeArgument(scope);
    validateArgumentNullability(key, "setting name is null");
    validateArgumentNullability(value, "setting value is null");
    return settingsDAO.countSettingsByNameAndValueAndScope(scope.getName(), scope.getId(), key, value);
  }

  private void validateScopeArgument(Scope scope) {
    if(scope == null) {
      throw new IllegalArgumentException("scope is null");
    }
    validateArgumentNullability(scope.getName(), "scope name is null");
  }

  private void validateContextArgument(Context context) {
    if(context == null) {
      throw new IllegalArgumentException("context is null");
    }
    validateArgumentNullability(context.getId(), "context id is null");
    validateArgumentNullability(context.getName(), "context name is null");
  }

  private void validateArgumentNullability(String arg, String message) {
    if(StringUtils.isBlank(arg)) {
      throw new IllegalArgumentException(message);
    }
  }

  private void validateArgumentNullability(Object obj, String message) {
    if(obj == null) {
      throw new IllegalArgumentException(message);
    }
  }
}

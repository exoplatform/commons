/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.settings.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.api.settings.data.SettingContext;
import org.exoplatform.commons.api.settings.data.SettingKey;
import org.exoplatform.commons.api.settings.data.SettingScope;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.future.FutureExoCache;
import org.exoplatform.services.cache.future.Loader;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.settings.cache.selector.SettingCacheSelector;
import org.exoplatform.settings.jpa.JPASettingServiceImpl;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform bangnv@exoplatform.com
 * Nov 15, 2012
 * CacheSettingServiceImpl is implemented for application which uses cache. CacheSettingServiceImpl contains also settingService for database. 
 * In case of saving and removing setting properties, CacheSettingService will effect the change in cache and database. 
 * Otherwise, it will search setting properties in cache at first and then in database, that allows to improve performance.
 * @LevelAPI Experimental    
 */
public class CacheSettingServiceImpl implements SettingService {
  /** Logger */
  private static final Log                                               LOG = ExoLogger.getLogger(CacheSettingServiceImpl.class);

  protected ExoCache<SettingKey, SettingValue>                           settingCache;

  protected FutureExoCache<SettingKey, SettingValue, SettingService> futureExoCache;

  private static final Logger                                            log = LoggerFactory.getLogger(CacheSettingServiceImpl.class);

  private final SettingService                                            service;
/**
 * Create cache setting service object with service for database and service for cache
 * @param service Setting service for database
 * @param cacheService Cache service
 * @LevelAPI Experimental
 */
  public CacheSettingServiceImpl(JPASettingServiceImpl service, CacheService cacheService) {

    settingCache = cacheService.getCacheInstance(SettingService.class.getSimpleName());

    Loader<SettingKey, SettingValue, SettingService> loader = new Loader<SettingKey, SettingValue, SettingService>() {
      @Override
      public SettingValue retrieve(SettingService service, SettingKey key) throws Exception {
        SettingValue<?> settingValue = service.get(key.getContext(), key.getScope(), key.getKey());
        if(settingValue == null) {
          settingValue = NullSettingValue.getInstance();
        }
        return settingValue;
      }
    };
    futureExoCache = new FutureExoCache<SettingKey, SettingValue, SettingService>(loader,
                                                                                      settingCache);
    this.service = service;

  }

  /**
   *  Set the specified value  with the key which is composed by context, scope, key. 
   *  The value will be saved in the cache and the database 
   * @param context context with which the specified value is to be associated
   * @param scope   scope with which  the specified value is to be associated
   * @param key     key with which the specified value is to be associated
   * @param value   value to be associated with the specified key.
   * @LevelAPI Experimental
   */
  @Override
  public void set(Context context, Scope scope, String key, SettingValue<?> value) {
    SettingKey settingKey = new SettingKey(context, scope, key);
    settingCache.put(settingKey, value);
    service.set(context, scope, key, value);
  }

  /**
   * Get setting value associated with composite key(context, scope, key)
   * This service will search in the cache first and then in the database.
   * @return Setting value with type of setting property, and null if the cache and the database doesn't contain the value for the composite key
   * @LevelAPI Experimental
   */
  @Override
  public SettingValue<?> get(Context context, Scope scope, String key) {
    try {
      SettingValue<?> settingValue = futureExoCache.get(service, new SettingKey(context, scope, key));
      if(settingValue == NullSettingValue.getInstance() || settingValue.getValue() == null) {
        return null;
      }
      return settingValue;
    } catch (Exception e) {
      if(LOG.isDebugEnabled()) {
        LOG.error("Exception raising when getting setting value ", e);
      } else {
        if(context != null) {
          LOG.warn("Exception raising when getting setting value associated to the key " + key + " and the context " + context.getName());
        } else {
          LOG.warn("Can't get setting value. The context is null");
        }
      }
    }
    return null;
  }

  /** 
   * Remove all the value associated with the composite key(context,scope,key) in cache and also in database.
   * @param context context with which the specified value is to be associated. The context type must be USER and context.id must be not null.
   * @param scope  	scope with which  the specified value is to be associated. The scope.id must be not null.
   * @param key		key with which the specified value is to be associated
   * @LevelAPI Experimental
   */
  @Override
  public void remove(Context context, Scope scope, String key) {
    SettingKey settingKey = new SettingKey(context, scope, key);
    settingCache.remove(settingKey);
    service.remove(context, scope, key);
  }

  /** remove all the value associated with the specified context and specified scope in cache and database also.
   * @param context context with which the specified value is to be associated. The context type must be USER and context.id must be not null.
   * @param scope  scope with which  the specified value is to be associated. The scope.id must be not null.
   * @LevelAPI Experimental
   */
  @Override
  public void remove(Context context, Scope scope) {
    SettingScope settingScope = new SettingScope(context, scope);
    try {
      settingCache.select(new SettingCacheSelector(settingScope));
    } catch (Exception e) {
      LOG.error("Cannot get setting cache",e);
    }
    service.remove(context, scope);
  }
  
  /** remove all the value associated with the specified context in cache and database also.
   * @param context context with which the specified value is to be associated. The context type must be USER and context.id must be not null.
   * @LevelAPI Experimental
   */
  @Override
  public void remove(Context context) {
    SettingContext settingContext = new SettingContext(context);
    try {
      settingCache.select(new SettingCacheSelector(settingContext));
    } catch (Exception e) {
      LOG.error("cannot get setting context",e);
    }
    service.remove(context);
  }

  @Override
  public long countContextsByType(String contextType) {
    return service.countContextsByType(contextType);
  }

  @Override
  public List<String> getContextNamesByType(String contextType, int offset, int limit) {
    return service.getContextNamesByType(contextType, offset, limit);
  }

  @Override
  public Map<Scope, Map<String, SettingValue<String>>> getSettingsByContext(Context context) {
    return service.getSettingsByContext(context);
  }

  @Override
  public List<Context> getContextsByTypeAndScopeAndSettingName(String contextType,
                                                               String scopeType,
                                                               String scopeName,
                                                               String settingName,
                                                               int offset,
                                                               int limit) {
    return service.getContextsByTypeAndScopeAndSettingName(contextType, scopeType, scopeName, settingName, offset, limit);
  }

  @Override
  public Set<String> getEmptyContextsByScopeAndContextType(String name, String name2, String id, int offset, int limit) {
    return service.getEmptyContextsByScopeAndContextType(name, name2, id, offset, limit);
  }

  @Override
  public void save(Context context) {
    service.save(context);
  }

}

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
import org.exoplatform.settings.impl.SettingServiceImpl;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

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

  protected FutureExoCache<SettingKey, SettingValue, SettingServiceImpl> futureExoCache;

  private static final Logger                                            log = LoggerFactory.getLogger(CacheSettingServiceImpl.class);

  private final SettingServiceImpl                                       service;
/**
 * Create cache setting service object with service for database and service for cache
 * @param service Setting service for database
 * @param cacheService Cache service
 * @LevelAPI Experimental
 */
  public CacheSettingServiceImpl(SettingServiceImpl service, CacheService cacheService) {

    settingCache = cacheService.getCacheInstance(SettingService.class.getSimpleName());

    Loader<SettingKey, SettingValue, SettingServiceImpl> loader = new Loader<SettingKey, SettingValue, SettingServiceImpl>() {
      @Override
      public SettingValue retrieve(SettingServiceImpl service, SettingKey key) throws Exception {
        return service.get(key.getContext(), key.getScope(), key.getKey());
      }
    };
    futureExoCache = new FutureExoCache<SettingKey, SettingValue, SettingServiceImpl>(loader,
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
    return futureExoCache.get(service, new SettingKey(context, scope, key));
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

}

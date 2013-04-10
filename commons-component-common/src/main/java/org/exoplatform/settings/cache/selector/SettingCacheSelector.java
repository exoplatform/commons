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
package org.exoplatform.settings.cache.selector;

import org.exoplatform.commons.api.settings.data.SettingContext;
import org.exoplatform.commons.api.settings.data.SettingKey;
import org.exoplatform.commons.api.settings.data.SettingScope;
import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ObjectCacheInfo;

/**
 * Created by The eXo Platform SAS
 * Author : Nguyen Viet Bang
 *          bangnv@exoplatform.com
 * Nov 27, 2012  
 * This class allows to select all setting cache objects which are in a specified Context or Scope.
 * The callback function of this selector is to remove all selected setting object.
 * @LevelAPI Experimental
 */

public class SettingCacheSelector implements CachedObjectSelector<SettingKey,Object>{
  private SettingContext provider;
  
  /**
   * Create a selector with a specified context 
   * @param provider context or scope with which the specified value is to be associated
   * @LevelAPI Experimental
   */
  public SettingCacheSelector( SettingContext provider) {
    if (provider==null) {
      throw new NullPointerException();
    }
    if(provider instanceof SettingScope ) {
      this.provider =(SettingScope) provider;
    }
    else {
    this.provider = provider;
    }
  }

  /**
   * This function allows this selector could select right object.
   * @param key 	the key which is composed by context, scope, key
   * @param ocinfo	cache info (expire time, cache associated object) 
   * @return return true if compared key is equals to provider, false if not
   * @LevelAPI Experimental
   */   
  @Override  
  public boolean select(SettingKey key, ObjectCacheInfo<? extends Object> ocinfo) {
    if (key!=null) {
      return provider.equals(key);
    }
    return false;
  }

  /**
   * Callback function if select function return true. This function will remove this selected setting key from cache.
   * @param cache	ExoCache 
   * @param key		setting will be removed
   * @param cinfo	cache info
   * @return This function will remove specified setting from cache.
   * @LevelAPI Experimental
   */
  @Override  
  public void onSelect(ExoCache<? extends SettingKey, ? extends Object> cache,
                       SettingKey key,
                       ObjectCacheInfo<? extends Object> ocinfo) throws Exception {
    cache.remove(key)    ;
  }

}

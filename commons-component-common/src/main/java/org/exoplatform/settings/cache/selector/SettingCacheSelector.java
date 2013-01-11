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
 */
public class SettingCacheSelector implements CachedObjectSelector<SettingKey,Object>{
  private SettingContext provider;
  

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

  @Override
  public boolean select(SettingKey key, ObjectCacheInfo<? extends Object> ocinfo) {
    if (key!=null) {
      return provider.equals(key);
    }
    return false;
  }

  @Override
  public void onSelect(ExoCache<? extends SettingKey, ? extends Object> cache,
                       SettingKey key,
                       ObjectCacheInfo<? extends Object> ocinfo) throws Exception {
    cache.remove(key)    ;
  }



}

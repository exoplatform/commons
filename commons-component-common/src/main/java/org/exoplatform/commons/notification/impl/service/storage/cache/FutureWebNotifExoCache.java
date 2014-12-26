/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.commons.notification.impl.service.storage.cache;

import java.io.Serializable;

import org.exoplatform.commons.cache.future.Loader;
import org.exoplatform.services.cache.ExoCache;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 15, 2014  
 */
public class FutureWebNotifExoCache<T, K extends Serializable, V extends AbstractWebNotifListData<K, T>, C> extends FutureWebNotifCache<K, V, C> {

  /** . */
  private final ExoCache<K, V> cache;

  public FutureWebNotifExoCache(Loader<K, V, C> loader, ExoCache<K, V> cache) {
    super(loader);

    //
    this.cache = cache;
  }

  public void clear() {
    cache.clearCache();
  }

  public void remove(K key) {
    cache.remove(key);
  }

  @Override
  protected V get(K key, long offset, long limit) {
    V v = cache.get(key);
    if (v == null) return null;
    //find better way to handle
    int size = (int)(offset + limit);
    //if the sublist's size < limit, return NULL for continue Loader
    return v.size() >= size ? v : null;
  }

  @Override
  protected void put(K key, V entry) {
    cache.put(key, entry);
  }
}
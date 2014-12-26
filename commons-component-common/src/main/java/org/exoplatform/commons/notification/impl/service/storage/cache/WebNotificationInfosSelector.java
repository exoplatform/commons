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

import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ObjectCacheInfo;

public class WebNotificationInfosSelector implements CachedObjectSelector<ListWebNotificationsKey, Object> {
  private final String userId;
  
  public WebNotificationInfosSelector(String userId) {
    this.userId = userId;
  }
  
  @Override
  public boolean select(ListWebNotificationsKey key, ObjectCacheInfo<? extends Object> ocinfo) {
    if (key != null && key.getUserId() != null) {
      return key.getUserId().equals(userId);
    }
    return false;
  }

  @Override
  public void onSelect(ExoCache<? extends ListWebNotificationsKey, ? extends Object> cache,
      ListWebNotificationsKey key, ObjectCacheInfo<? extends Object> ocinfo) throws Exception {
    cache.remove(key);
  }

}

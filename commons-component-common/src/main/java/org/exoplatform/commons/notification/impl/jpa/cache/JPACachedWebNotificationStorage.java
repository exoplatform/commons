/*
 * Copyright (C) 2003-2018 eXo Platform SAS.
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

package org.exoplatform.commons.notification.impl.jpa.cache;

import org.exoplatform.commons.notification.impl.jpa.web.JPAWebNotificationStorage;
import org.exoplatform.commons.notification.impl.service.storage.cache.CachedWebNotificationStorage;
import org.exoplatform.services.cache.CacheService;

public class JPACachedWebNotificationStorage extends CachedWebNotificationStorage {
  public JPACachedWebNotificationStorage(JPAWebNotificationStorage storage, CacheService cacheService) {
    super(null, cacheService);
    setStorage(storage);
  }
}

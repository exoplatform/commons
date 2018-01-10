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

package org.exoplatform.commons.notification.impl.service.storage.cache.model;

import java.io.Serializable;

import org.exoplatform.commons.notification.impl.service.storage.cache.CacheKey;

public class ListWebNotificationsKey implements CacheKey, Serializable {
  private static final long serialVersionUID = 1L;

  private final boolean     isOnPopover;

  private final String      userId;

  private final int         offset;

  private final int         limit;

  private ListWebNotificationsKey(final String userId, final boolean isOnPopover, int offset, int limit) {
    this.userId = userId;
    this.isOnPopover = isOnPopover;
    this.offset = offset;
    this.limit = limit;
  }
  
  public static ListWebNotificationsKey key(String userId, boolean isOnPopover, int offset, int limit) {
    return new ListWebNotificationsKey(userId, isOnPopover,offset, limit);
  }
  
  public boolean isOnPopover() {
    return isOnPopover;
  }

  public String getUserId() {
    return userId;
  }

  public int getOffset() {
    return offset;
  }

  public int getLimit() {
    return limit;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ListWebNotificationsKey)) {
      return false;
    }
    
    ListWebNotificationsKey that = (ListWebNotificationsKey) o;

    if (userId != null ? !userId.equals(that.userId) : that.userId != null)
      return false;
    
    if (offset != that.offset) {
      return false;
    }
    
    if (limit != that.limit) {
      return false;
    }
    
    if (isOnPopover != that.isOnPopover) {
      return false;
    }
    
    return true;
  }

  @Override
  public int hashCode() {
    int result = 31 * (userId != null ? userId.hashCode() : 0);
    result = 31 * result + (isOnPopover ? 1 : 0);
    result = 31 * result + offset;
    result = 31 * result + limit;
    return result;
  }

}

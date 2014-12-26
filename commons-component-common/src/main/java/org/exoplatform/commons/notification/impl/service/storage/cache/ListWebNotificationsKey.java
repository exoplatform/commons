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

public class ListWebNotificationsKey implements CacheKey, Serializable {
  private static final long serialVersionUID = 1L;
  private final boolean isOnPopover;
  private final String userId;

  private ListWebNotificationsKey(final String userId, final boolean isOnPopover) {
    this.userId = userId;
    this.isOnPopover = isOnPopover;
  }
  
  public static ListWebNotificationsKey key(String userId, boolean isOnPopover) {
    return new ListWebNotificationsKey(userId, isOnPopover);
  }
  
  public boolean isOnPopover() {
    return isOnPopover;
  }

  public String getUserId() {
    return userId;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ListWebNotificationsKey)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    ListWebNotificationsKey that = (ListWebNotificationsKey) o;

    if (userId != that.userId) {
      return false;
    }
    
    if (isOnPopover != that.isOnPopover) {
      return false;
    }
    
    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (userId != null ? userId.hashCode() : 0);
    result = 31 * result + (isOnPopover ? 1231 : 1237);
    return result;
  }

}

/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.commons.api.notification.model;

import java.io.Serializable;

import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;

public final class NotificationKey implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String id;
  
  public NotificationKey(AbstractNotificationPlugin plugin) {
    this(plugin.getId());
  }
  
  public NotificationKey(String id) {
    this.id = id;
  }

  public static NotificationKey key(AbstractNotificationPlugin plugin) {
    return new NotificationKey(plugin);
  }
  
  public static NotificationKey key(String id) {
    return new NotificationKey(id);
  }
  
  public String getId() {
    return this.id;
  }
  
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NotificationKey)) {
      return false;
    }

    NotificationKey that = (NotificationKey) o;

    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }

    return true;
  }
  @Override
  public int hashCode() {
    return (id != null ? id.hashCode() : 0);
  }
  
  @Override
  public String toString() {
      return "NotificationKey[id=" + id + "]";
  }

}

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
package org.exoplatform.commons.api.notification.model;

import java.io.Serializable;

import org.exoplatform.commons.api.notification.channel.AbstractChannel;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 14, 2014  
 */
public class ChannelKey implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String id;
  
  public ChannelKey(AbstractChannel channel) {
    this(channel.getId());
  }
  
  public ChannelKey(String id) {
    this.id = id;
  }

  public static ChannelKey key(AbstractChannel channel) {
    return new ChannelKey(channel);
  }
  
  public static ChannelKey key(String id) {
    return new ChannelKey(id);
  }
  
  public String getId() {
    return this.id;
  }
  
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ChannelKey)) {
      return false;
    }

    ChannelKey that = (ChannelKey) o;

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
      return "ChannelKey[id=" + id + "]";
  }

}
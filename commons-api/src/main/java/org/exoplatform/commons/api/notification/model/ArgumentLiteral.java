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

public final class ArgumentLiteral<T> implements Cloneable {
  
  private final Class<T> type;
  private String key;
  
  public ArgumentLiteral(Class<T> type, String key) {
    this.type = type;
    this.key = key;
  }
  
  public ArgumentLiteral(Class<T> type) {
    this(type, null);
  }

  public Class<T> getType() {
    return type;
  }
  
  public String getKey() {
    return key;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    
    ArgumentLiteral<?> other = null;
    if (obj instanceof ArgumentLiteral) {
      other = (ArgumentLiteral<?>) obj;
    } else {
      return false;
    }
    
    return this.type.equals(other.type);
  }
  
  
  public ArgumentLiteral<T> clone() throws CloneNotSupportedException {
    Object obj = super.clone();
    return (obj instanceof  ArgumentLiteral<?>) ? (ArgumentLiteral<T>) obj : null;
  }
}

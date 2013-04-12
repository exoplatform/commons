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
package org.exoplatform.commons.api.settings.data;


/**
 * SettingKey is composed by [context,scope,key], associates with a specified setting property value.
 * Use SettingKey to specify context of setting property in action with database, cache or in dispatching setting event.
 * @LevelAPI Experimental
 */
public class SettingKey extends SettingScope {

  private static final long serialVersionUID = 7109224384495691388L;

  private String            key;

  /**
   * Create setting key with composite key [context,scope,key]
   * @param context context with which the specified value is to be associated
   * @param scope scope with which the specified value is to be associated
   * @param key key with which the specified value is to be associated
   * @LevelAPI Experimental
   */
  public SettingKey(Context context, Scope scope, String key) {
    super(context, scope);
    this.key = key;
  }

  /**
   * get key value of setting-key
   * @return value of key
   * @LevelAPI Experimental
   */
  public String getKey() {
    return key;
  }

  /**
   * set key value to setting-key
   * @param key
   * @LevelAPI Experimental
   */
  public void setKey(String key) {
    this.key = key;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (obj instanceof SettingKey) {
      SettingKey that = (SettingKey) obj;
      return key.equals(that.getKey());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = super.repositoryName.hashCode();
    result = 31 * result + scopePath.hashCode();
    result = 31 * result + key.hashCode();
    return result;
  }

}

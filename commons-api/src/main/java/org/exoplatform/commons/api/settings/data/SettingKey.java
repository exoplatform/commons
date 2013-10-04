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
 * Associates setting properties with a specified key.
 * SettingKey is composed by [context, scope, key], and is used to
 * specify context of setting properties at the Key level when working with database, cache or dispatching the setting event.
 * @LevelAPI Experimental
 */
public class SettingKey extends SettingScope {

  private static final long serialVersionUID = 7109224384495691388L;

  private String            key;

  /**
   * Creates a SettingKey with a composite value [context, scope, key].
   * @param context The context value.
   * @param scope The scope value.
   * @param key The key value.
   * @LevelAPI Experimental
   */
  public SettingKey(Context context, Scope scope, String key) {
    super(context, scope);
    this.key = key;
  }

  /**
   * Gets a key value of the SettingKey object.
   * @return The key value.
   * @LevelAPI Experimental
   */
  public String getKey() {
    return key;
  }

  /**
   * Sets a key value of the SettingKey object.
   * @param key The key value.
   * @LevelAPI Experimental
   */
  public void setKey(String key) {
    this.key = key;
  }
  /**
   * Compares a specified object with the SettingKey for equality.
   */
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
  /**
   * Returns the hash code value for the SettingKey object.
   */
  @Override
  public int hashCode() {
    int result = super.repositoryName.hashCode();
    result = 31 * result + scopePath.hashCode();
    result = 31 * result + key.hashCode();
    return result;
  }

}

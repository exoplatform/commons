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
package org.exoplatform.commons.api.settings;

import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;

/**
 * Stores and removes a value associated with a key in JCR.
 * @LevelAPI Experimental
 */
public interface SettingService {

  /**
   * Sets a value with the key that is composed by context, scope,
   * key. The value will be saved in the database.
   * 
   * @param context The context with which the specified value is associated.
   * @param scope The scope with which the specified value is associated.
   * @param key The key with which the specified value is associated.
   * @param value The value associated with the specified key.
   * @LevelAPI Experimental
   */
  public void set(Context context, Scope scope, String key, SettingValue<?> value);

  /**
   * Removes a value associated with a specified composite key.
   * 
   * @param context The context with which the specified value is associated.
   * @param scope The scope with which the specified value is associated.
   * @param key The key with which the specified value is associated.
   * @LevelAPI Experimental
   */
  public void remove(Context context, Scope scope, String key);

  /**
   * Removes all values associated with a specified context and
   * scope from the database.
   * 
   * @param context The context with which the specified value is associated.
   *          The context type must be USER and context, and Id must not be "null".
   * @param scope The scope with which the specified value is associated. The
   *          scope.id must not be "null".
   * @LevelAPI Experimental
   */
  public void remove(Context context, Scope scope);

  /**
   * Removes all values associated with a specified context from the database.
   * @param context The context with which the specified value is associated.
   * The context type must be USER and context, and Id must not be "null".
   * @LevelAPI Experimental
   */
  public void remove(Context context);

  /**
   * Gets values associated with a specified composite key (context, scope, key) in
   * the database.
   * 
  * @param context The context with which the specified value is associated.
   *          The context type must be USER and context and Id must not be "null".
   * @param scope The scope with which the specified value is associated. The
   *          scope.id must not be "null".
   * @param key The key with which the specified value is associated.
   * @LevelAPI Experimental
   */
  public SettingValue<?> get(Context context, Scope scope, String key);

}

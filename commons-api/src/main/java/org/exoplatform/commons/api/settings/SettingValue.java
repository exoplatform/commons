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

import java.io.Serializable;

/**
 * All possible value types are stored in JCR.
 * @param T Type of the setting value object.
 * @LevelAPI Experimental
 */
public class SettingValue<T extends Object> implements Serializable {

  private static final long serialVersionUID = -1799382996718420564L;
  T value;

  /**
   * Creates a setting value object with a specified value type.
   * @param value The value type.
   * @LevelAPI Experimental
   */
public SettingValue(T value) {
    this.value = value;
  }


  /**
   * Gets the value of the setting object.
   * @return The value with the specified object type.
   * @LevelAPI Experimental
   */
  public T getValue() {
    return value;
  }

  /**
   * Creates a setting value object of the String type.
   * @param value The String value.
   * @return The setting value object of the String type.
   * @LevelAPI Experimental
   */
  public static SettingValue<String> create(String value) { return new SettingValue<String>(value); }
  /**
   * Creates a setting value object of the Long type.
   * @param value The Long value.
   * @return The setting value object of the Long type.
   * @LevelAPI Experimental
   */  
  public static SettingValue<Long> create(Long value) { return new SettingValue<Long>(value); }
  /**
   * Creates a setting value object of the Double type.
   * @param value The Double value.
   * @return The setting value object of the Double type.
   * @LevelAPI Experimental
   */
  public static SettingValue<Double> create(Double value) { return new SettingValue<Double>(value); }
  /**
   * Creates a setting value object of the Boolean type.
   * @param value The Boolean value.
   * @return The setting value object of the Boolean type.
   * @LevelAPI Experimental
   */ 
  public static SettingValue<Boolean> create(Boolean value) { return new SettingValue<Boolean>(value); }

}
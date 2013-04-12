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
 * All possible value type stored in JCR 
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @LevelAPI Platform
 */
public class SettingValue<T extends Object> implements Serializable {

  private static final long serialVersionUID = -1799382996718420564L;
  T value;

  /**
   * Create a setting value object with a specify value type
   * @param value
   * @LevelAPI Platform
   */
public SettingValue(T value) {
    this.value = value;
  }


  /**
   * Get value object of setting value
   * @return value in specified type
   * @LevelAPI Platform
   */
  public T getValue() {
    return value;
  }

  /**
   * create setting value object of type String
   * @param value String value of setting property will be created
   * @return created SettingValue object of type String
   * @LevelAPI Platform
   */
  public static SettingValue<String> create(String value) { return new SettingValue<String>(value); }
  /**
   * create setting value object of type Long
   * @param value Long value of setting property will be created
   * @return created SettingValue object of type Long
   * @LevelAPI Platform
   */  
  public static SettingValue<Long> create(Long value) { return new SettingValue<Long>(value); }
  /**
   * create setting value object of type Double
   * @param value Double value of setting property will be created
   * @return created SettingValue object of type Double
   * @LevelAPI Platform
   */
  public static SettingValue<Double> create(Double value) { return new SettingValue<Double>(value); }
  /**
   * create setting value object of type Boolean
   * @param value Boolean value of setting property will be created
   * @return created SettingValue object of type Boolean
   * @LevelAPI Platform
   */ 
  public static SettingValue<Boolean> create(Boolean value) { return new SettingValue<Boolean>(value); }

}
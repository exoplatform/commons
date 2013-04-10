package org.exoplatform.commons.api.settings;

import java.io.Serializable;

/**
 * All possible value type stored in JCR 
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @LevelAPI Experimental
 */
public class SettingValue<T extends Object> implements Serializable {

  private static final long serialVersionUID = -1799382996718420564L;
  T value;

  /**
   * Create a setting value object with a specify value type
   * @param value
   * @LevelAPI Experimental
   */
public SettingValue(T value) {
    this.value = value;
  }


  /**
   * Get value object of setting value
   * @return value in specified type
   * @LevelAPI Experimental
   */
  public T getValue() {
    return value;
  }

  /**
   * create setting value object of type String
   * @param value String value of setting property will be created
   * @return created SettingValue object of type String
   * @LevelAPI Experimental
   */
  public static SettingValue<String> create(String value) { return new SettingValue<String>(value); }
  /**
   * create setting value object of type Long
   * @param value Long value of setting property will be created
   * @return created SettingValue object of type Long
   * @LevelAPI Experimental
   */  
  public static SettingValue<Long> create(Long value) { return new SettingValue<Long>(value); }
  /**
   * create setting value object of type Double
   * @param value Double value of setting property will be created
   * @return created SettingValue object of type Double
   * @LevelAPI Experimental
   */
  public static SettingValue<Double> create(Double value) { return new SettingValue<Double>(value); }
  /**
   * create setting value object of type Boolean
   * @param value Boolean value of setting property will be created
   * @return created SettingValue object of type Boolean
   * @LevelAPI Experimental
   */ 
  public static SettingValue<Boolean> create(Boolean value) { return new SettingValue<Boolean>(value); }

}
package org.exoplatform.commons.api.settings;

import java.io.Serializable;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class SettingValue<T extends Object> implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -1799382996718420564L;
  T value;

  public SettingValue(T value) {
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  public static SettingValue<String> create(String value) { return new SettingValue<String>(value); }
  public static SettingValue<Long> create(Long value) { return new SettingValue<Long>(value); }
  public static SettingValue<Double> create(Double value) { return new SettingValue<Double>(value); }
  public static SettingValue<Boolean> create(Boolean value) { return new SettingValue<Boolean>(value); }

}
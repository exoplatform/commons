package org.exoplatform.settings.cache;

import org.exoplatform.commons.api.settings.SettingValue;

public class NullSettingValue extends SettingValue<Object> {
  private static final long serialVersionUID = 3106661756567727076L;
  private static final NullSettingValue INSTANCE = new NullSettingValue(null);

  private NullSettingValue(Object value) {
    super(null);
  }

  public static NullSettingValue getInstance() {
    return INSTANCE;
  }

}

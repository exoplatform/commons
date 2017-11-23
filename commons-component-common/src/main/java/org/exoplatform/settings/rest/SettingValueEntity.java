package org.exoplatform.settings.rest;

import java.io.Serializable;

public class SettingValueEntity implements Serializable {
  private String value;

  public SettingValueEntity() {
  }

  public SettingValueEntity(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}

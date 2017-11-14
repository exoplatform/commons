package org.exoplatform.settings.rest;

import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.json.JSONObject;

import java.io.Serializable;

public class SettingEntity implements Serializable {
  private Context context;
  private Scope scope;
  private String key;
  private String value;

  public SettingEntity() {
  }

  public SettingEntity(Context context, Scope scope, String key, String value) {
    this.context = context;
    this.scope = scope;
    this.key = key;
    this.value = value;
  }

  public Context getContext() {
    return context;
  }

  public void setContext(Context context) {
    this.context = context;
  }

  public Scope getScope() {
    return scope;
  }

  public void setScope(Scope scope) {
    this.scope = scope;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}

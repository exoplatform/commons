package org.exoplatform.commons.api.ui;

public class ActionContext extends BaseContext {

  public ActionContext(String pluginType, String actionName) {
    super(pluginType);
    this.name = actionName;
  }

  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}

package org.exoplatform.commons.api.ui;

import java.util.List;

public interface PlugableUIService {
  public void addPlugin(BaseUIPlugin plugin);

  public List<BaseUIPlugin> getPlugin(String type);

  public List<Response> render(RenderContext renderContext);

  public Response processAction(ActionContext actionContext);

}

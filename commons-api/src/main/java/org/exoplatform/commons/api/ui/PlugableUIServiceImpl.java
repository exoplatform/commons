package org.exoplatform.commons.api.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlugableUIServiceImpl implements PlugableUIService {
  private Map<String, List<BaseUIPlugin>> plugins = new HashMap<>();

  @Override
  public void addPlugin(BaseUIPlugin plugin) {
    List<BaseUIPlugin> lst = plugins.get(plugin.getType());
    if (lst == null) {
      lst = new ArrayList<>();
      plugins.put(plugin.getType(), lst);
    }
    lst.add(plugin);
  }

  @Override
  public List<BaseUIPlugin> getPlugin(String type) {
    return plugins.get(type);
  }

  @Override
  public List<Response> render(RenderContext renderContext) {
    List<BaseUIPlugin> plugins = getPlugin(renderContext.getPluginType());
    List<Response> response = new ArrayList<>();
    if (plugins != null) {
      for (BaseUIPlugin plugin : plugins) {      
        response.add(plugin.render(renderContext));
      }      
    }
    return response;
  }

  @Override
  public Response processAction(ActionContext actionContext) {
    List<BaseUIPlugin> plugins = getPlugin(actionContext.getPluginType());
    if (plugins != null) {
      for (BaseUIPlugin plugin : plugins) {
        Response response = plugin.processAction(actionContext);
        if (response != null) {
          return response;
        }        
      }
    }
    return null;
  }
}

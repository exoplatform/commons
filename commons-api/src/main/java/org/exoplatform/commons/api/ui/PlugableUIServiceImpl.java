package org.exoplatform.commons.api.ui;

import java.util.HashMap;
import java.util.Map;

public class PlugableUIServiceImpl implements PlugableUIService {
  private Map<String, BaseUIPlugin<?, ?>> plugins = new HashMap<>();

  @Override
  public void addPlugin(BaseUIPlugin<?, ?> plugin) {
    plugins.put(plugin.getType(), plugin);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends RenderContext, A extends ActionContext> BaseUIPlugin<R, A> getPlugin(String type) {
    return (BaseUIPlugin<R, A>)plugins.get(type);
  }

  @Override
  public <R extends RenderContext> Response render(R renderContext) {
    BaseUIPlugin<R, ?> plugin = getPlugin(renderContext.getPluginType());
    if (plugin != null) {
      return plugin.render(renderContext);
    } else {
      return null;
    }
  }

  @Override
  public <A extends ActionContext> void processAction(A actionContext) {
    BaseUIPlugin<?, A> plugin = getPlugin(actionContext.getPluginType());
    if (plugin != null) {
      plugin.processAction(actionContext);      
    }
  }
}

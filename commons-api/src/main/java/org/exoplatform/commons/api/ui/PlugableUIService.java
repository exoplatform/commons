package org.exoplatform.commons.api.ui;

public interface PlugableUIService {
  public void addPlugin(BaseUIPlugin<?, ?> plugin);

  public <R extends RenderContext, A extends ActionContext> BaseUIPlugin<R, A> getPlugin(String type);

  public <R extends RenderContext> Response render(R renderContext);

  public <A extends ActionContext> void processAction(A actionContext);

}

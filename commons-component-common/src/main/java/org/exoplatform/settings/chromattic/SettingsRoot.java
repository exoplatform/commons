package org.exoplatform.settings.chromattic;

import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.PrimaryType;

import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
@PrimaryType(name = "stg:settings")
public abstract class SettingsRoot {

  @OneToMany
  public abstract Map<String, ContextEntity> getContexts();

  public ContextEntity getContext(String contextName) {
    return getContexts().get(contextName);
  }

}

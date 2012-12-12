package org.exoplatform.settings.chromattic;

import java.util.Map;

import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.PrimaryType;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
@PrimaryType(name = "stg:subcontext")
public abstract class SubContextEntity extends ContextEntity {

  @OneToMany
  protected abstract Map<String, SimpleContextEntity> getContexts();

  public SimpleContextEntity getContext(String contextName) {
    return getContexts().get(contextName);
  }

}

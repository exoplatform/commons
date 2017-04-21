package org.exoplatform.settings.chromattic;

import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.PrimaryType;

import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
@PrimaryType(name = "stg:subcontext")
public abstract class SubContextEntity extends ContextEntity {

  @OneToMany
  public abstract Map<String, SimpleContextEntity> getContexts();

  public SimpleContextEntity getContext(String contextName) {
    return getContexts().get(contextName);
  }

}

package org.exoplatform.settings.chromattic;

import org.chromattic.api.annotations.Destroy;
import org.chromattic.api.annotations.PrimaryType;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
@PrimaryType(name = "stg:context")
public abstract class ContextEntity {
  
  @Destroy
  public abstract void remove();

}


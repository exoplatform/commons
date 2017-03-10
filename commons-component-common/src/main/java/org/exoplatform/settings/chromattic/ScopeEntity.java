package org.exoplatform.settings.chromattic;

import org.chromattic.api.annotations.Destroy;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Properties;

import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
@PrimaryType(name = "stg:scope")
public abstract class ScopeEntity {

  @OneToMany
  protected abstract Map<String, ScopeEntity> getInstances();

  @Properties
  public abstract Map<String, Object> getProperties();
  
  @Destroy
  public abstract void remove();

  public Object getValue(String name) {
    return getProperties().get(name);
  }
  public Object removeValue(String key) {
    return getProperties().remove(key);
  }

  public ScopeEntity removeScope(String name) {
    return getInstances().remove(name);
  }
  
  public void setValue(String name, Object value) {
    getProperties().put(name, value);
  }

  public ScopeEntity getInstance(String name) {
    return getInstances().get(name);
  }

}

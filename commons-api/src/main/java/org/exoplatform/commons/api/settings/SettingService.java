package org.exoplatform.commons.api.settings;

import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;


/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public interface SettingService {
  
  
  
  /**
   *  set the specified value  with the key which is composed by context, scope, key. The value will be saved in the database
   * @param context context with which the specified value is to be associated
   * @param scope   scope with which  the specified value is to be associated
   * @param key     key with which the specified value is to be associated
   * @param value   value to be associated with the specified key.
   */
  public void set(Context context, Scope scope, String key, SettingValue<?> value);
  
  /**  return the SettingValue to which the specified composite key is associated or null if the database don't contains the value for the composited key
   * 
   * @param context  context with which the specified value is to be associated
   * @param scope    scope with which  the specified value is to be associated
   * @param key      key with which the specified value is to be associated
   * @return
   */
  
  public void remove(Context context, Scope scope, String key);
  
  /** remove all the value associated with the specified context and specified scope in the database.
   * @param context context with which the specified value is to be associated. The context type must be USER and context.id must be not null.
   * @param scope  scope with which  the specified value is to be associated. The scope.id must be not null.
   */
  public void remove (Context context, Scope scope);
  
  /**
   * 
   * @param context context context with which the specified value is to be associated. The context type must be USER and context.id must be not null.
   */
  public void remove(Context context);
  
  
  public SettingValue<?> get(Context context, Scope scope, String key);
  
  /** remove the value associated with the composite key (context,scope,key) in the database
   *
   * @param context
   * @param scope
   * @param key
   */

  
  
  
  
  
  
}

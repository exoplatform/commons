package org.exoplatform.commons.api.settings;

import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;

/**
 * This Service allow to store and remove a value associated with a key in JCR
 * 
 * @LevelAPI Experimental
 */
public interface SettingService {

  /**
   * set the specified value with the key which is composed by context, scope,
   * key. The value will be saved in the database
   * 
   * @param context context with which the specified value is to be associated
   * @param scope scope with which the specified value is to be associated
   * @param key key with which the specified value is to be associated
   * @param value value to be associated with the specified key.
   * @LevelAPI Experimental
   */
  public void set(Context context, Scope scope, String key, SettingValue<?> value);

  /**
   * remove the SettingValue associated with the specified composite key
   * 
   * @param context context with which the specified value is to be associated
   * @param scope scope with which the specified value is to be associated
   * @param key key with which the specified value is to be associated
   * @LevelAPI Experimental
   */
  public void remove(Context context, Scope scope, String key);

  /**
   * remove all the value associated with the specified context and specified
   * scope in the database.
   * 
   * @param context context with which the specified value is to be associated.
   *          The context type must be USER and context.id must be not null.
   * @param scope scope with which the specified value is to be associated. The
   *          scope.id must be not null.
   * @LevelAPI Experimental
   */
  public void remove(Context context, Scope scope);

  /**
   * remove all the value asscociated with the specified context in the database
   * @param context context context with which the specified value is to be
   *          associated. The context type must be USER and context.id must be
   *          not null.
   * @LevelAPI Experimental
   */
  public void remove(Context context);

  /**
   * get values associated with the composite key (context,scope,key) in
   * the database
   * 
  * @param context context with which the specified value is to be associated.
   *          The context type must be USER and context.id must be not null.
   * @param scope scope with which the specified value is to be associated. The
   *          scope.id must be not null.
   * @param key key with which the specified value is to be associated
   * @LevelAPI Experimental
   */
  public SettingValue<?> get(Context context, Scope scope, String key);

}

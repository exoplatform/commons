/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.api.settings.data;


/**
 * This class is useful to build the path of object saved in Jcr.
 * @LevelAPI Experimental
 */
public class Tools {
  /**
   * Build the path to a specified scope in database
   * @param context context with which the path is to be associated
   * @param scope   scope with which the path is to be associated
   * @return path to setting data zone of this scope in the database
   * @LevelAPI Experimental
   */
  public static String buildScopePath(Context context, Scope scope) {

    StringBuilder path = new StringBuilder().append(buildContextPath(context));
    path.append("/").append(scope.name().toLowerCase());
    if (scope.getId() != null) {
      path.append("/").append(scope.getId());
    }
    return path.toString();
  }
  /**
   * Build path to a specified context in database
   * @param context context with which the path is to be associated
   * @return path to setting data zone of specified context in the database
   * @LevelAPI Experimental
   */
  public static String buildContextPath(Context context) {
    StringBuilder path = new StringBuilder().append("settings/").append(context.name()
                                                                               .toLowerCase());
    if (context.getId() != null && context.equals(Context.USER)) {
      path.append("/").append(context.getId());
    }
    return path.toString();
  }
  /**
   * Build path of a specified setting property stored in database
   * @param context associated context to setting property 
   * @param scope   associated scope to setting property 
   * @param key   associated key to setting property 
   * @return path to a specified setting properties in the database
   * @LevelAPI Experimental
   */
  public static String buildFullPath(Context context, Scope scope, String key) {
    StringBuilder path = new StringBuilder().append(buildScopePath(context, scope))
                                            .append("/")
                                            .append(key);
    return path.toString();
  }

}

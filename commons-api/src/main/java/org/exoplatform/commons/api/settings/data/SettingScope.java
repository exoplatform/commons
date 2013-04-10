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
 * SettingScope is composed by [context,scope], associates with setting properties in a specified scope.
 * Use SettingScope to specify context of setting properties in action with database, cache or in dispatching setting event.
 * Created by The eXo Platform SAS Author : Nguyen Viet Bang
 * bangnv@exoplatform.com Nov 26, 2012
 * @LevelAPI Experimental
 */
public class SettingScope extends SettingContext {

  /**
   * 
   */
  private static final long serialVersionUID = -8617975143175631988L;

  protected Scope           scope;

  protected String          scopePath;

  /**
   * Create setting scope object with composite key [context,scope]
   * @param context context with which the specified value is to be associated
   * @param scope scope with which the specified value is to be associated
   * @LevelAPI Experimental
   */
  public SettingScope(Context context, Scope scope) {
    super(context);
    this.scope = scope;
    scopePath = Tools.buildScopePath(context, scope);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }

    if (obj instanceof SettingScope) {
      SettingScope dest = (SettingScope) obj;
      return this.getScopePath().equals(dest.getScopePath());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = super.repositoryName.hashCode();
    result = 31 * result + scopePath.hashCode();
    return result;
  }
  /**
   * get scope value of setting-scope
   * @return value of key
   * @LevelAPI Experimental
   */
  public Scope getScope() {
    return scope;
  }
  /**
   * get path associated to this setting-scope 
   * @return path to setting data zone of this scope in the database
   * @LevelAPI Experimental
   */
  public String getScopePath() {
    return scopePath;
  }

}

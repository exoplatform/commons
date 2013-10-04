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
 * Associates setting properties with a specified scope (for example, GLOBAL/PORTAL/APPLICATION). 
 * SettingScope is composed by [context, scope] and is used to
 * specify context of setting properties at the Scope level when working with database, cache or dispatching the setting event.
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
   * Creates a setting scope object with a composite key [context, scope].
   * @param context The context value.
   * @param scope The scope value.
   * @LevelAPI Experimental
   */
  public SettingScope(Context context, Scope scope) {
    super(context);
    this.scope = scope;
    scopePath = Tools.buildScopePath(context, scope);
  }
  /**
   * Compares a specified object with the SettingScope for equality.
   */
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
  /**
   * Returns the hash code value for the SettingScope object.
   */
  @Override
  public int hashCode() {
    int result = super.repositoryName.hashCode();
    result = 31 * result + scopePath.hashCode();
    return result;
  }
  /**
   * Gets a scope value of the SettingScope object.
   * @return The value of key.
   * @LevelAPI Experimental
   */
  public Scope getScope() {
    return scope;
  }
  /**
   * Gets a path to the SettingScope object.
   * @return The SettingScope path in the database.
   * @LevelAPI Experimental
   */
  public String getScopePath() {
    return scopePath;
  }

}

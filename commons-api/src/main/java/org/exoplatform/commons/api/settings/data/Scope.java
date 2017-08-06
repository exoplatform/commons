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
 * Saves some settings linked to a scope.
 * 
 * @LevelAPI Experimental
 */
public class Scope implements Cloneable {

  /**
   * Settings of portlets or gadgets.
   * 
   * @LevelAPI Experimental
   */
  public static final Scope WINDOWS = new Scope("WINDOWS", null);

  /**
   * Settings of pages.
   * 
   * @LevelAPI Experimental
   */
  public static final Scope PAGE = new Scope("PAGE", null);

  /**
   * Settings of spaces.
   * 
   * @LevelAPI Experimental
   */
  public static final Scope SPACE = new Scope("SPACE", null);

  /**
   * Settings of sites.
   * 
   * @LevelAPI Experimental
   */
  public static final Scope SITE = new Scope("SITE", null);

  /**
   * Settings of the entire portal (and all its sites).
   * 
   * @LevelAPI Experimental
   */
  public static final Scope PORTAL = new Scope("PORTAL", null);

  /**
   * Settings of an application, like Forum, Content, or Social.
   * 
   * @LevelAPI Experimental
   */
  public static final Scope APPLICATION = new Scope("APPLICATION", null);

  /**
   * Settings of the whole eXo Platform (all sites and portals).
   * 
   * @LevelAPI Experimental
   */
  public static final Scope GLOBAL = new Scope("GLOBAL", null);

  private String            id;

  private String            name;

  public Scope(String name, String id) {
    this.name = name;
    this.id = id;
  }

  public Scope id(String id) {
    Scope result = null;
    try {
      result = (Scope) this.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
    result.id = id;
    return result;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Scope that = (Scope) o;
    return that.hashCode() == this.hashCode();
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    return result;
  }
}

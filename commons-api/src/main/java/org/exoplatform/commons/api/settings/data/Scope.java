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
 * @LevelAPI Experimental
 */
public enum Scope {   
  /**
   * Settings of portlets or gadgets. 
   * @LevelAPI Experimental
   */
  WINDOWS,
  /**
   * Settings of pages.
   * @LevelAPI Experimental 
   */
  PAGE, 
  /**
   * Settings of spaces.
   * @LevelAPI Experimental 
   */
  SPACE, 
  /**
   * Settings of sites.
   * @LevelAPI Experimental
   */
  SITE, 
  /**
   * Settings of the entire portal (and all its sites).
   * @LevelAPI Experimental 
   */
  PORTAL, 
  /**
   * Settings of an application, like Forum, Content, or Social.
   * @LevelAPI Experimental 
   */
  APPLICATION, 
  /**
   * Settings of the whole eXo Platform (all sites and portals).
   * @LevelAPI Experimental
   */
  GLOBAL;

  private String id;

  public Scope id(String id) {
    this.id = id;
    return this;
  }

  public String getId() {
    return id;
  }

}

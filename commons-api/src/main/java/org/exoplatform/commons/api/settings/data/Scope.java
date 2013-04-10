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
 * This class allow a user to be able to save some settings linked to a scope.
 * Created by The eXo Platform SAS Author : eXoPlatform
 * bangnv@exoplatform.com Nov 8, 2012
 * @LevelAPI Experimental
 */
public enum Scope {   
  /**
   * Settings for portlets or gadgets. 
   * @LevelAPI Experimental
   */
  WINDOWS,
  /**
   * Settings for a page.
   * @LevelAPI Experimental 
   */
  PAGE, 
  /**
   * Settings for a space.
   * @LevelAPI Experimental 
   */
  SPACE, 
  /**
   * Settings for one site in one portal.
   * @LevelAPI Experimental
   */
  SITE, 
  /**
   * Settings for the entire portal (and all these sites).
   * @LevelAPI Experimental 
   */
  PORTAL, 
  /**
   * Settings for an application like Forum, Content, Social, etc.
   * @LevelAPI Experimental 
   */
  APPLICATION, 
  /**
   * Settings for the platform (for all sites and portals).
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

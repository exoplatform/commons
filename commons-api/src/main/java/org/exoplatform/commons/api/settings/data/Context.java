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

import org.exoplatform.services.security.ConversationState;

/**
 * Saves some settings linked to a Context.
 * @LevelAPI Experimental
 */

public enum Context {

  /**
   * GLOBAL: Settings should impact all users in the underlying scope. 
   * @LevelAPI Experimental
   */
  GLOBAL, 
  /**
   * USER: Each user should be able to save his own settings. 
   * @LevelAPI Experimental
   */
  USER;

  private String id;

  /**
   * Creates a context with a specified Id.
   * @param id The Id that is displayed as username.
   * @LevelAPI Experimental
   */
  public Context id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Gets a context Id.
   * @return Returns "null" if the context is GLOBAL or user Id if the context is USER.
   * @LevelAPI Experimental
   */
  public String getId() {
    if (id != null)
      return id;
    ConversationState state = ConversationState.getCurrent();
    return (state != null) ? state.getIdentity().getUserId() : null;
  }

}

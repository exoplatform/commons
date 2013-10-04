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

import java.io.Serializable;

import javax.jcr.RepositoryException;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Associates setting properties with a specified context (GLOBAL/USER).
 * This is used to specify context of setting properties at the Context level when working with database and cache or dispatching the setting event.
 * @LevelAPI Experimental
 */
public class SettingContext implements Serializable {

  private static final long serialVersionUID = 437625857263645213L;
  /**
   * Name of the repository in JCR.
   */
  protected String          repositoryName;
  /**
   * Context of the setting object.
   */
  protected Context         context;
  /**
   * Path of the context in JCR.
   */
  protected String          ContextPath;

  /**
   * Creates a SettingContext object with a specified context type.
   * @param context The context type.
   * @LevelAPI Experimental
   */
  public SettingContext(Context context) {
    super();
    this.context = context;
    this.repositoryName = getCurrentRepositoryName();
    this.ContextPath = Tools.buildContextPath(context);
  }
  /**
   * Compares a specified object with the SettingContext for equality.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (this == obj) {
      return true;
    }

    if (obj instanceof SettingContext) {
      SettingContext dest = (SettingContext) obj;
      return this.repositoryName.equals(dest.getRepositoryName())
          && this.getContextPath().equals(dest.getContextPath());
    }
    return false;
  }
  /**
   * Returns the hash code value for the SettingContext object.
   */
  @Override
  public int hashCode() {
    int result = repositoryName.hashCode();
    result = 31 * result + ContextPath.hashCode();
    return result;
  }
  /**
   * Gets path of the SettingContext object.
   * @return The setting context path.
   * @LevelAPI Experimental
   */
  public String getContextPath() {
    return ContextPath;
  }
  /**
   * Gets a repository name associated with the SettingContext object.
   * @return The repository name.
   * @LevelAPI Experimental
   */
  public String getRepositoryName() {
    return repositoryName;
  }
  /**
   * Gets a context object associated with the SettingContext object.
   * @return The context object.
   * @LevelAPI Experimental
   */
  public Context getContext() {
    return context;
  }
  /**
   * Gets the current repository name.
   */
  public static String getCurrentRepositoryName() {
    RepositoryService repositoryService = (RepositoryService) PortalContainer.getInstance()
                                                                             .getComponentInstanceOfType(RepositoryService.class);
    try {
      return repositoryService.getCurrentRepository().getConfiguration().getName();
    } catch (RepositoryException e) {
      throw new RuntimeException(e);
    }
  }
}

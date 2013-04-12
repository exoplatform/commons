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
 * SettingContext associates setting properties with a specified context (GLOBAL/USER).
 * Use SettingScope to specify context of setting properties in action with database, cache or in dispatching setting event.
 * @LevelAPI Experimental
 */
public class SettingContext implements Serializable {

  private static final long serialVersionUID = 437625857263645213L;

  protected String          repositoryName;

  protected Context         context;

  /**
   * path of context in jcr
   */
  protected String          ContextPath;

  /**
   * Create a setting context object with a specified context
   * @param context context with which the specified value is to be associated
   * @LevelAPI Experimental
   */
  public SettingContext(Context context) {
    super();
    this.context = context;
    this.repositoryName = getCurrentRepositoryName();
    this.ContextPath = Tools.buildContextPath(context);
  }

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

  @Override
  public int hashCode() {
    int result = repositoryName.hashCode();
    result = 31 * result + ContextPath.hashCode();
    return result;
  }
  /**
   * get path associated to this setting-context 
   * @return path to setting data zone of this context in the database
   * @LevelAPI Experimental
   */
  public String getContextPath() {
    return ContextPath;
  }
  /**
   * get repository name associated to this setting-context
   * @return repository name
   * @LevelAPI Experimental
   */
  public String getRepositoryName() {
    return repositoryName;
  }
  /**
   * get context object associated to this setting-context
   * @return Context object
   * @LevelAPI Experimental
   */
  public Context getContext() {
    return context;
  }

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

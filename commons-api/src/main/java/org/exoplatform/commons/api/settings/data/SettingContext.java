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
 * Created by The eXo Platform SAS Author : Nguyen Viet Bang
 * bangnv@exoplatform.com Nov 27, 2012
 */
public class SettingContext implements Serializable {
  
  /**
   * 
   */
  private static final long serialVersionUID = 437625857263645213L;

  /**
   * 
   */

  protected String  repositoryName;

  protected Context context;

  protected String  ContextPath;

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

  public String getContextPath() {
    return ContextPath;
  }

  public String getRepositoryName() {
    return repositoryName;
  }

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

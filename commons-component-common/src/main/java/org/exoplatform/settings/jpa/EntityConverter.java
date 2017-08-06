/*
 *
 *  * Copyright (C) 2003-2017 eXo Platform SAS.
 *  *
 *  * This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License
 *  as published by the Free Software Foundation; either version 3
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see<http://www.gnu.org/licenses/>.
 *
 */
package org.exoplatform.settings.jpa;

import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.settings.jpa.entity.ContextEntity;
import org.exoplatform.settings.jpa.entity.ScopeEntity;

public class EntityConverter {

  public static ContextEntity convertContextToContextEntity(Context context) {
    if (context != null) {
      return new ContextEntity().setType(context.getName()).setName(context.getId());
    }
    return null;
  }

  public static ScopeEntity convertScopeToScopeEntity(Scope scope) {
    if (scope != null) {
      return new ScopeEntity().setType(scope.getName()).setName(scope.getId());
    }
    return null;
  }
}

/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.notification.impl;

public abstract class AbstractService {
  public static final String  VALUE_PATTERN      = "{VALUE}";

  public static final String  EXO_IS_ACTIVE      = "exo:isActive";

  public static final String  EXO_INSTANTLY      = "exo:instantly";

  public static final String  EXO_DAILY          = "exo:daily";

  public static final String  EXO_IS_ENABLED     = "exo:isEnabled";

  public static final String  EXO_LAST_READ_DATE = "exo:lastReadDate";

  public static final String  EXO_WEEKLY         = "exo:weekly";

  public static String getValues(String values) {
    if (values == null || values.isEmpty()) {
      return "";
    }
    return values.replace("{", "").replace("}", "");
  }

}

/***************************************************************************
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 *
 **************************************************************************/

package org.exoplatform.commons.utils;

public class XPathUtils {
  
  /**
   * Avoid the illegal xPath when user name starts with number or user name is an email 
   * @param path original path
   * @return Escaped string by converting '@' character and/or the first number of user name to hexa character
   */
  public static String escapeIllegalXPathName (String path) {
    if (path == null) return null;
    if (path.length() == 0) return "";
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < path.length(); i++) {
      char ch = path.charAt(i);
      if ((Character.isDigit(ch) && (i == 0 || (i > 0 && path.charAt(i - 1) == '/'))) || ch == '@' ) {
        buffer.append("_x");
        buffer.append(String.format("%04x", (int) ch));
        buffer.append("_");
      } else {
      buffer.append(ch);
      }
    }
    return buffer.toString();
  }
}

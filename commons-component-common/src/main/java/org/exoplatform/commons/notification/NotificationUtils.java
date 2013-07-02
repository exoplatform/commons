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
package org.exoplatform.commons.notification;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Value;


public class NotificationUtils {
  
  public static String listToString(List<String> list) {
    if (list == null || list.size() == 0) {
      return "";
    }
    StringBuffer values = new StringBuffer();
    for (String str : list) {
      if (values.length() > 0) {
        values.append(",");
      }
      values.append(str);
    }
    return values.toString();
  }

  public static String[] valuesToArray(Value[] Val) throws Exception {
    if (Val.length < 1)
      return new String[] {};
    List<String> list = valuesToList(Val);
    return list.toArray(new String[list.size()]);
  }

  public static List<String> valuesToList(Value[] values) throws Exception {
    List<String> list = new ArrayList<String>();
    if (values.length < 1)
      return list;
    String s;
    for (int i = 0; i < values.length; ++i) {
      s = values[i].getString();
      if (s != null && s.trim().length() > 0)
        list.add(s);
    }
    return list;
  }
}

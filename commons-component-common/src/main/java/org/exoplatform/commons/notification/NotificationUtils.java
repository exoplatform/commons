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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.jcr.Value;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.ResourceBundleService;


public class NotificationUtils {
  private static final Log LOG = ExoLogger.getLogger(NotificationUtils.class);

  public static String getResourceBundle(String key, Locale locale) {
    if (locale == null) {
      locale = Locale.ENGLISH;
    }
    ResourceBundle res = null;
    // if null, try another way
    ResourceBundleService bundleService = CommonsUtils.getService(ResourceBundleService.class);
    if (bundleService != null) {
      res = bundleService.getResourceBundle("locale.notification.Notification", locale);
    }
    // still null
    if (res == null) {
      LOG.warn("Can not resource bundle by key: " + key);
      return key.substring(key.lastIndexOf(".") + 1);
    }

    return res.getString(key);
  }

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

  public static String[] valuesToArray(Value[] values) throws Exception {
    if (values.length < 1)
      return new String[] {};
    List<String> list = valuesToList(values);
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
  
  public static boolean isWeekEnd(int number) {
    Calendar calendar = Calendar.getInstance();
    return (calendar.get(Calendar.DAY_OF_WEEK) == number) ? true : false;
  }
  
  public static boolean isMonthEnd(int number) {
    Calendar calendar = Calendar.getInstance();
    return (calendar.get(Calendar.DAY_OF_MONTH) == number) ? true : false;
  }
}

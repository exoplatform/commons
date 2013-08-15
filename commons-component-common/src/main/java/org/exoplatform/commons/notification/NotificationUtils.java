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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import javax.jcr.Value;

import org.exoplatform.commons.api.notification.plugin.config.TemplateConfig;
import org.exoplatform.commons.notification.template.TemplateResourceBundle;


public class NotificationUtils {

  public static final String DEFAULT_SUBJECT_KEY       = "Notification.subject.$providerid";

  public static final String DEFAULT_SIMPLE_DIGEST_KEY = "Notification.digest.$providerid";

  public static final String DEFAULT_DIGEST_ONE_KEY    = "Notification.digest.one.$providerid";

  public static final String DEFAULT_DIGEST_THREE_KEY  = "Notification.digest.three.$providerid";

  public static final String DEFAULT_DIGEST_MORE_KEY   = "Notification.digest.more.$providerid";
  
  public static final String FEATURE_NAME              = "notification";

  public static String getResourceBundle(String key, Locale locale, String srcResource) {
    return TemplateResourceBundle.getResourceBundle(key, locale, srcResource);
  }
  
  public static String getDefaultKey(String key, String providerId) {
    return key.replace("$providerid", providerId);
  }

  public static SubjectAndDigest getSubjectAndDigest(TemplateConfig templateConfig, String providerId, String language) {
    SubjectAndDigest subjectAndDigest = SubjectAndDigest.getInstance();
    String srcResource = templateConfig.getBundlePath();
    String subjectKey = templateConfig.getKeyValue(TemplateConfig.SUBJECT_KEY, getDefaultKey(DEFAULT_SUBJECT_KEY, providerId));
    String digestKey = templateConfig.getKeyValue(TemplateConfig.DIGEST_KEY, getDefaultKey(DEFAULT_SIMPLE_DIGEST_KEY, providerId));
    String digestOneKey = templateConfig.getKeyValue(TemplateConfig.DIGEST_ONE_KEY, getDefaultKey(DEFAULT_DIGEST_ONE_KEY, providerId));
    String digestThreeKey = templateConfig.getKeyValue(TemplateConfig.DIGEST_THREE_KEY, getDefaultKey(DEFAULT_DIGEST_THREE_KEY, providerId));
    String digestMoreKey = templateConfig.getKeyValue(TemplateConfig.DIGEST_MORE_KEY, getDefaultKey(DEFAULT_DIGEST_MORE_KEY, providerId));
    
    Locale locale = Locale.ENGLISH;
    if (language != null) {
      locale = new Locale(language);
    }
    subjectAndDigest.setLanguage(language)
      .setSubject(getResourceBundle(subjectKey, locale, srcResource))
      .setSimpleDigest(getResourceBundle(digestKey, locale, srcResource))
      .setDigestOne(getResourceBundle(digestOneKey, locale, srcResource))
      .setDigestThree(getResourceBundle(digestThreeKey, locale, srcResource))
      .setDigestMore(getResourceBundle(digestMoreKey, locale, srcResource));
    return subjectAndDigest;
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
  
  public static Date getStartTime(String stime) {
    //
    if (stime == null || stime.length() == 0 || stime.equals("-1")) {
      return new Date(System.currentTimeMillis() + 180000);
    }
    //
    if (stime.startsWith("+")) {
      return new Date(System.currentTimeMillis() + Long.parseLong(stime.substring(1)));
    }
    //
    stime = stime.toLowerCase().replace(":", "");

    int h = 0;
    if (stime.indexOf("am") > 0) {
      h = Integer.parseInt(stime.replace("am", "").trim());
    } else if (stime.indexOf("pm") > 0) {
      h = Integer.parseInt(stime.replace("pm", "").trim());
      h = 12 + h;
    } else {
      h = Integer.parseInt(stime.trim());
      if (h == 24) {
        h = 0;
      }
    }

    return getDateByHours(h);
  }

  public static Date getDateByHours(int h) {
    Calendar calendar = GregorianCalendar.getInstance();
    int crh = calendar.get(Calendar.HOUR_OF_DAY);
    calendar.set(Calendar.MINUTE, 0);
    if (h >= crh) {
      calendar.set(Calendar.HOUR_OF_DAY, h);
    } else {
      int delta = (24 - crh + h);
      calendar.setTimeInMillis(calendar.getTimeInMillis() + 3600000 * delta);
    }
    return calendar.getTime();
  }

  public static long getRepeatInterval(String period) {
    period = period.toLowerCase();

    if (period.indexOf("m") > 0) {
      return 60000 * Integer.parseInt(period.replace("m", "").trim());
    }

    if (period.indexOf("h") > 0) {
      return 3600000 * Integer.parseInt(period.replace("h", "").trim());
    }

    if (period.indexOf("d") > 0) {
      return 86400000 * Integer.parseInt(period.replace("d", "").trim());
    }

    return Long.parseLong(period.trim());
  }
}

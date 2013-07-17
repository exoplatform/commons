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

import javax.jcr.Value;

import org.exoplatform.commons.api.notification.plugin.MappingKey;
import org.exoplatform.commons.notification.template.TemplateResouceBundle;


public class NotificationUtils {

  public static final String DEFAULT_SUBJECT_KEY       = "Notification.subject.$providerid";

  public static final String DEFAULT_SIMPLE_DIGEST_KEY = "Notification.digest.$providerid";

  public static final String DEFAULT_DIGEST_ONE_KEY    = "Notification.digest.one.$providerid";

  public static final String DEFAULT_DIGEST_THREE_KEY  = "Notification.digest.three.$providerid";

  public static final String DEFAULT_DIGEST_MORE_KEY   = "Notification.digest.more.$providerid";

  public static String getResourceBundle(String key, Locale locale, String srcResource) {
    return TemplateResouceBundle.getResourceBundle(key, locale, srcResource);
  }
  
  public static String getDefaultKey(String key, String providerId) {
    return key.replace("$providerid", providerId);
  }

  public static SubjectAndDigestTemplate getTemplate(MappingKey mappingKey, String providerId, String language) {
    SubjectAndDigestTemplate subjectAndDigest = SubjectAndDigestTemplate.getInstance();
    String srcResource = mappingKey.getLocaleResouceBundle();
    String subjectKey = mappingKey.getKeyValue(MappingKey.SUBJECT_KEY, getDefaultKey(DEFAULT_SUBJECT_KEY, providerId));
    String digestKey = mappingKey.getKeyValue(MappingKey.DIGEST_KEY, getDefaultKey(DEFAULT_SIMPLE_DIGEST_KEY, providerId));
    String digestOneKey = mappingKey.getKeyValue(MappingKey.DIGEST_ONE_KEY, getDefaultKey(DEFAULT_DIGEST_ONE_KEY, providerId));
    String digestThreeKey = mappingKey.getKeyValue(MappingKey.DIGEST_THREE_KEY, getDefaultKey(DEFAULT_DIGEST_THREE_KEY, providerId));
    String digestMoreKey = mappingKey.getKeyValue(MappingKey.DIGEST_MORE_KEY, getDefaultKey(DEFAULT_DIGEST_MORE_KEY, providerId));
    
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
}

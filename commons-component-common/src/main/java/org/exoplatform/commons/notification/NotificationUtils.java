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

import java.text.DateFormatSymbols;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.jcr.Value;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.api.notification.plugin.config.TemplateConfig;
import org.exoplatform.commons.api.notification.template.Element;
import org.exoplatform.commons.notification.template.DigestTemplate;
import org.exoplatform.commons.notification.template.SimpleElement;
import org.exoplatform.commons.notification.template.TemplateUtils;


public class NotificationUtils {

  public static final String DEFAULT_SUBJECT_KEY       = "Notification.subject.{0}";

  public static final String DEFAULT_SIMPLE_DIGEST_KEY = "Notification.digest.{0}";

  public static final String DEFAULT_DIGEST_ONE_KEY    = "Notification.digest.one.{0}";

  public static final String DEFAULT_DIGEST_THREE_KEY  = "Notification.digest.three.{0}";
  
  public static final String DEFAULT_DIGEST_MORE_KEY   = "Notification.digest.more.{0}";

  public static final String FEATURE_NAME              = "notification";
  
  public static Pattern patternInteger = Pattern.compile("^[0-9]+$");
  
  private static Map<String, Integer> dataDayOfWeek = new HashMap<String, Integer>();
  

  public static String getDefaultKey(String key, String providerId) {
    return MessageFormat.format(key, providerId);
  }
  
  /**
   * Gets the digest's resource bundle
   * 
   * @param templateConfig
   * @param pluginId
   * @param language
   * @return
   */
  public static DigestTemplate getDigest(TemplateConfig templateConfig, String pluginId, String language) {
    String srcResource = templateConfig.getBundlePath();
    String digestOneKey = templateConfig.getKeyValue(TemplateConfig.DIGEST_ONE_KEY, getDefaultKey(DEFAULT_DIGEST_ONE_KEY, pluginId));
    String digestThreeKey = templateConfig.getKeyValue(TemplateConfig.DIGEST_THREE_KEY, getDefaultKey(DEFAULT_DIGEST_THREE_KEY, pluginId));
    String digestMoreKey = templateConfig.getKeyValue(TemplateConfig.DIGEST_MORE_KEY, getDefaultKey(DEFAULT_DIGEST_MORE_KEY, pluginId));
    
    Locale locale = new Locale(language);
    
    return new DigestTemplate().digestOne(TemplateUtils.getResourceBundle(digestOneKey, locale, srcResource))
                               .digestThree(TemplateUtils.getResourceBundle(digestThreeKey, locale, srcResource))
                               .digestMore(TemplateUtils.getResourceBundle(digestMoreKey, locale, srcResource));
        
                                
  }
  
  /**
   * Gets the subject's resource bundle
   * 
   * @param templateConfig
   * @param pluginId
   * @param language
   * @return
   */
  public static Element getSubject(TemplateConfig templateConfig, String pluginId, String language) {
    String bundlePath = templateConfig.getBundlePath();
    String subjectKey = templateConfig.getKeyValue(TemplateConfig.SUBJECT_KEY, getDefaultKey(DEFAULT_SUBJECT_KEY, pluginId));
    
    Locale locale = new Locale(language);
    
    return new SimpleElement().language(locale.getLanguage()).template(TemplateUtils.getResourceBundle(subjectKey, locale, bundlePath));
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
  
  public static boolean isInteger(String str) {
    return patternInteger.matcher(str).matches();
  }

  public static int getDayOfWeek(String dayName) {
    if (dataDayOfWeek.size() == 0) {
      DateFormatSymbols dateFormat = DateFormatSymbols.getInstance(Locale.ENGLISH);
      String symbolDayNames[] = dateFormat.getWeekdays();
      for (int countDayname = 0; countDayname < symbolDayNames.length; countDayname++) {
        dataDayOfWeek.put(symbolDayNames[countDayname].toLowerCase(), countDayname);
      }
    }
    if (dayName == null || dayName.length() == 0) {
      return Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
    }
    dayName = dayName.toLowerCase().trim();
    if (isInteger(dayName)) {
      return Integer.parseInt(dayName);
    } else {
      Integer dayOfWeek = dataDayOfWeek.get(dayName.toLowerCase());
      return (dayOfWeek == null) ? 0 : dayOfWeek;
    }
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
    if (stime == null || stime.trim().length() == 0 || stime.trim().indexOf("-") == 0) {
      return new Date(System.currentTimeMillis() + 120000);
    }
    //
    if (stime.startsWith("+")) {
      return new Date(System.currentTimeMillis() + Long.parseLong(stime.substring(1)));
    }
    //
    stime = stime.toLowerCase();
    int m = 0;
    int h = (stime.indexOf("pm") > 0) ? 12 : 0;

    stime = stime.replace("am", "").replace("pm", "").trim();
    if(stime.indexOf(":") > 0) {
      String []strs = stime.split(":");
      h += Integer.parseInt(strs[0].trim());
      m = Integer.parseInt(strs[1].trim());
    } else {
      h += Integer.parseInt(stime);
    }
    //
    h = h % 24;

    return getDateByHours(h, m);
  }

  /**
   * Parsers the hours and minutes  value from configuration to date value
   * 
   * @param h
   * @param m
   * @return
   */
  public static Date getDateByHours(int h, int m) {
    Calendar calendar = GregorianCalendar.getInstance();
    int crh = calendar.get(Calendar.HOUR_OF_DAY);
    calendar.set(Calendar.MINUTE, m);
    if (h >= crh) {
      calendar.set(Calendar.HOUR_OF_DAY, h);
    } else {
      int delta = (24 - crh + h);
      calendar.setTimeInMillis(calendar.getTimeInMillis() + 3600000 * delta);
    }
    return calendar.getTime();
  }

  /**
   * Parsers the repeat interval value from configuration to long value
   * @param period
   * @return
   */
  public static long getRepeatInterval(String period) {
    period = period.toLowerCase().replace("+", "");

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
  
  public static boolean isValidEmailAddresses(String addressList){
    if (addressList == null || addressList.length() < 0)
      return false;
    addressList = StringUtils.remove(addressList, " ");
    addressList = StringUtils.replace(addressList, ";", ",");
    try {
      InternetAddress[] iAdds = InternetAddress.parse(addressList, true);
      String emailRegex = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-.]+\\.[A-Za-z]{2,5}";
      for (int i = 0; i < iAdds.length; i++) {
        if (!iAdds[i].getAddress().matches(emailRegex))
          return false;
      }
    } catch (AddressException e) {
      return false;
    }
    return true;
  }
}

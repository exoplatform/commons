/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.webui.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang.ArrayUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jul 5, 2011  
 */
public class TimeConvertUtils {
  private static final Log      LOG = ExoLogger.getLogger(TimeConvertUtils.class);
  
  public static String[] strs                 = new String[] { "SECOND", "MINUTE", "HOUR", "DAY",
                                                               "WEEK", "MONTH", "YEAR", "DECADE"};

  public static int      DAY                  = 3;

  public static int      WEEK                 = 4;

  public static int      MONTH                = 5;

  public static int      YEAR                 = 6;

  private static float   MINISECOND_OF_MINUTE = 60 * 1000.0f;

  private static float   MINUTE_OF_HOUR       = 60.0f;

  private static float   HOUR_OF_DAY          = 24.0f;

  private static float   DAY_OF_WEEK          = 7.0f;

  private static float   WEEK_OF_MONTH        = 4.35f;

  private static float   MONTH_OF_YEAR        = 12;

  private static float   YEAR_OF_DECADE       = 10.0f;

  private static Float[] timeLength           = new Float[] { MINISECOND_OF_MINUTE, MINUTE_OF_HOUR, HOUR_OF_DAY,
                                                              DAY_OF_WEEK, WEEK_OF_MONTH, MONTH_OF_YEAR, 
                                                              YEAR_OF_DECADE, YEAR_OF_DECADE };
  private static String  JUSTNOW              = "JUSTNOW";

  private static String  SPACE                = " ";

  private static String  STR_EMPTY            = "";

  private static String  STR_S                = "_S";

  private static String  UNDERSCORE           = "_";

  private static String  RESOURCE_KEY         = "TimeConvert.type.";

  private static String convertXTimeAgo(Date myDate, Date baseDate){
    float delta = (baseDate.getTime() - myDate.getTime());
    int i = 0;
    for (i = 0; (delta >= timeLength[i]) && i < timeLength.length - 1; i++) {
      delta = delta / timeLength[i];
    }
    int l = (int) delta;
    if (l < 0 || i < 1) {
      return JUSTNOW;
    }
    return new StringBuilder().append(l).append(SPACE)
                              .append(strs[i]).append((l > 1) ? STR_S : STR_EMPTY).toString();
  }

  public static String convertXTimeAgo(Date myDate, String format) {
    return convertXTimeAgo(myDate, format, null);
  }

  public static String convertXTimeAgo(Date myDate, String format, Locale locale) {
    return convertXTimeAgo(myDate, format, locale, 0);
  }

  public static String convertXTimeAgo(Date myDate, String format, int limit) {
    return convertXTimeAgo(myDate, format, null, limit);
  }

  /**
   * Convert date to display string with format X time ago
   * 
   * @param myDate The object date input for convert, it must has ZoneTime is server ZoneTime
   * @param format The date/time format
   * @param locale The Local of current location(language/country).
   * @param limit The value set for limit convert x time ago. It must is: TimeConvertUtils.YEAR, MONTH, WEEK, DAY.
   * @return String
   */
  public static String convertXTimeAgoByTimeServer(Date myDate, String format, Locale locale, int limit) {
    Date baseDate = Calendar.getInstance().getTime();
    return convertXTimeAgo(myDate, baseDate, format, locale, limit);
  }

  /**
   * Convert date to display string with format X time ago
   * 
   * @param myDate The object date input for convert, it must has ZoneTime is GMT+0
   * @param format The date/time format
   * @param locale The Local of current location(language/country).
   * @param limit The value set for limit convert x time ago. It must is: TimeConvertUtils.YEAR, MONTH, WEEK, DAY.
   * @return String 
   */
  public static String convertXTimeAgo(Date myDate, String format, Locale locale, int limit) {
    Date baseDate = getGreenwichMeanTime().getTime();
    return convertXTimeAgo(myDate, baseDate, format, locale, limit);
  }
  
  private static String convertXTimeAgo(Date myDate, Date baseDate, String format, Locale locale, int limit) {
    String[] values = convertXTimeAgo(myDate, baseDate).split(SPACE);
    if (values[0].equals(JUSTNOW))
      return getResourceBundle(RESOURCE_KEY + JUSTNOW, locale);
    int i = ArrayUtils.indexOf(strs, values[1].replace(STR_S, STR_EMPTY));
    if (limit == 0 || i < limit) {
      return getMessage(getResourceBundle(RESOURCE_KEY + values[1].replace(UNDERSCORE, STR_EMPTY),
                                          locale),
                        new String[] { values[0] });
    }
          
    if (locale != null) {
      return getFormatDate(myDate, format, locale);
    } else {
      return getFormatDate(myDate, format);
    }
  }

  public static String getFormatDate(Date myDate, String format) {
    return getFormatDate(myDate, format, getLocale());
  }

  public static String getFormatDate(Date myDate, String format, Locale locale) {
    /* h,hh,H, m, mm, d, dd, EEE, EEEE, M, MM, MMM, MMMM, yy, yyyy */
    if (myDate == null)
      return STR_EMPTY;
    return new SimpleDateFormat(format, locale).format(myDate);
  }

  private static String getResourceBundle(String key, Locale locale) {
    if (locale == null) {
      locale = getLocale();
    }
    ResourceBundle res = null;
    RequestContext ctx = WebuiRequestContext.getCurrentInstance();
    if (ctx != null) {
      res = ctx.getApplicationResourceBundle();
    }
    // if null, try another way
    ResourceBundleService bundleService = (ResourceBundleService) ExoContainerContext.getCurrentContainer()
                                                                .getComponentInstanceOfType(ResourceBundleService.class);
    if (res == null && bundleService != null) {
      res = bundleService.getResourceBundle("locale.commons.Commons", locale);
    }
    // still null
    if (res == null) {
      LOG.warn("Can not resource bundle by key: " + key);
      return key.substring(key.lastIndexOf(".") + 1).toLowerCase();
    }

    return res.getString(key);
  }
  
  private static String getMessage(String message, String[] args) {
    if (message != null && args != null) {
      String oldMes = message;
      for (int i = 0; i < args.length; i++) {
        message = message.replace("{" + i + "}", args[i]);
      }
      if(message.equals(oldMes) && args.length == 1) {
        message = args[0] + SPACE + message;
      }
    }
    return message;
  }

  /**
   * Get current {@link Locale}
   * @return {@link Locale} 
   */
  public static Locale getLocale() {
    RequestContext ctx = WebuiRequestContext.getCurrentInstance();
    if (ctx == null) {
      return Locale.ENGLISH;
    }

    Locale locale = ctx.getLocale();
    if (locale == null) {
      return Locale.ENGLISH;
    }

    return locale;
  }

  /**
   * Get current time GMT/Zulu or UTC,(zone time is 0+GMT)
   * @return Calendar 
   */
  public static Calendar getGreenwichMeanTime() {
    Calendar calendar = GregorianCalendar.getInstance();
    calendar.setLenient(false);
    int gmtoffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
    calendar.setTimeInMillis(System.currentTimeMillis() - gmtoffset);
    return calendar;
  }
}

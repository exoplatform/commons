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
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jul 5, 2011  
 */
public class TimeConvertUtils {
  private static Log      log = ExoLogger.getLogger(TimeConvertUtils.class);
  
  public static String[] strs                 = new String[] { "SECOND", "MINUTE", "HOUR", "DAY", "WEEK", "MONTH", "YEAR", "DECADE" };

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
                                                              DAY_OF_WEEK, WEEK_OF_MONTH, MONTH_OF_YEAR, YEAR_OF_DECADE, YEAR_OF_DECADE };
  private static String  JUSTNOW              = "JUSTNOW";

  private static String  SPACE                = " ";

  private static String  STR_EMPTY            = "";

  private static String  STR_S                = "_S";

  private static String  UNDERSCORE           = "_";

  private static String  RESOURCE_KEY         = "TimeConvert.type.";

  private static String convertXTimeAgo(Date myDate) throws Exception {
    float delta = (getGreenwichMeanTime().getTimeInMillis() - myDate.getTime());
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

  public static String convertXTimeAgo(Date myDate, String format, Locale locale, int limit) {
    try {
      String[] values = convertXTimeAgo(myDate).split(SPACE);
      if (values[0].equals(JUSTNOW))
        return getResourceBundle(RESOURCE_KEY + JUSTNOW);
      int i = ArrayUtils.indexOf(strs, values[1].replace(STR_S, STR_EMPTY));
      if (limit == 0 || i < limit) {
        return new StringBuilder(values[0]).append(SPACE)
                   .append(getResourceBundle(RESOURCE_KEY + values[1].replace(UNDERSCORE, STR_EMPTY))).toString();
      }
    } catch (Exception e) {}
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

  private static String getResourceBundle(String key) {
    try {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      ResourceBundle res = context.getApplicationResourceBundle();
      return res.getString(key);
    } catch (Exception e) {
      log.warn("Can not resource bundle by key: " + key);
      return key.substring(key.lastIndexOf(".")+1).toLowerCase();
    }
  }

  /**
   * Get current {@link Locale}
   * @return {@link Locale} 
   */
  public static Locale getLocale() {
    try {
      PortalRequestContext portalContext = Util.getPortalRequestContext();
      return new Locale(portalContext.getLocale().getLanguage(), portalContext.getLocale().getCountry());
    } catch (Exception e) {
      return Locale.ENGLISH;
    }
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

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

import java.util.Calendar;
import java.util.Locale;

import org.exoplatform.webui.utils.TimeConvertUtils;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Sep 30, 2011  
 */
public class TestTimeConvertUtils extends TestCase {
  public TestTimeConvertUtils() throws Exception {
    super();
  }

  public void testConvertDateTime() throws Exception {
    long timeNow = TimeConvertUtils.getGreenwichMeanTime().getTimeInMillis();
    Calendar calendar = Calendar.getInstance();
    String format = "M-d-yyyy";
    long day = 24 * 60 * 60 * 1000;
    // test for 1 year ago
    calendar.setTimeInMillis(timeNow - 366 * day);
    assertEquals("1 year", TimeConvertUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));
    assertEquals(TimeConvertUtils.getFormatDate(calendar.getTime(), format),
                 TimeConvertUtils.convertXTimeAgo(calendar.getTime(), format, null, TimeConvertUtils.YEAR));
    // test for 2 years ago
    calendar.setTimeInMillis(timeNow - 2 * 366 * day);
    // set limit is year
    assertEquals("2 years", TimeConvertUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));

    // test for 1 month ago
    calendar.setTimeInMillis(timeNow - 31 * day);
    assertEquals("1 month", TimeConvertUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));
    // set limit is year
    assertEquals("1 month", TimeConvertUtils.convertXTimeAgo(calendar.getTime(), format, null, TimeConvertUtils.YEAR));
    // test for 2 months ago
    calendar.setTimeInMillis(timeNow - (2 * 31 * day));
    assertEquals("2 months", TimeConvertUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));
    // set limit is month
    assertEquals(TimeConvertUtils.getFormatDate(calendar.getTime(), format),
                 TimeConvertUtils.convertXTimeAgo(calendar.getTime(), format, null, TimeConvertUtils.MONTH));

    // test for 1 week ago
    calendar.setTimeInMillis(timeNow - 7 * day);
    assertEquals("1 week", TimeConvertUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));
    // set limit is month
    assertEquals("1 week", TimeConvertUtils.convertXTimeAgo(calendar.getTime(), format, null, TimeConvertUtils.MONTH));
    // test for 2 weeks ago
    calendar.setTimeInMillis(timeNow - 2 * 7 * day);
    assertEquals("2 weeks", TimeConvertUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));

    // test for 1 day ago
    calendar.setTimeInMillis(timeNow - day);
    assertEquals("1 day", TimeConvertUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));
    // test for 2 days ago
    calendar.setTimeInMillis(timeNow - 2 * day);
    assertEquals("2 days", TimeConvertUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));
    // set limit day
    assertEquals(TimeConvertUtils.getFormatDate(calendar.getTime(), format),
                 TimeConvertUtils.convertXTimeAgo(calendar.getTime(), format, null, TimeConvertUtils.DAY));

    // test for 1 hour ago
    calendar.setTimeInMillis(timeNow - 60 * 60 * 1000);
    assertEquals("1 hour", TimeConvertUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));
    // test for 2 hours ago
    calendar.setTimeInMillis(timeNow - 2 * 60 * 60 * 1000);
    assertEquals("2 hours", TimeConvertUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));
    // set limit day
    assertEquals("2 hours", TimeConvertUtils.convertXTimeAgo(calendar.getTime(), format, null, TimeConvertUtils.DAY));

    // test for 1 minute ago
    calendar.setTimeInMillis(timeNow - 60 * 1000);
    assertEquals("1 minute", TimeConvertUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));
    // test for 2 minute ago
    calendar.setTimeInMillis(timeNow - 2 * 60 * 1000);
    assertEquals("2 minutes", TimeConvertUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));

    // test for less than 1 minute ago
    calendar.setTimeInMillis(timeNow - 40 * 1000);
    assertEquals("justnow", TimeConvertUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));
  }

  public void testGetResourceBundle() {
    // Can not test this function because: can not get resource bundle from test case.
  }

  public void testGetLocale() {
    // Can not test this function because: can not get PortalRequestContext.
  }

  public void testGetFormatDate() {
    Locale locale = new Locale(Locale.ENGLISH.getLanguage(), Locale.UK.getCountry());
    Calendar calendar = Calendar.getInstance();
    // set date time: 28/08/2011 at 15h 30m
    calendar.set(2011, 07, 28, 15, 30);
    assertEquals("", TimeConvertUtils.getFormatDate(null, "M-d-yyyy", locale));
    assertEquals("", TimeConvertUtils.getFormatDate(calendar.getTime(), "", locale));

    assertEquals("8-28-2011", TimeConvertUtils.getFormatDate(calendar.getTime(), "M-d-yyyy", locale));
    assertEquals("8-28-11", TimeConvertUtils.getFormatDate(calendar.getTime(), "M-d-yy", locale));
    assertEquals("08-28-11", TimeConvertUtils.getFormatDate(calendar.getTime(), "MM-dd-yy", locale));
    assertEquals("08-28-2011", TimeConvertUtils.getFormatDate(calendar.getTime(), "MM-dd-yyyy", locale));
    assertEquals("2011-08-28", TimeConvertUtils.getFormatDate(calendar.getTime(), "yyyy-MM-dd", locale));
    assertEquals("11-08-28", TimeConvertUtils.getFormatDate(calendar.getTime(), "yy-MM-dd", locale));
    assertEquals("28-08-2011", TimeConvertUtils.getFormatDate(calendar.getTime(), "dd-MM-yyyy", locale));
    assertEquals("28-08-11", TimeConvertUtils.getFormatDate(calendar.getTime(), "dd-MM-yy", locale));
    assertEquals("8/28/2011", TimeConvertUtils.getFormatDate(calendar.getTime(), "M/d/yyyy", locale));
    assertEquals("8/28/11", TimeConvertUtils.getFormatDate(calendar.getTime(), "M/d/yy", locale));
    assertEquals("08/28/11", TimeConvertUtils.getFormatDate(calendar.getTime(), "MM/dd/yy", locale));
    assertEquals("08/28/2011", TimeConvertUtils.getFormatDate(calendar.getTime(), "MM/dd/yyyy", locale));
    assertEquals("2011/08/28", TimeConvertUtils.getFormatDate(calendar.getTime(), "yyyy/MM/dd", locale));
    assertEquals("11/08/28", TimeConvertUtils.getFormatDate(calendar.getTime(), "yy/MM/dd", locale));
    assertEquals("28/08/2011", TimeConvertUtils.getFormatDate(calendar.getTime(), "dd/MM/yyyy", locale));
    assertEquals("28/08/11", TimeConvertUtils.getFormatDate(calendar.getTime(), "dd/MM/yy", locale));

    assertEquals("Sun, August 28, 2011", TimeConvertUtils.getFormatDate(calendar.getTime(), "EEE, MMMM dd, yyyy", locale));
    assertEquals("Sunday, August 28, 2011", TimeConvertUtils.getFormatDate(calendar.getTime(), "EEEE, MMMM dd, yyyy", locale));
    assertEquals("Sunday, 28 August, 2011", TimeConvertUtils.getFormatDate(calendar.getTime(), "EEEE, dd MMMM, yyyy", locale));
    assertEquals("Sun, Aug 28, 2011", TimeConvertUtils.getFormatDate(calendar.getTime(), "EEE, MMM dd, yyyy", locale));
    assertEquals("Sunday, Aug 28, 2011", TimeConvertUtils.getFormatDate(calendar.getTime(), "EEEE, MMM dd, yyyy", locale));
    assertEquals("Sunday, 28 Aug, 2011", TimeConvertUtils.getFormatDate(calendar.getTime(), "EEEE, dd MMM, yyyy", locale));
    assertEquals("August 28, 2011", TimeConvertUtils.getFormatDate(calendar.getTime(), "MMMM dd, yyyy", locale));
    assertEquals("28 August, 2011", TimeConvertUtils.getFormatDate(calendar.getTime(), "dd MMMM, yyyy", locale));
    assertEquals("Aug 28, 2011", TimeConvertUtils.getFormatDate(calendar.getTime(), "MMM dd, yyyy", locale));
    assertEquals("28 Aug, 2011", TimeConvertUtils.getFormatDate(calendar.getTime(), "dd MMM, yyyy", locale));

    assertEquals("28 Aug, 2011, 03:30 PM", TimeConvertUtils.getFormatDate(calendar.getTime(), "dd MMM, yyyy, hh:mm a", locale));
    assertEquals("28 Aug, 2011, 15:30", TimeConvertUtils.getFormatDate(calendar.getTime(), "dd MMM, yyyy, HH:mm", locale));
  }
}

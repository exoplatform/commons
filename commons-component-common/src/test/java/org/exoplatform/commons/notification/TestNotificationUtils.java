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


import java.util.Locale;

import junit.framework.TestCase;

public class TestNotificationUtils extends TestCase {

  public TestNotificationUtils() {
  }
  
  public void testGetLocale() {
    String language = null;
    Locale actual = NotificationUtils.getLocale(language);
    assertEquals(Locale.ENGLISH, actual);
    
    language = "";
    actual = NotificationUtils.getLocale(language);
    assertEquals(Locale.ENGLISH, actual);
    
    language = "fr";
    actual = NotificationUtils.getLocale(language);
    assertEquals(Locale.FRENCH, actual);
    
    language = "pt_BR";
    actual = NotificationUtils.getLocale(language);
    assertEquals(new Locale("pt", "BR"), actual);
    
    language = "pt_BR_BR";
    actual = NotificationUtils.getLocale(language);
    assertEquals(new Locale("pt", "BR", "BR"), actual);
  }
  
  public void testIsValidEmailAddresses() {
    String emails = "";
    // email is empty
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));
    // email only text not @
    emails = "test";
    assertEquals(false, NotificationUtils.isValidEmailAddresses(emails));
    // email have @ but not '.'
    emails = "test@test";
    assertEquals(false, NotificationUtils.isValidEmailAddresses(emails));
    // email have charter strange
    emails = "#%^&test@test.com";
    assertEquals(false, NotificationUtils.isValidEmailAddresses(emails));
    // email have before '.' is number
    emails = "test@test.787";
    assertEquals(false, NotificationUtils.isValidEmailAddresses(emails));
    
    emails = "no reply aaa@xyz.com, demo+aaa@demo.com, ";
    assertEquals(false, NotificationUtils.isValidEmailAddresses(emails));
    
    // basic case
    emails = "test@test.com";
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));
    emails = "test@test.com.vn";
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));
    emails = "test@test.com, demo@demo.com, ";
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));
    emails = "test@test.com ,  demo@demo.com, ";
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));
    emails = "test+test@test.com, demo+aaa@demo.com, ";
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));
  }

  public void testProcessLinkInActivityTitle() throws Exception {
    String title = "<a href=\"www.yahoo.com\">Yahoo Site</a> is better than <a href=\"www.hotmail.com\">Hotmail Site</a>";
    title = NotificationUtils.processLinkTitle(title);
    assertEquals("<a href=\"www.yahoo.com\" style=\"color: #2f5e92; text-decoration: none;\">Yahoo Site</a> is better than <a href=\"www.hotmail.com\" style=\"color: #2f5e92; text-decoration: none;\">Hotmail Site</a>", title);
  }
  
}

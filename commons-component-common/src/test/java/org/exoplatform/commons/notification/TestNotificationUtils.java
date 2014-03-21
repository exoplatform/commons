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


import junit.framework.TestCase;

public class TestNotificationUtils extends TestCase {

  public TestNotificationUtils() {
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
    // basic case
    emails = "test@test.com";
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));
    emails = "test@test.com.vn";
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));
    emails = "test@test.com, demo@demo.com, ";
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));
  }

  
}

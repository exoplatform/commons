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

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;

import junit.framework.TestCase;

public class NotificationContextTest extends TestCase {

  public NotificationContextTest() {
    
  }
  
  public void testClone() {
    NotificationContext context = NotificationContextImpl.cloneInstance();
    ArgumentLiteral<String> theKey = new ArgumentLiteral<String>(String.class, "theKey");
    context.append(theKey, "text content");
    
    assertEquals("text content", context.value(theKey));
    
    NotificationContext context2 = context.clone();
    
    assertNull(context2.value(theKey));

    assertSame(context.getNotificationExecutor(), context2.getNotificationExecutor());
  }
}

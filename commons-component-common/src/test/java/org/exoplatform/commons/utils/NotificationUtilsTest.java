/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.utils;

import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.testing.BaseCommonsTestCase;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Feb 5, 2015  
 */
public class NotificationUtilsTest extends BaseCommonsTestCase {

  public void testRemoveLinkTitle() {
    String title = "<a href=\"http://exoplatform.github.io/\" target=\"_blank\">http://exoplatform.github.io/</a>";
    String newTitle = "<span class=\"user-name text-bold\">http://exoplatform.github.io/</span>";
    assertEquals(newTitle, NotificationUtils.removeLinkTitle(title));
    
    title = "MHM&amp;#39s B-day Party";
    assertEquals("MHM&#39s B-day Party", NotificationUtils.getNotificationActivityTitle(title, NotificationUtils.CALENDAR_ACTIVITY));
    
    title = "MHM&amp;#39s B-day Party";
    assertEquals("MHM&amp;#39s B-day Party", NotificationUtils.getNotificationActivityTitle(title, ""));
  }
}

/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.commons.notification;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.notification.job.NotificationJob;
import org.exoplatform.commons.testing.BaseCommonsTestCase;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Aug 21, 2014  
 */
public class NotificationContextImplTest extends BaseCommonsTestCase {
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
  }
  
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }
  
  public void testDailyContext() throws Exception {
    NotificationContext context = NotificationContextImpl.cloneInstance();
    context.append(NotificationJob.JOB_DAILY, true);
    Boolean value = context.value(NotificationJob.JOB_DAILY);
    assertTrue(value);
    value = context.value(NotificationJob.JOB_WEEKLY);
    assertFalse(value);
  }
  
  public void testWeeklyContext() throws Exception {
    NotificationContext context = NotificationContextImpl.cloneInstance();
    context.append(NotificationJob.JOB_WEEKLY, true);
    Boolean value = context.value(NotificationJob.JOB_WEEKLY);
    assertTrue(value);
    value = context.value(NotificationJob.JOB_DAILY);
    assertFalse(value);
  }
}

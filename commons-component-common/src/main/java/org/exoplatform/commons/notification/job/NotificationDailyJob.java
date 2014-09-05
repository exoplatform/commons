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
package org.exoplatform.commons.notification.job;

import java.util.Calendar;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.notification.job.mbeans.DailyService;
import org.exoplatform.commons.utils.CommonsUtils;

public class NotificationDailyJob extends NotificationJob {
  
  @Override
  protected void processSendNotification() throws Exception {
    if (DailyService.isStarted() == false) {
      LOG.info("Starting run job to send daily email notification ... ");
      NotificationContext context = NotificationContextImpl.cloneInstance();
      context.append(JOB_DAILY, true);
      String dayName = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
      context.append(DAY_OF_JOB, dayName);
      context.append(JOB_WEEKLY, false);
      CommonsUtils.getService(NotificationService.class).digest(context);
    }
  }
  
}

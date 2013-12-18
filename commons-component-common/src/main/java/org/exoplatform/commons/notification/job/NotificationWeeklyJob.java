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

import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.notification.NotificationConfiguration;
import org.exoplatform.commons.notification.job.mbeans.WeeklyService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class NotificationWeeklyJob extends NotificationJob {
  
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    if (isValid() == false) {
      return;
    }
    if (WeeklyService.isStarted() == false) {
      try {
        processSendNotification();
      } catch (Exception e) {
        LOG.error("Failed to running NotificationJob", e);
      }
    }
  }

  @Override
  protected void processSendNotification() throws Exception {
    LOG.info("Starting run job to send weekly email notification ... ");
    CommonsUtils.getService(NotificationConfiguration.class).setSendWeekly(true);
    CommonsUtils.getService(NotificationService.class).processDigest();
  }
  
}

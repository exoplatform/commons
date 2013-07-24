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
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.job.MultiTenancyJob;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.quartz.JobExecutionContext;

public class NotificationJob extends MultiTenancyJob {
  private static final Log LOG = ExoLogger.getLogger(NotificationJob.class);

  @Override
  public Class<? extends MultiTenancyTask> getTask() {
    return SendNotificationTask.class;
  }

  public class SendNotificationTask extends MultiTenancyTask {

    public SendNotificationTask(JobExecutionContext context, String repoName) {
      super(context, repoName);
    }

    @Override
    public void run() {
      super.run();
      try {
        //you could pass the container as argument on processDaily()...
        CommonsUtils.getService(NotificationService.class).processDaily();
      } catch (Exception e) {
        LOG.error("Failed to running NotificationJob", e);
      }
      
    }
  }
}

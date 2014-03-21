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

import java.util.concurrent.Callable;

import org.exoplatform.commons.api.notification.service.NotificationCompletionService;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.NotificationSessionManager;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public abstract class NotificationJob implements Job {
  protected static final Log LOG = ExoLogger.getLogger(NotificationJob.class);
  
  public NotificationJob() {
  }

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    if (isValid() == false) {
      return;
    }
    Callable<Boolean> task = new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        try {
          NotificationSessionManager.createSystemProvider();
          processSendNotification();
        } catch (Exception e) {
          LOG.error("Failed to running NotificationJob", e);
          return false;
        } finally {
          NotificationSessionManager.closeSessionProvider();
        }
        return true;
      }
    };
    //
    CommonsUtils.getService(NotificationCompletionService.class).addTask(task);
  }

  protected boolean isValid() {
    try {
      return CommonsUtils.getRepository().getState() != 0 && CommonsUtils.isFeatureActive(NotificationUtils.FEATURE_NAME);
    } catch (Exception e) {
      LOG.error("Failed to get current repository", e);
      return false;
    }
  }
  
  protected abstract void processSendNotification() throws Exception;
}

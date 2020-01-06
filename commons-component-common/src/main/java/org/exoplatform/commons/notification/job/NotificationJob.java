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

import org.quartz.*;

import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.service.NotificationCompletionService;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.*;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public abstract class NotificationJob implements Job {
  /** Defines the Logger instance*/
  protected static final Log LOG = ExoLogger.getLogger(NotificationJob.class);
  
  /** Define the argument parameter for DAILY job with Boolean type */  
  public final static ArgumentLiteral<Boolean> JOB_DAILY = new ArgumentLiteral<Boolean>(Boolean.class, "jobDaily");
  
  /** Define the argument parameter for DAY OF JOB job with String type */  
  public final static ArgumentLiteral<String> DAY_OF_JOB = new ArgumentLiteral<String>(String.class, "dayOfJob");
  
  /** Define the argument parameter for WEEKLY job with Boolean type */
  public final static ArgumentLiteral<Boolean> JOB_WEEKLY = new ArgumentLiteral<Boolean>(Boolean.class, "jobWeekly");

  private ExoContainer container;

  public NotificationJob() {
    this(PortalContainer.getInstance());
  }

  public NotificationJob(ExoContainer exoContainer) {
    this.container = exoContainer;
  }

  @Override
  public void execute(final JobExecutionContext context) throws JobExecutionException {
    Callable<Boolean> task = new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        ExoContainer currentContainer = ExoContainerContext.getCurrentContainer();
        ExoContainerContext.setCurrentContainer(container);
        RequestLifeCycle.begin(container);
        try {
          processSendNotification(context);
        } catch (Exception e) {
          LOG.error("Failed to running NotificationJob", e);
          return false;
        } finally {
          RequestLifeCycle.end();
          ExoContainerContext.setCurrentContainer(currentContainer);
        }
        return true;
      }
    };
    //
    CommonsUtils.getService(NotificationCompletionService.class).addTask(task);
  }

  /**
   * Process the job to build the message and send to target.
   * 
   * @throws Exception
   */
  protected void processSendNotification() throws Exception {}
  protected void processSendNotification(JobExecutionContext context) throws Exception {
    processSendNotification();
  }
}

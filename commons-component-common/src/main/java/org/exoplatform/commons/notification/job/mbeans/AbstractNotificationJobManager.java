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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.notification.job.mbeans;

import java.util.Date;

import org.exoplatform.management.ManagementAware;
import org.exoplatform.management.ManagementContext;
import org.exoplatform.management.annotations.Impact;
import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;

public abstract class AbstractNotificationJobManager implements ManagementAware {

  protected ManagementContext context;
  
  protected static final Log LOG = ExoLogger.getLogger(AbstractNotificationJobManager.class);
  
  //store the number of execution
  public static final String EXECUTION_COUNT = "count";
  //store the last execution duration time in second
  public static final String LAST_EXECUTION_DURATION = "duration";
  //digest type
  public static final String DIGEST_TYPE = "digest";
  
  protected Scheduler scheduler = null;
  
  protected JobDetail job = null;
  
  protected TriggerKey triggerKey = null;
  
  /**
   * Register an object as managed object
   * @param o input object will be registered
   */
  public void register(Object o) {
    if (context != null) {
      context.register(o);
    }
  }
  
  @Override
  public void setContext(ManagementContext context) {
    this.context = context;
  }
  
  @Managed
  @ManagedDescription("Get CronExpression of the Job.")
  @Impact(ImpactType.READ)
  public String getCronExpression() {
    try {
      return ((CronTrigger) scheduler.getTrigger(triggerKey)).getCronExpression();
    } catch (SchedulerException e) {
      LOG.warn(e);
    }
    return "";
  }
  
  @Managed
  @ManagedDescription("Get last execution time of the Job.")
  @Impact(ImpactType.READ)
  public Date getLastExecutionTime() {
    try {
      return ((CronTrigger) scheduler.getTrigger(triggerKey)).getPreviousFireTime();
    } catch (SchedulerException e) {
      LOG.warn(e);
    }
    return null;
  }
  
  @Managed
  @ManagedDescription("Get next execution time of the Job.")
  @Impact(ImpactType.READ)
  public Date getNextExecutionTime() {
    try {
      return ((CronTrigger) scheduler.getTrigger(triggerKey)).getNextFireTime();
    } catch (SchedulerException e) {
      LOG.warn(e);
    }
    return null;
  }
  
  @Managed
  @ManagedDescription("Get last execution duration in second.")
  @Impact(ImpactType.READ)
  public long getLastExecutionDuration() {
    try {
      if (job != null) {
        return scheduler.getJobDetail(job.getKey()).getJobDataMap().getLong(LAST_EXECUTION_DURATION);
      }
    } catch (SchedulerException e) {
      LOG.warn(e);
    } catch (ClassCastException e) {}
    return 0;
  }
  
  @Managed
  @ManagedDescription("Get execution counter")
  @Impact(ImpactType.READ)
  public int getExecutionCounter() {
    try {
      if (job != null) {
        return scheduler.getJobDetail(job.getKey()).getJobDataMap().getInt(EXECUTION_COUNT);
      }
    } catch (SchedulerException e) {
      LOG.warn(e);
    } catch (Exception e) {}
    return 0;
  }
  
  @Managed
  @ManagedDescription("Reset execution counter")
  @Impact(ImpactType.READ)
  public void resetExecutionCounter() {
    //restart job
    startJob(0);
  }
  
  @Managed
  @ManagedDescription("Start Job after n second.")
  @Impact(ImpactType.READ)
  public void startJobAfter(int second) {
    startJob(second);
  }
  
  protected abstract void startJob(int second);
}

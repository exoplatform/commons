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

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Calendar;

import org.exoplatform.management.annotations.Impact;
import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

@Managed
@ManagedDescription("Daily Notification Job Manager")
@NameTemplate({ 
  @Property(key = "service", value = "notification"), 
  @Property(key = "view", value = "DailyJob")
})
public class DailyJobManager extends AbstractNotificationJobManager {
  
  private DailyService service;
  
  private String CRON_EXPRESSION = "0 * 23 ? * *";
  
  public DailyJobManager(DailyService service) {
    this.service = service;
    this.service.setManagement(this);
  }

  @Managed
  @ManagedDescription("Return status of DailyJob is running or not.")
  @Impact(ImpactType.READ)
  public boolean isRunning() {
    return DailyService.isStarted();
  }
  
  @Managed
  @ManagedDescription("Turn off the DailyJob.")
  @Impact(ImpactType.READ)
  public void Stop() {
    try {
      if (job != null) {
        service.off();
        scheduler.deleteJob(job.getKey());
      }
    } catch (SchedulerException e) {
      LOG.warn(e);
    }
  }
  
  @Managed
  @ManagedDescription("Set CronExpression for Job. E.g: '0 0-59/15 * ? * *' ")
  @Impact(ImpactType.READ)
  public void setCronExpression(String cronExpression) {
    CRON_EXPRESSION = cronExpression;
  }
  
  @Override
  public void startJob(int second) {
    try {
      service.on();
      // First we must get a reference to a scheduler
      SchedulerFactory sf = new StdSchedulerFactory();
      scheduler = sf.getScheduler();
      
      //remove old job before create new one
      if (job != null) {
        scheduler.deleteJob(job.getKey());
      }
      
      // get a "nice round" time a few seconds in the future....
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.SECOND, second);
      
      JobDataMap data = new JobDataMap();
      data.put(DIGEST_TYPE, "daily");
      
      job = newJob(NotificationDigestJob.class)
          .withIdentity("dailyJob", "portal:DailyJob")
          .usingJobData(data)
          .build();
      
      CronTrigger trigger = newTrigger()
          .withIdentity("trigger-dailyJob", "portal:DailyJob")
          .withSchedule(cronSchedule(CRON_EXPRESSION))
          .startAt(cal.getTime())
          .build();
      
      triggerKey = trigger.getKey();
      
      scheduler.scheduleJob(job, trigger);
    } catch (SchedulerException e) {
      LOG.warn(e);
    }
  }
  
}

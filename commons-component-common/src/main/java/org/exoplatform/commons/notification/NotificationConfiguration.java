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

import java.util.Date;

import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.job.NotificationJob;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.PeriodInfo;
import org.picocontainer.Startable;
import org.quartz.JobDataMap;

public class NotificationConfiguration implements Startable {
  private static final Log LOG = ExoLogger.getLogger(NotificationConfiguration.class);

  private String workspace = AbstractService.DEFAULT_WORKSPACE_NAME;
  private int dayOfWeekend = 6;
  private int dayOfMonthend = 28;
  
  private String period;
  private String startAtTime;

  public NotificationConfiguration(InitParams params) {
    this.workspace = getValueParam(params, AbstractService.WORKSPACE_PARAM, AbstractService.DEFAULT_WORKSPACE_NAME);
    this.dayOfWeekend = getValueParam(params, "dayOfWeekend", 6);
    this.dayOfMonthend = getValueParam(params, "dayOfMonthend", 28);
    this.period = getValueParam(params, "period", "1d");
    this.startAtTime = getValueParam(params, "startAtTime", "01:am");
  }

  @Override
  public void start() {
    //
    createJob();
  }

  @Override
  public void stop() {

  }

  public void createJob() {
    try {
      String jobName = "NotificationJob";
      String jobGroup = "Notification";
      Date startTime = NotificationUtils.getStartTime(this.startAtTime);
      Date endTime = null;
      int repeatCount = 0;
      long repeatInterval = NotificationUtils.getRepeatInterval(period);// period

      PeriodInfo periodInfo = new PeriodInfo(startTime, endTime, repeatCount, repeatInterval);
      //
      JobInfo info = new JobInfo(jobName, jobGroup, NotificationJob.class);

      JobSchedulerService schedulerService = CommonsUtils.getService(JobSchedulerService.class);
      String repoName = CommonsUtils.getRepository().getConfiguration().getName();
      JobDataMap jdatamap = new JobDataMap();
      jdatamap.put("repositoryName", repoName);
      schedulerService.addPeriodJob(info, periodInfo, jdatamap);
    } catch (Exception e) {
      LOG.debug("Failed to add job for sending email notification ", e);
    }
  }
  
  
  public String getWorkspace() {
    return this.workspace;
  }

  /**
   * @return the dayOfWeekend
   */
  public int getDayOfWeekend() {
    return dayOfWeekend;
  }

  /**
   * @return the dayOfMonthend
   */
  public int getDayOfMonthend() {
    return dayOfMonthend;
  }

  private String getValueParam(InitParams params, String key, String defaultValue) {
    try {
      return params.getValueParam(key).getValue();
    } catch (Exception e) {
      return defaultValue;
    }
  }

  private int getValueParam(InitParams params, String key, int defaultValue) {
    try {
      return Integer.valueOf(params.getValueParam(key).getValue());
    } catch (Exception e) {
      return defaultValue;
    }
  }

}

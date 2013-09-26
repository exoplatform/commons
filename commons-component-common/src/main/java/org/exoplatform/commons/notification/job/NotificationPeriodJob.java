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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.utils.ExoProperties;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.scheduler.PeriodInfo;
import org.exoplatform.services.scheduler.PeriodJob;

public class NotificationPeriodJob extends PeriodJob {
  private PeriodInfo pjinfo_;

  public NotificationPeriodJob(InitParams params) throws Exception {
    super(params);
    ExoProperties props = params.getPropertiesParam("job.info").getProperties();
    String startAtTime = getValueParam(props, "startAtTime", "23:30");
    String period = getValueParam(props, "repeatTime", "1d");
    Date startTime = NotificationUtils.getStartTime(startAtTime);
    long repeatInterval = NotificationUtils.getRepeatInterval(period);// period
    Date endTime = getDate(props.getProperty("endTime"));
    int repeatCount = getValueParam(props, "repeatCount", 0);

    pjinfo_ = new PeriodInfo(startTime, endTime, repeatCount, repeatInterval);
  }

  @Override
  public PeriodInfo getPeriodInfo() {
    return pjinfo_;
  }

  private String getValueParam(ExoProperties props, String key, String defaultValue) {
    try {
      return props.getProperty(key);
    } catch (Exception e) {
      return defaultValue;
    }
  }

  private int getValueParam(ExoProperties props, String key, int defaultValue) {
    try {
      return Integer.valueOf(props.getProperty(key));
    } catch (Exception e) {
      return defaultValue;
    }
  }
  
  private Date getDate(String stime) throws Exception {
    if (stime == null || stime.equals("")) {
      return null;
    } else if (stime.startsWith("+")) {
      long val = Long.parseLong(stime.substring(1));
      return new Date(System.currentTimeMillis() + val);
    } else {
      SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
      return ft.parse(stime);
    }
  }
}

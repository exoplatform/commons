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

import org.exoplatform.commons.utils.ExoProperties;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.scheduler.PeriodJob;
import org.quartz.JobDataMap;

public class NotificationInfoJob extends PeriodJob {
  public static final String MAX_EMAIL_TO_SEND_ONE_TIME = "maxToSend";

  public static final String SLEEP_TIME_TO_NEXT_SEND    = "sleepTimeNextSend";

  private JobDataMap         jdatamap_;

  public NotificationInfoJob(InitParams params) throws Exception {
    super(params);
    ExoProperties props = params.getPropertiesParam("notification.info").getProperties();
    jdatamap_ = new JobDataMap();
    int maxToSend = Integer.valueOf(props.getProperty(MAX_EMAIL_TO_SEND_ONE_TIME));
    int sleepTimeSend = Integer.valueOf(props.getProperty(SLEEP_TIME_TO_NEXT_SEND));
    jdatamap_.put(MAX_EMAIL_TO_SEND_ONE_TIME, maxToSend);
    jdatamap_.put(SLEEP_TIME_TO_NEXT_SEND, sleepTimeSend);
  }

  @Override
  public JobDataMap getJobDataMap() {
    return jdatamap_;
  }

}

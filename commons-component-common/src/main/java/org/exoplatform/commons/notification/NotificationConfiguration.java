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

import java.io.Serializable;
import java.util.Calendar;

import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.container.xml.InitParams;

public class NotificationConfiguration implements Serializable {
  private static final long serialVersionUID = 1L;

  private static final String DAY_OF_WEEK_END_SYS_KEY   = "conf.notification.NotificationConfiguration.dayOfWeekend";
  private static final String DAY_OF_WEEK_END_KEY       = "dayOfWeekend";

  private String            workspace        = AbstractService.DEFAULT_WORKSPACE_NAME;

  private int               dayOfWeekend     = 6;

  private int               dayOfMonthend    = 28;

  public NotificationConfiguration(InitParams params) {
    this.workspace = NotificationUtils.getValueParam(params, AbstractService.WORKSPACE_PARAM, AbstractService.DEFAULT_WORKSPACE_NAME);

    String defaultDayName = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
    String dayOfWeekName = NotificationUtils.getSystemValue(params, DAY_OF_WEEK_END_SYS_KEY, DAY_OF_WEEK_END_KEY, defaultDayName);
    this.dayOfWeekend = NotificationUtils.getDayOfWeek(dayOfWeekName);
    this.dayOfMonthend = NotificationUtils.getValueParam(params, "dayOfMonthend", 28);
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
  
  public void setDayOfWeekend(int dayOfWeekend) {
    this.dayOfWeekend = dayOfWeekend;
  }

}

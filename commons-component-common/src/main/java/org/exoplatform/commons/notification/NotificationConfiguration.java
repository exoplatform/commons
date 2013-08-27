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

  private String            workspace        = AbstractService.DEFAULT_WORKSPACE_NAME;

  private int               dayOfWeekend     = 6;

  private int               dayOfMonthend    = 28;

  public NotificationConfiguration(InitParams params) {
    this.workspace = getValueParam(params, AbstractService.WORKSPACE_PARAM, AbstractService.DEFAULT_WORKSPACE_NAME);
    this.dayOfWeekend = NotificationUtils.getDayOfWeek(getValueParam(params, "dayOfWeekend", String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))));
    this.dayOfMonthend = getValueParam(params, "dayOfMonthend", 28);
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

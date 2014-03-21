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

import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.container.xml.InitParams;

public class NotificationConfiguration implements Serializable {
  private static final long serialVersionUID = 1L;

  private String            workspace        = AbstractService.DEFAULT_WORKSPACE_NAME;

  private boolean          isSendWeekly     = false;

  public NotificationConfiguration(InitParams params) {
    this.workspace = NotificationUtils.getValueParam(params, AbstractService.WORKSPACE_PARAM, AbstractService.DEFAULT_WORKSPACE_NAME);
  }

  public String getWorkspace() {
    return this.workspace;
  }

  /**
   * @return the isSendWeekly
   */
  public boolean isSendWeekly() {
    return isSendWeekly;
  }

  /**
   * @param isSendWeekly the isSendWeekly to set
   */
  public void setSendWeekly(boolean isSendWeekly) {
    this.isSendWeekly = isSendWeekly;
  }

  /**
   * @param workspace the workspace to set
   */
  public void setWorkspace(String workspace) {
    this.workspace = workspace;
  }
}

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
package org.exoplatform.commons.api.notification.plugin;

import java.io.Writer;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationMessage;
import org.exoplatform.container.xml.InitParams;

public class DigestDailyPlugin extends AbstractNotificationPlugin {

  public DigestDailyPlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public String getId() {
    return "DigestDailyPlugin";
  }

  @Override
  protected boolean makeDigest(NotificationContext ctx, Writer writer) {
    return false;
  }

  @Override
  protected NotificationMessage makeNotification(NotificationContext ctx) {
    return null;
  }

  @Override
  protected MessageInfo makeMessage(NotificationContext ctx) {
    return null;
  }

}

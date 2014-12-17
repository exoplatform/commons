/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.commons.notification.plugin;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.container.xml.InitParams;

public class PluginTest extends AbstractNotificationPlugin {

  public final static String ID = "TestPlugin";
  
  public PluginTest(InitParams initParams) {
    super(initParams);
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    return false;
  }

  @Override
  protected NotificationInfo makeNotification(NotificationContext ctx) {
    NotificationInfo notificationInfo = NotificationInfo.instance();
    return notificationInfo.to("demo").setTo("demo")
                            .with("USER", "root")
                            .with("TEST_VALUE", "Test value")
                            .with("CHILD_VALUE", "The content of child plugin ...")
                            .key(getId()).end();
  }
}
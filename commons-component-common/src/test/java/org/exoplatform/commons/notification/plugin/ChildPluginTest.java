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
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationChildPlugin;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.container.xml.InitParams;

public class ChildPluginTest extends AbstractNotificationChildPlugin {

  public ChildPluginTest(InitParams initParams) {
    super(initParams);
  }

  @Override
  public String makeContent(NotificationContext ctx) {
    NotificationInfo notification = ctx.getNotificationInfo();

    String language = getLanguage(notification);
    TemplateContext templateContext = new TemplateContext(getId(), language);

    String childContent = notification.getValueOwnerParameter("CHILD_VALUE");
    templateContext.put("CONTENT", childContent);
    //
    String content = TemplateUtils.processGroovy(templateContext);
    return content;
  }

  @Override
  public String getId() {
    return "Child_Plugin";
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    return false;
  }

}
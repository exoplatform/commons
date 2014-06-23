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

import java.io.Writer;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.NotificationKey;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationChildPlugin;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.api.notification.service.setting.PluginContainer;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;

public class PluginTest extends AbstractNotificationPlugin {

  public PluginTest(InitParams initParams) {
    super(initParams);
  }

  @Override
  public String getId() {
    return "Test_ID";
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

  
  @Override
  protected MessageInfo makeMessage(NotificationContext ctx) {
    NotificationInfo notification = ctx.getNotificationInfo();
    String language = getLanguage(notification);
    TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
    
    
    templateContext.put("USER", notification.getValueOwnerParameter("USER"));
    templateContext.put("SUBJECT", "Test plugin notification");
    String subject = TemplateUtils.processSubject(templateContext);

    String value = notification.getValueOwnerParameter("TEST_VALUE");
    templateContext.put("VALUE", value);
    StringBuilder childContent = new StringBuilder();
    
    PluginContainer pluginContainer = CommonsUtils.getService(PluginContainer.class);
    List<NotificationKey> childKeys = pluginContainer.getChildPluginKeys(getKey());
    for (NotificationKey notificationKey : childKeys) {
      AbstractNotificationPlugin child = pluginContainer.getPlugin(notificationKey);
      childContent.append("<br>").append(((AbstractNotificationChildPlugin) child).makeContent(ctx));
    }
    templateContext.put("CHILD_CONTENT", childContent.toString());
    
    return new MessageInfo().subject(subject).body(TemplateUtils.processGroovy(templateContext)).end();
  }

  @Override
  protected boolean makeDigest(NotificationContext ctx, Writer writer) {
    return false;
  }

}
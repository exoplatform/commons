/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.notification.plugin;

import java.io.Writer;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.container.xml.InitParams;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 23, 2014  
 */
public class PluginPLF41Test extends AbstractNotificationPlugin {
  public final static String ID = "PluginPLF41Test";
  public final static String SUBJECT = "PluginPLF41Test subject the message";
  
  public PluginPLF41Test(InitParams initParams) {
    super(initParams);
  }
  
  @Override
  protected MessageInfo makeMessage(NotificationContext ctx) {
    MessageInfo messageInfo = new MessageInfo();
    NotificationInfo notification = ctx.getNotificationInfo();
    
    String language = getLanguage(notification);
    TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
    String toUser = notification.getTo();
    
    templateContext.put("USER", toUser);
    templateContext.put("TEST_VALUE", "Test PLF4.1 plugin.");
    String subject = SUBJECT;
    String body = TemplateUtils.processGroovy(templateContext);

    return messageInfo.subject(subject).body(body).end();
  }

  @Override
  protected boolean makeDigest(NotificationContext ctx, Writer writer) {
    return false;
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    return true;
  }

  @Override
  protected NotificationInfo makeNotification(NotificationContext ctx) {
    NotificationInfo notificationInfo = NotificationInfo.instance();
    return notificationInfo.setTo("demo").to("root")
                            .with("USER", "root")
                            .with("TEST_VALUE", "Test PLF4.1 plugin.")
                            .key(getId()).end();
  }

}

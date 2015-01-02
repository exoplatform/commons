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
package org.exoplatform.commons.api.notification.channel.template;

import groovy.text.Template;

import java.io.Writer;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.NotificationPluginUtils;
import org.exoplatform.services.organization.OrganizationService;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 13, 2014  
 */
public abstract class AbstractTemplateBuilder {
  /** Holding the template to generate message.*/
  private Template engine;
  /**
   * Makes the MessageInfor from given NotificationMessage what keep inside NotificationContext
   * @param context
   * @return
   */
  protected abstract MessageInfo makeMessage(NotificationContext ctx);
  
  /**
   * Makes the Digest message from given NotificationMessage what keep inside NotificationContext
   * @param ctx
   * @param wtiter
   * @return
   */
  protected abstract boolean makeDigest(NotificationContext ctx, Writer writer);
  
  /**
   * Makes massage
   * @param ctx
   * @return
   */
  public MessageInfo buildMessage(NotificationContext ctx) {
    NotificationInfo notif = ctx.getNotificationInfo();
    MessageInfo messageInfo = makeMessage(ctx);
    return messageInfo.setId(notif.getId()).pluginId(notif.getKey().getId()).from(NotificationPluginUtils.getFrom(notif.getFrom()))
               .to(NotificationPluginUtils.getTo(notif.getTo())).end();
  }

  /**
   * Makes digest message
   * @param ctx
   * @param writer
   * @return
   */
  public boolean buildDigest(NotificationContext ctx, Writer writer) {
    return makeDigest(ctx, writer);
  }
  
  /**
   * 
   * @param notif
   * @return
   */
  protected String getLanguage(NotificationInfo notif) {
    return NotificationPluginUtils.getLanguage(notif.getTo());
  }
  
  protected OrganizationService getOrganizationService() {
    return NotificationPluginUtils.getOrganizationService();
  }

  /**
   * Get TemplateEngine of plugin
   * @return the TemplateEngine
   */
  public Template getTemplateEngine() {
    return engine;
  }

  /**
   * Set TemplateEngine for plugin
   * @param engine the TemplateEngine to set
   */
  public void setTemplateEngine(Template engine) {
    this.engine = engine;
  }

}

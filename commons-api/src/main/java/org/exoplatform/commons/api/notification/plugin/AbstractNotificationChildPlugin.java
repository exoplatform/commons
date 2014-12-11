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

import groovy.text.Template;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.xml.ValuesParam;

public abstract class AbstractNotificationChildPlugin extends AbstractNotificationPlugin {
  private static final String PARENT_ID_KEY = "parentIds";
  private static final String TEMPLATE_PATH_KEY = "templatePath";
  private List<String> parentPluginIds = new ArrayList<String>();

  private Template engine;
  private String templatePath;

  public AbstractNotificationChildPlugin(InitParams initParams) {
    super(initParams);
    //
    ValuesParam params = initParams.getValuesParam(PARENT_ID_KEY);
    if(params != null) {
      parentPluginIds.addAll(params.getValues());
    }
    ValueParam paramTemplatePath = initParams.getValueParam(TEMPLATE_PATH_KEY);
    if(paramTemplatePath != null) {
      templatePath = paramTemplatePath.getValue();
    }
  }
  
  /**
   * Gets the parents's id
   *  
   * @return
   */
  public List<String> getParentPluginIds() {
    return parentPluginIds;
  }

  @Override
  protected MessageInfo makeMessage(NotificationContext ctx) {
    throw new UnsupportedOperationException("The children plugin " + getId() + " unsupported method makeMessage.");
  }

  @Override
  protected NotificationInfo makeNotification(NotificationContext ctx) {
    throw new UnsupportedOperationException("The children plugin " + getId() + " unsupported method makeNotification.");
  }

  @Override
  protected boolean makeDigest(NotificationContext ctx, Writer writer) {
    throw new UnsupportedOperationException("The children plugin " + getId() + " unsupported method makeDigest.");
  }
  
  public abstract String makeContent(NotificationContext ctx);
  
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

  public String getTemplatePath() {
    return templatePath;
  }

  public void setTemplatePath(String templatePath) {
    this.templatePath = templatePath;
  }
}
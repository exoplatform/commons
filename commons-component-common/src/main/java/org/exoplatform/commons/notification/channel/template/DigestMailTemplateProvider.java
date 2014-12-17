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
package org.exoplatform.commons.notification.channel.template;

import java.io.Writer;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.annotation.TemplateConfig;
import org.exoplatform.commons.api.notification.annotation.TemplateConfigs;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.DigestDailyPlugin;
import org.exoplatform.commons.notification.impl.DigestWeeklyPlugin;
import org.exoplatform.container.xml.InitParams;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 12, 2014  
 */
@TemplateConfigs(
  templates = {
      @TemplateConfig(pluginId = DigestDailyPlugin.ID, template = "war:/notification/templates/DigestDailyPlugin.gtmpl"),
      @TemplateConfig(pluginId = DigestWeeklyPlugin.ID, template = "war:/notification/templates/DigestWeeklyPlugin.gtmpl")
  }
)
public class DigestMailTemplateProvider extends TemplateProvider {

  public DigestMailTemplateProvider(InitParams initParams) {
    super(initParams);
    templateBuilders.put(new PluginKey(DigestDailyPlugin.ID), dailyBuilder);
    templateBuilders.put(new PluginKey(DigestWeeklyPlugin.ID), weeklyBuilder);
  }
  
  AbstractTemplateBuilder dailyBuilder = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      return null;
    }
    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }
  }; 
  AbstractTemplateBuilder weeklyBuilder = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      return null;
    }
    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }
  }; 
}

/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.commons.notification.template;

import groovy.lang.Writable;
import groovy.text.Template;

import java.io.StringWriter;
import java.io.Writer;

import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationChildPlugin;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.api.notification.service.setting.PluginContainer;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.api.notification.template.Element;
import org.exoplatform.commons.api.notification.template.ElementVisitor;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Aug 8, 2013  
 */
public class GroovyElementVisitor implements ElementVisitor {
  private static final Log LOG = ExoLogger.getLogger(GroovyElementVisitor.class);
  private final Writer writer;
  private TemplateContext ctx;
  public GroovyElementVisitor() {
    writer = new StringWriter();
  }
  
  @Override
  public ElementVisitor visit(Element element) {
    this.ctx.put("_ctx", element);
    //
    try {
      Template engine = null;
      PluginKey pluginKey = new PluginKey(ctx.getPluginId());
      BaseNotificationPlugin plugin = CommonsUtils.getService(PluginContainer.class).getPlugin(pluginKey);
      if (plugin instanceof AbstractNotificationChildPlugin) {
        engine = ((AbstractNotificationChildPlugin) plugin).getTemplateEngine();
      } else if (plugin.isOldPlugin()) {
        engine = ((AbstractNotificationPlugin) plugin).getTemplateEngine();
      } else {
        AbstractChannel channel = CommonsUtils.getService(ChannelManager.class).getChannel(ctx.getChannelKey());
        AbstractTemplateBuilder builder = channel.getTemplateBuilder(pluginKey);
        if (builder != null) {
          engine = builder.getTemplateEngine();
        }
      }
      if (engine != null) {
        Writable writable = engine.make(getTemplateContext());
        writable.writeTo(writer);
      }
    } catch (Exception e) {
      LOG.error("Failed at visit().", e);
    }
    return this;
  }

  @Override
  public String out() {
    return writer.toString();
  }

  @Override
  public TemplateContext getTemplateContext() {
    return this.ctx;
  }

  @Override
  public ElementVisitor with(TemplateContext ctx) {
    this.ctx = ctx;
    return this;
  }

  @Override
  public Writer getWriter() {
    return this.writer;
  }
}

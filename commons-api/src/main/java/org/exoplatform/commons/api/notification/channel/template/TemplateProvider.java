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

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.commons.api.notification.annotation.TemplateConfig;
import org.exoplatform.commons.api.notification.annotation.TemplateConfigs;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 12, 2014  
 */
public abstract class TemplateProvider extends BaseComponentPlugin {
  /** logger */
  private static final Log LOG = ExoLogger.getLogger(TemplateProvider.class);
  /** */
  private final static String CHANNEL_ID_KEY = "channel-id";
  /** */
  protected final  Map<PluginKey, String> templateFilePaths = new HashMap<PluginKey, String>();
  /** */
  protected final Map<PluginKey, AbstractTemplateBuilder> templateBuilders = new HashMap<PluginKey, AbstractTemplateBuilder>();
  /** */
  private ChannelKey key = null;
  
  public TemplateProvider(InitParams initParams) {
    //parser the annotation and build the template map
    TemplateConfigs templates = this.getClass().getAnnotation(TemplateConfigs.class);
    if (templates != null) {
      for (TemplateConfig config : templates.templates()) {
        if (config != null && config.pluginId() != "") {
          templateFilePaths.put(PluginKey.key(config.pluginId()), config.template());
        }
      }
    }
    //
    ValueParam channelIdParam = initParams.getValueParam(CHANNEL_ID_KEY);
    //
    try {
      this.key = ChannelKey.key(channelIdParam.getValue());
    } catch (Exception e) {
      LOG.error("Register the template provider must allow the channelId.", e);
    }
  }
  
  private TemplateProvider() {}
  
  
  /**
   * Gets all of the template files
   * @return
   */
  public Map<PluginKey, String> getTemplateFilePathConfigs() {
    return templateFilePaths;
  }
  /**
   * Gets all of the template builder what assigned the channel
   * 
   * @return
   */
  public Map<PluginKey, AbstractTemplateBuilder> getTemplateBuilder() {
    return this.templateBuilders;
  }
  
  /**
   * Gets channelId
   * @return
   */
  public ChannelKey getChannelKey() {
    return key;
  }
  

}
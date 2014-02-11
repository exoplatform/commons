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
package org.exoplatform.commons.notification.impl.setting;

import groovy.text.GStringTemplateEngine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.exoplatform.commons.api.notification.model.NotificationKey;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.api.notification.service.setting.PluginContainer;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.notification.template.ResourceBundleConfigDeployer;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.gatein.wci.ServletContainerFactory;
import org.picocontainer.Startable;

public class NotificationPluginContainer implements PluginContainer, Startable {
  private static final Log LOG = ExoLogger.getLogger(NotificationPluginContainer.class);

  private final Map<NotificationKey, AbstractNotificationPlugin> pluginMap;

  private PluginSettingService                                   pSettingService;
  private ResourceBundleConfigDeployer                           deployer;
  private GStringTemplateEngine gTemplateEngine;

  public NotificationPluginContainer() {
    pluginMap = new HashMap<NotificationKey, AbstractNotificationPlugin>();
    pSettingService = CommonsUtils.getService(PluginSettingService.class);
    deployer = new ResourceBundleConfigDeployer();
    gTemplateEngine = new GStringTemplateEngine();
  }

  @Override
  public void start() {
    Set<String> datas = new HashSet<String>();
    for (AbstractNotificationPlugin plugin : pluginMap.values()) {
      for (PluginConfig pluginConfig : plugin.getPluginConfigs()) {
        pSettingService.registerPluginConfig(pluginConfig);
        datas.add(pluginConfig.getTemplateConfig().getBundlePath());
      }
    }
    if(ServletContainerFactory.getServletContainer().addWebAppListener(deployer)) {
      deployer.initBundlePath(datas);
    }
  }

  @Override
  public void stop() {
    ServletContainerFactory.getServletContainer().removeWebAppListener(deployer);
  }

  @Override
  public AbstractNotificationPlugin getPlugin(NotificationKey key) {
    return pluginMap.get(key);
  }

  @Override
  public void add(AbstractNotificationPlugin plugin) {
    registerPlugin(plugin);
  }

  private void registerPlugin(AbstractNotificationPlugin plugin) {
    try {
      String templatePath = plugin.getPluginConfigs().get(0).getTemplateConfig().getTemplatePath();
      String template = TemplateUtils.loadGroovyTemplate(templatePath);
      plugin.setTemplateEngine(gTemplateEngine.createTemplate(template));
    } catch (Exception e) {
      LOG.debug("Failed to register notification plugin " + plugin.getId());
    }
    pluginMap.put(plugin.getKey(), plugin);
  }

  @Override
  public boolean remove(NotificationKey key) {
    pluginMap.remove(key);
    return true;
  }


}

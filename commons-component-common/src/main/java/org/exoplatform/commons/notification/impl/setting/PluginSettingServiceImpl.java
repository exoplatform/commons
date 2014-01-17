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
package org.exoplatform.commons.notification.impl.setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jcr.Node;

import org.exoplatform.commons.api.notification.model.GroupProvider;
import org.exoplatform.commons.api.notification.model.PluginInfo;
import org.exoplatform.commons.api.notification.plugin.GroupProviderPlugin;
import org.exoplatform.commons.api.notification.plugin.config.GroupConfig;
import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.notification.NotificationConfiguration;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class PluginSettingServiceImpl extends AbstractService implements PluginSettingService {
  private static final Log LOG = ExoLogger.getLogger(PluginSettingServiceImpl.class);

  private List<PluginConfig>         pluginConfigs      = new ArrayList<PluginConfig>();

  private Map<String, GroupProvider> groupProviderMap   = new ConcurrentHashMap<String, GroupProvider>();

  private List<String>               activeProviderIds  = new ArrayList<String>();

  private List<PluginInfo>           activeProviders    = new ArrayList<PluginInfo>();

  private static final String       NAME_SPACES        = "exo:";

  private SettingService             settingService;
  
  public PluginSettingServiceImpl(SettingService settingService) {
    this.settingService = settingService;
  }

  @Override
  public void registerPluginConfig(PluginConfig pluginConfig) {
    pluginConfigs.add(pluginConfig);
    PluginInfo providerData = new PluginInfo();
    providerData.setType(pluginConfig.getPluginId())
                .setOrder(Integer.valueOf(pluginConfig.getOrder()))
                .setActive(isActive(pluginConfig.getPluginId(), true))
                .setResourceBundleKey(pluginConfig.getResourceBundleKey())
                .setBundlePath(pluginConfig.getTemplateConfig().getBundlePath())
                .setDefaultConfig(pluginConfig.getDefaultConfig());
    //
    String groupId = pluginConfig.getGroupId();
    GroupConfig gConfig = pluginConfig.getGroupConfig();
    if(gConfig != null) {
      groupId = gConfig.getId();
    }
    //
    if (groupProviderMap.containsKey(groupId)) {
      groupProviderMap.get(groupId).addProviderData(providerData);
    } else if (groupId != null && groupId.length() > 0) {
      GroupProvider groupProvider = new GroupProvider(groupId);
      groupProvider.addProviderData(providerData);
      if (gConfig != null) {
        groupProvider.setOrder(Integer.valueOf(gConfig.getOrder()));
        groupProvider.setResourceBundleKey(gConfig.getResourceBundleKey());
      }
      groupProviderMap.put(groupId, groupProvider);
    }
    //
    createParentNodeOfPlugin(pluginConfig.getPluginId());
  }
  
  @Override
  public void registerGroupConfig(GroupProviderPlugin groupConfigPlg) {
    for (GroupConfig gconfig : groupConfigPlg.getGroupProviders()) {
      GroupProvider groupProvider = new GroupProvider(gconfig.getId());
      groupProvider.setOrder(Integer.valueOf(gconfig.getOrder()));
      groupProvider.setResourceBundleKey(gconfig.getResourceBundleKey());
      if (groupProviderMap.containsKey(gconfig.getId())) {
        groupProvider.setProviderDatas(groupProviderMap.get(gconfig.getId()).getProviderDatas());
      }
      groupProviderMap.put(gconfig.getId(), groupProvider);
    }
  }

  @Override
  public PluginConfig getPluginConfig(String pluginId) {
    for (PluginConfig pluginConfig : pluginConfigs) {
      if (pluginConfig.getPluginId().equals(pluginId)) {
        return pluginConfig;
      }
    }
    return null;
  }

  @Override
  public List<GroupProvider> getGroupPlugins() {
    List<GroupProvider> groupProviders = new ArrayList<GroupProvider>();
    for (GroupProvider groupProvider : groupProviderMap.values()) {
      for (PluginInfo providerData : groupProvider.getProviderDatas()) {
        providerData.setActive(isActive(providerData.getType()));
      }
      groupProviders.add(groupProvider);
    }
    Collections.sort(groupProviders, new ComparatorASC());
    return groupProviders;
  }

  @Override
  public void savePlugin(String providerId, boolean isActive) {
    saveSetting(providerId, isActive);
    activeProviderIds.clear();
    activeProviders.clear();
  }
  
  @Override
  public boolean isActive(String providerId) {
    return isActive(providerId, false);
  }

  @Override
  public List<String> getActivePluginIds() {
    if(activeProviderIds.size() == 0) {
      Collections.sort(pluginConfigs, new ComparatorASC());
      for (PluginConfig pluginConfig : pluginConfigs) {
        if (isActive(pluginConfig.getPluginId())) {
          activeProviderIds.add(pluginConfig.getPluginId());
        }
      }
    }

    return activeProviderIds;
  }

  @Override
  public List<PluginInfo> getActivePlugins() {
    if(activeProviders.size() == 0) {
      for (GroupProvider groupProvider : groupProviderMap.values()) {
        for (PluginInfo providerData : groupProvider.getProviderDatas()) {
          if(isActive(providerData.getType())) {
            activeProviders.add(providerData);
          }
        }
      }
    }
    
    return activeProviders;
  }

  private void saveSetting(String property, boolean value) {
    settingService.set(Context.GLOBAL, Scope.GLOBAL, (NAME_SPACES + property), SettingValue.create(value));
  }

  private void createParentNodeOfPlugin(String pluginId) {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      NotificationConfiguration configuration = CommonsUtils.getService(NotificationConfiguration.class);
      Node node = getOrCreateMessageParent(sProvider, configuration.getWorkspace(), pluginId);
      sessionSave(node);
    } catch (Exception e) {
      LOG.error("Failed to create parent Node for plugin " + pluginId);
    } finally {
      sProvider.close();
    }
  }

  private boolean isActive(String providerId, boolean defaultValue) {
    if (providerId == null || providerId.length() == 0) {
      return false;
    }
    SettingValue<?> sValue = settingService.get(Context.GLOBAL, Scope.GLOBAL, (NAME_SPACES + providerId));
    if (sValue != null) {
      return ((Boolean) sValue.getValue()) ? true : false;
    } else if (defaultValue == true) {
      saveSetting(providerId, true);
    }
    return defaultValue;
  }

  private class ComparatorASC implements Comparator<Object> {
    @Override
    public int compare(Object o1, Object o2) {
      if (o1 instanceof GroupProvider) {
        Integer order1 = ((GroupProvider) o1).getOrder();
        Integer order2 = ((GroupProvider) o2).getOrder();
        return order1.compareTo(order2);
      }
      if (o1 instanceof PluginConfig) {
        Integer order1 = Integer.valueOf(((PluginConfig) o1).getOrder());
        Integer order2 = Integer.valueOf(((PluginConfig) o2).getOrder());
        return order1.compareTo(order2);
      }
      return 0;
    }
  }

}

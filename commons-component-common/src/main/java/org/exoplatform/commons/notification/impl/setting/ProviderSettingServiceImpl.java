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

import org.exoplatform.commons.api.notification.GroupProvider;
import org.exoplatform.commons.api.notification.ProviderData;
import org.exoplatform.commons.api.notification.plugin.GroupProviderPlugin;
import org.exoplatform.commons.api.notification.plugin.model.GroupConfig;
import org.exoplatform.commons.api.notification.plugin.model.PluginConfig;
import org.exoplatform.commons.api.notification.service.setting.ProviderSettingService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;

public class ProviderSettingServiceImpl implements ProviderSettingService {

  private List<PluginConfig>         pluginConfigs      = new ArrayList<PluginConfig>();

  private Map<String, GroupProvider> groupProviderMap   = new ConcurrentHashMap<String, GroupProvider>();

  private List<String>               activeProviderIds        = new ArrayList<String>();

  private static final String        NAME_SPACES        = "exo:";

  private static final String        ACTIVE_FEATURE_KEY = "feature.Notification";

  private SettingService             settingService;

  public ProviderSettingServiceImpl(SettingService settingService) {
    this.settingService = settingService;
  }

  @Override
  public void registerPluginConfig(PluginConfig pluginConfig) {
    pluginConfigs.add(pluginConfig);

    ProviderData providerData = new ProviderData();
    providerData.setType(pluginConfig.getPluginId())
                .setOrder(Integer.valueOf(pluginConfig.getOrder()))
                .setActive(isActive(pluginConfig.getPluginId(), true))
                .setResourceBundleKey(pluginConfig.getResourceBundleKey());
    GroupConfig gConfig = pluginConfig.getGroupConfig();
    if(gConfig != null) {
      if (groupProviderMap.containsKey(gConfig.getId())) {
        groupProviderMap.get(gConfig.getId()).addProviderData(providerData);
      } else {
        GroupProvider groupProvider = new GroupProvider(gConfig.getId());
        groupProvider.setOrder(Integer.valueOf(gConfig.getOrder()));
        groupProvider.setResouceBundleKey(gConfig.getResourceBundleKey());
        groupProvider.addProviderData(providerData);
        groupProviderMap.put(gConfig.getId(), groupProvider);
      }
    } else {
      String groupId = pluginConfig.getGroupId();
      if (groupProviderMap.containsKey(groupId)) {
        groupProviderMap.get(groupId).addProviderData(providerData);
      }
    }
    
    
  }

  @Override
  public void registerGroupConfig(GroupProviderPlugin groupConfigPlg) {
    for (GroupConfig gconfig : groupConfigPlg.getGroupProviders()) {
      GroupProvider groupProvider = new GroupProvider(gconfig.getId());
      groupProvider.setOrder(Integer.valueOf(gconfig.getOrder()));
      groupProvider.setResouceBundleKey(gconfig.getResourceBundleKey());
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
  public List<GroupProvider> getGroupProviders() {
    List<GroupProvider> groupProviders = new ArrayList<GroupProvider>(groupProviderMap.values());
    for (GroupProvider groupProvider : groupProviders) {
      for (ProviderData providerData : groupProvider.getProviderDatas()) {
        providerData.setActive(isActive(providerData.getType(), false));
      }
    }
    Collections.sort(groupProviders, new ComparatorASC());
    return groupProviders;
  }

  @Override
  public void saveActiveProviders(List<ProviderData> providerDatas) {
    for (ProviderData providerData : providerDatas) {
      if (providerData.isActive()) {
        saveSetting(providerData.getType(), true);
      } else {
        removeSetting(providerData.getType());
      }
    }
    activeProviderIds.clear();
  }

  @Override
  public List<String> getActiveProviderIds() {
    if(activeProviderIds.size() == 0) {
      Collections.sort(pluginConfigs, new ComparatorASC());
      for (PluginConfig pluginConfig : pluginConfigs) {
        if (isActive(pluginConfig.getPluginId(), false)) {
          activeProviderIds.add(pluginConfig.getPluginId());
        }
      }
    }

    return activeProviderIds;
  }

  private void saveSetting(String property, boolean value) {
    settingService.set(Context.GLOBAL, Scope.GLOBAL, (NAME_SPACES + property), SettingValue.create(value));
  }

  private void removeSetting(String property) {
    settingService.remove(Context.GLOBAL, Scope.GLOBAL, (NAME_SPACES + property));
  }

  private boolean isActive(String providerId, boolean defaultValue) {
    if (providerId == null || providerId.length() == 0) {
      return false;
    }
    SettingValue sValue = settingService.get(Context.GLOBAL, Scope.GLOBAL, (NAME_SPACES + providerId));
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

  @Override
  public boolean getActiveFeature() {
    return Boolean.valueOf(System.getProperty(ACTIVE_FEATURE_KEY, "true"));
  }

  @Override
  public void saveActiveFeature(boolean isActive) {

  }

}

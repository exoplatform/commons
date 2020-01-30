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
package org.exoplatform.settings.jpa;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.model.*;
import org.exoplatform.commons.api.notification.plugin.GroupProviderPlugin;
import org.exoplatform.commons.api.notification.plugin.config.GroupConfig;
import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.AbstractService;

public class JPAPluginSettingServiceImpl extends AbstractService implements PluginSettingService {
  private static final String NAME_SPACES        = "exo:";

  private List<PluginConfig> pluginConfigs = new ArrayList<PluginConfig>();

  private Map<String, GroupProvider> groupPluginMap = new ConcurrentHashMap<String, GroupProvider>();

  private SettingService settingService;
  private ChannelManager channelManager;

  public JPAPluginSettingServiceImpl(SettingService settingService, ChannelManager channelManager) { 
    this.settingService = settingService;
    this.channelManager = channelManager;
  }

  @Override
  public void registerPluginConfig(PluginConfig pluginConfig) {
    pluginConfigs.add(pluginConfig);
    Collections.sort(pluginConfigs, new OrderComparatorASC());
    if (pluginConfig.isChildPlugin() == false) {
      PluginInfo pluginInfo = new PluginInfo();
      pluginInfo.setType(pluginConfig.getPluginId())
                  .setOrder(Integer.valueOf(pluginConfig.getOrder()))
                  .setResourceBundleKey(pluginConfig.getResourceBundleKey())
                  .setBundlePath(pluginConfig.getBundlePath())
                  .setDefaultConfig(pluginConfig.getDefaultConfig());
      //all channels
      List<String> channels = new ArrayList<String>();
      for (AbstractChannel channel : channelManager.getChannels()) {
        channels.add(channel.getId());
      }
      pluginInfo.setChannelActives(getSettingPlugins(pluginConfig.getPluginId(), channels));
      
      String groupId = pluginConfig.getGroupId();
      GroupConfig gConfig = pluginConfig.getGroupConfig();
      if (gConfig != null) {
        groupId = gConfig.getId();
      }
      //
      if (groupPluginMap.containsKey(groupId)) {
        groupPluginMap.get(groupId).addPluginInfo(pluginInfo);
      } else if (groupId != null && groupId.length() > 0) {
        GroupProvider groupProvider = new GroupProvider(groupId);
        groupProvider.addPluginInfo(pluginInfo);
        if (gConfig != null) {
          groupProvider.setOrder(Integer.valueOf(gConfig.getOrder()));
          groupProvider.setResourceBundleKey(gConfig.getResourceBundleKey());
        }
        groupPluginMap.put(groupId, groupProvider);
      }
    }
  }

  @Override
  public void registerGroupConfig(GroupProviderPlugin groupConfigPlg) {
    for (GroupConfig gconfig : groupConfigPlg.getGroupProviders()) {
      GroupProvider groupProvider = new GroupProvider(gconfig.getId());
      groupProvider.setOrder(Integer.valueOf(gconfig.getOrder()));
      groupProvider.setResourceBundleKey(gconfig.getResourceBundleKey());
      if (groupPluginMap.containsKey(gconfig.getId())) {
        groupProvider.setPluginInfos(groupPluginMap.get(gconfig.getId()).getPluginInfos());
      }
      groupPluginMap.put(gconfig.getId(), groupProvider);
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
    for (GroupProvider groupPlugin : groupPluginMap.values()) {
      for (PluginInfo pluginInfo : groupPlugin.getPluginInfos()) {
        pluginInfo.setChannelActives(getSettingPlugins(pluginInfo.getType()));
      }
      groupProviders.add(groupPlugin);
    }
    Collections.sort(groupProviders, new OrderComparatorASC());
    return groupProviders;
  }

  @Override
  public void saveActivePlugin(String channelId, String pluginId, boolean isActive) {
    List<String> current = getSettingPlugins(pluginId);
    if (isActive) {
      if (!current.contains(channelId)) {
        current.add(channelId);
        saveActivePlugins(pluginId, current);
      }
    } else if (current.contains(channelId)) {
      current.remove(channelId);
      saveActivePlugins(pluginId, current);
    }
  }

  private void saveActivePlugins(String pluginId, List<String> channels) {
    saveActivePlugins(pluginId, NotificationUtils.listToString(channels, VALUE_PATTERN));
  }

  private void saveActivePlugins(String pluginId, String channelIds) {
    settingService.set(Context.GLOBAL, Scope.GLOBAL.id(null), (NAME_SPACES + pluginId), SettingValue.create(channelIds));
  }

  private List<String> getSettingPlugins(String pluginId, List<String> defaultValue) {
    return NotificationUtils.stringToList(getSetting(pluginId, NotificationUtils.listToString(defaultValue)));
  }

  private List<String> getSettingPlugins(String pluginId) {
    return getSettingPlugins(pluginId, null);
  }

  private String getSetting(String pluginId, String defaultValues) {
    SettingValue<?> sValue = settingService.get(Context.GLOBAL, Scope.GLOBAL.id(null), (NAME_SPACES + pluginId));
    String channels = defaultValues;
    String values = "";
    if (sValue != null) {
      values = String.valueOf(sValue.getValue());
      if ("false".equals(values)) {
        channels = defaultValues.replace(UserSetting.EMAIL_CHANNEL, "");
      } else if (!"true".equals(values)) {
        channels = getValues(values);
      }
    }
    // Upgrade old data
    if (!defaultValues.isEmpty() && (sValue == null || values.equals("true") || values.equals("false"))) {
      saveActivePlugins(pluginId, NotificationUtils.stringToList(channels));
    }
    return channels;
  }

  private boolean isActive(String channelId, String pluginId, boolean defaultValue) {
    List<String> current = getSettingPlugins(pluginId);
    if(current.contains(channelId)) {
      return true;
    }
    return defaultValue;
  }

  @Override
  public boolean isActive(String channelId, String pluginId) {
    return isActive(channelId, pluginId, false);
  }

  @Override
  public List<String> getActivePluginIds(String channelId) {
    Set<String> activePluginIds = new HashSet<String>();
    Iterator<PluginConfig> pluginsIterator = pluginConfigs.iterator();
    while (pluginsIterator.hasNext()) {
      PluginConfig pluginConfig = pluginsIterator.next();
      if (!pluginConfig.isChildPlugin() && isActive(channelId, pluginConfig.getPluginId())) {
        activePluginIds.add(pluginConfig.getPluginId());
      }
    }
    return Collections.unmodifiableList(new ArrayList<String>(activePluginIds));
  }

  @Override
  public List<PluginInfo> getActivePlugins(String channelId) {
    Set<PluginInfo> activePlugins = new HashSet<PluginInfo>();
    for (GroupProvider groupPlugin : groupPluginMap.values()) {
      for (PluginInfo pluginInfo : groupPlugin.getPluginInfos()) {
        if (isActive(channelId, pluginInfo.getType())) {
          activePlugins.add(pluginInfo);
        }
      }
    }
    return Collections.unmodifiableList(new ArrayList<PluginInfo>(activePlugins));
  }

  @Override
  public List<PluginInfo> getAllPlugins() {
    Set<PluginInfo> activePlugins = new HashSet<PluginInfo>();
    for (GroupProvider groupPlugin : groupPluginMap.values()) {
      for (PluginInfo pluginInfo : groupPlugin.getPluginInfos()) {
        activePlugins.add(pluginInfo);
      }
    }
    return Collections.unmodifiableList(new ArrayList<PluginInfo>(activePlugins));
  }

  @Override
  public void saveActive(String pluginId, boolean isActive) {
    saveActivePlugin(UserSetting.EMAIL_CHANNEL, pluginId, isActive);
  }

  @Override
  public boolean isActive(String pluginId) {
    return isActive(UserSetting.EMAIL_CHANNEL, pluginId);
  }

}

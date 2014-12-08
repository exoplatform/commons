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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jcr.Node;

import org.exoplatform.commons.api.notification.model.GroupProvider;
import org.exoplatform.commons.api.notification.model.PluginInfo;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.plugin.GroupProviderPlugin;
import org.exoplatform.commons.api.notification.plugin.config.GroupConfig;
import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.notification.NotificationConfiguration;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class PluginSettingServiceImpl extends AbstractService implements PluginSettingService {
  private static final Log LOG = ExoLogger.getLogger(PluginSettingServiceImpl.class);

  private List<PluginConfig> pluginConfigs = new ArrayList<PluginConfig>();

  private Map<String, GroupProvider> groupPluginMap = new ConcurrentHashMap<String, GroupProvider>();

  private static final String NAME_SPACES = "exo:";

  /** Defines the number of days in each month per plugin*/
  private static final int DAYS_OF_MONTH = 31;

  private SettingService settingService;

  public PluginSettingServiceImpl(SettingService settingService) { 
    this.settingService = settingService;
  }

  @Override
  public void registerPluginConfig(PluginConfig pluginConfig) {
    pluginConfigs.add(pluginConfig);
    if (pluginConfig.isChildPlugin() == false) {
      PluginInfo pluginInfo = new PluginInfo();
      pluginInfo.setType(pluginConfig.getPluginId())
                  .setOrder(Integer.valueOf(pluginConfig.getOrder()))
                  .setResourceBundleKey(pluginConfig.getResourceBundleKey())
                  .setBundlePath(pluginConfig.getTemplateConfig().getBundlePath())
                  .setDefaultConfig(pluginConfig.getDefaultConfig());
      // for all chanel
      pluginInfo.setChannelActives(getSettingPlugins(pluginConfig.getPluginId(), UserSetting.EMAIL_CHANNEL + "," + UserSetting.INTRANET_CHANNEL/*all channels*/));
      
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

      createParentNodeOfPlugin(pluginConfig.getPluginId());
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
        pluginInfo.setChannelActives(getSettingPlugins(pluginInfo.getType(), ""));
      }
      groupProviders.add(groupPlugin);
    }
    Collections.sort(groupProviders, new ComparatorASC());
    return groupProviders;
  }

  @Override
  public void saveActivePlugin(String channelId, String pluginId, boolean isActive) {
    List<String> current = getSettingPlugins(pluginId, "");
    if (isActive && !current.contains(channelId)) {
      current.add(channelId);
      saveActivePlugins(pluginId, NotificationUtils.listToString(current));
    } else if (current.contains(channelId)) {
      current.remove(channelId);
      saveActivePlugins(pluginId, NotificationUtils.listToString(current));
    }
  }

  public void saveActivePlugins(String pluginId, String channelIds) {
    settingService.set(Context.GLOBAL, Scope.GLOBAL, (NAME_SPACES + pluginId), SettingValue.create(channelIds));
  }

  private List<String> getSettingPlugins(String pluginId, String defaultValue) {
    return NotificationUtils.stringToList(getSetting(pluginId, defaultValue));
  }

  private String getSetting(String pluginId, String defaultChannelIds) {
    SettingValue<?> sValue = settingService.get(Context.GLOBAL, Scope.GLOBAL, (NAME_SPACES + pluginId));
    if (sValue != null) {
      String channels = String.valueOf(sValue.getValue());
      if (channels.equals("true")) { // old data is true
        channels = UserSetting.EMAIL_CHANNEL;
      } else if (channels.equals("false") || channels.isEmpty()) {
        channels = "";
      }
      //
      if (defaultChannelIds != null && !defaultChannelIds.isEmpty()) {
        channels = (channels.isEmpty()) ? "" : "," + defaultChannelIds;
        saveActivePlugins(pluginId, channels);
      }
      return channels;
    }
    return defaultChannelIds;
  }

  private boolean isActive(String channelId, String pluginId, boolean defaultValue) {
    List<String> current = getSettingPlugins(pluginId, (defaultValue) ? channelId : "");
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
    Collections.sort(pluginConfigs, new ComparatorASC());
    for (PluginConfig pluginConfig : pluginConfigs) {
      if (pluginConfig.isChildPlugin() == false && isActive(channelId, pluginConfig.getPluginId())) {
        activePluginIds.add(pluginConfig.getPluginId());
      }
    }
    return new ArrayList<String>(activePluginIds);
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

  private void createParentNodeOfPlugin(String pluginId) {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      NotificationConfiguration configuration = CommonsUtils.getService(NotificationConfiguration.class);
      Node node = getMessageNodeByPluginId(sProvider, configuration.getWorkspace(), pluginId);
      for(int i = 1 ; i <= DAYS_OF_MONTH; i++) {
        getOrCreateMessageNode(node, DAY + i);
      }
      
    } catch (Exception e) {
      LOG.error("Failed to create parent Node for plugin " + pluginId);
    } finally {
      sProvider.close();
    }
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
  public void saveActive(String pluginId, boolean isActive) {
    saveActivePlugin(UserSetting.EMAIL_CHANNEL, pluginId, isActive);
  }

  @Override
  public boolean isActive(String pluginId) {
    return isActive(UserSetting.EMAIL_CHANNEL, pluginId);
  }

}

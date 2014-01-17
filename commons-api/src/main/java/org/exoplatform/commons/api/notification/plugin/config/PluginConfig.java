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
package org.exoplatform.commons.api.notification.plugin.config;

import java.util.ArrayList;
import java.util.List;

public class PluginConfig {

  private String         pluginId;

  private String         resourceBundleKey;

  private String         order = "0";

  private String         groupId = "other";
  
  private List<String>   defaultConfig = new ArrayList<String>();

  private GroupConfig    groupConfig;

  private TemplateConfig templateConfig;

  public PluginConfig() {
  }

  /**
   * @return the pluginId
   */
  public String getPluginId() {
    return pluginId;
  }

  /**
   * @param pluginId the pluginId to set
   */
  public void setPluginId(String pluginId) {
    this.pluginId = pluginId;
  }

  /**
   * @return the resourceBundleKey
   */
  public String getResourceBundleKey() {
    return resourceBundleKey;
  }

  /**
   * @param resourceBundleKey the resourceBundleKey to set
   */
  public void setResourceBundleKey(String resourceBundleKey) {
    this.resourceBundleKey = resourceBundleKey;
  }

  /**
   * @return the order
   */
  public String getOrder() {
    return order;
  }

  /**
   * @param order the order to set
   */
  public void setOrder(String order) {
    this.order = order;
  }

  /**
   * @return the defaultConfig
   */
  public List<String> getDefaultConfig() {
    return defaultConfig;
  }

  /**
   * @param defaultConfig the defaultConfig to set
   */
  public void setDefaultConfig(List<String> defaultConfig) {
    this.defaultConfig = defaultConfig;
  }

  /**
   * @return the groupId
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * @param groupId the groupId to set
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * @return the groupConfig
   */
  public GroupConfig getGroupConfig() {
    if (groupConfig == null) {
      return null;
    }
    return groupConfig.addProvider(pluginId);
  }

  /**
   * @param groupConfig the groupConfig to set
   */
  public void setGroupConfig(GroupConfig groupConfig) {
    this.groupConfig = groupConfig;
  }

  /**
   * @return the templateConfig
   */
  public TemplateConfig getTemplateConfig() {
    return templateConfig.setProviderId(pluginId);
  }

  /**
   * @param templateConfig the templateConfig to set
   */
  public void setTemplateConfig(TemplateConfig templateConfig) {
    this.templateConfig = templateConfig;
  }

}

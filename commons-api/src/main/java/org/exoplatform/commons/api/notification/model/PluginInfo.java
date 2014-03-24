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
package org.exoplatform.commons.api.notification.model;

import java.util.ArrayList;
import java.util.List;


public class PluginInfo {

  private String       type;

  private int          order         = 0;

  private String       resourceBundleKey;
  
  private String       bundlePath;

  private boolean     isActive      = true;

  private List<String> defaultConfig = new ArrayList<String>();

  public PluginInfo() {

  }

  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * @param type the id to set
   */
  public PluginInfo setType(String type) {
    this.type = type;
    return this;
  }

  /**
   * @return the order
   */
  public int getOrder() {
    return order;
  }

  /**
   * @param order the order to set
   */
  public PluginInfo setOrder(int order) {
    this.order = order;
    return this;
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
   * @return the resourceBundleKey
   */
  public String getResourceBundleKey() {
    return resourceBundleKey;
  }

  /**
   * @param resourceBundleKey the resourceBundleKey to set
   */
  public PluginInfo setResourceBundleKey(String resourceBundleKey) {
    this.resourceBundleKey = resourceBundleKey;
    return this;
  }
  
  /**
   * @return the bundlePath
   */
  public String getBundlePath() {
    return bundlePath;
  }

  /**
   * @param bundlePath the bundlePath to set
   */
  public PluginInfo setBundlePath(String bundlePath) {
    this.bundlePath = bundlePath;
    return this;
  }

  /**
   * @return the isActive
   */
  public boolean isActive() {
    return isActive;
  }

  /**
   * @param isActive the isActive to set
   */
  public PluginInfo setActive(boolean isActive) {
    this.isActive = isActive;
    return this;
  }

  public PluginInfo end() {
    return this;
  }
}

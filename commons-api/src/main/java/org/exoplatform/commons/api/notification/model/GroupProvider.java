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
import java.util.Arrays;
import java.util.List;

public class GroupProvider {
  public static final List<String> defaultGroupIds = Arrays.asList("general", "connections", 
                                                                     "spaces", "activity_stream", "other");

  private String             groupId;

  private String             resourceBundleKey;

  private int                order         = 0;

  private List<PluginInfo> providerDatas = new ArrayList<PluginInfo>();

  public GroupProvider(String groupId) {
    this.groupId = groupId;
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
  public int getOrder() {
    return order;
  }

  /**
   * @param order the order to set
   */
  public void setOrder(int order) {
    this.order = order;
  }

  /**
   * @return the providerDatas
   */
  public List<PluginInfo> getProviderDatas() {
    return providerDatas;
  }

  /**
   * @param providerDatas the providerDatas to set
   */
  public void setProviderDatas(List<PluginInfo> providerDatas) {
    this.providerDatas = providerDatas;
  }

  /**
   * @param providerDatas the providerDatas to set
   */
  public void addProviderData(PluginInfo providerData) {
    this.providerDatas.add(providerData);
  }

}

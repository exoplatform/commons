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

public class GroupConfig {

  private String       order;

  private String       id;

  private String       resourceBundleKey = "";

  private List<String> providers;

  public GroupConfig() {
    providers = new ArrayList<String>();
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public GroupConfig setId(String id) {
    this.id = id;
    return this;
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
   * @return the providers
   */
  public List<String> getProviders() {
    return providers;
  }

  /**
   * @param providers the providers to set
   */
  public void setProviders(List<String> providers) {
    this.providers = providers;
  }

  public GroupConfig addProvider(String providerId) {
    this.providers.add(providerId);
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof GroupConfig) {
      return ((GroupConfig) obj).getId().equals(this.getId());
    }
    return super.equals(obj);
  }

}

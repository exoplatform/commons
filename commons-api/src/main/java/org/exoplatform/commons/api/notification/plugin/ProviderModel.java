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
package org.exoplatform.commons.api.notification.plugin;

import java.util.List;

public class ProviderModel {
  private String         type;

  private String         name;

  private List<String>   params;

  private List<Template> templates;

  public ProviderModel() {
  }

  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the params
   */
  public List<String> getParams() {
    return params;
  }

  /**
   * @param params the params to set
   */
  public void setParams(List<String> params) {
    this.params = params;
  }

  /**
   * @return the templates
   */
  public List<Template> getTemplates() {
    return templates;
  }

  /**
   * @param templates the templates to set
   */
  public void setTemplates(List<Template> templates) {
    this.templates = templates;
  }

}

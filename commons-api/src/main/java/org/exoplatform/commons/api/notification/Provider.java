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
package org.exoplatform.commons.api.notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Value;

public class Provider {
  private String              type;

  private String              name;

  private boolean            isActive = false;
  
  private List<String>        params = new ArrayList<String>();

  private Map<String, String> templates = new HashMap<String, String>();

  private Map<String, String> subjects = new HashMap<String, String>();

  public Provider() {

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
  public Provider setType(String type) {
    this.type = type;
    return this;
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
  public Provider setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * @return the boolean of isActive
   */
  public boolean isActive() {
    return isActive;
  }

  /**
   * @param isActive the isActive to set
   */

  public Provider setIsActive(boolean isActive) {
    this.isActive = isActive;
    return this;
  }

  /**
   * @return the params
   */
  public List<String> getParams() {
    return params;
  }

  /**
   * @return the params
   */
  public String[] getArrayParams() {
    if (params != null && params.size() > 0) {
      return params.toArray(new String[params.size()]);
    }
    return new String[] { "" };
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
  public Map<String, String> getTemplates() {
    return templates;
  }
  
  public String[] getArrayTemplates() {
    Set<String> keys = templates.keySet();
    String[] templates_ = new String[keys.size()];
    int i = 0;
    for (String key : keys) {
      templates_[i] = new StringBuffer(key).append("=").append(templates.get(key)).toString();
    }
    return templates_;
  }
  
  public void setTemplates(Value[] templates) {
    this.templates.clear();
    String values, language, template;
    for (Value value : templates) {
      try {
        values = value.getString();
        language = values.substring(0, values.indexOf("="));
        template = values.substring(values.indexOf("=") + 1);
        this.templates.put(language, template);
      } catch (Exception e) {
        continue;
      }
    }
  }

  /**
   * @param templates the templates to set
   */
  public void setTemplates(Map<String, String> templates) {
    this.templates = templates;
  }
  
  public void addTemplate(String language, String template) {
    this.templates.put(language, template);
  }

  /**
   * @return the subjects
   */
  public Map<String, String> getSubjects() {
    return subjects;
  }
  
  /**
   * @param subjects the subjects to set
   */
  public void setSubjects(Map<String, String> subjects) {
    this.subjects = subjects;
  }
  
  public void addSubject(String language, String subject) {
    this.subjects.put(language, subject);
  }
}

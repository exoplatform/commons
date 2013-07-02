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
// will rename to ProviderData
public class Provider {
  private String              type;

  private String              name;

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
  public Provider setParams(List<String> params) {
    this.params = params;
    return this;
  }

  /**
   * @param params the params to set
   */
  public Provider setParams(Value[] params) {
    this.params.clear();
    this.params = valuesToList(params);
    return this;
  }

  /**
   * @return the templates
   */
  public Map<String, String> getTemplates() {
    return templates;
  }
  
  public String[] getArrayTemplates() {
    return mapToArray(templates);
  }

  /**
   * @param templates the value set to templates
   */
  public Provider setTemplates(Value[] templates) {
    this.templates.clear();
    this.templates = valueToMap(templates);
    return this;
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
   * @return the arrays of subjects
   */
  public String[] getArraySubjects() {
    return mapToArray(subjects);
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

  public Provider setSubjects(Value[] subjects) {
    this.subjects.clear();
    this.subjects = valueToMap(subjects);
    return this;
  }
  
  private String[] mapToArray(Map<String, String> map) {
    Set<String> keys = map.keySet();
    String[] strs = new String[keys.size()];
    int i = 0;
    for (String key : keys) {
      strs[i] = new StringBuffer(key).append("=").append(map.get(key)).toString();
    }
    return strs;
  }

  private Map<String, String> valueToMap(Value[] templates) {
    Map<String, String> map = new HashMap<String, String>();
    String values, key, value;
    for (Value vl : templates) {
      try {
        values = vl.getString();
        key = values.substring(0, values.indexOf("="));
        value = values.substring(values.indexOf("=") + 1);
        map.put(key, value);
      } catch (Exception e) {
        continue;
      }
    }
    return map;
  }

  private List<String> valuesToList(Value[] values) {
    List<String> list = new ArrayList<String>();
    if (values.length < 1)
      return list;
    String s;
    for (int i = 0; i < values.length; ++i) {
      try {
        s = values[i].getString();
        if (s != null && s.trim().length() > 0) {
          list.add(s);
        }
      } catch (Exception e) {
        continue;
      }
    }
    return list;
  }
}

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
package org.exoplatform.commons.notification.template;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.commons.api.notification.service.template.TemplateGenerator;
import org.exoplatform.commons.notification.cache.CacheTemplateGenerator;
import org.exoplatform.commons.notification.cache.SimpleCacheKey;
import org.exoplatform.commons.utils.CommonsUtils;

public class TemplateElement {
  private String                language;

  private String                template;

  private String                templateText;

  private String                templatePath;

  private TemplateResourceBundle resourceBundle;

  private TemplateVisitorContext       context;

  private Map<String, String>   resourceBunldMappingKey = new HashMap<String, String>();

  public TemplateElement(String templatePath, String language) {
    this.language = language;
    this.templatePath = templatePath;
  }

  public TemplateVisitorContext accept(TemplateVisitorContext context) {
    this.context = context;
    //TODO WHY???
    this.context.put("_ctx", this);
    return this.context;
  }

  /**
   * @return the template
   */
  public String getTemplate() {
    return template;
  }

  /**
   * @param content the content to set
   */
  public void setTemplate(String template) {
    this.template = template;
  }

  /**
   * @return the templateText
   */
  public String getTemplateText() {
    return templateText;
  }

  /**
   * @param templateText the templateText to set
   */
  public void setTemplateText(String templateText) {
    this.templateText = templateText;
  }

  /**
   * @return the templatePath
   */
  public String getTemplatePath() {
    return templatePath;
  }

  /**
   * @param templatePath the templatePath to set
   */
  public void setTemplatePath(String templatePath) {
    this.templatePath = templatePath;
  }

  public TemplateVisitorContext getContext() {
    return context;
  }

  /**
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * @param language the language to set
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * @return the resourceBundle
   */
  public TemplateResourceBundle getResourceBundle() {
    return resourceBundle;
  }

  /**
   * @param resourceBundle the resourceBundle to set
   */
  public void setResourceBundle(TemplateResourceBundle resourceBundle) {
    this.resourceBundle = resourceBundle;
  }

  /**
   * @return the resourceBunldMappingKey
   */
  public Map<String, String> getResourceBunldMappingKey() {
    return resourceBunldMappingKey;
  }

  /**
   * @param resourceBunldMappingKey the resourceBunldMappingKey to set
   */
  public void setResourceBunldMappingKey(Map<String, String> resourceBunldMappingKey) {
    this.resourceBunldMappingKey = resourceBunldMappingKey;
  }

  public void addResourceBunldMappingKey(String key, String value) {
    this.resourceBunldMappingKey.put(key, value);
  }
  

  private String getBundleKey(String key) {
    String value = resourceBunldMappingKey.get(key);
    if (value != null) {
      return value;
    }
    return key;
  }

  public String appRes(String key) {
    return resourceBundle.appRes(getBundleKey(key));
  }

  public String appRes(String key, String... strs) {
    return resourceBundle.appRes(getBundleKey(key), strs);
  }

  public void include(String elementLocal) throws Exception {
    CacheTemplateGenerator generator = (CacheTemplateGenerator) CommonsUtils.getService(TemplateGenerator.class);
    SimpleCacheKey cacheKey = new SimpleCacheKey(elementLocal, language);
    TemplateElement element = generator.getTemplateElement(cacheKey);
    element.setResourceBundle(resourceBundle);
    //
    context.popElement();
    context.visit(element);
    if (element.getTemplateText() != null && element.getTemplateText().length() > 0) {
      generator.putTemplateToCache(cacheKey, element);
    }
  }

}

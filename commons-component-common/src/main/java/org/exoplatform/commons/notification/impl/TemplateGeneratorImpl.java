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
package org.exoplatform.commons.notification.impl;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.exoplatform.commons.api.notification.plugin.MappingKey;
import org.exoplatform.commons.api.notification.plugin.TemplateConfigurationPlugin;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.SubjectAndDigestTemplate;
import org.exoplatform.commons.notification.template.TemplateContext;
import org.exoplatform.commons.notification.template.TemplateElement;
import org.exoplatform.commons.notification.template.TemplateResouceBundle;

public class TemplateGeneratorImpl {

  private Map<String, SubjectAndDigestTemplate> cacheTemplate = new ConcurrentHashMap<String, SubjectAndDigestTemplate>();
  
  private Set<MappingKey> allMappingKeys = new HashSet<MappingKey>();
  
  private static TemplateGeneratorImpl instance;

  private TemplateGeneratorImpl() {
  }
  
  public static TemplateGeneratorImpl getInstance() {
    if (instance == null) {
      instance = new TemplateGeneratorImpl();
    }
    return instance;
  }
  
  public void registerTemplateConfigurationPlugin(TemplateConfigurationPlugin configurationPlugin) {
    allMappingKeys.addAll(configurationPlugin.getMappingKeys());
  }
  
  public String processTemplateIntoString(TemplateContext context, TemplateElement template) {
    context.visit(template);
    return template.getTemplate();
  }

  public TemplateElement getTemplateElement(String key, String language) {
    TemplateElement templateElement;
    if(key.indexOf("/") < 0) {
      MappingKey mappingKey = getMappingKey(key);
      String resouceLocal = mappingKey.getLocaleResouceTemplate();
      templateElement = new TemplateElement(resouceLocal, language);
      templateElement.setResouceBundle(new TemplateResouceBundle(language, mappingKey.getLocaleResouceBundle()));
      templateElement.setResouceBunldMappingKey(mappingKey.getKeyMapping());
    } else {
      templateElement = new TemplateElement(key, language);
    }
    return templateElement;
  }
  
  public String processSubjectIntoString(String providerId, Map<String, String> valueables, String language) {
    SubjectAndDigestTemplate template = getSubjectOrDigestTemplate(providerId, language);
    template.setValueables(valueables);
    return template.processSubject();
  }

  public String processDigestIntoString(String providerId, Map<String, String> valueables, String language, int size) {
    SubjectAndDigestTemplate template = getSubjectOrDigestTemplate(providerId, language);
    template.setValueables(valueables);
    return template.processDigest(size);
  }
  
  public SubjectAndDigestTemplate getSubjectOrDigestTemplate(String providerId, String language) {
    if (language == null) {
      language = Locale.ENGLISH.getLanguage();
    }
    String key = new StringBuffer(providerId).append(language).toString();
    if (cacheTemplate.containsKey(key)) {
      return cacheTemplate.get(key);
    }
    //
    MappingKey mappingKey = getMappingKey(providerId);
    SubjectAndDigestTemplate template = NotificationUtils.getTemplate(mappingKey, providerId, language);
    cacheTemplate.put(key, template);
    return template;
  }
  
  public MappingKey getMappingKey(String providerId) {
    for (MappingKey mappingKey : allMappingKeys) {
      if(mappingKey.getProviderId().equals(providerId)) {
        return mappingKey;
      }
    }
    return new MappingKey(providerId);
  }

}

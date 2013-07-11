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

import org.exoplatform.commons.api.notification.NotificationTemplate;
import org.exoplatform.commons.api.notification.plugin.MappingKey;
import org.exoplatform.commons.api.notification.plugin.TemplateConfigurationPlugin;
import org.exoplatform.commons.api.notification.service.TemplateGenerator;
import org.exoplatform.commons.notification.NotificationUtils;

public class TemplateGeneratorImpl implements TemplateGenerator {

  private Map<String, NotificationTemplate> cacheTemplate = new ConcurrentHashMap<String, NotificationTemplate>();
  
  private Set<MappingKey> allMappingKeys = new HashSet<MappingKey>();

  public TemplateGeneratorImpl() {
  }
  
  @Override
  public void registerTemplateConfigurationPlugin(TemplateConfigurationPlugin configurationPlugin) {
    allMappingKeys.addAll(configurationPlugin.getMappingKeys());
  }
  
  @Override
  public String processTemplateIntoString(String providerId, Map<String, String> valueables, String language) {
    NotificationTemplate template = getNotificationTemplate(providerId, language);
    template.setValueables(valueables);
    return template.processTemplate();
  }

  @Override
  public String processSubjectIntoString(String providerId, Map<String, String> valueables, String language) {
    NotificationTemplate template = getNotificationTemplate(providerId, language);
    template.setValueables(valueables);
    return template.processSubject();
  }
  @Override
  public String processDigestIntoString(String providerId, Map<String, String> valueables, String language, int size) {
    NotificationTemplate template = getNotificationTemplate(providerId, language);
    template.setValueables(valueables);
    return template.processDigest(size);
  }
  
  public NotificationTemplate getNotificationTemplate(String providerId, String language) {
    if (language == null) {
      language = Locale.ENGLISH.getLanguage();
    }
    String key = new StringBuffer(providerId).append(language).toString();
    if (cacheTemplate.containsKey(key)) {
      return cacheTemplate.get(key);
    }
    //
    MappingKey mappingKey = getMappingKey(providerId);
    NotificationTemplate template = NotificationUtils.getTemplate(mappingKey, providerId, language);
    cacheTemplate.put(key, template);
    return template;
  }
  
  
  public MappingKey getMappingKey(String providerId) {
    for (MappingKey mappingKey : allMappingKeys) {
      if(mappingKey.getProviderId().equals(providerId)) {
        return mappingKey;
      }
    }
    return new MappingKey();
  }

}

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
package org.exoplatform.commons.notification.impl.service.template;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.api.notification.plugin.config.TemplateConfig;
import org.exoplatform.commons.api.notification.service.setting.ProviderSettingService;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.SubjectAndDigest;
import org.exoplatform.commons.notification.template.TemplateElement;
import org.exoplatform.commons.notification.template.TemplateResourceBundle;
import org.exoplatform.commons.notification.template.TemplateVisitorContext;
import org.exoplatform.commons.utils.CommonsUtils;

public class TemplateGeneratorImpl {

  private Map<String, SubjectAndDigest> cacheTemplate = new ConcurrentHashMap<String, SubjectAndDigest>();
  
  private ProviderSettingService providerSettingService;

  private static TemplateGeneratorImpl instance;

  private TemplateGeneratorImpl() {
  }
  
  public static TemplateGeneratorImpl getInstance() {
    if (instance == null) {
      instance = new TemplateGeneratorImpl();
    }
    return instance;
  }

  private ProviderSettingService getProviderSettingService() {
    if (providerSettingService == null) {
      providerSettingService = CommonsUtils.getService(ProviderSettingService.class);
    }
    return providerSettingService;
  }

  public void setProviderSettingService(ProviderSettingService providerSettingService) {
    this.providerSettingService = providerSettingService;
  }

  public String processTemplateIntoString(TemplateVisitorContext context, TemplateElement template) {
    context.visit(template);
    return template.getTemplate();
  }

  public TemplateElement getTemplateElement(String key, String language) {
    TemplateElement templateElement;
    if(key.indexOf("/") < 0) {
      TemplateConfig templateConfig = getTemplateConfig(key);
      String templatePath = templateConfig.getTemplatePath();
      templateElement = new TemplateElement(templatePath, language);
      templateElement.setResourceBundle(new TemplateResourceBundle(language, templateConfig.getBundlePath()));
      templateElement.setResourceBunldMappingKey(templateConfig.getKeyMapping());
    } else {
      templateElement = new TemplateElement(key, language);
    }
    return templateElement;
  }
  
  public String processSubject(TemplateContext ctx) {
    SubjectAndDigest template = getSubjectOrDigestTemplate(ctx.getProviderId(), ctx.getLanguage());
    template.setValueables(ctx);
    return template.processSubject();
  }

  public String processDigest(TemplateContext ctx) {
    SubjectAndDigest template = getSubjectOrDigestTemplate(ctx.getProviderId(), ctx.getLanguage());
    template.setValueables(ctx);
    ctx.clear();
    return template.processDigest(ctx.getDigestSize());
  }
  
  public SubjectAndDigest getSubjectOrDigestTemplate(String providerId, String language) {
    if (language == null) {
      language = Locale.ENGLISH.getLanguage();
    }
    String key = new StringBuffer(providerId).append(language).toString();
    if (cacheTemplate.containsKey(key)) {
      return cacheTemplate.get(key);
    }
    //
    TemplateConfig templateConfig = getTemplateConfig(providerId);
    SubjectAndDigest template = NotificationUtils.getSubjectAndDigest(templateConfig, providerId, language);
    cacheTemplate.put(key, template);
    return template;
  }
  
  public TemplateConfig getTemplateConfig(String providerId) {
    PluginConfig pluginConfig = getProviderSettingService().getPluginConfig(providerId);
    if(pluginConfig != null) {
      return pluginConfig.getTemplateConfig();
    }
    return  new TemplateConfig(providerId);
  }

}

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
package org.exoplatform.commons.notification;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.exoplatform.commons.api.notification.TemplateContext;
import org.exoplatform.commons.api.notification.plugin.model.GroupConfig;
import org.exoplatform.commons.api.notification.plugin.model.PluginConfig;
import org.exoplatform.commons.api.notification.plugin.model.TemplateConfig;
import org.exoplatform.commons.api.notification.service.TemplateGenerator;
import org.exoplatform.commons.api.notification.service.setting.ProviderSettingService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.notification.cache.CacheTemplateGenerator;
import org.exoplatform.commons.notification.cache.SimpleCacheKey;
import org.exoplatform.commons.notification.impl.TemplateGeneratorImpl;
import org.exoplatform.commons.notification.impl.setting.ProviderSettingServiceImpl;
import org.exoplatform.commons.notification.template.TemplateElement;
import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.groovyscript.GroovyTemplate;

public class TemplateGeneratorTest extends BaseCommonsTestCase {
  
  private TemplateGenerator generator;
  private TemplateGeneratorImpl generatorImpl;
  private SettingService settingService;
  private List<String> providerIds = Arrays.asList("provider1", "provider2", "provider3");
  
  public void setUp() throws Exception {
    super.setUp();
    
    generator = getService(TemplateGenerator.class);
    
    generatorImpl = TemplateGeneratorImpl.getInstance();
    
    settingService = new SettingService() {
      @Override
      public void set(Context context, Scope scope, String key, SettingValue<?> value) {
      }
      @Override
      public void remove(Context context) {
      }
      @Override
      public void remove(Context context, Scope scope) {
      }
      @Override
      public void remove(Context context, Scope scope, String key) {
      }
      @Override
      public SettingValue<?> get(Context context, Scope scope, String key) {
        return null;
      }
    };
    
    makeTemplatePlugin();
    
  }
  
  private void makeTemplatePlugin() {

    ProviderSettingService providerSettingService = new ProviderSettingServiceImpl(settingService);
    
    for(String providerId : providerIds) {
      
      TemplateConfig templateConfig = new TemplateConfig();
      templateConfig.setProviderId(providerId);
      templateConfig.setLocaleResouceTemplate("jar:/groovy/notification/template/" + providerId + ".gtmpl");
      templateConfig.addKeyMapping(TemplateConfig.SUBJECT_KEY, "Notification." + providerId + ".subject")
                .addKeyMapping(TemplateConfig.DIGEST_KEY, "Notification." + providerId + ".digest")
                .addKeyMapping(TemplateConfig.DIGEST_ONE_KEY, "Notification." + providerId + ".digestone")
                .addKeyMapping(TemplateConfig.DIGEST_MORE_KEY, "Notification." + providerId + ".digestmore")
                .addKeyMapping(TemplateConfig.DIGEST_THREE_KEY, "Notification." + providerId + ".digestthree");
      
      GroupConfig groupConfig = new GroupConfig();
      groupConfig.setId("abc");
      groupConfig.setOrder("0");
      
      PluginConfig pluginConfig = new PluginConfig();
      pluginConfig.setOrder("0");
      pluginConfig.setPluginId(providerId);
      pluginConfig.setResourceBundleKey("");
      pluginConfig.setTemplateConfig(templateConfig);
      pluginConfig.setGroupConfig(groupConfig);
      providerSettingService.registerPluginConfig(pluginConfig);
    }
    //
    generatorImpl.setProviderSettingService(providerSettingService);
  }

  public void testGetTemplate(){
    String providerId = providerIds.get(0);
    TemplateElement template = generatorImpl.getTemplateElement(providerId, "en");
    assertNotNull(template);
    
    assertEquals("jar:/groovy/notification/template/" + providerId + ".gtmpl", template.getResouceLocal());
    assertEquals("locale.notification.template.Notification", template.getResouceBundle().getResouceLocal());
  }

  public void testProcessTemplate(){
    String providerId = providerIds.get(0);
    String language = "en";
    SimpleCacheKey cacheKey = new SimpleCacheKey(providerId, language);
    
    TemplateElement template = ((CacheTemplateGenerator)generator).getTemplateElement(cacheKey);
    assertNotNull(template);

    assertNull(template.getTemplateText());

    TemplateContext ctx = new TemplateContext(providerId, language);
    ctx.put("FIRSTNAME", "Demo");
    ctx.put("USER", "Root");
    ctx.put("ACTIVITY", "Hey demo, How are you ? Today, I have time to meet you. Are you busy ? Have good time !");
    ctx.put("REPLY_ACTION_URL", "http://localhost/test/reply/activtyxxx");
    ctx.put("VIEW_FULL_DISCUSSION_ACTION_URL", "http://localhost/test/view/activtyxxx");
    ctx.put("USER_NOTIFICATION_SETTINGS_URL", "http://localhost/test/settings/demo");

    ctx.put("subContent", "<br/> I love you !");
    
    String templateProcessed = generator.processTemplate(ctx);
    
    assertNotNull(templateProcessed);
    // Test cache template
    template = ((CacheTemplateGenerator)generator).getTemplateElement(cacheKey);
    assertNotNull(template);
    assertNotNull(template.getTemplateText());
    
    // Test include
    assertTrue(templateProcessed.indexOf("template-include") > 0);
    assertTrue(templateProcessed.indexOf("I love you") > 0);
    
    // Test process by groovy
    assertTrue(template.getTemplateText().indexOf("VIEW_FULL_DISCUSSION_ACTION_URL") > 0);
    assertTrue(templateProcessed.indexOf("http://localhost/test/view/activtyxxx") > 0);
    
    //Test resouce bundle
    assertTrue(templateProcessed.indexOf("Notification of activity post") > 0);
    
    // Test renderTemplateByUrl
    String url = "jar:/groovy/notification/template/include.gtmpl";
    TemplateContext ctx_ = new TemplateContext(url, language);
    ctx_.put("subContent", "<br/> test render by url!");
    String value = generator.processTemplate(ctx_);
    assertNotNull(value);
    assertTrue(value.indexOf("test render by url") > 0);
    
    // Test performance template
    ctx.put("_ctx", template);
    String tml = template.getTemplateText();
    try {
    GroovyTemplate gTemplate = new GroovyTemplate(tml);
    for (int i = 0; i < 1; ++i) {
//      generator.processTemplate(ctx_);
        StringWriter  writer = new StringWriter();
        gTemplate.render(writer, ctx);
    }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}

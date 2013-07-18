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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.exoplatform.commons.api.notification.plugin.MappingKey;
import org.exoplatform.commons.api.notification.plugin.TemplateConfigurationPlugin;
import org.exoplatform.commons.api.notification.service.TemplateGenerator;
import org.exoplatform.commons.notification.cache.CacheTemplateGenerator;
import org.exoplatform.commons.notification.cache.SimpleCacheKey;
import org.exoplatform.commons.notification.impl.TemplateGeneratorImpl;
import org.exoplatform.commons.notification.template.TemplateElement;
import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;

public class TemplateGeneratorTest extends BaseCommonsTestCase {
  
  private TemplateGenerator generator;
  private TemplateGeneratorImpl generatorImpl;
  private List<String> providerIds = Arrays.asList("provider1", "provider2", "provider3");
  
  public void setUp() throws Exception {
    super.setUp();
    
    generator = getService(TemplateGenerator.class);
    
    generatorImpl = TemplateGeneratorImpl.getInstance();
    makeTemplatePlugin();
    
  }
  
  private void makeTemplatePlugin() {

    InitParams params = new InitParams();
    Random random = new Random(System.currentTimeMillis());
    for(String providerId : providerIds) {
      
      MappingKey mappingKey = new MappingKey();
      mappingKey.setProviderId(providerId);
      mappingKey.setLocaleResouceTemplate("jar:/groovy/notification/template/" + providerId + ".gtmpl");
      mappingKey.addKeyMapping(MappingKey.SUBJECT_KEY, "Notification." + providerId + ".subject")
                .addKeyMapping(MappingKey.DIGEST_KEY, "Notification." + providerId + ".digest")
                .addKeyMapping(MappingKey.DIGEST_ONE_KEY, "Notification." + providerId + ".digestone")
                .addKeyMapping(MappingKey.DIGEST_MORE_KEY, "Notification." + providerId + ".digestmore")
                .addKeyMapping(MappingKey.DIGEST_THREE_KEY, "Notification." + providerId + ".digestthree");
      
      ObjectParameter parameter = new ObjectParameter();
      parameter.setName("pr" + random.nextLong());
      parameter.setObject(mappingKey);
      parameter.setDescription("");
      params.addParam(parameter);
      
    }
    TemplateConfigurationPlugin configurationPlugin = new TemplateConfigurationPlugin(params);
    generator.registerTemplateConfigurationPlugin(configurationPlugin);
  }

  public void testGetTemplate(){
    String providerId = providerIds.get(0);
    TemplateElement template = generatorImpl.getTemplateElement(providerId, "en");
    assertNotNull(template);
    
    assertEquals("jar:/groovy/notification/template/" + providerId + ".gtmpl", template.getResouceLocal());
    assertEquals("locale.notification.template.NotificationTemplate", template.getResouceBundle().getResouceLocal());
  }

  public void testProcessTemplate(){
    String providerId = providerIds.get(0);
    String language = "en";
    SimpleCacheKey cacheKey = new SimpleCacheKey(providerId, language);
    
    TemplateElement template = ((CacheTemplateGenerator)generator).getTemplateElement(cacheKey);
    assertNotNull(template);

    assertNull(template.getTemplateText());

    Map<String, String> valueables = new HashMap<String, String>();
    valueables.put("FIRSTNAME", "Demo");
    valueables.put("USER", "Root");
    valueables.put("ACTIVITY", "Hey demo, How are you ? Today, I have time to meet you. Are you busy ? Have good time !");
    valueables.put("REPLY_ACTION_URL", "http://localhost/test/reply/activtyxxx");
    valueables.put("VIEW_FULL_DISCUSSION_ACTION_URL", "http://localhost/test/view/activtyxxx");
    valueables.put("USER_NOTIFICATION_SETTINGS_URL", "http://localhost/test/settings/demo");

    valueables.put("subContent", "<br/> I love you !");
    
    String templateProcessed = generator.processTemplate(providerId, valueables, language);
    
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
    valueables.clear();
    valueables.put("subContent", "<br/> test render by url!");
    String value = generator.processTemplate(url, valueables, language);
    assertNotNull(value);
    assertTrue(value.indexOf("test render by url") > 0);
  }

}

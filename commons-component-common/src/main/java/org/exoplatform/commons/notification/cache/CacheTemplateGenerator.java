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
package org.exoplatform.commons.notification.cache;

import java.util.Map;

import org.exoplatform.commons.api.notification.plugin.TemplateConfigurationPlugin;
import org.exoplatform.commons.api.notification.service.TemplateGenerator;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.notification.impl.TemplateGeneratorImpl;
import org.exoplatform.commons.notification.template.TemplateElement;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.future.FutureExoCache;
import org.exoplatform.services.cache.future.Loader;

public class CacheTemplateGenerator implements TemplateGenerator {
  
  protected ExoCache<SimpleCacheKey, TemplateElement> templateCache;
  
  protected FutureExoCache<SimpleCacheKey, TemplateElement, TemplateGeneratorImpl> futureExoCache;

  private TemplateGeneratorImpl generatorImpl;
  
  public CacheTemplateGenerator(CacheService cacheService) {
    generatorImpl = TemplateGeneratorImpl.getInstance();
    //
    templateCache = cacheService.getCacheInstance(SettingService.class.getSimpleName());
    //
    Loader<SimpleCacheKey, TemplateElement, TemplateGeneratorImpl> loader = 
          new Loader<SimpleCacheKey, TemplateElement, TemplateGeneratorImpl>() {
      @Override
      public TemplateElement retrieve(TemplateGeneratorImpl service, SimpleCacheKey key) throws Exception {
        return service.getTemplateElement(key.getType(), key.getKey());
      }
    };
    futureExoCache = new FutureExoCache<SimpleCacheKey, TemplateElement, TemplateGeneratorImpl>(loader,templateCache);
  }

  @Override
  public void registerTemplateConfigurationPlugin(TemplateConfigurationPlugin configurationPlugin) {
    generatorImpl.registerTemplateConfigurationPlugin(configurationPlugin);
  }

  public TemplateElement getTemplateElement(SimpleCacheKey cacheKey) {
    return futureExoCache.get(generatorImpl, cacheKey);
  }
  
  @Override
  public String processTemplateIntoString(String providerId, Map<String, String> valueables, String language) {
    SimpleCacheKey cacheKey = new SimpleCacheKey(providerId, language);
    TemplateElement template = getTemplateElement(cacheKey);
    boolean isAddCache = (template.getTemplateText() == null || template.getTemplateText().length() == 0);
    String content = generatorImpl.processTemplateIntoString(template.putAllValueables(valueables));
    //
    if (isAddCache) {
      templateCache.put(cacheKey, template);
    }
    return content;
  }

  @Override
  public String processSubjectIntoString(String providerId, Map<String, String> valueables, String language) {
    return generatorImpl.processSubjectIntoString(providerId, valueables, language);
  }

  @Override
  public String processDigestIntoString(String providerId, Map<String, String> valueables, String language, int size) {
    return generatorImpl.processDigestIntoString(providerId, valueables, language, size);
  }

}

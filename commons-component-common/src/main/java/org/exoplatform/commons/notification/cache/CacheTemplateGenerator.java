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

import org.exoplatform.commons.api.notification.TemplateContext;
import org.exoplatform.commons.api.notification.service.TemplateGenerator;
import org.exoplatform.commons.notification.impl.TemplateGeneratorImpl;
import org.exoplatform.commons.notification.template.TemplateElement;
import org.exoplatform.commons.notification.template.TemplateVisitorContext;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.future.FutureExoCache;
import org.exoplatform.services.cache.future.Loader;

public class CacheTemplateGenerator implements TemplateGenerator {
  
  protected ExoCache<SimpleCacheKey, TemplateElement> templateCache;
  
  protected FutureExoCache<SimpleCacheKey, TemplateElement, TemplateGeneratorImpl> futureExoCache;

  private TemplateGeneratorImpl generatorImpl;
  
  
  private static final String CONTAINER_LOCALE = "war:/notification/templates/TemplateContainer.gtmpl";
  
  public CacheTemplateGenerator(CacheService cacheService) {
    generatorImpl = TemplateGeneratorImpl.getInstance();
    //
    templateCache = cacheService.getCacheInstance(TemplateGenerator.class.getSimpleName());
    //
    Loader<SimpleCacheKey, TemplateElement, TemplateGeneratorImpl> loader = 
          new Loader<SimpleCacheKey, TemplateElement, TemplateGeneratorImpl>() {
      @Override
      public TemplateElement retrieve(TemplateGeneratorImpl service, SimpleCacheKey key) throws Exception {
        return service.getTemplateElement(key.getType(), key.getKey());
      }
    };
    futureExoCache = new FutureExoCache<SimpleCacheKey, TemplateElement, TemplateGeneratorImpl>(loader, templateCache);
    
  }

  public TemplateElement getTemplateElement(SimpleCacheKey cacheKey) {
    return futureExoCache.get(generatorImpl, cacheKey);
  }
  
  
  public void putTemplateToCache(SimpleCacheKey cacheKey, TemplateElement template) {
    templateCache.put(cacheKey, template);
  }
  
  @Override
  public String processTemplate(TemplateContext ctx) {
    SimpleCacheKey cacheKey = new SimpleCacheKey(ctx.getProviderId(), ctx.getLanguage());
    TemplateElement template = getTemplateElement(cacheKey);
    boolean isAddCache = isAddCache(template);
    TemplateVisitorContext context = TemplateVisitorContext.getInstance();
    context.putAll(ctx);
    String content = generatorImpl.processTemplateIntoString(context, template);
    //
    if (isAddCache) {
      putTemplateToCache(cacheKey, template);
    }
    return content;
  }

  private boolean isAddCache(TemplateElement template) {
    return (template.getTemplateText() == null || template.getTemplateText().length() == 0);
  }
  
  @Override
  public String processTemplateInContainer(TemplateContext ctx) {
    SimpleCacheKey cacheKey = new SimpleCacheKey(ctx.getProviderId(), ctx.getLanguage());
    TemplateElement template = getTemplateElement(cacheKey);

    SimpleCacheKey containerKey = new SimpleCacheKey(CONTAINER_LOCALE, ctx.getLanguage());
    TemplateElement container =  getTemplateElement(containerKey);
    boolean isAddCacheContainer = isAddCache(template); 
    
    TemplateVisitorContext context = TemplateVisitorContext.getInstance();
    context.putAll(ctx);
    context.put("childLocal", template.getResouceLocal());
    container.setResouceBundle(template.getResouceBundle());
    container.setResouceBunldMappingKey(template.getResouceBunldMappingKey());
    
    String content = generatorImpl.processTemplateIntoString(context, container);
    
    //
    if (isAddCacheContainer) {
      putTemplateToCache(containerKey, container);
    }
    return content;
  }

  @Override
  public String processSubject(TemplateContext ctx) {
    return generatorImpl.processSubject(ctx);
  }

  @Override
  public String processDigest(TemplateContext ctx) {
    return generatorImpl.processDigest(ctx);
  }

}

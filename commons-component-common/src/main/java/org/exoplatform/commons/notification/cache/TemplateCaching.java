/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.notification.cache;

import org.exoplatform.commons.api.notification.template.Element;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.future.FutureExoCache;
import org.exoplatform.services.cache.future.Loader;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Aug 18, 2013  
 */
public class TemplateCaching {

  private ExoCache<ElementCacheKey, Element> templateCache;
  private FutureExoCache<ElementCacheKey, Element, Object> futureExoCache;
  private final static String CACHING_NAME = "common.notification.caching";
  
  public TemplateCaching(CacheService cacheService) {
    templateCache = cacheService.getCacheInstance(CACHING_NAME);
    //
    Loader<ElementCacheKey, Element, Object> loader = 
          new Loader<ElementCacheKey, Element, Object>() {
      @Override
      public Element retrieve(Object service, ElementCacheKey key) throws Exception {
        return TemplateUtils.loadGroovyElement(key.getPlugId(), key.getLanguage());
      }
    };
    futureExoCache = new FutureExoCache<ElementCacheKey, Element, Object>(loader, templateCache);
  }
  
  /**
   * 
   * @param cacheKey
   * @return
   */
  public Element getTemplateElement(ElementCacheKey cacheKey) {
    return futureExoCache.get(null, cacheKey);
  }
  
  /**
   * Determines is cached or not
   * @param cacheKey
   * @return
   */
  public boolean isCached(ElementCacheKey cacheKey) {
    return templateCache.get(cacheKey) != null;
  }

  /**
   * Puts value in the caching
   * @param cacheKey
   * @param value
   */
  public void put(ElementCacheKey cacheKey, Element value) {
    templateCache.put(cacheKey, value);
  }
  
}


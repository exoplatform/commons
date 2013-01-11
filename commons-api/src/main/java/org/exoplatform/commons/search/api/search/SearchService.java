/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.commons.search.api.search;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.exoplatform.commons.search.api.search.data.SearchResult;
import org.exoplatform.commons.search.api.search.data.SearchType;

/**
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Nov 21, 2012  
 */
public class SearchService {
  private static Map<String, SearchType> registry = new HashMap<String, SearchType>();
  
  public static Map<String, SearchType> getRegistry() {
    return registry;
  }

  public static void setRegistry(Map<String, SearchType> registry) {
    SearchService.registry = registry;
  }

  public static void setRegistry(String json) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      Map<String, SearchType> reg = mapper.readValue(json, new TypeReference<Map<String, SearchType>>(){});
      SearchService.registry = reg;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public static void register(SearchType searchType) {
    registry.put(searchType.getName(), searchType);
  }

  public static void unregister(String searchTypeName) {
    registry.remove(searchTypeName);
  }

  public static boolean isRegistered(String searchTypeName) {
    return registry.containsKey(searchTypeName);
  }

  public static Map<String, Collection<SearchResult>> search(String query, Collection<String> sites, Collection<String> types, int offset, int limit, String sort, String order) {
    Map<String, Collection<SearchResult>> results = new HashMap<String, Collection<SearchResult>>();
    try {
      for(Entry<String, SearchType> entry:registry.entrySet()){
        SearchType searchType = entry.getValue();
        if(null!=types && !types.isEmpty() && !types.contains("all") && !types.contains(searchType.getName())) continue; // search requested types only
        Class<? extends Search> handler = searchType.getHandler();
        System.out.println("\n[UNIFIED SEARCH]: handler = " + handler.getSimpleName());
        results.put(searchType.getName(), handler.newInstance().search(query, sites, types, offset, limit, sort, order));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }
  
}
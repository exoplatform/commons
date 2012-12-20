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
package org.exoplatform.commons.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

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
  
  public static void registerSearchType(SearchType searchType) {
    registry.put(searchType.getName(), searchType);
  }
  
  public static boolean isRegistered(String searchTypeName) {
    return registry.containsKey(searchTypeName);
  }

  public static Collection<SearchResult> search(String query) {
    Collection<SearchResult> results = new ArrayList<SearchResult>();
    try {
      for(Entry<String, SearchType> entry:registry.entrySet()){
        SearchType searchType = entry.getValue();
        Class<? extends Search> clazz = searchType.getHandler();
        System.out.println("[UNIFIED SEARCH]: clazz = " + clazz.getSimpleName());
        results.addAll(clazz.newInstance().search(query));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }
 
  public static Map<String, Collection<SearchResult>> categorizedSearch(String query) {
    return categorize(search(query));
  }

  public static Map<String, Collection<SearchResult>> categorize(Collection<SearchResult> results){
    Map<String, Collection<SearchResult>> categoryMap = new HashMap<String, Collection<SearchResult>>();
    
    for(SearchResult result:results) {
      String resultType = result.getType(); //categorize search results by their type
      if(SearchService.isRegistered(resultType)) resultType = SearchService.getRegistry().get(resultType).getDisplayName();
      try {
        // put the entry to an existing category or a new category if it doesn't exist      
        Collection<SearchResult> categoryResultList;
        if(categoryMap.containsKey(resultType)){
          categoryResultList = categoryMap.get(resultType);
        } else {
          categoryResultList = new ArrayList<SearchResult>();
          categoryMap.put(resultType, categoryResultList);
        }
        categoryResultList.add(result);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return categoryMap;
  }
  
}
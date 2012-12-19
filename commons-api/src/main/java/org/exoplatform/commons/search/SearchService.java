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
import java.util.Iterator;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Nov 21, 2012  
 */
public abstract class SearchService {
  protected static Map<String, SearchEntryType> registry = new HashMap<String, SearchEntryType>();
  
  public static Map<String, SearchEntryType> getRegistry() {
    return registry;
  }

  public static void setRegistry(Map<String, SearchEntryType> registry) {
    SearchService.registry = registry;
  }

  public static void setRegistry(String json) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      Map<String, SearchEntryType> reg = mapper.readValue(json, new TypeReference<Map<String, SearchEntryType>>(){});
      SearchService.registry = reg;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public abstract Collection<SearchEntry> search(String query);
  
  public abstract Map<String, String> getEntryDetail(SearchEntryId entryId);
  
  // associate an entry type with a SearchEntry class (as the handler, e.g for it to be displayed on result page with its unique style)
  public static void registerEntryType(SearchEntryType searchEntryType) {
    Class<? extends SearchEntry> clazz = searchEntryType.getHandler();
    try {
      clazz.getConstructor(SearchEntry.class); //check if the class has a copy constructor of SearchEntry (to be used for casting)
      registry.put(searchEntryType.getName(), searchEntryType);
    } catch (NoSuchMethodException e) {
      System.out.format("[UNIFIED SEARCH] The class %s must define a copy constructor of SearchEntry (to be used for casting)\n", clazz.getSimpleName());
      e.printStackTrace();
    } catch (Exception e) {
      System.out.format("[UNIFIED SEARCH] Cannot register '%s' as '%s'\n", searchEntryType.getName(), clazz.getSimpleName());
      e.printStackTrace();
    }
  }
  
  public static boolean isRegistered(String entryType) {
    return registry.containsKey(entryType);
  }
  
  public static SearchEntry convert(SearchEntry entry, String type) {
    try {
      return registry.get(type).getHandler().getConstructor(SearchEntry.class).newInstance(entry);
    } catch (Exception e) {
      System.out.format("[UNIFIED SEARCH]: cannot convert search entry '%s' to %s\n", entry, type);
      e.printStackTrace();
      return entry;
    }
  }
  
  public Map<String, Collection<SearchEntry>> categorizedSearch(String query) {
    return categorize(search(query));
  }
  
  public Map<String, Collection<SearchEntry>> categorize(Collection<SearchEntry> entries){
    Map<String, Collection<SearchEntry>> categoryMap = new HashMap<String, Collection<SearchEntry>>();
    
    Iterator<SearchEntry> iter = entries.iterator();
    while(iter.hasNext()){
      SearchEntry entry = iter.next();
      String entryType = entry.getId().getType(); //categorize search entries by their type
      if(isRegistered(entryType)) entryType = registry.get(entryType).getDisplayName();
      try {
        // put the entry to an existing category or a new category if it doesn't exist      
        Collection<SearchEntry> seList;
        if(categoryMap.containsKey(entryType)){
          seList = categoryMap.get(entryType);
        } else {
          seList = new ArrayList<SearchEntry>();
          categoryMap.put(entryType, seList);
        }
        seList.add(entry);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return categoryMap;
  }

}
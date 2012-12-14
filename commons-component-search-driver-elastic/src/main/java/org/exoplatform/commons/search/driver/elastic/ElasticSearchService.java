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
 
package org.exoplatform.commons.search.driver.elastic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.exoplatform.commons.search.SearchEntry;
import org.exoplatform.commons.search.SearchEntryId;
import org.exoplatform.commons.search.SearchService;

/**
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Nov 21, 2012  
 */
public class ElasticSearchService extends SearchService {
  //private final static Log LOG = ExoLogger.getLogger(ElasticSearchService.class);
  private Client client;
  
  public ElasticSearchService(Client client){
    this.client = client;
  }  

  @Override
  public List<SearchEntry> search(String query) {
    List<SearchEntry> result = new ArrayList<SearchEntry>();
    
    SearchRequestBuilder searchRequestBuilder = client.prepareSearch();
    List<String> types = new ArrayList<String>();
    
    // Handle the case "mary type:[user, topic]"
    Matcher matcher = Pattern.compile("type:\\[(.+?)\\]").matcher(query);
    while(matcher.find()){
      for(String type:matcher.group(1).split(",")){
        types.add(type.trim());
      }
    }
    query = matcher.replaceAll("");
    
    // Handle the case "mary type:user"
    matcher = Pattern.compile("type:(\\w+)").matcher(query);
    while(matcher.find()){
       types.add(matcher.group(1).trim());
    }
    query = matcher.replaceAll("");
        
    String[] typesArr = new String[types.size()];
    types.toArray(typesArr);
    searchRequestBuilder.setTypes(typesArr);
    
    if(query.trim().isEmpty()) query = "*";
    
    SearchResponse searchResponse = searchRequestBuilder.setQuery(QueryBuilders.queryString(query)).execute().actionGet();
    SearchHit[] hits = searchResponse.getHits().hits();

    for(SearchHit hit:hits){
      String entryType = hit.getType();
      SearchEntry entry = new SearchEntry(hit.getIndex(), entryType, hit.getId(), hit.getSource());
      if(SearchService.isRegistered(entryType)){
        entry = SearchService.convert(entry, entryType);
      }
      result.add(entry);
    }

    return result;
  }

  @Override
  public Map<String, String> getEntryDetail(SearchEntryId entryId) {
    // TODO Auto-generated method stub
    return null;
  }

}
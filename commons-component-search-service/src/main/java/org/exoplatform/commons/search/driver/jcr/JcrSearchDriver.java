package org.exoplatform.commons.search.driver.jcr;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.exoplatform.commons.api.search.SearchService;
import org.exoplatform.commons.api.search.SearchServiceConnector;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.commons.search.service.SearchType;
import org.exoplatform.commons.search.service.UnifiedSearchService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class JcrSearchDriver implements SearchService {
  private final static Log LOG = ExoLogger.getLogger(JcrSearchDriver.class);
  
  @Override
  public Map<String, Collection<SearchResult>> search(String query, Collection<String> sites, Collection<String> types, int offset, int limit, String sort, String order) {
    Map<String, Collection<SearchResult>> results = new HashMap<String, Collection<SearchResult>>();
    if(null==types || types.isEmpty()) return results;
    try {
      for(Entry<String, SearchType> entry:UnifiedSearchService.getRegistry().entrySet()){
        SearchType searchType = entry.getValue();
        if(!types.contains("all") && !types.contains(searchType.getName())) continue; // search requested types only
        Class<? extends SearchServiceConnector> handler = searchType.getHandler();
        LOG.debug("\n[UNIFIED SEARCH]: handler = " + handler.getSimpleName());
        results.put(searchType.getName(), handler.newInstance().search(query, sites, offset, limit, sort, order));
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
    return results;
  }


}

package org.exoplatform.commons.search.driver.jcr;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.api.search.SearchService;
import org.exoplatform.commons.api.search.SearchServiceConnector;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.commons.search.service.UnifiedSearchService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class JcrSearchDriver extends SearchService {
  private final static Log LOG = ExoLogger.getLogger(JcrSearchDriver.class);
  
  @Override
  public Map<String, Collection<SearchResult>> search(String query, Collection<String> sites, Collection<String> types, int offset, int limit, String sort, String order) {    
    Map<String, Collection<SearchResult>> results = new HashMap<String, Collection<SearchResult>>();
    if(null==types || types.isEmpty()) return results;
    List<String> enabledTypes = UnifiedSearchService.getEnabledSearchTypes();
    try {
      for(SearchServiceConnector connector:this.getConnectors()){
        if(!enabledTypes.contains(connector.getSearchType())) continue; //ignore disabled types
        if(!types.contains("all") && !types.contains(connector.getSearchType())) continue; // search requested types only
        LOG.debug("\n[UNIFIED SEARCH]: connector = " + connector.getClass().getSimpleName());
        results.put(connector.getSearchType(), connector.search(query, sites, offset, limit, sort, order));
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
    return results;    
  }


}

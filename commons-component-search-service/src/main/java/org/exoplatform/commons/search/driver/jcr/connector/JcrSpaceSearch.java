package org.exoplatform.commons.search.driver.jcr.connector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.exoplatform.commons.api.search.SearchServiceConnector;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.commons.search.driver.jcr.JcrSearch;
import org.exoplatform.commons.search.driver.jcr.JcrSearchResult;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class JcrSpaceSearch extends SearchServiceConnector {
  private final static Log LOG = ExoLogger.getLogger(JcrSpaceSearch.class);
  
  @SuppressWarnings("serial")
  private final static Map<String, String> sortFieldsMap = new LinkedHashMap<String, String>(){{
    put("relevancy", "jcr:score()");
  }};
  
  public JcrSpaceSearch(InitParams params) {
    super(params);
  }

  public Collection<SearchResult> search(String query, Collection<String> sites, int offset, int limit, String sort, String order) {
    Collection<SearchResult> searchResults = new ArrayList<SearchResult>();
    
    Map<String, Object> parameters = new HashMap<String, Object>(); 
    parameters.put("sites", sites);
    parameters.put("offset", offset);
    parameters.put("limit", limit);
    parameters.put("sort", sortFieldsMap.get(sort));
    parameters.put("order", order);
    
    parameters.put("repository", "repository");
    parameters.put("workspace", "social");
    parameters.put("from", "soc:spacedefinition");
    
    Collection<JcrSearchResult> jcrResults = JcrSearch.search(query, parameters);
    SpaceService spaceSvc = (SpaceService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);

    for(JcrSearchResult jcrResult: jcrResults) {
      try {
        String spaceUrl = (String) jcrResult.getProperty("soc:url");        
        Space space = spaceSvc.getSpaceByUrl(spaceUrl);

        SearchResult result = new SearchResult(spaceUrl, jcrResult.getScore());
        result.setTitle(space.getDisplayName());
        result.setExcerpt(space.getDescription());
        result.setDetail(space.getDisplayName() + " - " + String.valueOf(space.getMembers().length) + " - " + space.getVisibility());
        String avatar = space.getAvatarUrl();
        if(null==avatar) avatar = "/social-resources/skin/ShareImages/SpaceImages/SpaceLogoDefault_61x61.gif";
        result.setImageUrl(avatar);

        searchResults.add(result);
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
      } 
    }

    return searchResults;
  }

}

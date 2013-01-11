package org.exoplatform.commons.search.driver.jcr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.commons.api.search.Search;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.commons.search.service.UnifiedSearch;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class JcrSpaceSearch implements Search {

  public Collection<SearchResult> search(String query, Collection<String> sites, Collection<String> types, int offset, int limit, String sort, String order) {
    Collection<SearchResult> searchResults = new ArrayList<SearchResult>();
    
    Map<String, Object> parameters = new HashMap<String, Object>(); 
    parameters.put("sites", sites);
    parameters.put("types", types);
    parameters.put("offset", offset);
    parameters.put("limit", limit);
    parameters.put("sort", sort);
    parameters.put("order", order);
    
    parameters.put("type", UnifiedSearch.SPACE);
    parameters.put("repository", "repository");
    parameters.put("workspace", "social");
    parameters.put("from", "soc:spacedefinition");
    
    Collection<JcrSearchResult> jcrResults = JcrSearchService.search(query, parameters);
    SpaceService spaceSvc = (SpaceService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);

    for(JcrSearchResult jcrResult: jcrResults) {
      try {
        String spaceUrl = (String) jcrResult.getProperty("soc:url");        
        Space space = spaceSvc.getSpaceByUrl(spaceUrl);

        SearchResult result = new SearchResult(UnifiedSearch.SPACE, spaceUrl);
        result.setTitle(space.getDisplayName());
        result.setExcerpt(space.getDescription());
        result.setDetail(space.getDisplayName() + " - " + String.valueOf(space.getMembers().length) + " - " + space.getVisibility());
        String avatar = space.getAvatarUrl();
        if(null==avatar) avatar = "/social-resources/skin/ShareImages/SpaceImages/SpaceLogoDefault_61x61.gif";
        result.setAvatar(avatar);

        searchResults.add(result);
      } catch (Exception e) {
        e.printStackTrace();
      } 
    }

    return searchResults;
  }

}

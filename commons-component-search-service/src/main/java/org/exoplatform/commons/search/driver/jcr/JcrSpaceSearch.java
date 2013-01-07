package org.exoplatform.commons.search.driver.jcr;

import java.util.ArrayList;
import java.util.Collection;

import org.exoplatform.commons.search.Search;
import org.exoplatform.commons.search.SearchResult;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class JcrSpaceSearch implements Search {

  public Collection<SearchResult> search(String query) {
    Collection<SearchResult> searchResults = new ArrayList<SearchResult>();
    Collection<JcrSearchResult> jcrResults = JcrSearchService.search("repository=repository workspace=social from=soc:spacedefinition where=CONTAINS(*,'${query}') " + query);
    SpaceService spaceSvc = (SpaceService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);

    for(JcrSearchResult jcrResult: jcrResults) {
      try {
        String spaceUrl = (String) jcrResult.getProperty("soc:url");        
        Space space = spaceSvc.getSpaceByUrl(spaceUrl);

        SearchResult result = new SearchResult("space", spaceUrl);
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

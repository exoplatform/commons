package org.exoplatform.commons.search.driver.jcr;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.commons.search.Search;
import org.exoplatform.commons.search.SearchResult;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.groovyscript.GroovyTemplate;
import org.exoplatform.social.core.space.spi.SpaceService;

public class JcrSpaceSearch implements Search {
  private static final String TEMPLATE_FILE = "/template/search-entry/space.gtmpl";

  public Collection<SearchResult> search(String query) {
    Collection<SearchResult> searchResults = new ArrayList<SearchResult>();
    try {
      Collection<JcrSearchResult> jcrResults = JcrSearchService.search("soc:spacedefinition", "CONTAINS(*, '${query}') AND NOT CONTAINS(exo:lastModifier, '${query}')".replaceAll("\\$\\{query\\}", query));
      for(JcrSearchResult jcrResult: jcrResults) {
        String spaceUrl = (String) jcrResult.getProperty("soc:url");
        
        SpaceService spaceSvc = (SpaceService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);
        org.exoplatform.social.core.space.model.Space space = spaceSvc.getSpaceByUrl(spaceUrl);

        Map<String, String> binding = new HashMap<String, String>();
        binding.put("spaceUrl", spaceUrl);
        binding.put("displayName", space.getDisplayName());
        binding.put("description", space.getDescription());
        binding.put("shortName", space.getDisplayName());
        String avatarUrl = space.getAvatarUrl();
        if(null==avatarUrl) avatarUrl = "/social-resources/skin/ShareImages/SpaceImages/SpaceLogoDefault_61x61.gif";
        binding.put("avatarUrl", avatarUrl);
        binding.put("members", String.valueOf(space.getMembers().length));
        binding.put("visibility", space.getVisibility());

        SearchResult result = new SearchResult();
        result.setType("space");
        result.setHtml(new GroovyTemplate(new InputStreamReader(JcrSpaceSearch.class.getResourceAsStream(TEMPLATE_FILE))).render(binding));
        searchResults.add(result);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } 

    return searchResults;
  }

}

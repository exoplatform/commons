package org.exoplatform.commons.search.driver.jcr;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.search.Search;
import org.exoplatform.commons.search.SearchResult;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.groovyscript.GroovyTemplate;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

public class JcrPeopleSearch implements Search {
  private static final String TEMPLATE_FILE = "/template/search-entry/people.gtmpl";

  public Collection<SearchResult> search(String query) {
    Collection<SearchResult> searchResults = new ArrayList<SearchResult>();
    try {
      Collection<JcrSearchResult> jcrResults = JcrSearchService.search("soc:profiledefinition", "CONTAINS(*, '${query}') AND NOT CONTAINS(exo:lastModifier, '${query}')".replaceAll("\\$\\{query\\}", query));
      for(JcrSearchResult jcrResult: jcrResults) {
        @SuppressWarnings("unchecked")
        String username = ((List<String>)jcrResult.getProperty("void-username")).get(0);
        
        IdentityManager identityManager = (IdentityManager)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
        Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, true);
        Profile profile = identity.getProfile();

        String fullName = profile.getFullName();
        String email = profile.getEmail();
        
        String position = profile.getPosition();
        if(null == position) position = "";
        String avatarUrl = profile.getAvatarUrl();      
        if(null == avatarUrl) avatarUrl = "/social-resources/skin/ShareImages/Avatar.gif";

        Map<String, String> binding = new HashMap<String, String>();
        binding.put("fullName", fullName);
        binding.put("position", position);
        binding.put("email", email);
        binding.put("avatarUrl", avatarUrl);
        binding.put("profileUrl", profile.getUrl());
        
        SearchResult result = new SearchResult();
        result.setType("people");
        result.setHtml(new GroovyTemplate(new InputStreamReader(JcrPeopleSearch.class.getResourceAsStream(TEMPLATE_FILE))).render(binding));
        searchResults.add(result);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } 

    return searchResults;
  }

}

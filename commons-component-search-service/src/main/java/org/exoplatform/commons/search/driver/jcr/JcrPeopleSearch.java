package org.exoplatform.commons.search.driver.jcr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.search.Search;
import org.exoplatform.commons.search.SearchResult;
import org.exoplatform.commons.search.service.UnifiedSearch;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

public class JcrPeopleSearch implements Search {
  public Collection<SearchResult> search(String query, Collection<String> sites, Collection<String> types, int offset, int limit, String sort, String order) {
    Collection<SearchResult> searchResults = new ArrayList<SearchResult>();
    
    Map<String, Object> parameters = new HashMap<String, Object>(); 
    parameters.put("sites", sites);
    parameters.put("types", types);
    parameters.put("offset", offset);
    parameters.put("limit", limit);
    parameters.put("sort", sort);
    parameters.put("order", order);
    
    parameters.put("type", UnifiedSearch.PEOPLE);
    parameters.put("repository", "repository");
    parameters.put("workspace", "social");
    parameters.put("from", "soc:profiledefinition");
    
    Collection<JcrSearchResult> jcrResults = JcrSearchService.search(query, parameters);
    IdentityManager identityManager = (IdentityManager)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);

    for(JcrSearchResult jcrResult: jcrResults) {
      try {
        @SuppressWarnings("unchecked")
        String username = ((List<String>)jcrResult.getProperty("void-username")).get(0);
        Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, true);
        Profile profile = identity.getProfile();

        SearchResult result = new SearchResult(UnifiedSearch.PEOPLE, profile.getUrl());
        result.setTitle(profile.getFullName());
        String position = profile.getPosition();
        if(null == position) position = "";
        result.setExcerpt(position);
        result.setDetail(profile.getEmail());
        String avatar = profile.getAvatarUrl();      
        if(null == avatar) avatar = "/social-resources/skin/ShareImages/Avatar.gif";
        result.setAvatar(avatar);

        searchResults.add(result);
      } catch (Exception e) {
        e.printStackTrace();
      } 
    }

    return searchResults;
  }

}

package org.exoplatform.commons.search.driver.jcr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exoplatform.commons.search.Search;
import org.exoplatform.commons.search.SearchResult;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

public class JcrPeopleSearch implements Search {

  public Collection<SearchResult> search(String query) {
    Collection<SearchResult> searchResults = new ArrayList<SearchResult>();
    try {
      int offset = 0;
      int limit = 0;
      Collection<JcrSearchResult> jcrResults = JcrSearchService.search(JcrSearchService.buildSql("soc:profiledefinition", "CONTAINS(*, '" + query + "')", "", query), offset, limit);
      IdentityManager identityManager = (IdentityManager)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
      
      for(JcrSearchResult jcrResult: jcrResults) {
        @SuppressWarnings("unchecked")
        String username = ((List<String>)jcrResult.getProperty("void-username")).get(0);
        Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, true);
        Profile profile = identity.getProfile();

        SearchResult result = new SearchResult("people", profile.getUrl());
        result.setTitle(profile.getFullName());
        String position = profile.getPosition();
        if(null == position) position = "";
        result.setExcerpt(position);
        result.setDetail(profile.getEmail());
        String avatar = profile.getAvatarUrl();      
        if(null == avatar) avatar = "/social-resources/skin/ShareImages/Avatar.gif";
        result.setAvatar(avatar);
        
        searchResults.add(result);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } 

    return searchResults;
  }

}

package org.exoplatform.commons.search.driver.jcr.connector;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.commons.api.search.SearchServiceConnector;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.commons.search.driver.jcr.JcrSearch;
import org.exoplatform.commons.search.driver.jcr.JcrSearchResult;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

public class JcrPeopleSearch extends SearchServiceConnector {
  private final static Log LOG = ExoLogger.getLogger(JcrPeopleSearch.class);

  @SuppressWarnings("serial")
  private final static Map<String, String> sortFieldsMap = new LinkedHashMap<String, String>(){{
    put("relevancy", "jcr:score()");
    put("date", "exo:dateCreated");
    put("title", "void-fullName");
  }};
  
  public JcrPeopleSearch(InitParams params) {
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
    parameters.put("from", "soc:profiledefinition");
    parameters.put("likeFields", Arrays.asList("void-username", "void-fullName"));
    
    Collection<JcrSearchResult> jcrResults = JcrSearch.search(query, parameters);
    IdentityManager identityManager = (IdentityManager)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);

    for(JcrSearchResult jcrResult: jcrResults) {
      try {
        @SuppressWarnings("unchecked")
        String username = ((List<String>)jcrResult.getProperty("void-username")).get(0);
        Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, true);
        Profile profile = identity.getProfile();

        SearchResult result = new SearchResult(profile.getUrl(), jcrResult.getScore());
        result.setTitle(profile.getFullName());
        String position = profile.getPosition();
        if(null == position) position = "";
        result.setExcerpt(position);
        result.setDetail(profile.getEmail());
        String avatar = profile.getAvatarUrl();      
        if(null == avatar) avatar = "/social-resources/skin/ShareImages/Avatar.gif";
        result.setImageUrl(avatar);
        String sDate =  (String) jcrResult.getProperty("exo:dateCreated");
        if(null!=sDate) result.setDate(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US).parse(sDate).getTime());
        
        searchResults.add(result);
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
      } 
    }

    return searchResults;
  }

}

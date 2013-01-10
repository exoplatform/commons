package org.exoplatform.commons.search.driver.jcr;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.commons.search.Search;
import org.exoplatform.commons.search.SearchResult;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;

/**
 * Created by The eXo Platform SAS
 * Author : Canh Pham Van
 *          canhpv@exoplatform.com
 * Jan 2, 2013 
 */
public class JcrActivitySearch implements Search {
  public Collection<SearchResult> search(String query, Collection<String> sites, Collection<String> types, int offset, int limit, String sort, String order) {
    Collection<SearchResult> searchResults = new ArrayList<SearchResult>();
    
    Map<String, Object> parameters = new HashMap<String, Object>(); 
    parameters.put("sites", sites);
    parameters.put("types", types);
    parameters.put("offset", offset);
    parameters.put("limit", limit);
    parameters.put("sort", sort);
    parameters.put("order", order);
    
    parameters.put("type", "activity");
    parameters.put("repository", "repository");
    parameters.put("workspace", "social");
    parameters.put("from", "soc:activity");
    
    Collection<JcrSearchResult> jcrResults = JcrSearchService.search(query, parameters);
    ActivityManager activityManager = (ActivityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ActivityManager.class);
    for (JcrSearchResult jcrResult: jcrResults){       
      try {
        String activityId = (String) jcrResult.getProperty("jcr:uuid");        
        ExoSocialActivity activity = activityManager.getActivity(activityId);                       

        SearchResult result = new SearchResult("activity",activity.getStreamUrl());
        result.setTitle(activity.getTitle());
        result.setExcerpt(jcrResult.getExcerpt());
        StringBuffer buf = new StringBuffer();
        buf.append(activity.getStreamOwner());
        buf.append(" - ");
        SimpleDateFormat sdf = new SimpleDateFormat("EEEEE, MMMMMMMM d, yyyy K:mm a");        
        buf.append(sdf.format(activity.getUpdated()));
        result.setDetail(buf.toString());
        String    avatar = "/social/gadgets/Activities/style/images/ActivityIcon.gif";
        result.setAvatar(avatar);
        searchResults.add(result);
      } catch (Exception e) {
        e.printStackTrace();
      } 
    }      

    return searchResults;
  }

}

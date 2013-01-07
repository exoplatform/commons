package org.exoplatform.commons.search.driver.jcr;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

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
  public Collection<SearchResult> search(String query) {
    Collection<SearchResult> searchResults = new ArrayList<SearchResult>();
    Collection<JcrSearchResult> jcrResults = JcrSearchService.search("repository=repository workspace=social from=soc:activity " + query);

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

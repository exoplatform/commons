package org.exoplatform.commons.search.driver.jcr;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.commons.search.Search;
import org.exoplatform.commons.search.SearchResult;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWiki;
import org.exoplatform.wiki.mow.core.api.wiki.UserWiki;
import org.exoplatform.wiki.service.WikiService;

/**
 * Created by The eXo Platform SAS
 * Author : Canh Pham Van
 *          canhpv@exoplatform.com
 * Jan 2, 2013 
 */
public class JcrActivitySearch implements Search {
  public Collection<SearchResult> search(String query) {
    Collection<SearchResult> searchResults = new ArrayList<SearchResult>();
    try {
      int offset = 0;
      int limit = 0;      
      Collection<JcrSearchResult> jcrResults = JcrSearchService.search(JcrSearchService.buildSql("soc:activity", "CONTAINS(*, '" + query + "')", "", query), offset, limit);
      
      ActivityManager activityManager = (ActivityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ActivityManager.class);
      for (JcrSearchResult jcrResult: jcrResults){       
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
      }      
    } catch (Exception e) {
      e.printStackTrace();
    } 

    return searchResults;
  }

}

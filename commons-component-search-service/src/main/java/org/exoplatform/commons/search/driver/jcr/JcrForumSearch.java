package org.exoplatform.commons.search.driver.jcr;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

import org.exoplatform.commons.search.Search;
import org.exoplatform.commons.search.SearchResult;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.Utils;

/**
 * Created by The eXo Platform SAS
 * Author : Canh Pham Van
 *          canhpv@exoplatform.com
 * Dec 24, 2012  
 */
public class JcrForumSearch implements Search {
  public Collection<SearchResult> search(String query) {
    Collection<SearchResult> searchResults = new ArrayList<SearchResult>();
    Collection<JcrSearchResult> jcrResults = JcrSearchService.search("repository=repository workspace=knowledge from=exo:topic where=CONTAINS(*,'${query}') " + query);      

    ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
    for (JcrSearchResult jcrResult: jcrResults){
      try {
        String path = jcrResult.getPath();
        Topic topic = forumService.getTopicByPath(path, false);

        String category ="";
        String forumId = "";
        if (!Utils.isEmpty(path) && path.lastIndexOf(Utils.CATEGORY) != -1) {
          category = path.substring(path.lastIndexOf(Utils.CATEGORY));
          if (category.indexOf("/") != -1) {
            category = category.substring(0, category.indexOf("/"));
          }
        }        
        if (!Utils.isEmpty(path) && path.lastIndexOf(Utils.FORUM) != -1) {
          forumId = path.substring(path.lastIndexOf(Utils.FORUM));
          if (forumId.indexOf("/") != -1) {
            forumId = forumId.substring(0, forumId.indexOf("/"));
          }          
        }

        Forum forum = (Forum)forumService.getForum(category, forumId);        

        ///Forum forum = (Forum)forumService.getObjectNameByPath(path.substring(path.indexOf(Utils.CATEGORY), path.lastIndexOf("/")));
        SearchResult result = new SearchResult("forum",topic.getLink());
        result.setTitle(topic.getTopicName());
        result.setExcerpt(topic.getDescription());
        StringBuffer buf = new StringBuffer();
        buf.append(forum.getForumName());
        buf.append(" - ");
        buf.append(topic.getPostCount() + " replies");
        buf.append(" - ");
        buf.append(topic.getVoteRating().doubleValue());
        buf.append(" - ");
        SimpleDateFormat sdf = new SimpleDateFormat("EEEEE, MMMMMMMM d, yyyy K:mm a");
        buf.append(topic.getLastPostDate()!=null?sdf.format(topic.getLastPostDate()):"");        

        result.setDetail(buf.toString());        
        String    avatar = "/forum/skin/DefaultSkin/webui/skinIcons/24x24/icons/HotThreadNewPost.gif";
        result.setAvatar(avatar);
        searchResults.add(result);
      } catch (Exception e) {
        e.printStackTrace();
      } 
    }      

    return searchResults;
  }
}

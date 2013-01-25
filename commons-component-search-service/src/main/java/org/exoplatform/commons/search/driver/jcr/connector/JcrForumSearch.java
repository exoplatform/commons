package org.exoplatform.commons.search.driver.jcr.connector;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.exoplatform.commons.api.search.SearchServiceConnector;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.commons.search.driver.jcr.JcrSearch;
import org.exoplatform.commons.search.driver.jcr.JcrSearchResult;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Canh Pham Van
 *          canhpv@exoplatform.com
 * Dec 24, 2012  
 */
public class JcrForumSearch extends SearchServiceConnector  {
  private final static Log LOG = ExoLogger.getLogger(JcrForumSearch.class);
  
  @SuppressWarnings("serial")
  private final static Map<String, String> sortFieldsMap = new LinkedHashMap<String, String>(){{
    put("relevancy", "jcr:score()");
    put("date", "exo:dateCreated");
    put("title", "exo:name");
  }};
  
  public JcrForumSearch(InitParams params) {
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
    parameters.put("workspace", "knowledge");
    parameters.put("from", "exo:topic");
    
    Collection<JcrSearchResult> jcrResults = JcrSearch.search(query, parameters);
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
        SearchResult result = new SearchResult(topic.getLink(), jcrResult.getScore());
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
        result.setImageUrl(avatar);
        result.setDate(topic.getLastPostDate().getTime());
        searchResults.add(result);
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
      } 
    }      

    return searchResults;
  }
  
}

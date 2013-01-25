package org.exoplatform.commons.search.driver.jcr.connector;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.exoplatform.commons.api.search.SearchServiceConnector;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.commons.search.driver.jcr.JcrSearch;
import org.exoplatform.commons.search.driver.jcr.JcrSearchResult;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
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
 * Dec 24, 2012  
 */
public class JcrWikiSearch extends SearchServiceConnector {
  private final static Log LOG = ExoLogger.getLogger(JcrWikiSearch.class);
  
  @SuppressWarnings("serial")
  private final static Map<String, String> sortFieldsMap = new LinkedHashMap<String, String>(){{
    put("relevancy", "jcr:score()");
    put("date", "updatedDate");
    put("title", "title");
  }};
  
  public JcrWikiSearch(InitParams params) {
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
    parameters.put("workspace", "collaboration");
    parameters.put("from", "wiki:page");
    
    Collection<JcrSearchResult> jcrResults = JcrSearch.search(query, parameters);
    WikiService wikiService = (WikiService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(WikiService.class);
    for (JcrSearchResult jcrResult: jcrResults){
      try {
        String url = (String) jcrResult.getProperty("url");
        String uuid = (String) jcrResult.getProperty("jcr:uuid");
        //String wikiName = (String)jcrResult.getProperty("exo:name");

        Page page = wikiService.getWikiPageByUUID(uuid);
        Page parentPage = page.getParentPage();
        String wikiName =page.getTitle();
        if (parentPage != null){
          wikiName = parentPage.getTitle();
        }  
        String title = (String)jcrResult.getProperty("title");
        SearchResult result = new SearchResult(url, jcrResult.getScore());
        result.setTitle(title);
        result.setExcerpt(jcrResult.getExcerpt());
        StringBuffer buf = new StringBuffer();
        buf.append(wikiName);
        buf.append(" - ");
        SimpleDateFormat sdf = new SimpleDateFormat("EEEEE, MMMMMMMM d, yyyy K:mm a");        
        buf.append(page.getUpdatedDate()!=null?sdf.format(page.getUpdatedDate()):sdf.format(page.getCreatedDate()));
        result.setDetail(buf.toString());
        String    avatar = "/wiki/skin/DefaultSkin/webui/background/Page.gif";
        result.setImageUrl(avatar);
        result.setDate((page.getUpdatedDate()!=null ? page.getUpdatedDate() : page.getCreatedDate()).getTime());
        searchResults.add(result);
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
      } 
    }      

    return searchResults;
  }

  public String getSpaceNameByGroupId(String groupId) throws Exception {
    try {
      Class spaceServiceClass = Class.forName("org.exoplatform.social.core.space.spi.SpaceService");
      Object spaceService = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(spaceServiceClass);

      Class spaceClass = Class.forName("org.exoplatform.social.core.space.model.Space");
      Object space = spaceServiceClass.getDeclaredMethod("getSpaceByGroupId", String.class).invoke(spaceService, groupId);
      return String.valueOf(spaceClass.getDeclaredMethod("getDisplayName").invoke(space));
    } catch (ClassNotFoundException e) {
      //Model model = getModel();
      //Wiki wiki = getWiki(PortalConfig.GROUP_TYPE, groupId.substring(1), model);
      return "";
    }
  }  

  public String getWikiNameById(String wikiId, Wiki wiki) throws Exception {    
    if (wiki instanceof PortalWiki) {
      String displayName = wiki.getName();
      int slashIndex = displayName.lastIndexOf('/');
      if (slashIndex > -1) {
        displayName = displayName.substring(slashIndex + 1); 
      }
      return displayName;
    }

    if (wiki instanceof UserWiki) {
      String currentUser = org.exoplatform.wiki.utils.Utils.getCurrentUser();
      if (wiki.getOwner().equals(currentUser)) {
        WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
        ResourceBundle res = context.getApplicationResourceBundle();
        String mySpaceLabel = res.getString("UIWikiSpaceSwitcher.title.my-space");
        return mySpaceLabel;
      }
      return wiki.getOwner();
    }

    //WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class);
    return getSpaceNameByGroupId(wiki.getOwner());
  }

}

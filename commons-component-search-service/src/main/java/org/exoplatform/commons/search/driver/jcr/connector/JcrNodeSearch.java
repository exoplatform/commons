package org.exoplatform.commons.search.driver.jcr.connector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.exoplatform.commons.api.search.SearchServiceConnector;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.commons.search.driver.jcr.JcrSearch;
import org.exoplatform.commons.search.driver.jcr.JcrSearchResult;
import org.exoplatform.commons.search.service.UnifiedSearchService;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class JcrNodeSearch extends SearchServiceConnector {
  private static final String SEARCH_TYPE_NAME = "jcrNode";
  private final static Log LOG = ExoLogger.getLogger(JcrNodeSearch.class);
  
  @Override
  public Collection<SearchResult> search(String query, Collection<String> sites, Collection<String> types, int offset, int limit, String sort, String order) {
    Map<String, Object> parameters = new HashMap<String, Object>(); 
    parameters.put("sites", sites);
    parameters.put("types", types);
    parameters.put("offset", offset);
    parameters.put("limit", limit);
    parameters.put("sort", sort);
    parameters.put("order", order);
    
    if(query.startsWith("SELECT")) return sqlExec(query); // sql mode (for testing)
    Collection<SearchResult> results = new ArrayList<SearchResult>();
    parameters.put("type", SEARCH_TYPE_NAME);
    parameters.put("repository", "repository");
    parameters.put("workspace", "collaboration");
    parameters.put("from", "nt:base");
    
    Collection<JcrSearchResult> jcrResults = JcrSearch.search(query, parameters);
    String sortBy = null==parameters.get("sort") ? "jcr:score()" : (String)parameters.get("sort");
    for(JcrSearchResult jcrResult: jcrResults) {
      try {
        String nodeUrl = jcrResult.getRepository() + "/" + jcrResult.getWorkspace() + jcrResult.getPath();
        SearchResult result = new SearchResult(SEARCH_TYPE_NAME, "/rest/jcr/" + nodeUrl);
        String score = String.valueOf(jcrResult.getScore());
        result.setTitle(nodeUrl + " (score = " + score + ")");
        result.setExcerpt(jcrResult.getExcerpt());
        String sortByValue = sortBy.equals("jcr:score()") ? score : (String)jcrResult.getProperty(sortBy);
        result.setDetail(sortBy + " = " + sortByValue);
        String avatar = (String) UnifiedSearchService.getRegistry().get(SEARCH_TYPE_NAME).getProperties().get("avatar");
        if(null!=avatar) result.setImageUrl(avatar.replaceAll("__SLASH__", "/"));
        
        results.add(result);
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
      }
    }
    return results;
  }


  private static Collection<SearchResult> sqlExec(String sql) {
    LOG.debug(String.format("[UNIFIED SEARCH] JcrNodeSearch.sqlExec()\nsql = %s\n", sql));
    Collection<SearchResult> results = new ArrayList<SearchResult>();
    String sortBy = "jcr:score()";
    Matcher matcher = Pattern.compile("ORDER BY\\s+([\\S]+)").matcher(sql);
    if(matcher.find()) {
      sortBy = matcher.group(1);
    }

    try {
      RepositoryService repositoryService = (RepositoryService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
      for(RepositoryEntry repositoryEntry:repositoryService.getConfig().getRepositoryConfigurations()){
        String repoName = repositoryEntry.getName();
        LOG.debug(String.format("[UNIFIED SEARCH]: searching repository '%s'...\n", repoName));

        ManageableRepository repository = repositoryService.getRepository(repoName);
        List<SearchResult> result = new ArrayList<SearchResult>();    

        for(String workspaceName:repository.getWorkspaceNames()){
          LOG.debug(String.format("[UNIFIED SEARCH]: searching workspace '%s'...\n", workspaceName));

          Session session = repository.login(workspaceName);
          QueryManager queryManager = session.getWorkspace().getQueryManager();
          Query jcrQuery = queryManager.createQuery(sql, Query.SQL);
          QueryResult queryResult = jcrQuery.execute();

          RowIterator rit = queryResult.getRows();
          while(rit.hasNext()){
            Row row = rit.nextRow();
            String path = row.getValue("jcr:path").getString();

            String collection = repository.getConfiguration().getName() + "/" + session.getWorkspace().getName();
            String jcrType = row.getValue("jcr:primaryType").getString();
            if(jcrType.equals("nt:resource")){
              path = path.substring(0, path.lastIndexOf("/jcr:content"));
            }

            String score = String.valueOf(row.getValue("jcr:score").getLong());
            SearchResult resultItem = new SearchResult(jcrType, "/rest/jcr/" + collection + path);
            resultItem.setTitle(collection + path + " (score = " + score + ")");
            Value excerpt = row.getValue("rep:excerpt()");
            resultItem.setExcerpt(null!=excerpt?excerpt.getString():"");
            String sortByValue = sortBy.equals("jcr:score()") ? score : "&lt;Click the icon to see all properties of this node&gt;";
            resultItem.setDetail(sortBy + " = " + sortByValue);
            String avatar = (String) UnifiedSearchService.getRegistry().get(SEARCH_TYPE_NAME).getProperties().get("avatar");
            if(null!=avatar) resultItem.setImageUrl(avatar.replaceAll("__SLASH__", "/"));

            result.add(resultItem);
          }
        }

        results.addAll(result);
      }      
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
    return results;
  }

}

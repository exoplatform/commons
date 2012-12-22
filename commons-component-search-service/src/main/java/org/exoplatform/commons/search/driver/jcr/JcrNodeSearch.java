package org.exoplatform.commons.search.driver.jcr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.exoplatform.commons.search.Search;
import org.exoplatform.commons.search.SearchResult;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;

public class JcrNodeSearch implements Search {
      
  @Override
  public Collection<SearchResult> search(String query) {
    Collection<SearchResult> results = new ArrayList<SearchResult>();
    try {
      RepositoryService repositoryService = (RepositoryService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
      for(RepositoryEntry repositoryEntry:repositoryService.getConfig().getRepositoryConfigurations()){
        String repoName = repositoryEntry.getName();
        if(!JcrSearchService.getSearchScope().containsKey(repoName)) continue; //ignore repositories which are not in the search scope
        System.out.format("[UNIFIED SEARCH]: searching repository '%s'...\n", repoName);
        List<String> searchableWorkspaces = JcrSearchService.getSearchScope().get(repoName);
        
        ManageableRepository repository = repositoryService.getRepository(repoName);
        List<SearchResult> result = new ArrayList<SearchResult>();    
        
        for(String workspaceName:repository.getWorkspaceNames()){
          if(!searchableWorkspaces.contains(workspaceName)) continue; //ignore workspaces which are not in the search scope
          System.out.format("[UNIFIED SEARCH]: searching workspace '%s'...\n", workspaceName);
          
          Session session = repository.login(workspaceName);
          QueryManager queryManager = session.getWorkspace().getQueryManager();
          query = query.startsWith("SELECT")?query:JcrSearchService.buildSql("nt:base", "CONTAINS(*, '" + query + "')", "", query); //sql mode is for testing only
          System.out.println("[UNIFIED SEARCH] query = " + query);
          Query jcrQuery = queryManager.createQuery(query, Query.SQL);
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

            SearchResult resultItem = new SearchResult(jcrType, "/rest/jcr/" + collection + path);
            resultItem.setTitle(collection + path + " (score = " + row.getValue("jcr:score").getLong() + ")");
            Value excerpt = row.getValue("rep:excerpt()");
            resultItem.setExcerpt(null!=excerpt?excerpt.getString():"");
            resultItem.setDetail("");
            resultItem.setAvatar("/eXoWCMResources/skin/DefaultSkin/skinIcons/48x48/icons/NodeTypes/default.gif");

            result.add(resultItem);
          }
        }

        results.addAll(result);
      }      
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }
    
}

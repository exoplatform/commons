package org.exoplatform.commons.search.driver.jcr;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

import org.exoplatform.commons.search.Search;
import org.exoplatform.commons.search.SearchResult;
import org.exoplatform.commons.search.SearchService;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.groovyscript.GroovyTemplate;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;

public class JcrNodeSearch implements Search {
  private static final String TEMPLATE_FILE = "/template/search-entry/jcr-node.gtmpl";
      
  @Override
  public Collection<SearchResult> search(String query) {
    Collection<SearchResult> results = new ArrayList<SearchResult>();
    try {
      RepositoryService repositoryService = (RepositoryService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
      for(RepositoryEntry repositoryEntry:repositoryService.getConfig().getRepositoryConfigurations()){
        String repoName = repositoryEntry.getName();
        if(!JcrSearchService.getSearchScope().containsKey(repoName)) continue; //ignore repositories which are not in the search scope
        List<String> searchableWorkspaces = JcrSearchService.getSearchScope().get(repoName);
        
        ManageableRepository repository = repositoryService.getRepository(repoName);
        List<SearchResult> result = new ArrayList<SearchResult>();    
        
        for(String workspaceName:repository.getWorkspaceNames()){
          if(!searchableWorkspaces.contains(workspaceName)) continue; //ignore workspaces which are not in the search scope
          
          Session session = repository.login(workspaceName);
          QueryManager queryManager = session.getWorkspace().getQueryManager();
          query = query.startsWith("SELECT")?query:queryToSql(query);
          System.out.println("[UNIFIED SEARCH] query = " + query);
          Query jcrQuery = queryManager.createQuery(query, Query.SQL); //sql mode is for testing only
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

            SearchResult resultItem = new SearchResult();
            resultItem.setType(jcrType);
            
            Map<String, String> binding = new HashMap<String, String>();
            binding.put("url", "/rest/jcr/" + collection + path);
            binding.put("title", collection + path + " (score = " + row.getValue("jcr:score").getLong() + ")");
            Value excerpt = row.getValue("rep:excerpt()");
            binding.put("excerpt", null!=excerpt?excerpt.getString():"");
            binding.put("details", "details");

            resultItem.setHtml(new GroovyTemplate(new InputStreamReader(JcrPeopleSearch.class.getResourceAsStream(TEMPLATE_FILE))).render(binding));
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

  private static String queryToSql(String query){
    List<String> types = new ArrayList<String>();
    
    // Handle the case "mary type:[user, topic]"
    Matcher matcher = Pattern.compile("type:\\[(.+?)\\]").matcher(query);
    while(matcher.find()){
      for(String type:matcher.group(1).split(",")){
        types.add(type.trim());
      }
    }
    query = matcher.replaceAll("");
    
    // Handle the case "mary type:user"
    matcher = Pattern.compile("type:(\\w+)").matcher(query);
    while(matcher.find()){
       types.add(matcher.group(1).trim());
    }
    query = matcher.replaceAll("");
            
    query = query.trim();
    if(query.isEmpty()) query = "*";

    //TODO: define a list of fields should be ignored like exo:lastModifier
    String sql = "SELECT rep:excerpt(), jcr:primaryType FROM nt:base WHERE CONTAINS(*, '${query}') AND NOT CONTAINS(exo:lastModifier, '${query}')";

    StringBuilder sb = new StringBuilder();
    String delimiter = "";
    for(String type:types){
      Iterator<String> jcrTypes = getJcrTypes(type).iterator();
      while(jcrTypes.hasNext()) {
        sb.append(delimiter);
        sb.append("jcr:primaryType='" + jcrTypes.next() + "'");
        delimiter=" OR ";        
      }
    }
    
    //TODO: if types is not specified, limit search to all registered types only
    return sql.replace("${query}", query) + (types.isEmpty()||sb.toString().isEmpty()?"":" AND (" + sb.toString() + ")");
  }
  
  @SuppressWarnings("unchecked")
  private static Collection<String> getJcrTypes(String entryType){
    try {
      Map<String, Object> entryProps = SearchService.getRegistry().get(entryType).getProperties();
      Map<String, String> firstProp = (Map<String, String>) entryProps.entrySet().iterator().next().getValue();
      return firstProp.keySet();
    } catch (Exception e) {
      System.out.format("[UNIFIED SEARCH]: cannot get jcr types associated with '%s'\n", entryType);
      e.printStackTrace();
      return new HashSet<String>();
    }
  }
    
}

/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
 
package org.exoplatform.commons.search.driver.jcr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.exoplatform.commons.search.SearchEntry;
import org.exoplatform.commons.search.SearchEntryId;
import org.exoplatform.commons.search.SearchService;
import org.exoplatform.commons.search.SimpleEntry;
import org.exoplatform.commons.search.util.JsonMap;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * Created by The eXo Platform SAS
 * Author : Canh Pham Van
 *          canhpv@exoplatform.com
 * Nov 21, 2012  
 */
@Path("/search/jcr")
@Produces(MediaType.APPLICATION_JSON)
public class JcrSearchService extends SearchService implements ResourceContainer {
  private static Map<String, List<String>> searchScope; //e.g: {repository:[collaboration, knowledge, social], ...}
  
  private static final CacheControl cacheControl;
  static {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
  }
      
  public static Map<String, List<String>> getSearchScope() {
    return searchScope;
  }

  public static void setSearchScope(Map<String, List<String>> searchScope) {
    JcrSearchService.searchScope = searchScope;
  }

  public static void setSearchScope(String json) {
    JcrSearchService.searchScope = new JsonMap<String, List<String>>(json);
  }

  // temporary implementation for testing
  @Override
  public List<SearchEntry> search(String query) {
    List<SearchEntry> results = new ArrayList<SearchEntry>();
    Map<String, String> jcrTypes = getJcrTypes();
    try {
      RepositoryService repositoryService = (RepositoryService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
      for(RepositoryEntry repositoryEntry:repositoryService.getConfig().getRepositoryConfigurations()){
        String repoName = repositoryEntry.getName();
        if(!searchScope.containsKey(repoName)) continue; //ignore repositories which are not in the search scope
        List<String> searchableWorkspaces = searchScope.get(repoName);
        
        ManageableRepository repository = repositoryService.getRepository(repoName);
        List<SearchEntry> result = new ArrayList<SearchEntry>();    
        
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
            String type = jcrTypes.get(jcrType);
            String name = path;

            SimpleEntry entry = new SimpleEntry();
            entry.setId(new SearchEntryId(collection, null!=type?type:jcrType, name));
            
            if(jcrType.equals("nt:resource")){
              path = path.substring(0, path.lastIndexOf("/jcr:content"));
            }

            entry.setTitle(collection + path + " (score = " + row.getValue("jcr:score").getLong() + ")");
            Value excerpt = row.getValue("rep:excerpt()");
            entry.setExcerpt(null!=excerpt?excerpt.getString():"");
            entry.setUrl("/rest/jcr/" + collection + path); // webdav url
            
            if(SearchService.isRegistered(type)){
              result.add(SearchService.convert(entry, type));
            } else {
              result.add(entry);
            }
            
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
      Map<String, Object> entryProps = registry.get(entryType).getProperties();
      Map<String, String> firstProp = (Map<String, String>) entryProps.entrySet().iterator().next().getValue();
      return firstProp.keySet();
    } catch (Exception e) {
      System.out.format("[UNIFIED SEARCH]: cannot get jcr types associated with '%s'\n", entryType);
      e.printStackTrace();
      return new HashSet<String>();
    }
  }
  
  @SuppressWarnings("unchecked")
  private static Map<String, String> getJcrTypes(){
    Map<String, String> jcrTypeMap = new HashMap<String, String>();
    Iterator<String> entryTypes = registry.keySet().iterator();
    String entryType;
    while(entryTypes.hasNext()){
      entryType = entryTypes.next();
      Map<String, Object> entryProps = registry.get(entryType).getProperties();
      if(null==entryProps || entryProps.isEmpty()) continue;
      Map<String, String> firstProp = (Map<String, String>) entryProps.entrySet().iterator().next().getValue();
      Iterator<String> jcrTypes = firstProp.keySet().iterator();
      while(jcrTypes.hasNext()){
        jcrTypeMap.put(jcrTypes.next(), entryType);
      }
    }
    return jcrTypeMap;
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public Map<String, String> getEntryDetail(SearchEntryId entryId) {
    Map<String, String> details = new HashMap<String, String>();
    if(!SearchService.isRegistered(entryId.getType())) return details;
    
    try {
      Map<String, Object> jcrProps = getJcrNodeProperties(entryId.getCollection() + entryId.getName());
      String nodeType = (String) jcrProps.get("jcr:primaryType");
      
      Map<String, Object> entryProps = registry.get(entryId.getType()).getProperties();
      if(entryProps.isEmpty()) return details;
      
      Iterator<String> detailFieldNames = entryProps.keySet().iterator();
      while(detailFieldNames.hasNext()){
        String detailFieldName = detailFieldNames.next();
        String jcrType = ((Map<String, String>)entryProps.get(detailFieldName)).get(nodeType);
        Object detailFieldValue = jcrProps.get(jcrType); 
        details.put(detailFieldName, (String)(detailFieldValue instanceof List<?> ? ((List)detailFieldValue).get(0) : detailFieldValue));
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return details;
  }
    
  @GET
  @Path("/props")
  public static Response jcrNodeProperties(@QueryParam("node") String nodePath) {
    try {
      return Response.ok(getJcrNodeProperties(nodePath), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }

  public static Map<String, Object> getJcrNodeProperties(String nodePath) throws Exception {
    int firstSlash = nodePath.indexOf("/");
    int secondSlash = nodePath.indexOf("/", firstSlash+1);
    String repositoryName = nodePath.substring(0, firstSlash);
    String workspaceName = nodePath.substring(firstSlash+1, secondSlash);

    RepositoryService repositoryService = (RepositoryService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
    ManageableRepository repository = repositoryService.getRepository(repositoryName);
    Session session = repository.login(workspaceName);
    Node node = session.getRootNode().getNode(nodePath.substring(secondSlash+1));
    return getProperties(node);
  }

  private static Map<String, Object> getProperties(Node node) throws Exception{
    Map<String, Object> props = new HashMap<String, Object>();

    PropertyIterator propertyIterator = node.getProperties();
    while (propertyIterator.hasNext()) {
      Property property = (Property) propertyIterator.nextProperty();

      try {
        Value value = property.getValue(); // as single-valued
        switch(value.getType()){
        case PropertyType.BINARY:
          props.put(property.getName(), value.getStream());
          break;
        case PropertyType.BOOLEAN:
          props.put(property.getName(), value.getBoolean());
          break;
        case PropertyType.DATE:
          props.put(property.getName(), value.getDate().getTime().toString());
          break;
        case PropertyType.LONG:
          props.put(property.getName(), value.getLong());
          break;
        default:
          props.put(property.getName(), value.getString());
        }
      } catch (ValueFormatException vfe) {
        //vfe.printStackTrace();
        Value[] values = property.getValues(); // as multi-valued
        List<Object> valueList = new ArrayList<Object>();
        for(Value value:values){
          switch(value.getType()){
          case PropertyType.BINARY:
            valueList.add(value.getStream());
            break;
          case PropertyType.BOOLEAN:
            valueList.add(value.getBoolean());
            break;
          case PropertyType.DATE:
            valueList.add(value.getDate().getTime().toString());
            break;
          case PropertyType.LONG:
            valueList.add(value.getLong());
            break;
          default:
            valueList.add(value.getString());
          }
        }
        props.put(property.getName(), valueList);
      }
    }
    return props;
  }
  
}


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
import java.util.List;
import java.util.Map;

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
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.exoplatform.commons.search.SearchService;
import org.exoplatform.commons.search.util.JsonMap;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
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
public class JcrSearchService implements ResourceContainer {
  public static final String BASE_SQL = "SELECT rep:excerpt(), jcr:primaryType FROM ${from} WHERE ${where} ${option}";
  public static String[] IGNORED_TYPES;
  public static String[] IGNORED_FIELDS;
  
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
  
  public static Collection<JcrSearchResult> search(String sql, int offset, int limit) {
    Collection<JcrSearchResult> results = new ArrayList<JcrSearchResult>();
    try {
      RepositoryService repositoryService = (RepositoryService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
      for(RepositoryEntry repositoryEntry:repositoryService.getConfig().getRepositoryConfigurations()){
        String repoName = repositoryEntry.getName();
        if(!searchScope.containsKey(repoName)) continue; //ignore repositories which are not in the search scope
        System.out.format("[UNIFIED SEARCH]: searching repository '%s'...\n", repoName);
        List<String> searchableWorkspaces = searchScope.get(repoName);
        
        ManageableRepository repository = repositoryService.getRepository(repoName);
        List<JcrSearchResult> result = new ArrayList<JcrSearchResult>();    
        
        for(String workspaceName:repository.getWorkspaceNames()){
          if(!searchableWorkspaces.contains(workspaceName)) continue; //ignore workspaces which are not in the search scope
          System.out.format("[UNIFIED SEARCH]: searching workspace '%s'...\n", workspaceName);

          Session session = repository.login(workspaceName);
          QueryManager queryManager = session.getWorkspace().getQueryManager();
          
          System.out.println("[UNIFIED SEARCH] query = " + sql);
          QueryImpl jcrQuery = (QueryImpl) queryManager.createQuery(sql, Query.SQL);
          jcrQuery.setOffset(offset);
          jcrQuery.setLimit(limit);
          QueryResult queryResult = jcrQuery.execute();
          
          RowIterator rit = queryResult.getRows();
          while(rit.hasNext()){
            Row row = rit.nextRow();
            JcrSearchResult resultItem = new JcrSearchResult();

            resultItem.setRepository(repository.getConfiguration().getName());
            resultItem.setWorkspace(session.getWorkspace().getName());
            resultItem.setPath(row.getValue("jcr:path").getString());
            resultItem.setPrimaryType(row.getValue("jcr:primaryType").getString());
            Value excerpt = row.getValue("rep:excerpt()");
            resultItem.setExcerpt(null!=excerpt?excerpt.getString():"");
            resultItem.setScore(row.getValue("jcr:score").getLong());
            
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

  public static Map<String, Object> getNodeProperties(Node node) throws Exception{
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
  
  // for testing
  @GET
  @Path("/ignored-types")
  public static Response getIgnoredTypes() {
    try {
      return Response.ok(IGNORED_TYPES, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }

  @GET
  @Path("/ignored-types={ignoredTypes}")
  @Consumes(MediaType.APPLICATION_JSON)
  public static Response setIgnoredTypes(@PathParam("ignoredTypes") String ignoredTypes) {
    try {
      IGNORED_TYPES = ignoredTypes.split(",");
      return Response.ok(IGNORED_TYPES, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }

  @GET
  @Path("/ignored-fields")
  public static Response getIgnoredFields() {
    try {
      return Response.ok(IGNORED_FIELDS, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }

  @GET
  @Path("/ignored-fields={ignoredFields}")
  @Consumes(MediaType.APPLICATION_JSON)
  public static Response setIgnoredFields(@PathParam("ignoredFields") String ignoredFields) {
    try {
      IGNORED_FIELDS = ignoredFields.split(",");
      return Response.ok(IGNORED_FIELDS, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }

  @GET
  @Path("/search")
  public Response search(@QueryParam("q") String query, @QueryParam("categorized") boolean categorized) {
    try {
      if(categorized) {
        return Response.ok(SearchService.categorize(new JcrNodeSearch().search(query)), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
      } else {
        return Response.ok(new JcrNodeSearch().search(query), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
    }
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

  private static Map<String, Object> getJcrNodeProperties(String nodePath) throws Exception {
    int firstSlash = nodePath.indexOf("/");
    int secondSlash = nodePath.indexOf("/", firstSlash+1);
    String repositoryName = nodePath.substring(0, firstSlash);
    String workspaceName = nodePath.substring(firstSlash+1, secondSlash);

    RepositoryService repositoryService = (RepositoryService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
    ManageableRepository repository = repositoryService.getRepository(repositoryName);
    Session session = repository.login(workspaceName);
    Node node = session.getRootNode().getNode(nodePath.substring(secondSlash+1));
    return JcrSearchService.getNodeProperties(node);
  }

  public static String buildSql(String from, String where, String option, String query){
    where = where + (where.isEmpty()?"":" AND NOT ") + repeat("CONTAINS(%s, '"+ query + "')", IGNORED_FIELDS);
    where = where + " AND NOT " + repeat("jcr:primaryType='%s'", IGNORED_TYPES);
    return BASE_SQL.replace("${from}", from).replace("${where}", where).replace("${option}", option);
  }
  
  private static String repeat(String format, String[] strArr){
    StringBuilder sb=new StringBuilder();
    String delimiter = "";
    for(String str:strArr) {
      sb.append(delimiter).append(String.format(format, str));
      delimiter = " OR ";
    }
    
    return String.format("(%s)", sb);
  }

}


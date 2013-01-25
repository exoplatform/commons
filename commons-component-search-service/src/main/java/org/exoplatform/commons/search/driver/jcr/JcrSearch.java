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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
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
public class JcrSearch implements ResourceContainer {
  public static String[] IGNORED_TYPES = new String[] {"nt:version", "nt:frozenNode", "nt:unstructured", "nt:folder"};
  public static String[] IGNORED_FIELDS = new String[] {"exo:lastModifier"};
  
  private final static Log LOG = ExoLogger.getLogger(JcrSearch.class);
  
  private static final CacheControl cacheControl;
  static {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);    
  }
  
  @SuppressWarnings("unchecked")
  public static Collection<JcrSearchResult> search(String query, Map<String, Object> parameters) {
    String repositoryName = (String) parameters.get("repository");
    if(null==repositoryName||repositoryName.isEmpty()) repositoryName = "repository";
    
    String workspaceName = (String) parameters.get("workspace");
    if(null==workspaceName||workspaceName.isEmpty()) workspaceName = "collaboration";

    Collection<String> siteNames = (Collection<String>) parameters.get("sites");
    if(null==siteNames) siteNames = Arrays.asList("all");

    Integer offset = (Integer) parameters.get("offset");
    if(null==offset) offset = 0;
    
    Integer limit = (Integer) parameters.get("limit");
    if(null==limit) limit=0;
    
    Boolean caseSensitive = (Boolean) parameters.get("caseSensitive");
    if(null==caseSensitive) caseSensitive=false;
    if(!caseSensitive) query = query.toLowerCase();

    String from = (String) parameters.get("from");
    if(null==from) from = "nt:base";
    String where = (String) parameters.get("where");
    where = (null==where) ? "" : where+" AND ";

    List<String> terms = parse(query);
    where = where + String.format("(%s)", repeat("CONTAINS(*,'%s')", terms, " OR ")); //for full text search
    
    String likeStmt = (!caseSensitive?"LOWER(%s)":"%s") + " LIKE '%%"+repeat("%s", terms, "%%")+"%%'";
    
    if(!(query.startsWith("\"") && query.endsWith("\""))) { //not exact search
      if(parameters.containsKey("likeFields")) {
        Collection<String> likeFields = (Collection<String>) parameters.get("likeFields");
        if(null!=likeFields && !likeFields.isEmpty()) where = where + " OR " + String.format("(%s)", repeat(likeStmt, likeFields, " OR "));
      }
    }
    
    if(0!=IGNORED_FIELDS.length) where = where + " AND NOT " + String.format("(%s)", repeat(likeStmt, Arrays.asList(IGNORED_FIELDS), " OR "));
    List<String> nodeTypes = (List<String>) parameters.get("nodeTypes");
    if(null!= nodeTypes && !nodeTypes.isEmpty()) where = where + " AND " + String.format("(%s)", repeat("jcr:primaryType='%s'", nodeTypes, " OR "));
    if(0!=IGNORED_TYPES.length) where = where + " AND NOT " + String.format("(%s)", repeat("jcr:primaryType='%s'", Arrays.asList(IGNORED_TYPES), " OR "));
        
    String sort = (String) parameters.get("sort");
    if(null==sort||sort.isEmpty()) sort = "jcr:score()";    
    String order = (String) parameters.get("order");
    if(null==order||order.isEmpty()) order = "DESC";
    String option = "ORDER BY " + sort + " " + order;

    String sql = String.format("SELECT rep:excerpt(), jcr:primaryType, jcr:created FROM %s WHERE %s %s", from, where, option);   
    return search(repositoryName, workspaceName, siteNames, sql, offset, limit);
  }
  
  private static Collection<JcrSearchResult> search(String repositoryName, String workspaceName, Collection<String> siteNames, String sql, int offset, int limit) {
    LOG.debug(String.format("[UNIFIED SEARCH] JcrSearchService.search()\nrepository = %s\nworkspace = %s\nsql = %s\noffset = %s\nlimit = %s\n", repositoryName, workspaceName, sql, offset, limit));
    Collection<JcrSearchResult> results = new ArrayList<JcrSearchResult>();
    try {
      RepositoryService repositoryService = (RepositoryService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
        
      ManageableRepository repository = repositoryService.getRepository(repositoryName);
      List<JcrSearchResult> result = new ArrayList<JcrSearchResult>();    
      
      Session session = repository.login(workspaceName);
      QueryManager queryManager = session.getWorkspace().getQueryManager();
      
      QueryImpl jcrQuery = (QueryImpl) queryManager.createQuery(sql, Query.SQL);
      jcrQuery.setOffset(offset);
      jcrQuery.setLimit(limit);
      QueryResult queryResult = jcrQuery.execute();
      
      RowIterator rit = queryResult.getRows();
      while(rit.hasNext()){
        Row row = rit.nextRow();
        String path = row.getValue("jcr:path").getString();
        
        boolean isSiteContentPath = path.contains("/sites content/live/");
        boolean isUnderSearchingSites = false;        
        if(isSiteContentPath) {
          for(String site:siteNames){
            if(site.equals("all") || path.contains("/sites content/live/"+site+"/")) {
              isUnderSearchingSites = true; //the path is under one of the sites being searched for
              break;
            }
          }
        }        
        if(isSiteContentPath && !isUnderSearchingSites) continue; //ignore this result
        
        JcrSearchResult resultItem = new JcrSearchResult();

        resultItem.setRepository(repository.getConfiguration().getName());
        resultItem.setWorkspace(session.getWorkspace().getName());
        resultItem.setPath(path);
        resultItem.setPrimaryType(row.getValue("jcr:primaryType").getString());
        Value excerpt = row.getValue("rep:excerpt()");
        resultItem.setExcerpt(null!=excerpt?excerpt.getString():"");
        resultItem.setScore(row.getValue("jcr:score").getLong());
        Value date = row.getValue("jcr:created");
        resultItem.setDate(null!=date?date.getLong():0);
        
        result.add(resultItem);
      }

      results.addAll(result);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
    return results;
  }

  @GET
  @Path("/ignored-types")
  public static Response ignoredTypes() {
    try {
      return Response.ok(IGNORED_TYPES, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }

  @GET
  @Path("/ignored-types={ignoredTypes}")
  @Consumes(MediaType.APPLICATION_JSON)
  public static Response ignoredTypes(@PathParam("ignoredTypes") String ignoredTypes) {
    try {
      IGNORED_TYPES = ignoredTypes.split(",");
      return Response.ok(IGNORED_TYPES, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }

  @GET
  @Path("/ignored-fields")
  public static Response ignoredFields() {
    try {
      return Response.ok(IGNORED_FIELDS, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }

  @GET
  @Path("/ignored-fields={ignoredFields}")
  @Consumes(MediaType.APPLICATION_JSON)
  public static Response ignoredFields(@PathParam("ignoredFields") String ignoredFields) {
    try {
      IGNORED_FIELDS = ignoredFields.split(",");
      return Response.ok(IGNORED_FIELDS, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }

  @GET
  @Path("/props")
  public static Response props(@QueryParam("node") String nodePath) {
    try {
      return Response.ok(getJcrNodeProperties(nodePath), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
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
 
  private static List<String> parse(String input) {
    List<String> terms = new ArrayList<String>();
    Matcher matcher = Pattern.compile("\"([^\"]+)\"").matcher(input);
    while (matcher.find()) {
      String founds = matcher.group(1);
      terms.add(founds);
    }
    String remain = matcher.replaceAll("").replaceAll("\"", "").trim(); //remove all remaining double quotes
    if(!remain.isEmpty()) terms.addAll(Arrays.asList(remain.split("\\s+")));
    return terms;
  }

  private static String repeat(String format, Collection<String> strArr, String delimiter){
    StringBuilder sb=new StringBuilder();
    String delim = "";
    for(String str:strArr) {
      sb.append(delim).append(String.format(format, str));
      delim = delimiter;
    }
    return sb.toString();
  }
  
}


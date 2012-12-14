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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.exoplatform.commons.search.SearchEntry;
import org.exoplatform.commons.search.SearchEntryId;
import org.exoplatform.commons.search.SearchService;
import org.exoplatform.commons.search.SimpleEntry;
import org.exoplatform.commons.search.entrytype.Content;
import org.exoplatform.commons.search.entrytype.People;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;

/**
 * Created by The eXo Platform SAS
 * Author : Canh Pham Van
 *          canhpv@exoplatform.com
 * Nov 21, 2012  
 */
public class JcrSearchService extends SearchService {
  private Map<String, List<String>> searchScope; //e.g: {repository:[collaboration, knowledge, social], ...}
  private static OneToManyBidirectionalMap<Map<String, String>> typeMap;
  
  public JcrSearchService() {
    searchScope = new HashMap<String, List<String>>();
    searchScope.put("repository", Arrays.asList("collaboration","knowledge","social")); //TODO: do this in config file
    
    typeMap = new OneToManyBidirectionalMap<Map<String, String>>(); //TODO: do this in config file
    
    Map<String, Map<String, String>> PEOPLE_JCR_TYPES = new HashMap<String, Map<String, String>>(){{
      put("soc:profiledefinition", new HashMap<String, String>(){{
        put("userId", "void-username");
      }});
      
      put("exo:contact", new HashMap<String, String>(){{
        put("userId", "exo:id");
      }});
    }};

    SearchService.registerEntryType("people", People.class);
    SearchService.registerEntryType("content", Content.class);

    typeMap.put("people", PEOPLE_JCR_TYPES);
    //typeMap.put("content", null);
    
    //typeMap.put("people", Arrays.asList("exo:contact"));
    /*typeMap.put("space", Arrays.asList("soc:spacedefinition"));
    typeMap.put("activity", Arrays.asList("soc:activity"));
    typeMap.put("question", Arrays.asList("exo:faqQuestion", "exo:answer", "exo:comment"));
    typeMap.put("event", Arrays.asList("exo:calendarEvent", "exo:eventAttachment"));
    typeMap.put("discussion", Arrays.asList("exo:post", "exo:privateMessage", "exo:topic", "exo:poll", "exo:forumAttachment"));
    typeMap.put("wiki", Arrays.asList("wiki:template", "wiki:helppage", "exo:wikihome", "wiki:page", "wiki:attachment"));
    typeMap.put("page", Arrays.asList("exo:article"));
    typeMap.put("file", Arrays.asList("nt:file", "nt:resource"));
    typeMap.put("content", Arrays.asList("exo:webContent")); //for testing on PLF
    typeMap.put("people", Arrays.asList("soc:profiledefinition")); //for testing on PLF
     */  
  }
  
  // temporary implementation for testing
  @Override
  public List<SearchEntry> search(String query) {
    List<SearchEntry> results = new ArrayList<SearchEntry>();    
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
            String type = typeMap.getKey(jcrType);
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
  
  private String queryToSql(String query){
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

    String sql = "SELECT rep:excerpt(), jcr:primaryType FROM nt:base WHERE CONTAINS(*, '${query}')";
    
    StringBuilder sb = new StringBuilder();
    String delimiter = "";
    for(String type:types){
      Map<String, Map<String, String>> jcrTypes = typeMap.get(type);
      Iterator<String> iter = jcrTypes.keySet().iterator();
      while(iter.hasNext()) {
        sb.append(delimiter);
        sb.append("jcr:primaryType='" + iter.next() + "'");
        delimiter=" OR ";        
      }
    }
    
    //TODO: if types is not specified, limit search to all registered types only
    return sql.replace("${query}", query) + (types.isEmpty()?"":" AND (" + sb.toString() + ")");
  }

  public Map<String, String> getEntryDetail(SearchEntryId entryId) {
    Map<String, String> details = new HashMap<String, String>();
    
    try {
      Map<String, Object> props = getJcrNodeProperties(entryId.getCollection() + entryId.getName());
      String nodeType = (String) props.get("jcr:primaryType");
      if(!typeMap.containsKey(entryId.getType())) return details;
      Map<String, Map<String, String>> searchType = typeMap.get(entryId.getType());
      if(!searchType.containsKey(nodeType)) return details;
      Map<String, String> registeredFields = searchType.get(nodeType);
      Iterator<Entry<String, String>> iter = registeredFields.entrySet().iterator();
      
      while(iter.hasNext()){
        Entry<String, String> entry = iter.next();
        String registeredField = entry.getKey();
        String jcrField = entry.getValue();
        Object value = props.get(jcrField);
        details.put(registeredField, (String)(value instanceof List<?> ? ((List)value).get(0) : value));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return details;
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

class OneToManyBidirectionalMap<ValueType> {
  private Map<String, Map<String, ValueType>> map;
  private Map<String, String> keyMap;
  
  public OneToManyBidirectionalMap(){
    map = new HashMap<String, Map<String, ValueType>>();
    keyMap = new HashMap<String, String>();
  }
  
  public OneToManyBidirectionalMap(String json) throws Exception{
    ObjectMapper mapper = new ObjectMapper();
    map = mapper.readValue(json, new TypeReference<Map<String, Map<String, ValueType>>>(){});
    keyMap = new HashMap<String, String>();
    //update keyMap
    Iterator<Entry<String, Map<String, ValueType>>> iter = map.entrySet().iterator();
    while(iter.hasNext()){
      Entry<String, Map<String, ValueType>> entry = iter.next();
      updateKeyMap(entry.getValue(), entry.getKey());
    }
  }

  public void put(String key, Map<String, ValueType> values){
    map.put(key, values);
    updateKeyMap(values, key);
  }
  
  public Map<String, ValueType> get(String key){
    return map.get(key);
  }
  
  public boolean containsKey(String key){
    return map.containsKey(key);
  }
  
  public String getKey(String valueKey){
    return keyMap.get(valueKey);
  } 
  
  public Map<String, Map<String, ValueType>> getMap(){
    return map;
  }
  
  @Override
  public String toString() {
    ObjectMapper mapper = new ObjectMapper();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    String json = "";
    try {
      mapper.writeValue(bos, map);
      json = bos.toString();
      bos.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return json;
  }
  
  private void updateKeyMap(Map<String, ValueType> values, String key){
    Iterator<String> valueKeys = values.keySet().iterator();
    while(valueKeys.hasNext()){
      keyMap.put(valueKeys.next(), key);
    }    
  }
}

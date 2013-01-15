package org.exoplatform.commons.search.service;

import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.exoplatform.commons.api.search.SearchServiceConnector;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Define search type handled by SearchService, e.g: people search, wiki search...  
 *
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Nov 21, 2012  
 */
public class SearchType {
  private String name; //search type name
  private String displayName; //for use when rendering
  private Map<String, Object> properties; // optional miscellaneous properties used by the connector or for rendering on UI
  private Class<? extends SearchServiceConnector> handler; //the connector which provide result for this search type
  
  private final static Log LOG = ExoLogger.getLogger(SearchType.class);
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
  public Map<String, Object> getProperties() {
    return properties;
  }
  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }
  public Class<? extends SearchServiceConnector> getHandler() {
    return handler;
  }
  public void setHandler(Class<? extends SearchServiceConnector> handler) {
    this.handler = handler;
  }
  
  // need for jackson
  public SearchType() {
  }
  
  public SearchType(String name, String displayName, Map<String, Object> properties, Class<? extends SearchServiceConnector> handler) {
    this.name = name;
    this.displayName = displayName;
    this.properties = properties;
    this.handler = handler;
  }

  @SuppressWarnings("unchecked")
  public SearchType(String name, String displayName, String properties_json, String handler_className){
    this.name = name;
    this.displayName = displayName;
    try {
      ObjectMapper mapper = new ObjectMapper();
      this.properties = mapper.readValue(properties_json, new TypeReference<Map<String, Object>>(){});
      this.handler = (Class<? extends SearchServiceConnector>) Class.forName(handler_className);
    } catch (ClassNotFoundException e) {
      LOG.error(e.getMessage(), e);
      this.handler = null;
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }    
  }
  
  public SearchType(String json){
    ObjectMapper mapper = new ObjectMapper();
    try {
      SearchType entryType = mapper.readValue(json, SearchType.class);
      this.name = entryType.name;
      this.displayName = entryType.displayName;
      this.properties = entryType.properties;
      this.handler = entryType.handler;
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }
}

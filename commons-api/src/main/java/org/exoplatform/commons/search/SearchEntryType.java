package org.exoplatform.commons.search;

import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.exoplatform.commons.search.util.JsonMap;

public class SearchEntryType {
  private String name;
  private String displayName;
  private Map<String, Object> properties;
  private Class<? extends SearchEntry> handler;
  
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
  public Class<? extends SearchEntry> getHandler() {
    return handler;
  }
  public void setHandler(Class<? extends SearchEntry> handler) {
    this.handler = handler;
  }
  
  // need for jackson
  public SearchEntryType() {
  }
  
  public SearchEntryType(String name, String displayName, Map<String, Object> properties, Class<? extends SearchEntry> handler) {
    this.name = name;
    this.displayName = displayName;
    this.properties = properties;
    this.handler = handler;
  }

  @SuppressWarnings("unchecked")
  public SearchEntryType(String name, String displayName, String properties_json, String handler_className){
    this.name = name;
    this.displayName = displayName;
    this.properties = new JsonMap<String, Object>(properties_json);
    try {
      this.handler = (Class<? extends SearchEntry>) Class.forName(handler_className);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }    
  }
  
  public SearchEntryType(String json){
    ObjectMapper mapper = new ObjectMapper();
    try {
      SearchEntryType entryType = mapper.readValue(json, SearchEntryType.class);
      this.name = entryType.name;
      this.displayName = entryType.displayName;
      this.properties = entryType.properties;
      this.handler = entryType.handler;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

package org.exoplatform.commons.search.api.search.data;

import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.exoplatform.commons.search.api.search.Search;

public class SearchType {
  private String name;
  private String displayName;
  private Map<String, Object> properties;
  private Class<? extends Search> handler;
  
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
  public Class<? extends Search> getHandler() {
    return handler;
  }
  public void setHandler(Class<? extends Search> handler) {
    this.handler = handler;
  }
  
  // need for jackson
  public SearchType() {
  }
  
  public SearchType(String name, String displayName, Map<String, Object> properties, Class<? extends Search> handler) {
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
      this.handler = (Class<? extends Search>) Class.forName(handler_className);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      this.handler = null;
    } catch (Exception e) {
      e.printStackTrace();
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
      e.printStackTrace();
    }
  }
}

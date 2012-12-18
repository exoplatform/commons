package org.exoplatform.commons.search.util;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;


@SuppressWarnings("serial")
public class JsonMap<KeyType, ValueType> extends HashMap<KeyType, ValueType>{
  public JsonMap(){
    super();
  }
  
  public JsonMap(String json){
    ObjectMapper mapper = new ObjectMapper();
    try {
      Map<KeyType, ValueType> map = mapper.readValue(json, new TypeReference<Map<KeyType, ValueType>>(){});
      this.clear();
      this.putAll(map);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public JsonMap(Map<KeyType, ValueType> map){
    this.clear();
    this.putAll(map);
  }

  @Override
  public String toString() {
    ObjectMapper mapper = new ObjectMapper();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    String json = "";
    try {
      mapper.writeValue(bos, this);
      json = bos.toString();
      bos.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return json;
  }

}


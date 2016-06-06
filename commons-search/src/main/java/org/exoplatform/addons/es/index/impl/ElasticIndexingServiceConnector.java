/* 
* Copyright (C) 2003-2015 eXo Platform SAS.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see http://www.gnu.org/licenses/ .
*/
package org.exoplatform.addons.es.index.impl;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.addons.es.index.IndexingServiceConnector;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.json.simple.JSONObject;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 7/22/15
 */
public abstract class ElasticIndexingServiceConnector extends IndexingServiceConnector {

  private static final Integer REPLICAS_NUMBER_DEFAULT = 1;
  private static final String REPLICAS_NUMBER_PROPERTY_NAME = "exo.es.indexing.replica.number.default";
  private static final Integer SHARDS_NUMBER_DEFAULT = 5;
  private static final String SHARDS_PROPERTY_NAME = "exo.es.indexing.shard.number.default";

  protected String index;
  protected Integer shards = SHARDS_NUMBER_DEFAULT;
  protected Integer replicas = REPLICAS_NUMBER_DEFAULT;

  public ElasticIndexingServiceConnector(InitParams initParams) {
    PropertiesParam param = initParams.getPropertiesParam("constructor.params");
    this.index = param.getProperty("index");
    setType(param.getProperty("type"));
    //Get number of replicas in connector declaration or exo properties
    if (StringUtils.isNotBlank(param.getProperty("replica.number"))) {
      this.replicas = Integer.valueOf(param.getProperty("replica.number"));
    }
    else if (StringUtils.isNotBlank(PropertyManager.getProperty(REPLICAS_NUMBER_PROPERTY_NAME))) {
      this.replicas = Integer.valueOf(PropertyManager.getProperty(REPLICAS_NUMBER_PROPERTY_NAME));
    }
    //Get number of shards in connector declaration or exo properties
    if (StringUtils.isNotBlank(param.getProperty("shard.number"))) {
      this.replicas = Integer.valueOf(param.getProperty("shard.number"));
    }
    else if (StringUtils.isNotBlank(PropertyManager.getProperty(SHARDS_PROPERTY_NAME))) {
      this.shards = Integer.valueOf(PropertyManager.getProperty(SHARDS_PROPERTY_NAME));
    }
  }

  /**
   *
   * Default mapping rules for ES type
   * {
     "type_name" : {
       "properties" : {
         "permissions" : {"type" : "string", "index" : "not_analyzed" },
         "sites" : {"type" : "string", "index" : "not_analyzed" }
       }
     }
   }
   *
   * This method must be overridden by your specific connector if you want to define special mapping
   *
   * @return JSON containing a mapping to create new type
   *
   */
  public String getMapping() {

      JSONObject notAnalyzedField = new JSONObject();
      notAnalyzedField.put("type", "string");
      notAnalyzedField.put("index", "not_analyzed");

      JSONObject properties = new JSONObject();
      properties.put("permissions", notAnalyzedField);
      properties.put("url", notAnalyzedField);
      properties.put("sites", notAnalyzedField);

      JSONObject mappingProperties = new JSONObject();
      mappingProperties.put("properties",properties);

      JSONObject mappingJSON = new JSONObject();
      mappingJSON.put(getType(), mappingProperties);

      return mappingJSON.toJSONString();
  }

  public String getIndex() {
    return index;
  }

  public void setIndex(String index) {
    this.index = index;
  }

  public Integer getShards() {
    return shards;
  }

  public void setShards(Integer shards) {
    this.shards = shards;
  }

  public Integer getReplicas() {
    return replicas;
  }

  public void setReplicas(Integer replicas) {
    this.replicas = replicas;
  }

  @Override
  public String delete(String id) {
    return id;
  }
}


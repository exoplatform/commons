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
package org.exoplatform.commons.search.es.client;

import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.commons.search.index.impl.ElasticIndexingServiceConnector;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 9/3/15
 */
public class ElasticContentRequestBuilder {

  private static final Log LOG = ExoLogger.getExoLogger(ElasticContentRequestBuilder.class);

  /**
   *
   * Get an ES create Index request content
   *
   * @return JSON containing a create index request content
   *
   */
  public String getCreateIndexRequestContent(ElasticIndexingServiceConnector connector) {

    StringBuilder mapping = new StringBuilder()
            .append("{")
            .append("  \"settings\" : {\n")
            .append("    \"number_of_shards\" : \"").append(connector.getShards()).append("\",\n")
            .append("    \"number_of_replicas\" : \"").append(connector.getReplicas()).append("\",\n")
            .append("    \"analysis\" : {")
            .append("      \"analyzer\" : {")
            .append("        \"default\" : {")
            .append("          \"tokenizer\" : \"standard\",")
            .append("          \"filter\" : [\"standard\", \"lowercase\", \"asciifolding\"]")
            .append("        },")
            .append("        \"letter_lowercase_asciifolding\" : {")
            .append("          \"tokenizer\" : \"letter\",")
            .append("          \"filter\" : [\"standard\", \"lowercase\", \"asciifolding\"]")
            .append("        },")
            .append("        \"whitespace_lowercase_asciifolding\" : {")
            .append("          \"tokenizer\" : \"whitespace\",")
            .append("          \"filter\" : [\"lowercase\", \"asciifolding\"]")
            .append("        }")
            .append("      }")
            .append("    }\n")
            .append("  }\n")
            .append("}");

    String request =  mapping.toString();

    LOG.debug("Create index request to ES: \n {}", request);
    return request;
  }

  /**
   *
   * Get a deleteAll ES query
   *
   * @return JSON containing a delete request
   *
   */
  public String getDeleteAllDocumentsRequestContent() {

    JSONObject deleteAllRequest = new JSONObject();
    JSONObject deleteQueryRequest = new JSONObject();
    deleteQueryRequest.put("match_all", new JSONObject());
    deleteAllRequest.put("query", deleteQueryRequest);

    String request = deleteAllRequest.toJSONString();

    LOG.debug("Delete All request to ES: \n {}", request);
    return request;
  }

  /**
   *
   * Get an ES delete document content to insert in a bulk request
   * For instance:
   * { "delete" : { "_index" : "blog", "_type" : "post", "_id" : "blog_post_1" } }
   *
   * @return JSON containing a delete request
   *
   */
  public String getDeleteDocumentRequestContent(ElasticIndexingServiceConnector connector, String id) {

    JSONObject cudHeaderRequest = createCUDHeaderRequestContent(connector, id);

    String request = null;
    if (cudHeaderRequest != null) {
      JSONObject deleteRequest = new JSONObject();
      deleteRequest.put("delete", cudHeaderRequest);

      request =  deleteRequest.toJSONString()+"\n";
    }

    LOG.debug("Delete request to ES: \n {}", request);

    return request;
  }

  /**
   *
   * Get an ES create document content to insert in a bulk request
   * For instance:
   * { "create" : { "_index" : "blog", "_type" : "post", "_id" : "blog_post_1" } }
   * { "field1" : "value3" }
   *
   * @return JSON containing a create document request
   *
   */
  public String getCreateDocumentRequestContent(ElasticIndexingServiceConnector connector, String id) {

    JSONObject ElasticInformation = createCUDHeaderRequestContent(connector, id);

    Document document = connector.create(id);
    if (document==null) {
      LOG.debug("Can't find document with id '{}' using connector '{}'. Ignore it.", id, connector.getName());
      return null;
    }

    JSONObject createRequest = new JSONObject();
    createRequest.put("create", ElasticInformation);

    String request = createRequest.toJSONString() + "\n" + document.toJSON() + "\n";

    LOG.debug("Create request to ES: \n {}", request);

    return request;
  }

  /**
   *
   * Get an ES create/update document content to put into a pipeline
   * The content of the request will update the full document (and not partially)
   * 
   * For instance:
   * 
   * { "field1" : "value3" }
   *
   * @return JSON containing a create/update Document to inject to a pipeline
   *
   */
  public String getCreatePipelineDocumentRequestContent(ElasticIndexingServiceConnector connector, String id) {
    Document document = connector.update(id);

    String request = null;
    if (document != null) {
      request = document.toJSON();
    }

    LOG.debug("Create Pipeline document request to ES: \n {}", request);

    return request;
  }

  /**
   *
   * Get an ES update document content to insert in a bulk request
   * We use the create api to reindex the full document (and not partially)
   * For instance:
   * { "create" : { "_index" : "blog", "_type" : "post", "_id" : "blog_post_1" } }
   * { "field1" : "value3" }
   *
   * @return JSON containing an update document request
   *
   */
  public String getUpdateDocumentRequestContent(ElasticIndexingServiceConnector connector, String id) {

    JSONObject ElasticInformation = createCUDHeaderRequestContent(connector, id);

    Document document = connector.update(id);

    String request = null;
    if (document != null) {
      JSONObject updateRequest = new JSONObject();
      updateRequest.put("index", ElasticInformation);

      request = updateRequest.toJSONString() + "\n" + document.toJSON() + "\n";
    }

    LOG.debug("Update request to ES: \n {}", request);

    return request;
  }

  /**
   *
   * Create an ES content containing information for bulk request
   * For instance:
   * { "_index" : "blog", "_type" : "post", "_id" : "blog_post_1" }
   *
   * @return JSON containing information for bulk request
   *
   */
  private JSONObject createCUDHeaderRequestContent(ElasticIndexingServiceConnector connector, String id) {

    JSONObject CUDHeader = new JSONObject();
    CUDHeader.put("_index", connector.getIndex());
    CUDHeader.put("_type", connector.getType());
    CUDHeader.put("_id", id);

    return CUDHeader;
  }

}


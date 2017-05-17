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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS Author : Thibault Clement
 * tclement@exoplatform.com 9/1/15
 */
public class ElasticIndexingClient extends ElasticClient {
  public static final String        EMPTY_JSON                        = "{}";
  private static final Log          LOG                               = ExoLogger.getExoLogger(ElasticIndexingClient.class);
  private static final String       ES_INDEX_CLIENT_PROPERTY_NAME     = "exo.es.index.server.url";
  private static final String       ES_INDEX_CLIENT_PROPERTY_USERNAME = "exo.es.index.server.username";
  private static final String       ES_INDEX_CLIENT_PROPERTY_PASSWORD = "exo.es.index.server.password";
  private ElasticIndexingAuditTrail auditTrail;

  public ElasticIndexingClient(ElasticIndexingAuditTrail auditTrail) {
    super(auditTrail);
    if (auditTrail == null) {
      throw new IllegalArgumentException("AuditTrail is null");
    }
    this.auditTrail = auditTrail;
    // Get url client from exo global properties
    if (StringUtils.isNotBlank(PropertyManager.getProperty(ES_INDEX_CLIENT_PROPERTY_NAME))) {
      this.urlClient = PropertyManager.getProperty(ES_INDEX_CLIENT_PROPERTY_NAME);
      LOG.info("Using {} as Indexing URL", this.urlClient);
    } else {
      LOG.info("Using default as Indexing URL");
    }
  }

  /**
   * Send request to ES to create a new index
   */
  public boolean sendCreateIndexRequest(String index, String settings) {
    String indexURL = urlClient + "/" + index;
    if (sendIsIndexExistsRequest(index)) {
      LOG.info("Index {} already exists. Index creation requests will not be sent.", index);
      return false;
    } else {
      LOG.info("Index {} doesn't exist. Index creation requests will be sent.", index);

      long startTime = System.currentTimeMillis();
      ElasticResponse responseCreate = sendHttpPutRequest(indexURL, settings);
      auditTrail.audit(ElasticIndexingAuditTrail.CREATE_INDEX,
                       null,
                       index,
                       null,
                       responseCreate.getStatusCode(),
                       responseCreate.getMessage(),
                       (System.currentTimeMillis() - startTime));
      return true;
    }
  }

  /**
   * Send request to ES to create a new type
   */
  public void sendCreateTypeRequest(String index, String type, String mappings) {
    String url = urlClient + "/" + index + "/_mapping/" + type;
    if (!sendIsTypeExistsRequest(index, type)) {
      LOG.info("Mapping doesn't exist for type {}. Mapping creation requests will be sent.", type);
      long startTime = System.currentTimeMillis();
      ElasticResponse response = sendHttpPutRequest(url, mappings);
      auditTrail.audit(ElasticIndexingAuditTrail.CREATE_TYPE,
                       null,
                       index,
                       type,
                       response.getStatusCode(),
                       response.getMessage(),
                       (System.currentTimeMillis() - startTime));
    } else {
      LOG.info("Mapping already exists for type {}. Mapping creation requests will not be sent.", type);
    }
  }

  /**
   * Send request to ES to delete all documents of the given type
   */
  public void sendDeleteAllDocsOfTypeRequest(String index, String type) {
    long startTime = System.currentTimeMillis();
    String request = getDeleteAllDocumentsRequestContent();
    ElasticResponse response = sendHttpPostRequest(urlClient + "/" + index + "/" + type + "/_delete_by_query?conflicts=proceed&wait_for_completion=true",  request);
    auditTrail.audit(ElasticIndexingAuditTrail.DELETE_TYPE,
                     null,
                     index,
                     type,
                     response.getStatusCode(),
                     response.getMessage(),
                     (System.currentTimeMillis() - startTime));
  }

  /**
   * Send request to ES to perform a C-reate, U-pdate or D-elete operation on a
   * ES document
   * 
   * @param bulkRequest JSON containing C-reate, U-pdate or D-elete operation
   */
  public void sendCUDRequest(String bulkRequest) {
    long startTime = System.currentTimeMillis();
    ElasticResponse response = sendHttpPostRequest(urlClient + "/_bulk", bulkRequest);
    logBulkResponse(response.getMessage(), (System.currentTimeMillis() - startTime));
  }

  /**
   * Send request to ES to create a new Ingest pipeline for attachment
   * 
   * @param index
   * @param type
   * @param pipelineName
   * @param processorMappings
   */
  public void sendCreateAttachmentPipelineRequest(String index, String type, String pipelineName, String processorMappings) {
    String url = urlClient + "/_ingest/pipeline/" +  pipelineName;
    ElasticResponse responseExists = sendHttpGetRequest(url);
    if (responseExists.getStatusCode() == HttpStatus.SC_OK || responseExists.getStatusCode() == HttpStatus.SC_NOT_FOUND
        || responseExists.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
      if (EMPTY_JSON.equals(responseExists.getMessage())) {
        LOG.info("Pipeline doesn't exist for type {}. Mapping creation requests will be sent.", type);

        long startTime = System.currentTimeMillis();
        ElasticResponse response = sendHttpPutRequest(url, processorMappings);
        auditTrail.audit(ElasticIndexingAuditTrail.CREATE_PIPELINE,
                         null,
                         index,
                         type,
                         response.getStatusCode(),
                         response.getMessage(),
                         (System.currentTimeMillis() - startTime));
      } else {
        LOG.info("Pipeline already exists for type {}. Pipeline creation requests will not be sent.", type);
      }
    } else {
      LOG.error("Error while creating pipeline: Unsupported HttpStatusCode {}. url={}", responseExists.getStatusCode(), url);
    }
  }

  /**
   * Send request to ES to create a new Ingest pipeline for attachment
   * 
   * @param index
   * @param type
   * @param id
   * @param pipelineName
   * @param pipelineRequestOperation
   */
  public void sendCreateDocOnPipeline(String index, String type, String id, String pipelineName, String pipelineRequestOperation) {
    refreshIndex(index);
    String pipelineURL = urlClient + "/_ingest/pipeline/" +  pipelineName;
    ElasticResponse responseExists = sendHttpGetRequest(pipelineURL);
    if (responseExists.getStatusCode() == HttpStatus.SC_OK) {
      long startTime = System.currentTimeMillis();
      String url = urlClient + "/" + index + "/" + type + "/" + id + "?pipeline=" + pipelineName;
      ElasticResponse response = sendHttpPutRequest(url, pipelineRequestOperation);
      auditTrail.audit(ElasticIndexingAuditTrail.CREATE_DOC_PIPELINE,
                       null,
                       index,
                       type,
                       response.getStatusCode(),
                       response.getMessage(),
                       (System.currentTimeMillis() - startTime));
    } else {
      LOG.error("Error while creating attachment on pipeline '{}': Unsupported HttpStatusCode {}. url={}", pipelineName, responseExists.getStatusCode(), pipelineURL);
    }
  }

  /**
   * Send request to ES to create a new index alias for new ES Index and remove it from old index if exists
   */
  public void sendCreateIndexAliasRequest(String index, String oldIndex, String indexAlias) {
    if(oldIndex == null) {
      LOG.info("Index alias '{}' will be created to refer the index '{}'", indexAlias, index);
    } else {
      LOG.info("Index alias '{}' will be created to refer the index {} instead of old index '{}'", indexAlias, index, oldIndex);
    }
    long startTime = System.currentTimeMillis();
    String aliasesURL = urlClient + "/_aliases";
    ElasticResponse responseUpdateIndex = sendHttpPostRequest(aliasesURL, getCreateAliasRequestContent(index, oldIndex, indexAlias));
    if(responseUpdateIndex.getStatusCode() == HttpStatus.SC_OK) {
      auditTrail.audit(ElasticIndexingAuditTrail.CREATE_INDEX_ALIAS,
                       null,
                       index,
                       null,
                       responseUpdateIndex.getStatusCode(),
                       responseUpdateIndex.getMessage(),
                       (System.currentTimeMillis() - startTime));
    } else {
      auditTrail.audit(ElasticIndexingAuditTrail.CREATE_INDEX_ALIAS,
                       null,
                       index,
                       null,
                       responseUpdateIndex.getStatusCode(),
                       responseUpdateIndex.getMessage(),
                       (System.currentTimeMillis() - startTime));
      throw new ElasticClientException("Index alias " + indexAlias + " update from old index " + oldIndex + " to new index "
          + index + " error, http code = '" + responseUpdateIndex.getStatusCode() + "', message = '"
          + responseUpdateIndex.getMessage() + "'");
    }
  }

  /**
   * Send request to ES to get type existence information
   */
  public boolean sendIsTypeExistsRequest(String index, String type) {
    String url = urlClient + "/" + index + "/_mapping/" + type;
    ElasticResponse responseExists = sendHttpGetRequest(url);
    if (responseExists.getStatusCode() == HttpStatus.SC_OK) {
      if (EMPTY_JSON.equals(responseExists.getMessage())) {
        return false;
      } else {
        return true;
      }
    } else {
      LOG.error("Error while creating Mapping: Unsupported HttpStatusCode {}. url={}", responseExists.getStatusCode(), url);
      throw new ElasticClientException("Can't request ES to get index/type " + index + "/" + type + " existence status");
    }
  }

  /**
   * Send request to ES to get index aliases
   */
  public Set<String> sendGetIndexAliasesRequest(String index) {
    String indexAliasURL = urlClient + "/" +  index + "/_aliases/";
    ElasticResponse responseExists = sendHttpGetRequest(indexAliasURL);
    // Test if he alias already exists
    if (responseExists.getStatusCode() == HttpStatus.SC_OK) {
      // Get all aliases information
      String aliasesURL = urlClient + "/_aliases";
      ElasticResponse responseAliases = sendHttpGetRequest(indexAliasURL);
      // An ES communication can happen, so throw an exception
      if (responseAliases.getStatusCode() != HttpStatus.SC_OK) {
        throw new ElasticClientException("Can't get aliases from URL " + aliasesURL);
      }
      String jsonResponse = responseAliases.getMessage();
      // Parse aliases mappings
      JSONParser parser = new JSONParser();
      Map<?, ?> json;
      try {
        json = (Map<?, ?>)parser.parse(jsonResponse);
      } catch (ParseException e) {
        throw new ElasticClientException("Unable to parse JSON response: " + jsonResponse, e);
      }

      // if alias exists and old index doesn't exist
      // this means the alias is made on new index name
      // So nothing to change
      if(!json.containsKey(index)) {
        return Collections.emptySet();
      }
      JSONObject indexAliases = (JSONObject)(((Map<?, ?>) json.get(index)).get("aliases"));
      return indexAliases.keySet();
    } else {
      throw new ElasticClientException("Uknow response code was sent by ES: \\n\\t\\t code = " + responseExists.getStatusCode()
          + ", \\n\\t\\t message: " + responseExists.getMessage());
    }
  }

  /**
   * Send request to ES to count all documents found in index
   */
  public long sendCountIndexObjectsRequest(String index) {
    refreshIndex(index);
    String indexCountObjectsURL = urlClient + "/" +  index + "/_count?q=*";
    ElasticResponse mappingsResponse = sendHttpGetRequest(indexCountObjectsURL);
    if (mappingsResponse.getStatusCode() == HttpStatus.SC_OK) {
      String jsonResponse = mappingsResponse.getMessage();
      // Parse mappings
      JSONParser parser = new JSONParser();
      Map<?, ?> json;
      try {
        json = (Map<?, ?>)parser.parse(jsonResponse);
      } catch (ParseException e) {
        throw new ElasticClientException("Unable to parse JSON response: " + jsonResponse, e);
      }

      if(!json.containsKey("count")) {
        throw new ElasticClientException("Unexpected content in JSON response from ES: " + jsonResponse);
      }
      return (Long)json.get("count");
    } else {
      throw new ElasticClientException("Uknow response code was sent by ES: \\n\\t\\t code = " + mappingsResponse.getStatusCode()
          + ", \\n\\t\\t message: " + mappingsResponse.getMessage());
    }
  }

  /**
   * Send request to ES to get version
   */
  public String sendGetESVersion() {
    ElasticResponse mappingsResponse = sendHttpGetRequest(urlClient);
    if (mappingsResponse.getStatusCode() == HttpStatus.SC_OK) {
      String jsonResponse = mappingsResponse.getMessage();
      // Parse mappings
      JSONParser parser = new JSONParser();
      Map<?, ?> json;
      try {
        json = (Map<?, ?>)parser.parse(jsonResponse);
      } catch (ParseException e) {
        throw new ElasticClientException("Unable to parse JSON response: " + jsonResponse, e);
      }

      if(!json.containsKey("version")) {
        throw new ElasticClientException("Unexpected content in JSON response from ES: " + jsonResponse);
      }
      return (String) ((JSONObject)json.get("version")).get("number");
    } else {
      throw new ElasticClientException("Uknow response code was sent by ES: \\n\\t\\t code = " + mappingsResponse.getStatusCode()
          + ", \\n\\t\\t message: " + mappingsResponse.getMessage());
    }
  }

  /**
   * Deletes an index from ES
   * 
   * @param index index name to delete
   */
  public void sendDeleteIndexRequest(String index) {
    long startTime = System.currentTimeMillis();
    ElasticResponse response = sendHttpDeleteRequest(urlClient + "/" + index);
    auditTrail.audit(ElasticIndexingAuditTrail.DELETE_TYPE,
                     null,
                     index,
                     null,
                     response.getStatusCode(),
                     response.getMessage(),
                     (System.currentTimeMillis() - startTime));
    if(response.getStatusCode() != HttpStatus.SC_OK) {
      throw new ElasticClientException("Can't delete index " + index + ", reqponse code = " + response.getStatusCode()
          + ", message = " + response.getMessage());
    }
  }

  /**
   * This operation reindex the documents from old index/type to new index/type mapping.
   * A pipeline could be used when reindexing in case Ingest Attachment plugin is used
   * by a target type.
   * 
   * @param index target index name
   * @param oldIndex source index name
   * @param type source type name
   * @param pipeline target pipeline name (optional)
   */
  public void sendReindexTypeRequest(String index, String oldIndex, String type, String pipeline) {
    long startTime = System.currentTimeMillis();
    String request = getReindexRequestContent(index, oldIndex, type, pipeline);
    ElasticResponse response = sendHttpPostRequest(urlClient + "/_reindex", request);
    auditTrail.audit(ElasticIndexingAuditTrail.REINDEX_TYPE,
                     null,
                     index,
                     type,
                     response.getStatusCode(),
                     response.getMessage(),
                     (System.currentTimeMillis() - startTime));
    if(response.getStatusCode() != HttpStatus.SC_OK) {
      throw new ElasticClientException("Can't reindex index " + index + ", type = " + type + ", reqponse code = " + response.getStatusCode()
          + ", message = " + response.getMessage());
    }
  }

  /**
   * Send request to ES to test if index exists
   *
   * @param index ES index
   * @return true if index exists in ES
   */
  public boolean sendIsIndexExistsRequest(String index) {
    String indexURL = urlClient + "/" + index;
    ElasticResponse responseExists = sendHttpGetRequest(indexURL);
    if (responseExists.getStatusCode() == HttpStatus.SC_OK) {
      return true;
    } else if (responseExists.getStatusCode() == HttpStatus.SC_NOT_FOUND
        || responseExists.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
      return false;
    } else {
      throw new ElasticClientException("Can't get index '"+ index +"' status");
    }
  }

  private void refreshIndex(String index) {
    String indexRefreshURL = urlClient + "/" +  index + "/_refresh";
    sendHttpPostRequest(indexRefreshURL, null);
  }

  private void logBulkResponse(String response, long executionTime) {
    try {
      Object parsedResponse = JSONValue.parseWithException(response);
      if (!(parsedResponse instanceof JSONObject)) {
        LOG.error("Unable to parse Bulk response: response is not a JSON. response={}", response);
        throw new ElasticClientException("Unable to parse Bulk response: response is not a JSON.");
      }
      // process items
      Object items = ((JSONObject) parsedResponse).get("items");
      if (items != null) {
        if (!(items instanceof JSONArray)) {
          LOG.error("Unable to parse Bulk response: items is not a JSONArray. items={}", items);
          throw new ElasticClientException("Unable to parse Bulk response: items is not a JSONArray.");
        }
        // Looping over all the items is required because
        // in case of error, ES send a response with Status 200 and a flag
        // errors:true
        // in the JSON message
        for (Object item : ((JSONArray) items).toArray()) {
          if (!(item instanceof JSONObject)) {
            LOG.error("Unable to parse Bulk response: item is not a JSONObject. item={}", item);
            throw new ElasticClientException("Unable to parse Bulk response: item is not a JSONObject.");
          }
          logBulkResponseItem((JSONObject) item, executionTime);
        }
      }
    } catch (ParseException e) {
      throw new ElasticClientException("Unable to parse Bulk response", e);
    }
  }

  private void logBulkResponseItem(JSONObject item, long executionTime) {
    for (Map.Entry operation : (Set<Map.Entry>) item.entrySet()) {
      String operationName = operation.getKey() == null ? null : (String) operation.getKey();
      if (operation.getValue() != null) {
        JSONObject operationDetails = (JSONObject) operation.getValue();
        String index = operationDetails.get("_index") == null ? null : (String) operationDetails.get("_index");
        String type = operationDetails.get("_type") == null ? null : (String) operationDetails.get("_type");
        String id = operationDetails.get("_id") == null ? null : (String) operationDetails.get("_id");
        Long status = operationDetails.get("status") == null ? null : (Long) operationDetails.get("status");
        String error = operationDetails.get("error") == null ? null : (String) ((JSONObject) operationDetails.get("error")).get("reason");
        Integer httpStatusCode = status == null ? null : status.intValue();
        if (ElasticIndexingAuditTrail.isError(httpStatusCode)) {
          auditTrail.logRejectedDocumentBulkOperation(operationName, id, index, type, httpStatusCode, error, executionTime);
        } else {
          if (auditTrail.isFullLogEnabled()) {
            auditTrail.logAcceptedBulkOperation(operationName, id, index, type, httpStatusCode, error, executionTime);
          }
        }
      }
    }
  }

  private String getDeleteAllDocumentsRequestContent() {

    JSONObject deleteAllRequest = new JSONObject();
    JSONObject deleteQueryRequest = new JSONObject();
    deleteQueryRequest.put("match_all", new JSONObject());
    deleteAllRequest.put("query", deleteQueryRequest);

    String request = deleteAllRequest.toJSONString();

    LOG.debug("Delete All request to ES: \n {}", request);
    return request;
  }

  private String getReindexRequestContent(String index, String oldIndex, String type, String pipeline) {
    JSONObject reindexRequest = new JSONObject();

    JSONObject reindexSourceRequest = new JSONObject();
    reindexRequest.put("source", reindexSourceRequest);
    reindexSourceRequest.put("index", oldIndex);
    reindexSourceRequest.put("type", type);

    JSONObject reindexDestRequest = new JSONObject();
    reindexRequest.put("dest", reindexDestRequest);
    reindexDestRequest.put("index", index);
    if(pipeline != null) {
      reindexDestRequest.put("pipeline", pipeline);
    }

    String request = reindexRequest.toJSONString();

    LOG.debug("Reindex Request from old index {} type {} to new index : \n {}", oldIndex, type, index, request);
    return request;
  }

  private String getCreateAliasRequestContent(String index, String oldIndex, String alias) {
    JSONObject updateAliasRequest = new JSONObject();
    JSONArray updateAliasActionsRequest = new JSONArray();
    updateAliasRequest.put("actions", updateAliasActionsRequest);
    if(oldIndex != null) {
      JSONObject updateAliasActionRemoveRequest = new JSONObject();
      JSONObject updateAliasActionRemoveOptionsRequest = new JSONObject();
      updateAliasActionRemoveRequest.put("remove", updateAliasActionRemoveOptionsRequest);
      updateAliasActionRemoveOptionsRequest.put("alias", alias);
      updateAliasActionRemoveOptionsRequest.put("index", oldIndex);
      updateAliasActionsRequest.add(updateAliasActionRemoveRequest);
    }
    JSONObject updateAliasActionAddRequest = new JSONObject();
    JSONObject updateAliasActionAddOptionsRequest = new JSONObject();
    updateAliasActionAddRequest.put("add", updateAliasActionAddOptionsRequest);
    updateAliasActionAddOptionsRequest.put("alias", alias);
    updateAliasActionAddOptionsRequest.put("index", index);
    updateAliasActionsRequest.add(updateAliasActionAddRequest);

    String request = updateAliasRequest.toJSONString();

    LOG.debug("Create Index alias ES: \n {}", request);
    return request;
  }

  @Override
  protected String getEsUsernameProperty() {
    return PropertyManager.getProperty(ES_INDEX_CLIENT_PROPERTY_USERNAME);
  }

  @Override
  protected String getEsPasswordProperty() {
    return PropertyManager.getProperty(ES_INDEX_CLIENT_PROPERTY_PASSWORD);
  }

  @Override
  protected HttpClientConnectionManager getClientConnectionManager() {
    return new PoolingHttpClientConnectionManager();
  }

}

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
package org.exoplatform.addons.es.client;

import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

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
  public void sendCreateIndexRequest(String index, String settings) {
    String url = urlClient + "/" + index;
    ElasticResponse responseExists = sendHttpGetRequest(url);
    if (responseExists.getStatusCode() == HttpStatus.SC_OK) {
      LOG.info("Index {} already exists. Index creation requests will not be sent.", index);
    } else if (responseExists.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
      LOG.info("Index {} doesn't exist. Index creation requests will be sent.", index);

      long startTime = System.currentTimeMillis();
      ElasticResponse responseCreate = sendHttpPostRequest(url, settings);
      auditTrail.audit(ElasticIndexingAuditTrail.CREATE_INDEX,
                       null,
                       index,
                       null,
                       responseCreate.getStatusCode(),
                       responseCreate.getMessage(),
                       (System.currentTimeMillis() - startTime));
    } else {
      LOG.error("Index exists: Unsupported HttpStatusCode {}. url={}", responseExists.getStatusCode(), url);
    }
  }

  /**
   * Send request to ES to create a new type
   */
  public void sendCreateTypeRequest(String index, String type, String mappings) {
    String url = urlClient + "/" + index + "/_mapping/" + type;
    ElasticResponse responseExists = sendHttpGetRequest(url);
    if (responseExists.getStatusCode() == HttpStatus.SC_OK) {
      if (EMPTY_JSON.equals(responseExists.getMessage())) {
        LOG.info("Mapping doesn't exist for type {}. Mapping creation requests will be sent.", type);

        long startTime = System.currentTimeMillis();
        ElasticResponse response = sendHttpPostRequest(url, mappings);
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
    } else {
      LOG.error("Mapping exists: Unsupported HttpStatusCode {}. url={}", responseExists.getStatusCode(), url);
    }
  }

  /**
   * Send request to ES to delete all documents of the given type
   */
  public void sendDeleteAllDocsOfTypeRequest(String index, String type) {
    long startTime = System.currentTimeMillis();
    ElasticResponse response = sendHttpDeleteRequest(urlClient + "/" + index + "/" + type + "/_query?q=*");
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

  @Override
  protected String getEsUsernameProperty() {
    return PropertyManager.getProperty(ES_INDEX_CLIENT_PROPERTY_USERNAME);
  }

  @Override
  protected String getEsPasswordProperty() {
    return PropertyManager.getProperty(ES_INDEX_CLIENT_PROPERTY_PASSWORD);
  }
  
  @Override
  protected ClientConnectionManager getClientConnectionManager() {
    return new SingleClientConnManager();
  }

}

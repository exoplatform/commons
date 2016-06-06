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
package org.exoplatform.addons.es.rest;

import io.swagger.annotations.*;
import org.apache.commons.lang.StringUtils;
import org.exoplatform.addons.es.domain.IndexingOperation;
import org.exoplatform.addons.es.index.IndexingOperationProcessor;
import org.exoplatform.addons.es.index.IndexingService;
import org.exoplatform.addons.es.index.IndexingServiceConnector;
import org.exoplatform.addons.es.index.impl.QueueIndexingService;
import org.exoplatform.addons.es.rest.resource.CollectionResource;
import org.exoplatform.addons.es.rest.resource.CollectionSizeResource;
import org.exoplatform.addons.es.rest.resource.ConnectorResource;
import org.exoplatform.addons.es.rest.resource.OperationResource;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.ws.frameworks.json.impl.JsonException;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;
import org.exoplatform.ws.frameworks.json.value.JsonValue;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 10/6/15
 */
@Path(IndexingManagementRestServiceV1.BASE_VERSION_URI+ IndexingManagementRestServiceV1.INDEXING_MANAGEMENT_URI)
@RolesAllowed("administrators")
@Api(
    value = IndexingManagementRestServiceV1.BASE_VERSION_URI+ IndexingManagementRestServiceV1.INDEXING_MANAGEMENT_URI,
    description = "Entry point for Indexing Management resources",
    basePath = IndexingManagementRestServiceV1.BASE_VERSION_URI+ IndexingManagementRestServiceV1.INDEXING_MANAGEMENT_URI
)
public class IndexingManagementRestServiceV1 implements ResourceContainer {

  public final static String BASE_VERSION_URI = "/v1";
  public final static String INDEXING_MANAGEMENT_URI = "/indexingManagement";
  public final static String CONNECTORS_URI = "/connectors";
  public final static String OPERATIONS_URI = "/operations";
  public final static String ERRORS_URI = "/errors";

  private final static Log LOG = ExoLogger.getLogger(IndexingManagementRestServiceV1.class);

  private QueueIndexingService indexingService;
  private IndexingOperationProcessor indexingOperationProcessor;

  public IndexingManagementRestServiceV1(IndexingService indexingService, IndexingOperationProcessor indexingOperationProcessor) {
    this.indexingService = (QueueIndexingService) indexingService;
    this.indexingOperationProcessor = indexingOperationProcessor;
  }

  // Indexing Service Connectors

  @GET
  @Path(IndexingManagementRestServiceV1.CONNECTORS_URI)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("administrators")
  @ApiOperation(value = "Return all Indexing Connectors")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful retrieval of all Indexing Connectors"),
      @ApiResponse(code = 500, message = "Can't generate JSON file") })
  public Response getConnectors(
      @ApiParam(
          value = "The name of a JavaScript function to be used as the JSONP callback",
          required = false)
      @QueryParam("jsonp")
      String jsonp,
      @ApiParam(
          value = "Tell the service if it must return the size of the collection in the store",
          required = false)
      @QueryParam("returnSize")
      boolean returnSize
  ) {

    //Get connectors
    List<IndexingServiceConnector> connectors = new ArrayList<>(indexingOperationProcessor.getConnectors().values());

    CollectionResource<IndexingServiceConnector> connectorData;

    //Manage return size parameter
    if (returnSize) {
      int connectorNb = indexingOperationProcessor.getConnectors().size();
      connectorData = new CollectionSizeResource<>(connectors, connectorNb);
    }
    else {
      connectorData = new CollectionResource<>(connectors);
    }

    Response.ResponseBuilder response;

    //Manage json-callback parameter
    if (StringUtils.isNotBlank(jsonp)) {
      try {
        response = buildJsonCallBack(connectorData, jsonp);
      } catch (JsonException e) {
        LOG.error(e);
        response = Response.status(HTTPStatus.INTERNAL_ERROR);
      }
    }
    else {
      response = Response.ok(connectorData, MediaType.APPLICATION_JSON);
    }

    return response.build();
  }

  @GET
  @Path(IndexingManagementRestServiceV1.CONNECTORS_URI+"/{connectorType}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("administrators")
  @ApiOperation(value = "Return the Indexing Connectors with the specified Connector Type")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful retrieval of the Indexing Connector"),
      @ApiResponse(code = 404, message = "Indexing Connector with specified type Not Found"),
      @ApiResponse(code = 500, message = "Can't generate JSON file") })
  public Response getConnector(
      @ApiParam(
          value = "Type of the Indexing Connector to retrieve",
          required = true)
      @PathParam("connectorType")
      String connectorType,
      @ApiParam(
          value = "The name of a JavaScript function to be used as the JSONP callback",
          required = false)
      @QueryParam("jsonp")
      String jsonp
  ) {

    IndexingServiceConnector connector = indexingOperationProcessor.getConnectors().get(connectorType);

    if (connector == null) return Response.status(HTTPStatus.NOT_FOUND).build();

    Response.ResponseBuilder response;

    //Manage json-callback parameter
    if (StringUtils.isNotBlank(jsonp)) {
      try {
        response = buildJsonCallBack(connector, jsonp);
      } catch (JsonException e) {
        LOG.error(e);
        response = Response.status(HTTPStatus.INTERNAL_ERROR);
      }
    }
    else {
      response = Response.ok(connector, MediaType.APPLICATION_JSON);
    }

    return response.build();
  }

  @PUT
  @Path(IndexingManagementRestServiceV1.CONNECTORS_URI+"/{connectorType}")
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed("administrators")
  @ApiOperation(value = "Update an Indexing Connector to enable / disable it")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful update of the Indexing Connector"),
      @ApiResponse(code = 404, message = "Indexing Connector with specified type Not Found") })
  public Response updateConnector(
      @ApiParam(
          value = "Type of the Indexing Connector to update",
          required = true)
      @PathParam("connectorType")
      String connectorType,
      @ApiParam(
          value = "An Indexing Connector Resource",
          required = true)
      ConnectorResource connectorResource
  ) {

    if (indexingOperationProcessor.getConnectors().get(connectorType) == null) {
      return Response.status(HTTPStatus.NOT_FOUND).build();
    }

    indexingOperationProcessor.getConnectors().get(connectorType).setEnable(connectorResource.isEnable());

    return Response.ok().build();
  }

  // Indexing Operations

  @GET
  @Path(IndexingManagementRestServiceV1.OPERATIONS_URI)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("administrators")
  @ApiOperation(value = "Return all Indexing Operations")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful retrieval of all Indexing Operations"),
      @ApiResponse(code = 500, message = "Can't generate JSON file") })
  public Response getOperations(
      @ApiParam(
          value = "The name of a JavaScript function to be used as the JSONP callback",
          required = false)
      @QueryParam("jsonp")
      String jsonp,
      @ApiParam(value = "The starting point when paging through a list of entities",
          required = false)
      @QueryParam("offset")
      int offset,
      @ApiParam(value = "The maximum number of results when paging through a list of entities. " +
          "If not specified or exceed the *query_limit* configuration of Indexing Management rest service, " +
          "it will use the *query_limit*",
          required = false)
      @QueryParam("limit")
      int limit,
      @ApiParam(
          value = "Tell the service if it must return the size of the collection in the store",
          required = false)
      @QueryParam("returnSize")
      boolean returnSize
  ) {

    offset = parseOffset(offset);
    limit = parseLimit(limit);

    //Get operations
    List<IndexingOperation> operations = new ArrayList<>(indexingService.getOperations(offset, limit));

    CollectionResource<IndexingOperation> operationData;
    
    //Manage return size parameter
    if (returnSize) {
      int operationNb = indexingService.getNumberOperations().intValue();
      operationData = new CollectionSizeResource<>(operations, operationNb);
    }
    else {
      operationData = new CollectionResource<>(operations);
    }
    operationData.setLimit(limit);
    operationData.setOffset(offset);

    Response.ResponseBuilder response;

    //Manage json-callback parameter
    if (StringUtils.isNotBlank(jsonp)) {
      try {
        response = buildJsonCallBack(operationData, jsonp);
      } catch (JsonException e) {
        LOG.error(e);
        response = Response.status(HTTPStatus.INTERNAL_ERROR);
      }
    }
    else {
      response = Response.ok(operationData, MediaType.APPLICATION_JSON);
    }
    
    return response.build();
  }

  @POST
  @Path(IndexingManagementRestServiceV1.OPERATIONS_URI)
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed("administrators")
  @ApiOperation(value = "Add an Indexing Operation to the queue")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Indexing Operation successfully added"),
      @ApiResponse(code = 400, message = "The specified Indexing Operation is unknown")})
  public Response addOperation(
      @ApiParam(
          value = "An Indexing Operation Resource",
          required = true)
      OperationResource operationResource
  ) {

    switch (operationResource.getOperation()) {

      case "init": indexingService.init(operationResource.getEntityType());
        break;
      case "index": indexingService.index(operationResource.getEntityType(), operationResource.getEntityId());
        break;
      case "reindex": indexingService.reindex(operationResource.getEntityType(), operationResource.getEntityId());
        break;
      case "unindex": indexingService.unindex(operationResource.getEntityType(), operationResource.getEntityId());
        break;
      case "reindexAll": indexingService.reindexAll(operationResource.getEntityType());
        break;
      case "unindexAll": indexingService.unindexAll(operationResource.getEntityType());
        break;
      default: return getBadRequestResponse().build();

    }

    return Response.status(HTTPStatus.CREATED).build();
  }

  @DELETE
  @Path(IndexingManagementRestServiceV1.OPERATIONS_URI)
  @RolesAllowed("administrators")
  @ApiOperation(value = "Delete all Indexing Operation")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful deletion of all Indexing Operations") })
  public Response deleteOperations() {

    indexingService.deleteAllOperations();

    return Response.ok().build();

  }

  @GET
  @Path(IndexingManagementRestServiceV1.OPERATIONS_URI+"/{operationId}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("administrators")
  @ApiOperation(value = "Return the Indexing Operation with the specified Opertion Id")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful retrieval of the Indexing Operation"),
      @ApiResponse(code = 404, message = "Indexing Operation with specified Id Not Found"),
      @ApiResponse(code = 500, message = "Can't generate JSON file") })
  public Response getOperation(
      @ApiParam(
          value = "Id of the Indexing Operation to retrieve",
          required = true)
      @PathParam("operationId")
      String operationId,
      @ApiParam(
          value = "The name of a JavaScript function to be used as the JSONP callback",
          required = false)
      @QueryParam("jsonp")
      String jsonp
  ) {

    IndexingOperation operation = indexingService.getOperation(operationId);

    if (operation == null) return Response.status(HTTPStatus.NOT_FOUND).build();

    Response.ResponseBuilder response;

    //Manage json-callback parameter
    if (StringUtils.isNotBlank(jsonp)) {
      try {
        response = buildJsonCallBack(operation, jsonp);
      } catch (JsonException e) {
        LOG.error(e);
        response = Response.status(HTTPStatus.INTERNAL_ERROR);
      }
    }
    else {
      response = Response.ok(operation, MediaType.APPLICATION_JSON);
    }

    return response.build();

  }

  @DELETE
  @Path(IndexingManagementRestServiceV1.OPERATIONS_URI+"/{operationId}")
  @RolesAllowed("administrators")
  @ApiOperation(value = "Delete a specified Indexing Operation")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful deletion of the Indexing Operations"),
      @ApiResponse(code = 404, message = "Indexing Operation with specified Id Not Found") })
  public Response DeleteOperation(
      @ApiParam(
          value = "Id of the Indexing Operation to delete",
          required = true)
      @PathParam("operationId")
      String operationId
  ) {

    IndexingOperation operation = indexingService.getOperation(operationId);

    if (operation == null) return Response.status(HTTPStatus.NOT_FOUND).build();

    indexingService.deleteOperation(operation);

    return Response.ok().build();

  }

  //TODO manage Bulk Operation

  // Indexing Errors

  //TODO implement Error REST Service

  // Utils method

  private Response.ResponseBuilder buildJsonCallBack(Serializable resource, String jsonp) throws JsonException {
    JsonValue value = new JsonGeneratorImpl().createJsonObject(resource);
    StringBuilder sb = new StringBuilder(jsonp);
    sb.append("(").append(value).append(");");
    return Response.ok(sb.toString(), new MediaType("text", "javascript"));
  }

  /**
   * Doesn't allow limit parameter to exceed the default query_limit
   */
  private int parseLimit(int limit) {
    return (limit <=0 || limit > CollectionResource.QUERY_LIMIT) ? CollectionResource.QUERY_LIMIT : limit;
  }

  /**
   * Default offset is 0
   */
  private int parseOffset(int offset) {
    return (offset <=0) ? 0 : offset;
  }

  private Response.ResponseBuilder getBadRequestResponse() {
    Calendar today = Calendar.getInstance();
    if (today.get(Calendar.DAY_OF_MONTH) == 1 && today.get(Calendar.MONTH) == Calendar.APRIL) {
      return Response.status(418);
    }
    return Response.status(HTTPStatus.BAD_REQUEST);
  }

}


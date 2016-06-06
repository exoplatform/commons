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
import org.exoplatform.addons.es.client.ElasticContentRequestBuilder;
import org.exoplatform.addons.es.client.ElasticIndexingAuditTrail;
import org.exoplatform.addons.es.client.ElasticIndexingClient;
import org.exoplatform.addons.es.dao.IndexingOperationDAO;
import org.exoplatform.addons.es.domain.IndexingOperation;
import org.exoplatform.addons.es.domain.OperationType;
import org.exoplatform.addons.es.index.IndexingOperationProcessor;
import org.exoplatform.addons.es.index.IndexingServiceConnector;
import org.exoplatform.commons.api.persistence.DataInitializer;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

import java.util.*;

/**
 * Created by The eXo Platform SAS Author : Thibault Clement
 * tclement@exoplatform.com 10/12/15
 */
public class ElasticIndexingOperationProcessor extends IndexingOperationProcessor implements Startable {

  private static final Log                   LOG                                 = ExoLogger.getExoLogger(ElasticIndexingOperationProcessor.class);
  private static final String                BATCH_NUMBER_PROPERTY_NAME          = "exo.es.indexing.batch.number";
  private static final Integer               BATCH_NUMBER_DEFAULT                = 1000;
  private static final String                REQUEST_SIZE_LIMIT_PROPERTY_NAME    = "exo.es.indexing.request.size.limit";
  /** in bytes, default=10MB **/
  private static final Integer               REQUEST_SIZE_LIMIT_DEFAULT          = 10485760;
  private static final String                REINDEXING_BATCH_SIZE_PROPERTY_NAME = "exo.es.reindex.batch.size";
  private static final int                   REINDEXING_BATCH_SIZE_DEFAULT_VALUE = 100;

  // Service
  private final IndexingOperationDAO         indexingOperationDAO;
  private final ElasticIndexingClient        elasticIndexingClient;
  private final ElasticContentRequestBuilder elasticContentRequestBuilder;
  private final ElasticIndexingAuditTrail    auditTrail;
  private Integer                            batchNumber                         = BATCH_NUMBER_DEFAULT;
  private Integer                            requestSizeLimit                    = REQUEST_SIZE_LIMIT_DEFAULT;
  private int                                reindexBatchSize                    = REINDEXING_BATCH_SIZE_DEFAULT_VALUE;

  public ElasticIndexingOperationProcessor(IndexingOperationDAO indexingOperationDAO,
                                           ElasticIndexingClient elasticIndexingClient,
                                           ElasticContentRequestBuilder elasticContentRequestBuilder,
                                           ElasticIndexingAuditTrail auditTrail,
                                           DataInitializer dataInitializer) {
    this.indexingOperationDAO = indexingOperationDAO;
    this.auditTrail = auditTrail;
    this.elasticIndexingClient = elasticIndexingClient;
    this.elasticContentRequestBuilder = elasticContentRequestBuilder;
    if (StringUtils.isNotBlank(PropertyManager.getProperty(BATCH_NUMBER_PROPERTY_NAME))) {
      this.batchNumber = Integer.valueOf(PropertyManager.getProperty(BATCH_NUMBER_PROPERTY_NAME));
    }
    if (StringUtils.isNotBlank(PropertyManager.getProperty(REQUEST_SIZE_LIMIT_PROPERTY_NAME))) {
      this.requestSizeLimit = Integer.valueOf(PropertyManager.getProperty(REQUEST_SIZE_LIMIT_PROPERTY_NAME));
    }
    if (StringUtils.isNotBlank(PropertyManager.getProperty(REINDEXING_BATCH_SIZE_PROPERTY_NAME))) {
      this.reindexBatchSize = Integer.valueOf(PropertyManager.getProperty(REINDEXING_BATCH_SIZE_PROPERTY_NAME));
    }
  }

  @Override
  public void addConnector(IndexingServiceConnector indexingServiceConnector) {
    addConnector(indexingServiceConnector, false);
  }

  @Override
  public void addConnector(IndexingServiceConnector indexingServiceConnector, Boolean override) {
    if (getConnectors().containsKey(indexingServiceConnector.getType()) && override.equals(false)) {
      LOG.error("Impossible to add connector {}. A connector with the same name has already been registered.",
                indexingServiceConnector.getType());
    } else {
      getConnectors().put(indexingServiceConnector.getType(), indexingServiceConnector);
      LOG.info("An Indexing Connector has been added: {}", indexingServiceConnector.getType());
    }
  }

  /**
   * Handle the Indexing queue Get all data in the indexing queue, transform
   * them to ES requests, send requests to ES This method is ONLY called by the
   * job scheduler. This method is not annotated with @ExoTransactional because
   * we don't want it to be executed in one transaction. Every
   */
  @Override
  public void process() {
    // Loop until the number of data retrieved from indexing queue is less than
    // BATCH_NUMBER (default = 1000)
    int processedOperations;
    do {
      processedOperations = processBulk();
    } while (processedOperations >= batchNumber);
  }

  private int processBulk() {
    // Map<OperationType={Create,Delete,...}, Map<String=EntityType,
    // List<IndexingOperation>>> indexingQueueSorted
    Map<OperationType, Map<String, List<IndexingOperation>>> indexingQueueSorted = new HashMap<>();
    List<IndexingOperation> indexingOperations;
    long maxIndexingOperationId = 0;

    // Get BATCH_NUMBER (default = 1000) first indexing operations
    indexingOperations = indexingOperationDAO.findAllFirst(batchNumber);

    // Get all Indexing operations and order them per operation and type in map:
    // <Operation, <Type, List<IndexingOperation>>>
    for (IndexingOperation indexingOperation : indexingOperations) {
      putIndexingOperationInMemoryQueue(indexingOperation, indexingQueueSorted);
      // Get the max ID of IndexingOperation of the bulk
      if (maxIndexingOperationId < indexingOperation.getId()) {
        maxIndexingOperationId = indexingOperation.getId();
      }
    }

    processInit(indexingQueueSorted);
    processDeleteAll(indexingQueueSorted);
    processReindexAll(indexingQueueSorted);
    processCUD(indexingQueueSorted);

    // Removes the processed IDs from the “indexing queue” table that have
    // timestamp older than the timestamp of
    // start of processing
    indexingOperationDAO.deleteAllIndexingOperationsHavingIdLessThanOrEqual(maxIndexingOperationId);
    return indexingOperations.size();
  }

  /**
   * Add an indexing operation to the Temporary inMemory IndexingQueue
   * 
   * @param indexingOperation the operation to add to the Temporary inMemory
   *          IndexingQueue
   * @param indexingQueueSorted Temporary inMemory IndexingQueue
   */
  private void putIndexingOperationInMemoryQueue(IndexingOperation indexingOperation,
                                                 Map<OperationType, Map<String, List<IndexingOperation>>> indexingQueueSorted) {
    // Check if the Indexing Operation map already contains a specific operation
    if (!indexingQueueSorted.containsKey(indexingOperation.getOperation())) {
      // If not add a new operation in the map
      indexingQueueSorted.put(indexingOperation.getOperation(), new HashMap<String, List<IndexingOperation>>());
    }
    // Check if the operation map already contains a specific type
    if (!indexingQueueSorted.get(indexingOperation.getOperation()).containsKey(indexingOperation.getEntityType())) {
      // If not add a new type for the operation above
      indexingQueueSorted.get(indexingOperation.getOperation()).put(indexingOperation.getEntityType(),
                                                                    new ArrayList<IndexingOperation>());
    }
    // Add the indexing operation in the specific Operation -> Type
    indexingQueueSorted.get(indexingOperation.getOperation()).get(indexingOperation.getEntityType()).add(indexingOperation);
  }

  /**
   * Process all the Create / Update / Delete operations
   * 
   * @param indexingQueueSorted Temporary inMemory IndexingQueue
   */
  private void processCUD(Map<OperationType, Map<String, List<IndexingOperation>>> indexingQueueSorted) {
    // Initialise bulk request for CUD operations
    String bulkRequest = "";

    // Process Delete document operation
    if (indexingQueueSorted.containsKey(OperationType.DELETE)) {
      for (String entityType : indexingQueueSorted.get(OperationType.DELETE).keySet()) {
        for (IndexingOperation deleteIndexQueue : indexingQueueSorted.get(OperationType.DELETE).get(entityType)) {
          bulkRequest += elasticContentRequestBuilder.getDeleteDocumentRequestContent((ElasticIndexingServiceConnector) getConnectors().get(deleteIndexQueue.getEntityType()),
                                                                                      deleteIndexQueue.getEntityId());
          // Remove the object from other create or update operations planned
          // before the timestamp of the delete operation
          deleteOperationsByEntityIdForTypesBefore(new OperationType[] { OperationType.CREATE },
                                                   indexingQueueSorted,
                                                   deleteIndexQueue);
          deleteOperationsByEntityIdForTypes(new OperationType[] { OperationType.UPDATE }, indexingQueueSorted, deleteIndexQueue);
        }
      }
      // Remove the delete operations from the map
      indexingQueueSorted.remove(OperationType.DELETE);
    }

    // Process Create document operation
    if (indexingQueueSorted.containsKey(OperationType.CREATE)) {
      for (String entityType : indexingQueueSorted.get(OperationType.CREATE).keySet()) {
        for (IndexingOperation createIndexQueue : indexingQueueSorted.get(OperationType.CREATE).get(entityType)) {
          bulkRequest += elasticContentRequestBuilder.getCreateDocumentRequestContent((ElasticIndexingServiceConnector) getConnectors().get(createIndexQueue.getEntityType()),
                                                                                      createIndexQueue.getEntityId());
          // Remove the object from other update operations for this entityId
          deleteOperationsByEntityIdForTypes(new OperationType[] { OperationType.UPDATE }, indexingQueueSorted, createIndexQueue);
          // Check if the bulk request limit size is already reached
          bulkRequest = checkBulkRequestSizeReachedLimitation(bulkRequest);
        }
      }
      // Remove the create operations from the map
      indexingQueueSorted.remove(OperationType.CREATE);
    }

    // Process Update document operation
    if (indexingQueueSorted.containsKey(OperationType.UPDATE)) {
      for (String entityType : indexingQueueSorted.get(OperationType.UPDATE).keySet()) {
        for (IndexingOperation updateIndexQueue : indexingQueueSorted.get(OperationType.UPDATE).get(entityType)) {
          bulkRequest += elasticContentRequestBuilder.getUpdateDocumentRequestContent((ElasticIndexingServiceConnector) getConnectors().get(updateIndexQueue.getEntityType()),
                                                                                      updateIndexQueue.getEntityId());
          // Check if the bulk request limit size is already reached
          bulkRequest = checkBulkRequestSizeReachedLimitation(bulkRequest);
        }
      }
      // Remove the update operations from the map
      indexingQueueSorted.remove(OperationType.UPDATE);
    }

    if (StringUtils.isNotBlank(bulkRequest)) {
      elasticIndexingClient.sendCUDRequest(bulkRequest);
    }
  }

  /**
   * Process all the requests for “init of the ES create mapping” (Operation
   * type = I) in the indexing queue (if any)
   * 
   * @param indexingQueueSorted Temporary inMemory IndexingQueue
   */
  private void processInit(Map<OperationType, Map<String, List<IndexingOperation>>> indexingQueueSorted) {
    if (indexingQueueSorted.containsKey(OperationType.INIT)) {
      for (String entityType : indexingQueueSorted.get(OperationType.INIT).keySet()) {
        sendInitRequests(getConnectors().get(entityType));
      }
      indexingQueueSorted.remove(OperationType.INIT);
    }
  }

  /**
   * Process all the requests for “remove all documents of type” (Operation type
   * = X) in the indexing queue (if any) = Delete type in ES
   * 
   * @param indexingQueueSorted Temporary inMemory IndexingQueue
   */
  private void processDeleteAll(Map<OperationType, Map<String, List<IndexingOperation>>> indexingQueueSorted) {
    if (indexingQueueSorted.containsKey(OperationType.DELETE_ALL)) {
      for (String entityType : indexingQueueSorted.get(OperationType.DELETE_ALL).keySet()) {
        if (indexingQueueSorted.get(OperationType.DELETE_ALL).containsKey(entityType)) {
          for (IndexingOperation indexingOperation : indexingQueueSorted.get(OperationType.DELETE_ALL).get(entityType)) {
            processDeleteAll(indexingOperation, indexingQueueSorted);
          }
        }
      }
      indexingQueueSorted.remove(OperationType.DELETE_ALL);
    }
  }

  /**
   * @param indexingOperation
   * @param indexingQueueSorted Temporary inMemory IndexingQueue
   */
  private void processDeleteAll(IndexingOperation indexingOperation,
                                Map<OperationType, Map<String, List<IndexingOperation>>> indexingQueueSorted) {
    // Remove the type (= remove all documents of this type) and recreate it
    ElasticIndexingServiceConnector connector = (ElasticIndexingServiceConnector) getConnectors().get(indexingOperation.getEntityType());
    // log in Audit Trail
    auditTrail.audit(ElasticIndexingAuditTrail.DELETE_ALL, null, null, connector.getType(), null, null, 0);
    // Call ES
    elasticIndexingClient.sendDeleteAllDocsOfTypeRequest(connector.getIndex(), connector.getType());
    // Remove all useless CUD operation that was plan before this delete all
    deleteOperationsForTypesBefore(new OperationType[] { OperationType.CREATE, OperationType.UPDATE, OperationType.DELETE },
                                   indexingQueueSorted,
                                   indexingOperation);
  }

  /**
   * Process all the requests for “Reindex all documents of type” (Operation
   * type = R) in the indexing queue (if any)
   * 
   * @param indexingQueueSorted Temporary inMemory IndexingQueue
   */
  private void processReindexAll(Map<OperationType, Map<String, List<IndexingOperation>>> indexingQueueSorted) {
    List<IndexingOperation> operations;
    List<String> ids;
    int numberIndexed;
    int offset;

    if (indexingQueueSorted.containsKey(OperationType.REINDEX_ALL)) {
      for (String entityType : indexingQueueSorted.get(OperationType.REINDEX_ALL).keySet()) {
        if (indexingQueueSorted.get(OperationType.REINDEX_ALL).containsKey(entityType)) {
          for (IndexingOperation indexingOperation : indexingQueueSorted.get(OperationType.REINDEX_ALL).get(entityType)) {
            long startTime = System.currentTimeMillis();
            // 1- Delete all documents in ES (and purge the indexing queue)
            indexingOperationDAO.create(new IndexingOperation(null, entityType, OperationType.DELETE_ALL));
            // 2- Get all the documents ID
            IndexingServiceConnector connector = getConnectors().get(indexingOperation.getEntityType());
            // 3- Inject as a CUD operation
            offset = 0;
            do {
              ids = connector.getAllIds(offset, reindexBatchSize);
              if (ids == null) {
                numberIndexed = 0;
              } else {
                operations = new ArrayList<>(ids.size());
                for (String id : ids) {
                  operations.add(new IndexingOperation(id, entityType, OperationType.CREATE));
                }
                indexingOperationDAO.createAll(operations);
                numberIndexed = ids.size();
                offset += reindexBatchSize;
              }
            } while (numberIndexed == reindexBatchSize);
            // 4- log in Audit Trail
            auditTrail.audit(ElasticIndexingAuditTrail.REINDEX_ALL, null, null, entityType, null, null, (System.currentTimeMillis()-startTime));
          }
        }
      }
      indexingQueueSorted.remove(OperationType.REINDEX_ALL);
    }
  }

  private void deleteOperationsForTypesBefore(OperationType[] operations,
                                              Map<OperationType, Map<String, List<IndexingOperation>>> indexingQueueSorted,
                                              IndexingOperation refIindexOperation) {
    for (OperationType operation : operations) {
      if (indexingQueueSorted.containsKey(operation)) {
        if (indexingQueueSorted.get(operation).containsKey(refIindexOperation.getEntityType())) {
          for (Iterator<IndexingOperation> iterator = indexingQueueSorted.get(operation)
                                                                         .get(refIindexOperation.getEntityType())
                                                                         .iterator(); iterator.hasNext();) {
            IndexingOperation indexingOperation = iterator.next();
            // Check timestamp higher than the timestamp of the reference
            // indexing operation, the index operation is removed
            if (refIindexOperation.getId() > indexingOperation.getId()) {
              iterator.remove();
            }
          }
        }
      }
    }
  }

  private void deleteOperationsByEntityIdForTypesBefore(OperationType[] operations,
                                                        Map<OperationType, Map<String, List<IndexingOperation>>> indexingQueueSorted,
                                                        IndexingOperation indexQueue) {
    for (OperationType operation : operations) {
      if (indexingQueueSorted.containsKey(operation)) {
        if (indexingQueueSorted.get(operation).containsKey(indexQueue.getEntityType())) {
          for (Iterator<IndexingOperation> iterator = indexingQueueSorted.get(operation)
                                                                         .get(indexQueue.getEntityType())
                                                                         .iterator(); iterator.hasNext();) {
            IndexingOperation indexingOperation = iterator.next();
            // Check Id higher than the Id of the CUD indexing queue, the index
            // queue is removed
            if ((indexQueue.getId() > indexingOperation.getId())
                && indexingOperation.getEntityId().equals(indexQueue.getEntityId())) {
              iterator.remove();
            }
          }
        }
      }
    }
  }

  private void deleteOperationsByEntityIdForTypes(OperationType[] operations,
                                                  Map<OperationType, Map<String, List<IndexingOperation>>> indexingQueueSorted,
                                                  IndexingOperation indexQueue) {
    for (OperationType operation : operations) {
      if (indexingQueueSorted.containsKey(operation)) {
        if (indexingQueueSorted.get(operation).containsKey(indexQueue.getEntityType())) {
          for (Iterator<IndexingOperation> iterator = indexingQueueSorted.get(operation)
                                                                         .get(indexQueue.getEntityType())
                                                                         .iterator(); iterator.hasNext();) {
            IndexingOperation indexingOperation = iterator.next();
            if (indexingOperation.getEntityId().equals(indexQueue.getEntityId())) {
              iterator.remove();
            }
          }
        }
      }
    }
  }

  private void sendInitRequests(IndexingServiceConnector IndexingServiceConnector) {
    ElasticIndexingServiceConnector connector = (ElasticIndexingServiceConnector) IndexingServiceConnector;

    // Send request to create index
    elasticIndexingClient.sendCreateIndexRequest(connector.getIndex(),
                                                 elasticContentRequestBuilder.getCreateIndexRequestContent(connector));

    // Send request to create type
    elasticIndexingClient.sendCreateTypeRequest(connector.getIndex(), connector.getType(), connector.getMapping());
  }

  /**
   * If the bulk request already reached a size limitation, the bulk request
   * need to be sent immediately
   *
   * @param bulkRequest to analyze
   * @return
   */
  private String checkBulkRequestSizeReachedLimitation(String bulkRequest) {
    if (bulkRequest.getBytes().length >= requestSizeLimit) {
      elasticIndexingClient.sendCUDRequest(bulkRequest);
      // return an empty bulk request
      return "";
    } else {
      return bulkRequest;
    }
  }

  private void addInitOperation(String connector) {
    IndexingOperation indexingOperation = new IndexingOperation();
    indexingOperation.setEntityType(connector);
    indexingOperation.setOperation(OperationType.INIT);
    indexingOperationDAO.create(indexingOperation);
  }

  public Integer getBatchNumber() {
    return batchNumber;
  }

  public void setBatchNumber(Integer batchNumber) {
    this.batchNumber = batchNumber;
  }

  public Integer getRequestSizeLimit() {
    return requestSizeLimit;
  }

  public void setRequestSizeLimit(Integer requestSizeLimit) {
    this.requestSizeLimit = requestSizeLimit;
  }

  public int getReindexBatchSize() {
    return reindexBatchSize;
  }

  public void setReindexBatchSize(int reindexBatchSize) {
    this.reindexBatchSize = reindexBatchSize;
  }

  @Override
  public void start() {
    // ES index and type need to be created for all registered connectors
    initConnectors();
  }

  @Override
  public void stop() {

  }

  private void initConnectors() {
    for (Map.Entry<String, IndexingServiceConnector> entry: getConnectors().entrySet()) {
      addInitOperation(entry.getValue().getType());
    }
  }
}

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
package org.exoplatform.commons.search.index.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.picocontainer.Startable;

import org.exoplatform.commons.api.persistence.DataInitializer;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.search.dao.IndexingOperationDAO;
import org.exoplatform.commons.search.domain.IndexingOperation;
import org.exoplatform.commons.search.domain.OperationType;
import org.exoplatform.commons.search.es.client.ElasticContentRequestBuilder;
import org.exoplatform.commons.search.es.client.ElasticIndexingAuditTrail;
import org.exoplatform.commons.search.es.client.ElasticIndexingClient;
import org.exoplatform.commons.search.index.IndexingOperationProcessor;
import org.exoplatform.commons.search.index.IndexingServiceConnector;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

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
  private final ElasticIndexingClient elasticIndexingClient;
  private final ElasticContentRequestBuilder elasticContentRequestBuilder;
  private final ElasticIndexingAuditTrail    auditTrail;
  private final EntityManagerService         entityManagerService;
  private Integer                            batchNumber                         = BATCH_NUMBER_DEFAULT;
  private Integer                            requestSizeLimit                    = REQUEST_SIZE_LIMIT_DEFAULT;
  private int                                reindexBatchSize                    = REINDEXING_BATCH_SIZE_DEFAULT_VALUE;
  private Map<String, Set<String>>           typesOrIndexUpgrading               = new HashMap<>();

  private ExecutorService executors = Executors.newCachedThreadPool();

  private String                             esVersion;

  private boolean interrupted = false;

  public ElasticIndexingOperationProcessor(IndexingOperationDAO indexingOperationDAO,
                                           ElasticIndexingClient elasticIndexingClient,
                                           ElasticContentRequestBuilder elasticContentRequestBuilder,
                                           ElasticIndexingAuditTrail auditTrail,
                                           EntityManagerService entityManagerService,
                                           DataInitializer dataInitializer,
                                           InitParams initParams) {
    this.indexingOperationDAO = indexingOperationDAO;
    this.auditTrail = auditTrail;
    this.entityManagerService = entityManagerService;
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
    if (initParams == null || !initParams.containsKey("es.version")) {
      throw new IllegalStateException("es.version parameter is mandatory");
    }
    this.esVersion = initParams.getValueParam("es.version").getValue();
    LOG.info("Use ES Version {}", esVersion);
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
   * we don't want it to be executed in one transaction.
   *
   * A request lifecycle is started and ended for all jobs, it is done by
   * org.exoplatform.services.scheduler.impl.JobEnvironmentConfigListener. It
   * means that we have 1 entity manager per job execution. Because of that, we
   * have to take care of cleaning the persistence context regularly to avoid
   * to have too big sessions and bad performances.
   *
   * This method is synchronized to make sure the queue is processed by only one
   * thread at a time, since the indexing queue does not support multi-thread
   * processing for the moment.
   */
  @Override
  public synchronized void process() {
    this.interrupted = false;
    try {
      // Loop until the number of data retrieved from indexing queue is less than
      // BATCH_NUMBER (default = 1000)
      int processedOperations;
      do {
        processedOperations = processBulk();
      } while (processedOperations >= batchNumber);
    } finally {
      if (this.interrupted) {
        LOG.info("Indexing queue processing interruption done");
      }
    }
  }

  /**
   * Set the indexing process as interrupted in order to terminate it as soon
   * as possible without finishing the whole process.
   * Since the indexing process can take time (for a reindexAll operation for example), it
   * allows to interrupt it gracefully (without killing the thread).
   */
  @Override
  public void interrupt() {
    LOG.info("Indexing queue processing has been interrupted. Please wait until the service exists cleanly...");
    this.interrupted = true;
  }

  private boolean isInterrupted() {
    if(Thread.currentThread().isInterrupted()) {
      LOG.info("Thread running indexing queue processing has been interrupted. Please wait until the service exists cleanly...");
      this.interrupted = true;
    }
    return this.interrupted;
  }

  private boolean isUpgradeInProgress() {
    return !typesOrIndexUpgrading.isEmpty();
  }

  private int processBulk() {
    // Choose operation to delete from Queue one by one instead
    if (isUpgradeInProgress()) {
      LOG.info("Migration of indexes is in progress, indexation is suspended until migration finishes");
      return 0;
    }

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

    if(isInterrupted()) {
      throw new RuntimeException("Indexing queue processing interrupted");
    }

    // Removes the processed IDs from the “indexing queue” table that have
    // timestamp older than the timestamp of
    // start of processing
    indexingOperationDAO.deleteAllIndexingOperationsHavingIdLessThanOrEqual(maxIndexingOperationId);

    // clear entity manager content after each bulk
    entityManagerService.getEntityManager().clear();

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
    // Initialize bulk request for CUD operations
    String bulkRequest = "";

    // Process Delete document operation
    if (indexingQueueSorted.containsKey(OperationType.DELETE)) {
      Map<String, List<IndexingOperation>> deleteIndexingOperationsMap = indexingQueueSorted.get(OperationType.DELETE);
      for (String entityType : deleteIndexingOperationsMap.keySet()) {
        ElasticIndexingServiceConnector connector = (ElasticIndexingServiceConnector) getConnectors().get(entityType);
        List<IndexingOperation> deleteIndexingOperationsList = deleteIndexingOperationsMap.get(entityType);
        if (deleteIndexingOperationsList == null || deleteIndexingOperationsList.isEmpty()) {
          continue;
        }
        Iterator<IndexingOperation> deleteIndexingOperationsIterator = deleteIndexingOperationsList.iterator();
        while (deleteIndexingOperationsIterator.hasNext()) {
          if(isInterrupted()) {
            return;
          }
          IndexingOperation deleteIndexQueue = deleteIndexingOperationsIterator.next();
          try {
            String deleteDocumentRequestContent = elasticContentRequestBuilder.getDeleteDocumentRequestContent(connector,
                    deleteIndexQueue.getEntityId());
            if(deleteDocumentRequestContent != null) {
              bulkRequest += deleteDocumentRequestContent;
            }
            // Choose operation to delete from Queue one by one instead
          } catch(Exception e) {
            LOG.warn("Error while *deleting* index entry of entity, type = " + entityType + ", id =" + (deleteIndexQueue == null ? null : deleteIndexQueue.getEntityId()) + ", cause:", e);
          } finally {
            // Remove the delete operations from the map
            indexingQueueSorted.remove(OperationType.DELETE, deleteIndexQueue.getEntityId());
          }

          // Delete added indexation operation from queue even if the request fails
          deleteIndexingOperationsIterator.remove();

          // Remove the object from other create or update operations planned
          // before the timestamp of the delete operation
          deleteOperationsByEntityIdForTypesBefore(new OperationType[] { OperationType.CREATE },
                                                   indexingQueueSorted,
                                                   deleteIndexQueue);
          deleteOperationsByEntityIdForTypes(new OperationType[] { OperationType.UPDATE }, indexingQueueSorted, deleteIndexQueue);
          // Check if the bulk request limit size is already reached
          bulkRequest = checkBulkRequestSizeReachedLimitation(bulkRequest);
        }
      }
    }

    // Process Create document operation
    if (indexingQueueSorted.containsKey(OperationType.CREATE)) {
      Map<String, List<IndexingOperation>> createIndexingOperationsMap = indexingQueueSorted.get(OperationType.CREATE);
      for (String entityType : createIndexingOperationsMap.keySet()) {
        ElasticIndexingServiceConnector connector = (ElasticIndexingServiceConnector) getConnectors().get(entityType);
        List<IndexingOperation> createIndexingOperationsList = createIndexingOperationsMap.get(entityType);
        if (createIndexingOperationsList == null || createIndexingOperationsList.isEmpty()) {
          continue;
        }
        Iterator<IndexingOperation> createIndexingOperationsIterator = createIndexingOperationsList.iterator();
        while (createIndexingOperationsIterator.hasNext()) {
          if(isInterrupted()) {
            return;
          }
          IndexingOperation createIndexQueue = createIndexingOperationsIterator.next();
          try {
            if(connector.isNeedIngestPipeline()) {
              String singleRequestOperation = elasticContentRequestBuilder.getCreatePipelineDocumentRequestContent(connector, createIndexQueue.getEntityId());
              if(singleRequestOperation != null) {
                elasticIndexingClient.sendCreateDocOnPipeline(connector.getIndex(),
                        connector.getType(),
                        createIndexQueue.getEntityId(),
                        connector.getPipelineName(),
                        singleRequestOperation);
              }
              // Delete this single operation since it's not indexed in bulk
              indexingQueueSorted.remove(OperationType.CREATE, createIndexQueue.getEntityId());
              indexingQueueSorted.remove(OperationType.UPDATE, createIndexQueue.getEntityId());
            } else {
              String singleRequestOperation = elasticContentRequestBuilder.getCreateDocumentRequestContent(connector, createIndexQueue.getEntityId());
              if(singleRequestOperation != null) {
                bulkRequest += singleRequestOperation;
              }
            }
          } catch(Exception e) {
            LOG.warn("Error while *creating* index entry of entity, type = " + entityType + ", id =" + (createIndexQueue == null ? null : createIndexQueue.getEntityId()) + ", cause:", e);
          } finally {

            // Delete added indexation operation from queue even if the request fails
            createIndexingOperationsIterator.remove();

            // Remove the object from other update operations for this entityId
            deleteOperationsByEntityIdForTypes(new OperationType[] { OperationType.UPDATE }, indexingQueueSorted, createIndexQueue);

            // Delete this single operation since it's not indexed in bulk
            indexingQueueSorted.remove(OperationType.CREATE, createIndexQueue.getEntityId());
          }

          // Check if the bulk request limit size is already reached
          bulkRequest = checkBulkRequestSizeReachedLimitation(bulkRequest);
        }
      }
    }

    // Process Update document operation
    if (indexingQueueSorted.containsKey(OperationType.UPDATE)) {
      Map<String, List<IndexingOperation>> updateIndexingOperationsMap = indexingQueueSorted.get(OperationType.UPDATE);
      for (String entityType : updateIndexingOperationsMap.keySet()) {
        ElasticIndexingServiceConnector connector = (ElasticIndexingServiceConnector) getConnectors().get(entityType);
        List<IndexingOperation> updateIndexingOperationsList = updateIndexingOperationsMap.get(entityType);
        if (updateIndexingOperationsList == null || updateIndexingOperationsList.isEmpty()) {
          continue;
        }
        Iterator<IndexingOperation> updateIndexingOperationsIterator = updateIndexingOperationsList.iterator();
        while (updateIndexingOperationsIterator.hasNext()) {
          if(isInterrupted()) {
            return;
          }
          IndexingOperation updateIndexQueue = updateIndexingOperationsIterator.next();
          try {
            if(connector.isNeedIngestPipeline()) {
              String singleRequestOperation = elasticContentRequestBuilder.getCreatePipelineDocumentRequestContent(connector, updateIndexQueue.getEntityId());
              if(singleRequestOperation != null) {
                elasticIndexingClient.sendCreateDocOnPipeline(connector.getIndex(),
                        connector.getType(),
                        updateIndexQueue.getEntityId(),
                        connector.getPipelineName(),
                        singleRequestOperation);
              }
            } else {
              String singleRequestOperation = elasticContentRequestBuilder.getUpdateDocumentRequestContent(connector, updateIndexQueue.getEntityId());
              if(singleRequestOperation != null) {
                bulkRequest += singleRequestOperation;
              }
            }
          } catch(Exception e) {
            LOG.warn("Error while *updating* index entry of entity, type = " + entityType + ", id =" + (updateIndexQueue == null ? null : updateIndexQueue.getEntityId()) + ", cause:", e);
          } finally {
            // Delete this single operation since it's not indexed in bulk
            indexingQueueSorted.remove(OperationType.UPDATE, updateIndexQueue.getEntityId());
          }

          // Delete added indexation operation from queue even if the request fails
          updateIndexingOperationsIterator.remove();

          // Check if the bulk request limit size is already reached
          bulkRequest = checkBulkRequestSizeReachedLimitation(bulkRequest);
        }
      }
    }

    if (StringUtils.isNotBlank(bulkRequest) && !isInterrupted()) {
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
        if(isInterrupted()) {
          return;
        }
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
        if(isInterrupted()) {
          return;
        }
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
    if (indexingQueueSorted.containsKey(OperationType.REINDEX_ALL)) {
      for (String entityType : indexingQueueSorted.get(OperationType.REINDEX_ALL).keySet()) {
        if (indexingQueueSorted.get(OperationType.REINDEX_ALL).containsKey(entityType)) {
          for (IndexingOperation indexingOperation : indexingQueueSorted.get(OperationType.REINDEX_ALL).get(entityType)) {
            if(isInterrupted()) {
              return;
            }
            reindexAllByEntityType(indexingOperation.getEntityType());
            // clear entity manager content
            entityManagerService.getEntityManager().clear();
          }
        }
      }
      indexingQueueSorted.remove(OperationType.REINDEX_ALL);
    }
  }

  /**
   * Reindex all the entities of the given entity type.
   *
   * @param entityType Entity type of the entities to reindex
   */
  @ExoTransactional
  private void reindexAllByEntityType(String entityType) {
    long startTime = System.currentTimeMillis();
    // 1- Delete all documents in ES (and purge the indexing queue)
    indexingOperationDAO.create(new IndexingOperation(null, entityType, OperationType.DELETE_ALL));
    // 2- Get all the documents ID
    IndexingServiceConnector connector = getConnectors().get(entityType);
    // 3- Inject as a CUD operation
    int offset = 0;
    int numberIndexed;
    do {
      if(isInterrupted()) {
        return;
      }
      List<String> ids = connector.getAllIds(offset, reindexBatchSize);
      if (ids == null) {
        numberIndexed = 0;
      } else {
        List<IndexingOperation> operations = new ArrayList<>(ids.size());
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

    String indexAlias = connector.getIndex();
    String previousIndex = connector.getPreviousIndex();
    String index = connector.getCurrentIndex();
    String type = connector.getType();
    if (typesOrIndexUpgrading.containsKey(indexAlias) && typesOrIndexUpgrading.get(indexAlias).contains(type)) {
      boolean newIndexExists = elasticIndexingClient.sendIsIndexExistsRequest(index);

      // If the upgrade is incomplete (should point the alias on previous index)
      boolean aliasExistsOnPreviousIndex = elasticIndexingClient.sendGetIndexAliasesRequest(previousIndex).contains(indexAlias);
      if(!aliasExistsOnPreviousIndex) {
        boolean aliasExistsOnCurrentIndex = newIndexExists && elasticIndexingClient.sendGetIndexAliasesRequest(index).contains(indexAlias);
        // If the alias points to the new index, point it again to the previous one, else add new alias to previous
        elasticIndexingClient.sendCreateIndexAliasRequest(previousIndex, aliasExistsOnCurrentIndex ? index : null, indexAlias);
      }

      if (newIndexExists) {
        boolean newTypeExists = elasticIndexingClient.sendIsTypeExistsRequest(index, type);
        if (newTypeExists) {
          // Upgrade was interrupted, so remove it and upgrade again
          LOG.warn("ES index upgrade '{}' was interrupted, the new index/type {}/{} will be recreated", previousIndex, index, type);
          elasticIndexingClient.sendDeleteAllDocsOfTypeRequest(index, type);
        }

        // Send request to create type
        elasticIndexingClient.sendCreateTypeRequest(index, connector.getType(), connector.getMapping());
      } else {
        elasticIndexingClient.sendCreateIndexRequest(index,
                                                 elasticContentRequestBuilder.getCreateIndexRequestContent(connector));
        // Send request to create type
        elasticIndexingClient.sendCreateTypeRequest(index, connector.getType(), connector.getMapping());

        if(connector.isNeedIngestPipeline()) {
          elasticIndexingClient.sendCreateAttachmentPipelineRequest(index, connector.getType(), connector.getPipelineName(), connector.getAttachmentProcessor());
        }
      }

      // Init reindex. Once the reindex finished, the index alias will be updated to new index
      executors.submit(new ReindexESType(ExoContainerContext.getCurrentContainer(), connector));
    } else {
      boolean useAlias = true;
      if (index == null) {
        index = indexAlias;
        useAlias = false;
      }

      // Send request to create index
      boolean newlyCreated = elasticIndexingClient.sendCreateIndexRequest(index,
                                                   elasticContentRequestBuilder.getCreateIndexRequestContent(connector));
      if (newlyCreated && useAlias) {
        elasticIndexingClient.sendCreateIndexAliasRequest(index, null, indexAlias);
      }

      // Send request to create type
      elasticIndexingClient.sendCreateTypeRequest(index, connector.getType(), connector.getMapping());

      if(connector.isNeedIngestPipeline()) {
        elasticIndexingClient.sendCreateAttachmentPipelineRequest(index, connector.getType(), connector.getPipelineName(), connector.getAttachmentProcessor());
      }
    }

    // Make sure that the migration is not executed twice on the same connector
    connector.setPreviousIndex(null);
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
    try {
      String esVersion = elasticIndexingClient.sendGetESVersion();
      if (esVersion == null || !esVersion.startsWith(this.esVersion + ".")) {
        LOG.error("Expected Version of ES version is " + this.esVersion + " but was " + esVersion
            + ". If this is a compatible version, you can configure 'exo.es.version.minor' to delete this error message.");
      }
      // ES index and type need to be created for all registered connectors
      initConnectors();
    } catch (Exception e) {
      LOG.error("Error while initializing ES connectors", e);
    }
  }

  @Override
  public void stop() {
    executors.shutdownNow();
  }

  private void initConnectors() {
    for (Map.Entry<String, IndexingServiceConnector> entry: getConnectors().entrySet()) {
      ElasticIndexingServiceConnector connector = (ElasticIndexingServiceConnector) entry.getValue();
      String previousIndex = connector.getPreviousIndex();
      String index = connector.getCurrentIndex();
      String indexAlias = connector.getIndex();

      boolean needsUpgrade = false;
      if (previousIndex != null) {
        // Need to check the upgrade status (incomplete/ not run == new index doesn't exists or indexAlias is not added to new index)
        needsUpgrade = elasticIndexingClient.sendIsIndexExistsRequest(previousIndex)
            && (!elasticIndexingClient.sendIsIndexExistsRequest(index)
                || !elasticIndexingClient.sendGetIndexAliasesRequest(index).contains(indexAlias));
      }

      if(needsUpgrade) {
        if(!typesOrIndexUpgrading.containsKey(indexAlias)) {
          typesOrIndexUpgrading.put(indexAlias, new HashSet<>());
        }
        typesOrIndexUpgrading.get(indexAlias).add(entry.getKey());
      }
    }
    for (Map.Entry<String, IndexingServiceConnector> entry: getConnectors().entrySet()) {
      sendInitRequests(entry.getValue());
    }
  }

  public class ReindexESType implements Runnable {
    private String       index;

    private String       previousIndex;

    private String       indexAlias;

    private String       type;

    private String       pipeline;

    private boolean      reindexFromDB;

    private ExoContainer exoContainer;

    public ReindexESType(ExoContainer exoContainer, ElasticIndexingServiceConnector connector) {
      this.exoContainer = exoContainer;
      this.index = connector.getCurrentIndex();
      this.previousIndex = connector.getPreviousIndex();
      this.indexAlias = connector.getIndex();
      this.type = connector.getType();
      this.pipeline = connector.getPipelineName();
      this.reindexFromDB = connector.isReindexOnUpgrade();
    }

    @Override
    public void run() {
      try {
        if (reindexFromDB) {
          ExoContainerContext.setCurrentContainer(exoContainer);
          reindexAllByEntityType(type);
        } else {
          LOG.info("Reindexing index alias {} from old index {} to new index {}, for type {}",
                   indexAlias,
                   previousIndex,
                   index,
                   type);
            try {
                elasticIndexingClient.sendReindexTypeRequest(index, previousIndex, type, pipeline);
                LOG.info("Reindexation finished for index alias {} from old index {} to new index {}, for type {}",
                        indexAlias,
                        previousIndex,
                        index,
                        type);
            } catch (Exception e) {
                LOG.warn("Reindexation using pipeline error for index alias {} from old index {} to new index {}, for type {}. The reindexation will proceed from eXo DB",
                        indexAlias,
                        previousIndex,
                        index,
                        type);
                ExoContainerContext.setCurrentContainer(exoContainer);
                reindexAllByEntityType(type);
            }
        }

        // This algorithm should be thread safe
        synchronized (typesOrIndexUpgrading) {
          boolean indexMigrationInProgress = typesOrIndexUpgrading.get(indexAlias).size() > 1;

          if (indexMigrationInProgress) {
            LOG.info("The index {} has some types not completely migrated yet, the old index will be deleted after migration is finished",
                     previousIndex);
            typesOrIndexUpgrading.get(indexAlias).remove(type);
          } else {
            LOG.info("Switching index alias {} from old index {} to new index {}", indexAlias, previousIndex, index);
            elasticIndexingClient.sendCreateIndexAliasRequest(index, previousIndex, indexAlias);

            typesOrIndexUpgrading.remove(indexAlias);

            LOG.info("Remove old index {}", previousIndex);
            elasticIndexingClient.sendDeleteIndexRequest(previousIndex);

            if(typesOrIndexUpgrading.isEmpty()) {
              LOG.info("ES indexes migration finished (except indexes that will be reindexed from DB)");
            }
          }
        }
      } catch (Exception e) {
        LOG.error("An error occurred while upgrading index " + previousIndex + " type " + type, e);
      } finally {
          typesOrIndexUpgrading.remove(indexAlias);
      }
    }
  }
}

package org.exoplatform.addons.es.index.impl;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.addons.es.dao.IndexingOperationDAO;
import org.exoplatform.addons.es.domain.IndexingOperation;
import org.exoplatform.addons.es.domain.OperationType;
import org.exoplatform.addons.es.index.IndexingService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 7/22/15
 */
public class QueueIndexingService implements IndexingService {

  private static final Log LOG = ExoLogger.getExoLogger(QueueIndexingService.class);

  private final IndexingOperationDAO indexingOperationDAO;

  public QueueIndexingService(IndexingOperationDAO indexingOperationDAO) {
    this.indexingOperationDAO = indexingOperationDAO;
  }

  public void init(String connectorName) {
    addToIndexingQueue(connectorName, null, OperationType.INIT);
  }

  public void index(String connectorName, String id) {
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("Id is null");
    }
    addToIndexingQueue(connectorName, id, OperationType.CREATE);
  }

  public void reindex(String connectorName, String id) {
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("Id is null");
    }
    addToIndexingQueue(connectorName, id, OperationType.UPDATE);
  }

  public void unindex(String connectorName, String id) {
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("Id is null");
    }
    addToIndexingQueue(connectorName, id, OperationType.DELETE);
  }

  public void reindexAll(String connectorName) {
    addToIndexingQueue(connectorName, null, OperationType.REINDEX_ALL);
  }

  public void unindexAll(String connectorName) {
    addToIndexingQueue(connectorName, null, OperationType.DELETE_ALL);
  }

  /**
   * Add a new operation to the create queue
   * @param connectorName Name of the connector
   * @param entityId id of the document
   * @param operation operation to the create {create, update, delete, init}
   * @LevelAPI Experimental
   */
  private void addToIndexingQueue(String connectorName, String entityId, OperationType operation) {
    if (operation==null) {
      throw new IllegalArgumentException("Operation cannot be null");
    }
    /*
    if (!getConnectors().containsKey(connectorName)) {
      throw new IllegalStateException("Connector ["+connectorName+"] has not been registered.");
    }*/
    switch (operation) {
      //A new type of document need to be initialise
      case INIT: indexingOperationDAO.create(getIndexingOperation(connectorName, OperationType.INIT, entityId));
        break;
      //A new entity need to be indexed
      case CREATE: indexingOperationDAO.create(getIndexingOperation(connectorName, OperationType.CREATE, entityId));
        break;
      //An existing entity need to be updated in the create
      case UPDATE: indexingOperationDAO.create(getIndexingOperation(connectorName, OperationType.UPDATE, entityId));
        break;
      //An existing entity need to be deleted from the create
      case DELETE: indexingOperationDAO.create(getIndexingOperation(connectorName, OperationType.DELETE, entityId));
        break;
      //All entities of a specific type need to be deleted
      case DELETE_ALL: indexingOperationDAO.create(getIndexingOperation(connectorName, OperationType.DELETE_ALL, entityId));
        break;
      //All entities of a specific type need to be reindexed
      case REINDEX_ALL: indexingOperationDAO.create(getIndexingOperation(connectorName, OperationType.REINDEX_ALL, entityId));
        break;
      default:
        throw new IllegalArgumentException(operation+" is not an accepted operation for the Indexing Queue");
    }
  }

  private IndexingOperation getIndexingOperation (String connector, OperationType operation, String entityId) {
    IndexingOperation indexingOperation = new IndexingOperation();
    indexingOperation.setEntityType(connector);
    indexingOperation.setOperation(operation);
    if (entityId != null) indexingOperation.setEntityId(entityId);
    return indexingOperation;
  }

  /**
   * Clear the indexQueue
   * @LevelAPI Experimental
   */
  public void clearIndexingQueue() {
    indexingOperationDAO.deleteAll();
  }

  /**
   * get the number of operations in indexQueue
   * @LevelAPI Experimental
   */
  public Long getNumberOperations() {
    return indexingOperationDAO.count();
  }

  public List<IndexingOperation> getOperations(int offset, int limit) {
    return indexingOperationDAO.findAll(offset, limit);
  }

  public IndexingOperation getOperation(String operationId) {
    return indexingOperationDAO.find(Long.getLong(operationId));
  }

  public void deleteAllOperations() {
    indexingOperationDAO.deleteAll();
  }

  public void deleteOperation(IndexingOperation indexingOperation) {
    indexingOperationDAO.delete(indexingOperation);
  }
}

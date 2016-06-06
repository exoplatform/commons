package org.exoplatform.addons.es.index;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * 10/12/15
 */
public interface IndexingService {

  /**
   * Add a init operation to the indexing queue to init the index
   * @param connectorName Name of the connector
   * @LevelAPI Experimental
   */
  void init(String connectorName);

  /**
   * Add a create operation to the indexing queue
   * @param connectorName Name of the connector
   * @param id id of the document
   * @LevelAPI Experimental
   */
  void index(String connectorName, String id);

  /**
   * Add a update operation to the indexing queue
   * @param connectorName Name of the connector
   * @param id id of the document
   * @LevelAPI Experimental
   */
  void reindex(String connectorName, String id);

  /**
   * Add a delete operation to the indexing queue
   * @param connectorName Name of the connector
   * @param id id of the document
   * @LevelAPI Experimental
   */
  void unindex(String connectorName, String id);

  /**
   * Add a reindex all operation to the indexing queue
   * @param connectorName Name of the connector
   * @LevelAPI Experimental
   */
  void reindexAll(String connectorName);

  /**
   * Add a delete all type operation to the indexing queue
   * @param connectorName Name of the connector
   * @LevelAPI Experimental
   */
  void unindexAll(String connectorName);

}
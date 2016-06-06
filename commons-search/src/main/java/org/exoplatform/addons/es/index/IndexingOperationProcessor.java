package org.exoplatform.addons.es.index;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * 10/12/15
 */
public abstract class IndexingOperationProcessor {

  private static final Log LOG = ExoLogger.getExoLogger(IndexingOperationProcessor.class);

  private static final ThreadLocal<String> CURRENT_TENANT_NAME = new ThreadLocal<>();

  private Map<String, IndexingServiceConnector> connectors = new HashMap<String, IndexingServiceConnector>();

  public static String getCurrentTenantName() {
    return CURRENT_TENANT_NAME.get();
  }

  public static void setCurrentTenantName(String tenantName) {
    CURRENT_TENANT_NAME.set(tenantName);
  }

  /**
   * Add Indexing Connector to the service
   * @param indexingServiceConnector the indexing connector to add
   * @LevelAPI Experimental
   */
  public void addConnector (IndexingServiceConnector indexingServiceConnector) {
    addConnector(indexingServiceConnector, false);
  }

  /**
   * Add Indexing Connector to the service
   * @param indexingServiceConnector the indexing connector to add
   * @param override equal true if we can override an existing connector, false otherwise
   * @LevelAPI Experimental
   */
  public void addConnector (IndexingServiceConnector indexingServiceConnector, Boolean override) {
    if (connectors.containsKey(indexingServiceConnector.getType()) && override.equals(false)) {
      LOG.error("Impossible to add connector {}. A connector with the same name has already been registered.",
          indexingServiceConnector.getType());
    } else {
      connectors.put(indexingServiceConnector.getType(), indexingServiceConnector);
      LOG.info("An Indexing Connector has been added: {}", indexingServiceConnector.getType());
    }
  }

  /**
   * Gets all current connectors
   * @return Connectors
   * @LevelAPI Experimental
   */
  public Map<String, IndexingServiceConnector> getConnectors() {
    return connectors;
  }

  /**
   * Index all document in the indexing queue
   * @LevelAPI Experimental
   */
  public abstract void process();

}

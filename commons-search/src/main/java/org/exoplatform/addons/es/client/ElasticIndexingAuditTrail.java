package org.exoplatform.addons.es.client;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 * 10/28/15
 */
public class ElasticIndexingAuditTrail {
  public static final String REINDEX_ALL  = "reindex_all";
  public static final String DELETE_ALL   = "delete_all";
  public static final String CREATE_INDEX = "create_index";
  public static final String CREATE_TYPE  = "create_type";
  public static final String DELETE_TYPE  = "delete_type";
  public static final String SEARCH_TYPE  = "search_type";

  private static final Log   AUDIT_TRAIL  = ExoLogger.getExoLogger("org.exoplatform.es.audittrail");

  private static final char  SEPARATOR    = ';';
  private static final String LOG_PATTERN = "{}"+StringUtils.repeat(SEPARATOR+"{}",6);

  static boolean isError(Integer httpStatusCode) {
    return (httpStatusCode != null) && ((httpStatusCode < 200) || (httpStatusCode > 299));
  }

  public void audit(String action,
                    String entityId,
                    String index,
                    String type,
                    Integer httpStatusCode,
                    String message,
                    long executionTime) {
    if (isError(httpStatusCode)) {
      logError(action, entityId, index, type, httpStatusCode, message, executionTime);
    } else {
      logInfo(action, entityId, index, type, httpStatusCode, message, executionTime);
    }
  }

  public void logRejectedDocumentBulkOperation(String action,
                                               String entityId,
                                               String index,
                                               String type,
                                               Integer httpStatusCode,
                                               String message,
                                               long executionTime) {
    logError(action, entityId, index, type, httpStatusCode, message, executionTime);
  }

  public boolean isFullLogEnabled() {
    return AUDIT_TRAIL.isDebugEnabled();
  }

  public void logAcceptedBulkOperation(String action,
                                       String entityId,
                                       String index,
                                       String type,
                                       Integer httpStatusCode,
                                       String message,
                                       long executionTime) {
    logDebug(action, entityId, index, type, httpStatusCode, message, executionTime);
  }

  public void logRejectedSearchOperation(String action,
                                            String index,
                                            String type,
                                            Integer httpStatusCode,
                                            String message,
                                            long executionTime) {
    logError(action, null, index, type, httpStatusCode, message, executionTime);
  }

  public void logAcceptedSearchOperation(String action,
                                       String index,
                                       String type,
                                       Integer httpStatusCode,
                                       String message,
                                       long executionTime) {
    logDebug(action, null, index, type, httpStatusCode, message, executionTime);
  }

  private void logInfo(String action,
                       String entityId,
                       String index,
                       String type,
                       Integer httpStatusCode,
                       String message,
                       long executionTime) {
    AUDIT_TRAIL.info(LOG_PATTERN,
        action,
        StringUtils.isBlank(entityId) ? "" : escape(entityId),
        StringUtils.isBlank(index) ? "" : escape(index),
        StringUtils.isBlank(type) ? "" : escape(type),
        httpStatusCode == null ? "" : httpStatusCode,
        StringUtils.isBlank(message) ? "" : escape(message),
        executionTime);
  }

  private void logError(String action,
                        String entityId,
                        String index,
                        String type,
                        Integer httpStatusCode,
                        String message,
                        long executionTime) {
    AUDIT_TRAIL.error(LOG_PATTERN,
        action,
        StringUtils.isBlank(entityId) ? "" : escape(entityId),
        StringUtils.isBlank(index) ? "" : escape(index),
        StringUtils.isBlank(type) ? "" : escape(type),
        httpStatusCode == null ? "" : httpStatusCode,
        StringUtils.isBlank(message) ? "" : escape(message),
        executionTime);
  }

  private void logDebug(String action,
                        String entityId,
                        String index,
                        String type,
                        Integer httpStatusCode,
                        String message,
                        long executionTime) {
    AUDIT_TRAIL.debug(LOG_PATTERN,
        action,
        StringUtils.isBlank(entityId) ? "" : escape(entityId),
        StringUtils.isBlank(index) ? "" : escape(index),
        StringUtils.isBlank(type) ? "" : escape(type),
        httpStatusCode == null ? "" : httpStatusCode,
        StringUtils.isBlank(message) ? "" : escape(message),
        executionTime);
  }

  private String escape(String message) {
    if (message == null) {
      return null;
    }
    return message.replace(SEPARATOR, ',');
  }
}

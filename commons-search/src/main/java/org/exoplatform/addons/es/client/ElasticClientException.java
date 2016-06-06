package org.exoplatform.addons.es.client;

/**
 * Generic exception in case of error while sending requests to ElasticSearch
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 * 9/11/15
 */
public class ElasticClientException extends RuntimeException {

  public ElasticClientException(Throwable cause) {
    super(cause);
  }

  public ElasticClientException(String message) {
    super(message);
  }

  public ElasticClientException(String message, Throwable cause) {
    super(message, cause);
  }
}

package org.exoplatform.addons.es.client;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 * exo@exoplatform.com
 * 10/28/15
 */
public class ElasticResponse {
  private final String message;
  private final int statusCode;

  public ElasticResponse(String message, int statusCode) {
    this.message = message;
    this.statusCode = statusCode;
  }

  public String getMessage() {
    return message;
  }

  public int getStatusCode() {
    return statusCode;
  }
}

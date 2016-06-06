package org.exoplatform.addons.es.client;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 * exo@exoplatform.com
 * 10/28/15
 */
public class ElasticClientAuthenticationException extends ElasticClientException {
  ElasticClientAuthenticationException() {
    super("Authentication Required");
  }
}

package org.exoplatform.commons.search.es;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 * exo@exoplatform.com
 * 9/14/15
 */
public class ElasticSearchException extends RuntimeException {
    public ElasticSearchException(String message, Throwable cause) {
        super(message, cause);
    }
}

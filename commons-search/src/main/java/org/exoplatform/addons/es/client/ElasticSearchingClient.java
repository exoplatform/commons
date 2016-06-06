package org.exoplatform.addons.es.client;

import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 * 9/14/15
 */
public class ElasticSearchingClient extends ElasticClient {
  private static final Log LOG = ExoLogger.getLogger(ElasticSearchingClient.class);

  private static final String ES_SEARCH_CLIENT_PROPERTY_NAME = "exo.es.search.server.url";
  private static final String ES_SEARCH_CLIENT_PROPERTY_USERNAME = "exo.es.search.server.username";
  private static final String ES_SEARCH_CLIENT_PROPERTY_PASSWORD = "exo.es.search.server.password";


  public ElasticSearchingClient(ElasticIndexingAuditTrail auditTrail) {
    super(auditTrail);
    //Get url client from exo global properties
    if (StringUtils.isNotBlank(PropertyManager.getProperty(ES_SEARCH_CLIENT_PROPERTY_NAME))) {
      this.urlClient = PropertyManager.getProperty(ES_SEARCH_CLIENT_PROPERTY_NAME);
      LOG.info("Using {} as Searching URL", this.urlClient);
    } else {
      LOG.info("Using default as Searching URL");
    }
  }



  public String sendRequest(String esQuery, String index, String type) {
    long startTime = System.currentTimeMillis();
    StringBuilder url = new StringBuilder();
    url.append(urlClient);
    if (StringUtils.isNotBlank(index)) {
      url.append("/" + index);
      if (StringUtils.isNotBlank(type)) url.append("/" + type);
    }
    url.append("/_search");
    ElasticResponse elasticResponse = sendHttpPostRequest(url.toString(), esQuery);
    String response = elasticResponse.getMessage();
    int statusCode = elasticResponse.getStatusCode();
    if (ElasticIndexingAuditTrail.isError(statusCode)) {
      auditTrail.logRejectedSearchOperation(ElasticIndexingAuditTrail.SEARCH_TYPE, index, type, statusCode, response, (System.currentTimeMillis() - startTime));
    }
    else {
      if (auditTrail.isFullLogEnabled()) {
        auditTrail.logAcceptedSearchOperation(ElasticIndexingAuditTrail.SEARCH_TYPE, index, type, statusCode, response, (System.currentTimeMillis() - startTime));
      }
    }
    return response;
  }

  @Override
  protected String getEsUsernameProperty() {
    return PropertyManager.getProperty(ES_SEARCH_CLIENT_PROPERTY_USERNAME);
  }

  @Override
  protected String getEsPasswordProperty() {
    return PropertyManager.getProperty(ES_SEARCH_CLIENT_PROPERTY_PASSWORD);
  }

  @Override
  protected ClientConnectionManager getClientConnectionManager() {
    return new ThreadSafeClientConnManager();
  }

}

package org.exoplatform.commons.api.search.data;

import org.exoplatform.web.controller.router.Router;

/**
 * Search context contains context information needed for SearchService and its connectors
 *  
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Feb 22, 2013  
 */
public class SearchContext {
  private Router router; // Gatein router, provides routing information for building resource URLs
  
  public Router getRouter() {
    return router;
  }

  public void setRouter(Router router) {
    this.router = router;
  }

  public SearchContext(Router router) {
    this.router = router;
  }
}

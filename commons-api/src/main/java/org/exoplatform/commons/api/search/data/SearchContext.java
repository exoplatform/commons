package org.exoplatform.commons.api.search.data;

import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.router.Router;
import org.exoplatform.services.log.Log; 

/**
 * Search context contains context information needed for SearchService and its connectors
 *  
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Feb 22, 2013  
 */
public class SearchContext {
  
  private static Log log = ExoLogger.getExoLogger(SearchContext.class);
  
  public static enum RouterParams {
   SITE_TYPE("sitetype"),
   SITE_NAME("sitename"),
   HANDLER("handler"),
   PATH("path"),
   LANG("lang");
   
   private final static String PREFIX = "gtn";
   private String paramName = null;
   
   /**
    * constructor with paramName
    * @param paramName
    */
   private RouterParams(String paramName) {
     this.paramName = paramName;
   }
   
   public QualifiedName create() {
     return QualifiedName.create(PREFIX, paramName);
   }
   
  };
  /** */
  private Router router; // Gatein router, provides routing information for building resource URLs
  
  /** */
  private Map<QualifiedName, String> qualifiedName = null;
  
  public Router getRouter() {
    return router;
  }

  public void setRouter(Router router) {
    this.router = router;
  }

  public SearchContext(Router router) {
    this.router = router;
    qualifiedName = new HashedMap();
  }
  
  /**
   * puts Handler value into QualifiedName map
   * @param value
   * @return
   */
  public SearchContext handler(String value) {
    qualifiedName.put(RouterParams.HANDLER.create(), value);
    return this;
  }
  
  /**
   * puts Lang value into QualifiedName map
   * @param value
   * @return
   */
  public SearchContext lang(String value) {
    qualifiedName.put(RouterParams.LANG.create(), value);
    return this;
  }
  
  /**
   * puts Path value into QualifiedName map
   * @param value
   * @return
   */
  public SearchContext path(String value) {
    qualifiedName.put(RouterParams.PATH.create(), value);
    return this;
  }
  
  /**
   * puts SiteType value into QualifiedName map
   * @param value
   * @return
   */
  public SearchContext siteType(String value) {
    qualifiedName.put(RouterParams.SITE_TYPE.create(), value);
    return this;
  }
  
  /**
   * puts SiteType value into QualifiedName map
   * @param value
   * @return
   */
  public SearchContext siteName(String value) {
    qualifiedName.put(RouterParams.SITE_NAME.create(), value);
    return this;
  }
  
  /**
   * render link base on router and Map<QualifiedName, String>
   * @return
   * @throws Exception
   */
  public String renderLink() throws Exception {
    //
    if (qualifiedName.containsKey(RouterParams.LANG.create()) == false) {
      lang("");
    }
    
    //
    if (qualifiedName.containsKey(RouterParams.HANDLER.create()) == false) {
      log.warn("Handler of QualifiedName not found!");
    }
    
    //
    if (qualifiedName.containsKey(RouterParams.SITE_NAME.create()) == false) {
      log.warn("SiteName of QualifiedName not found!");
    }
    
    //
    if (qualifiedName.containsKey(RouterParams.SITE_TYPE.create()) == false) {
      log.warn("SiteType of QualifiedName not found!");
    }
    
    //
    return router.render(qualifiedName);
  }
  
}

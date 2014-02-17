package org.exoplatform.commons.api.search.data;

import org.apache.commons.collections.map.HashedMap;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.router.Router;

import java.util.Map;

/**
 * Search Context contains a set of data needed for SearchService and all connectors.
 *  
 * @LevelAPI Experimental  
 */
public class SearchContext {
  
  private static Log LOG = ExoLogger.getExoLogger(SearchContext.class);
  
  public static enum RouterParams {
   SITE_TYPE("sitetype"),
   SITE_NAME("sitename"),
   HANDLER("handler"),
   PATH("path"),
   LANG("lang");
   
   private final static String PREFIX = "gtn";
   private String paramName = null;
   
   /**
    * Constructor with paramName
    * @param paramName
    * @LevelAPI Experimental
    */
   private RouterParams(String paramName) {
     this.paramName = paramName;
   }
   
   /**
    * Create qualified name
    * @LevelAPI Experimental
    * @return QualifiedName
    * @LevelAPI Experimental 
    */
   public QualifiedName create() {
     return QualifiedName.create(PREFIX, paramName);
   }
   
  };
  /** */
  private Router router; // Gatein router, provides routing information for building resource URLs
  
  /** */
  private String siteName;
  
  /** */
  private Map<QualifiedName, String> qualifiedName = null;
  
  /**
   * Get router
   * @return Router
   * @LevelAPI Experimental 
   */
  public Router getRouter() {
    return router;
  }

  /**
   * Set router
   * @param router
   * @LevelAPI Experimental 
   */
  public void setRouter(Router router) {
    this.router = router;
  }

  /**
   * Get site name, e.g. intranet, acme, ..
   * @return String
   * @LevelAPI Experimental
   */
  public String getSiteName() {
    return siteName;
  }

  /**
   * Contructor to create a context for search service
   * @param router
   * @param siteName
   * @return SearchContext
   * @LevelAPI Experimental 
   */
  public SearchContext(Router router, String siteName) {
    this.router = router;
    this.siteName = siteName;
    qualifiedName = new HashedMap();
  }
  
  /**
   * Puts Handler value into QualifiedName map
   * @param value
   * @return SearchContext
   * @LevelAPI Experimental  
   */
  public SearchContext handler(String value) {
    qualifiedName.put(RouterParams.HANDLER.create(), value);
    return this;
  }
  
  /**
   * Puts Lang value into QualifiedName map
   * @param value
   * @return SearchContext
   * @LevelAPI Experimental 
   */
  public SearchContext lang(String value) {
    qualifiedName.put(RouterParams.LANG.create(), value);
    return this;
  }
  
  /**
   * Puts Path value into QualifiedName map
   * @param value
   * @return SearchContext
   * @LevelAPI Experimental
   */
  public SearchContext path(String value) {
    qualifiedName.put(RouterParams.PATH.create(), value);
    return this;
  }
  
  /**
   * Puts SiteType value into QualifiedName map
   * @param value
   * @return SearchContext
   * @LevelAPI Experimental
   */
  public SearchContext siteType(String value) {
    qualifiedName.put(RouterParams.SITE_TYPE.create(), value);
    return this;
  }
  
  /**
   * Puts SiteType value into QualifiedName map
   * @param value
   * @return SearchContext
   * @LevelAPI Experimental
   */
  public SearchContext siteName(String value) {
    qualifiedName.put(RouterParams.SITE_NAME.create(), value);
    return this;
  }
  
  /**
   * Render link base on router and Map<QualifiedName, String>
   * @return String
   * @throws Exception
   * @LevelAPI Experimental
   */
  public String renderLink() throws Exception {
    //
    if (qualifiedName.containsKey(RouterParams.LANG.create()) == false) {
      lang("");
    }
    
    //
    if (qualifiedName.containsKey(RouterParams.HANDLER.create()) == false) {
      LOG.warn("Handler of QualifiedName not found!");
    }
    
    //
    if (qualifiedName.containsKey(RouterParams.SITE_NAME.create()) == false) {
      LOG.warn("SiteName of QualifiedName not found!");
    }
    
    //
    if (qualifiedName.containsKey(RouterParams.SITE_TYPE.create()) == false) {
      LOG.warn("SiteType of QualifiedName not found!");
    }
    
    //
    return router.render(qualifiedName);
  }
  
}

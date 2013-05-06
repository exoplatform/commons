package org.exoplatform.commons.api.search;

import java.util.Collection;

import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;

/**
 * This abstract class is extended by all SearchService connectors, it allow to build from configuration the list of the needed connector for the Unified Search.
 * 
 */
public abstract class SearchServiceConnector extends BaseComponentPlugin {
  private String searchType; //search type name
  private String displayName; //for use when rendering
  
  /**
   * Get search type
   * @return String
   * @LevelAPI Experimental
   */
  public String getSearchType() {
    return searchType;
  }

  /**
   * Set search type
   * @param searchType
   * @LevelAPI Experimental
   */
  public void setSearchType(String searchType) {
    this.searchType = searchType;
  }

  /**
   * Get display name
   * @return String
   * @LevelAPI Experimental
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Set display name
   * @param displayName
   * @LevelAPI Experimental
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
  /**
   * Constructor is default that other connecters must implement by this way  
   * @param initParams parameters are initialized from configuration
   * @LevelAPI Experimental
   */
  public SearchServiceConnector(InitParams initParams) {
    PropertiesParam param = initParams.getPropertiesParam("constructor.params");
    this.searchType = param.getProperty("searchType");
    this.displayName = param.getProperty("displayName");
  }

  /**
   * The connectors must implement this search method, with the following parameters and return a collection of SearchResult
   * @param context Search context
   * @param query The user-input query to search for
   * @param sites Search on these specified sites only (e.g acme, intranet...)
   * @param offset Start offset of the result set
   * @param limit Maximum size of the result set 
   * @param sort The field to sort the result set 
   * @param order Sort order (ASC, DESC)
   * @return a collection of SearchResult
   * @LevelAPI Experimental 
   */
  public abstract Collection<SearchResult> search(SearchContext context, String query, Collection<String> sites, int offset, int limit, String sort, String order);
}

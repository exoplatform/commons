package org.exoplatform.commons.api.search;

import java.util.Collection;

import org.exoplatform.commons.api.search.data.SearchResult;

/**
 * This abstract class is extended by the SearchService connectors which provide search result for a specific content type
 * 
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Nov 21, 2012  
 */
public abstract class SearchServiceConnector {
  /**
   * The connectors must implement this search method, with the following parameters and return a collection of SearchResult
   * @param query The user-input query to search for
   * @param sites Search on these specified sites only (e.g acme, intranet...)
   * @param offset Start offset of the result set
   * @param limit Maximum size of the result set 
   * @param sort The field to sort the result set 
   * @param order Sort order (ASC, DESC)
   * @return a collection of SearchResult
   */
  public abstract Collection<SearchResult> search(String query, Collection<String> sites, int offset, int limit, String sort, String order);
}

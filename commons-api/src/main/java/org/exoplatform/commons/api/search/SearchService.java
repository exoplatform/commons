/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.api.search;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;

/** 
 * These class defines the Search API that a driver can implement and provides add/get methods on the connector.  
 * @LevelAPI Experimental 
 */
public abstract class SearchService {
  private LinkedList<SearchServiceConnector> connectors = new LinkedList<SearchServiceConnector>();
  
  /**
   * Get all connectors currently
   * @return Collection of connectors
   * @LevelAPI Experimental 
   */
  public LinkedList<SearchServiceConnector> getConnectors() {
    return connectors;
  }

  /**
   * Add a connector that implemented search API
   * @param connector
   * @LevelAPI Experimental 
   */
  public void addConnector(SearchServiceConnector connector) {
    connectors.add(connector);
  }
  
  /**
   * This search method aggregates search results from all connectors
   * @param context Search context
   * @param query The user-input query to search for
   * @param sites Search on these specified sites only (e.g acme, intranet...)
   * @param types Search for these specified content types only (e.g people, discussion, event, task, wiki, activity, social, file, document...)
   * @param offset Start offset of the result set
   * @param limit Maximum size of the result set 
   * @param sort The field to sort the result set 
   * @param order Sort order (ASC, DESC)
   * @return a map of connector with their search result
   * @LevelAPI Experimental 
   */
  public abstract Map<String, Collection<SearchResult>> search(SearchContext context, String query, Collection<String> sites, Collection<String> types, int offset, int limit, String sort, String order);  
}

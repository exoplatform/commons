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
 * Defines a Search API that a driver can implement, and provides the add/get methods to the connector.  
 */
public abstract class SearchService {
  private LinkedList<SearchServiceConnector> connectors = new LinkedList<SearchServiceConnector>();
  
  /**
   * Gets all current connectors.
   * @return Connectors.
   * @LevelAPI Experimental 
   */
  public LinkedList<SearchServiceConnector> getConnectors() {
    return connectors;
  }

  /**
   * Adds a connector which is implemented by the Search API.
   * @param connector The connector to be added.
   * @LevelAPI Experimental 
   */
  public void addConnector(SearchServiceConnector connector) {
    connectors.add(connector);
  }
  
  /**
   * Aggregates search results from all connectors.
   * @param context The search context.
   * @param query The query statement.
   * @param sites Specified sites where the search is performed (for example, Acme, or Intranet).
   * @param types Specified types by which the search is performed (for example, people, discussion, event, task, wiki, activity, social, file, document).
   * @param offset The start point from which the search results are returned.
   * @param limit The limitation number of search results.
   * @param sort The sorting criteria (title, relevancy and date).
   * @param order The sorting order (ascending and descending).
   * @return A map of connectors with search results.
   * @LevelAPI Experimental 
   */
  public abstract Map<String, Collection<SearchResult>> search(SearchContext context, String query, Collection<String> sites, Collection<String> types, int offset, int limit, String sort, String order);  
}

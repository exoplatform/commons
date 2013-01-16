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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.api.search.data.SearchResult;

/**
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Nov 21, 2012  
 */
public abstract class SearchService {
  private List<SearchServiceConnector> connectors = new ArrayList<SearchServiceConnector>();
    
  public List<SearchServiceConnector> getConnectors() {
    return connectors;
  }

  public void addConnector(SearchServiceConnector connector) {
    connectors.add(connector);
  }
  
  /**
   * This search method aggregates search results from all connectors
   * @param query The user-input query to search for
   * @param sites Search on these specified sites only (e.g acme, intranet...)
   * @param types Search for these specified content types only (e.g people, discussion, event, task, wiki, activity, social, file, document...)
   * @param offset Start offset of the result set
   * @param limit Maximum size of the result set 
   * @param sort The field to sort the result set 
   * @param order Sort order (ASC, DESC)
   * @return a map of connector with their search result
   */
  public abstract Map<String, Collection<SearchResult>> search(String query, Collection<String> sites, Collection<String> types, int offset, int limit, String sort, String order);  
}
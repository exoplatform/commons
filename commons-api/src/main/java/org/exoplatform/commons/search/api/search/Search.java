package org.exoplatform.commons.search.api.search;

import java.util.Collection;

import org.exoplatform.commons.search.api.search.data.SearchResult;

public interface Search {
  public Collection<SearchResult> search(String query, Collection<String> sites, Collection<String> types, int offset, int limit, String sort, String order);
}

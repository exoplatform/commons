package org.exoplatform.commons.search;

import java.util.Collection;

public interface Search {
  public Collection<SearchResult> search(String query, Collection<String> sites, Collection<String> types, int offset, int limit, String sort, String order);
}

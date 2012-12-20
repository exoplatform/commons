package org.exoplatform.commons.search;

import java.util.Collection;

public interface Search {
  public Collection<SearchResult> search(String query);
}

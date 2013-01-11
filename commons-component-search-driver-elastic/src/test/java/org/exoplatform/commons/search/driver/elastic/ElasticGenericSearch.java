package org.exoplatform.commons.search.driver.elastic;

import java.util.Collection;

import org.exoplatform.commons.api.search.Search;
import org.exoplatform.commons.api.search.data.SearchResult;

public class ElasticGenericSearch implements Search {
  @Override
  public Collection<SearchResult> search(String query, Collection<String> sites, Collection<String> types, int offset, int limit, String sort, String order) {
    return ElasticSearchService.search(query);
  }
}

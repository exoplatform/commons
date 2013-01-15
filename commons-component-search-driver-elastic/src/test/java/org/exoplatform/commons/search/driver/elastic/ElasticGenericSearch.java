package org.exoplatform.commons.search.driver.elastic;

import java.util.Collection;

import org.exoplatform.commons.api.search.SearchServiceConnector;
import org.exoplatform.commons.api.search.data.SearchResult;

public class ElasticGenericSearch extends SearchServiceConnector {
  @Override
  public Collection<SearchResult> search(String query, Collection<String> sites, Collection<String> types, int offset, int limit, String sort, String order) {
    return ElasticSearchService.search(query);
  }
}

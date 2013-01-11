package org.exoplatform.commons.search.driver.solr;

import java.util.Collection;

import org.exoplatform.commons.search.api.search.Search;
import org.exoplatform.commons.search.api.search.data.SearchResult;

public class SolrGenericSearch implements Search {

  @Override
  public Collection<SearchResult> search(String query, Collection<String> sites, Collection<String> types, int offset, int limit, String sort, String order) {
    return SolrSearchService.search(query);
  }

}

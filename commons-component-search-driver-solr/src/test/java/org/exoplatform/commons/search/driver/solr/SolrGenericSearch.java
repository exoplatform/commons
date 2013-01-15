package org.exoplatform.commons.search.driver.solr;

import java.util.Collection;

import org.exoplatform.commons.api.search.SearchServiceConnector;
import org.exoplatform.commons.api.search.data.SearchResult;

public class SolrGenericSearch extends SearchServiceConnector {

  @Override
  public Collection<SearchResult> search(String query, Collection<String> sites, int offset, int limit, String sort, String order) {
    return SolrSearchService.search(query);
  }

}

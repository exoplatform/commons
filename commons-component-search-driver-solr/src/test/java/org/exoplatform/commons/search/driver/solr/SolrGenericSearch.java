package org.exoplatform.commons.search.driver.solr;

import java.util.Collection;

import org.exoplatform.commons.search.Search;
import org.exoplatform.commons.search.SearchResult;

public class SolrGenericSearch implements Search {

  @Override
  public Collection<SearchResult> search(String query) {
    return SolrSearchService.search(query);
  }

}

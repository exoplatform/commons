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
package org.exoplatform.commons.search.driver.solr;


import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.commons.search.sample.BaseTest;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Nov 21, 2012  
 */
public class SolrTest extends BaseTest  {
  private final static Log LOG = ExoLogger.getLogger(SolrTest.class);
  private static final String SOLR_HOME = "src/test/resources/solr";
  SolrServer server;

  public Map<String, Collection<SearchResult>> search(String query, Collection<String> sites, Collection<String> types, int offset, int limit, String sort, String order) {
    Map<String, Collection<SearchResult>> results = new HashMap<String, Collection<SearchResult>>();
    try {
      Collection<SearchResult> result = new SolrGenericSearch().search(query, sites, offset, limit, sort, order);
      results.put("Solr generic search", result);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
    return results;
  }

  private void search(String queryString){
    LOG.info("\n====================================\nSearching for '" + queryString + "' (categorized)...\nResults:");
    Map<String, Collection<SearchResult>> result = search(queryString, null, null, 0, 0, "", "");
    
    Iterator<String> iter = result.keySet().iterator();
    while(iter.hasNext()){
      String searchType = iter.next();
      LOG.info("\n" + searchType + ":");
      Collection<SearchResult> entries = result.get(searchType);
      Iterator<SearchResult> entriesIter = entries.iterator();
      while(entriesIter.hasNext()){
        LOG.info(" * " + entriesIter.next());
      }
    }    
  }
  

  @Override
  public void setUp() throws Exception {
    System.setProperty("solr.solr.home", SOLR_HOME);
    CoreContainer.Initializer initializer = new CoreContainer.Initializer();
    CoreContainer coreContainer = initializer.initialize();
    server = new EmbeddedSolrServer(coreContainer, "");
    //server = new HttpSolrServer("http://localhost:8983/solr");
    
    indexingService = new SolrIndexingService(server);
    SolrSearchService.setServer(server);
//    SearchService.register(new SearchType("user", "User", null, SolrGenericSearch.class));
//    SearchService.register(new SearchType("topic", "Forum topic", null, SolrGenericSearch.class));
    super.setUp();
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    FileUtils.deleteDirectory(new File(SOLR_HOME + "/data")); // cleanup local Solr server's generated data
  }

  public void testSearch() throws Exception {
    search("\"anthony cena\" mary");
    search("creationAuthor_t:john"); //TODO: hide the underscore
  }
}

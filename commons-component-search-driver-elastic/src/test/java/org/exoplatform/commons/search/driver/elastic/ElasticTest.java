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
package org.exoplatform.commons.search.driver.elastic;


import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.exoplatform.commons.search.SearchEntry;
import org.exoplatform.commons.search.SearchEntryId;
import org.exoplatform.commons.search.SearchService;
import org.exoplatform.commons.search.SearchType;
import org.exoplatform.commons.search.sample.BaseTest;
import org.exoplatform.commons.search.sample.UserSearchEntry;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Nov 21, 2012  
 */
public class ElasticTest extends BaseTest  {
  private final static Log LOG = ExoLogger.getLogger(ElasticTest.class);
  private Client client;

  private SearchEntry getEntry(SearchEntryId id) {
    GetRequest getRequest = new GetRequest(id.getCollection(), id.getType(), id.getName());
    GetResponse res = client.get(getRequest).actionGet();
    return new SearchEntry(res.getIndex(), res.getType(), res.getId(), res.getSource());
  }

  @Override
  protected void setUp() throws Exception {
    Node node = nodeBuilder().node();
    client = node.client();
    //client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("localhost", 9300));        
    indexingService = new ElasticIndexingService(client);
    ElasticSearchService.setClient(client);
    SearchService.register(new SearchType("user", "User", null, ElasticGenericSearch.class));
    SearchService.register(new SearchType("topic", "Forum topic", null, ElasticGenericSearch.class));
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    client.close();
    // cleanup local Elastic node's generated data
    try {
      FileUtils.deleteDirectory(new File("data"));
    } catch (IOException e) {
      Thread.sleep(3000);
      FileUtils.deleteDirectory(new File("data"));
    }
  }

  public void testUpdate(){
    System.out.println("Before: " + getEntry(UserSearchEntry.getEntryId("mary")));

    System.out.println("Updating lastname --> Jane ...");
    Map<String, Object> changes = new HashMap<String, Object>();
    changes.put("lastname", "Jane");
    indexingService.update(UserSearchEntry.getEntryId("mary"), changes);
    
    System.out.println("After: " + getEntry(UserSearchEntry.getEntryId("mary")));
  }

  public void testSearch(){
    // Full text search
    categorizedSearch("*");
    search("\"anthony cena\" mary");
    search("creationAuthor:john");
    search("elastic creationAuthor:john");

    // Search by type
    search("* type:user");
    search("* type:topic");
    search("type:topic");

    search("mary");
    search("mary type:user");
    search("mary type:topic");
    search("mary type:[user, topic]");
    search("mary type:user type:topic");
    search("mary type:[user] type:topic");
    
    // Meta search
    //TODO: search("#types"); //list all available types
  }
  
}

/* 
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/ .
 */
package org.exoplatform.addons.es.integration;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.exoplatform.addons.es.search.ElasticSearchServiceConnector;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by The eXo Platform SAS Author : Thibault Clement
 * tclement@exoplatform.com 9/11/15
 */
public class ElasticSearchingIntegrationTest extends BaseIntegrationTest {

  ElasticSearchServiceConnector elasticSearchServiceConnector;

  @Before
  public void initServices() {
    Identity identity = new Identity("BCH", Collections.singletonList(new MembershipEntry("Admin")));
    ConversationState.setCurrent(new ConversationState(identity));
    elasticSearchServiceConnector = new ElasticSearchServiceConnector(getInitConnectorParams(), elasticSearchingClient);
  }

  @Test
  public void testSearchingDocument() {

    // Given
    assertEquals(0, documentNumber());

    String mapping = "{ \"properties\" : " + "   {\"permissions\" : "
        + "       {\"type\" : \"string\", \"index\" : \"not_analyzed\"}" + "   }" + "}";
    CreateIndexRequestBuilder cirb = node.client().admin().indices().prepareCreate("test").addMapping("type1", mapping);
    cirb.execute().actionGet();
    String bulkRequest = "{ \"create\" : { \"_index\" : \"test\", \"_type\" : \"type1\", \"_id\" : \"1\" } }\n"
        + "{ \"field1\" : \"value1\", \"permissions\" : [\"BCH\"] }\n"
        + "{ \"create\" : { \"_index\" : \"test\", \"_type\" : \"type1\", \"_id\" : \"2\" } }\n"
        + "{ \"field1\" : \"value2\", \"permissions\" : [\"BCH\"] }\n"
        + "{ \"create\" : { \"_index\" : \"test\", \"_type\" : \"type1\", \"_id\" : \"3\" } }\n"
        + "{ \"field1\" : \"value3\", \"permissions\" : [\"BCH\"] }\n";
    elasticIndexingClient.sendCUDRequest(bulkRequest);
    // Elasticsearch has near real-time search: document changes are not visible
    // to search immediately,
    // but will become visible within 1 second
    node.client().admin().indices().prepareRefresh().execute().actionGet();

    // When
    List<SearchResult> searchResults = new ArrayList<>(elasticSearchServiceConnector.search(null,
                                                                                            "value1",
                                                                                            null,
                                                                                            0,
                                                                                            10,
                                                                                            null,
                                                                                            null));

    // Then
    assertEquals(1, searchResults.size());

  }

  @Test
  public void testExcerpt() throws InterruptedException {

    // Given
    assertEquals(0, documentNumber());
    String mapping = "{ \"properties\" : " + "   {\"permissions\" : "
        + "       {\"type\" : \"string\", \"index\" : \"not_analyzed\"}" + "   }" + "}";
    CreateIndexRequestBuilder cirb = node.client().admin().indices().prepareCreate("test").addMapping("type1", mapping);
    cirb.execute().actionGet();
    String bulkRequest = "{ \"create\" : { \"_index\" : \"test\", \"_type\" : \"type1\", \"_id\" : \"1\" } }\n"
        + "{ \"field1\" : \"value1\", \"permissions\" : [\"BCH\"] }\n"
        + "{ \"create\" : { \"_index\" : \"test\", \"_type\" : \"type1\", \"_id\" : \"2\" } }\n"
        + "{ \"field1\" : \"value2\", \"permissions\" : [\"BCH\"] }\n"
        + "{ \"create\" : { \"_index\" : \"test\", \"_type\" : \"type1\", \"_id\" : \"3\" } }\n"
        + "{ \"field1\" : \"value3\", \"permissions\" : [\"BCH\"] }\n";
    elasticIndexingClient.sendCUDRequest(bulkRequest);
    // Elasticsearch has near real-time search: document changes are not visible
    // to search immediately,
    // but will become visible within 1 second
    node.client().admin().indices().prepareRefresh().execute().actionGet();

    // When
    List<SearchResult> searchResults = new ArrayList<>(elasticSearchServiceConnector.search(null,
                                                                                            "value1",
                                                                                            null,
                                                                                            0,
                                                                                            10,
                                                                                            null,
                                                                                            null));

    // Then
    assertEquals("... <strong>value1</strong>", searchResults.get(0).getExcerpt());

  }

  @Test
  public void testSearchingDocument_NoTypeSpecify() {

    // Given
    assertEquals(0, documentNumber());

    elasticSearchServiceConnector = new ElasticSearchServiceConnector(getInitConnectorWithoutTypeParams(), elasticSearchingClient);

    //Create two types
    String mapping = "{ \"properties\" : " + "   {\"permissions\" : "
        + "       {\"type\" : \"string\", \"index\" : \"not_analyzed\"}" + "   }" + "}";
    CreateIndexRequestBuilder cirb = node.client().admin().indices().prepareCreate("test");
    cirb.execute().actionGet();

    node.client().admin().indices()
        .preparePutMapping("test")
        .setType("type1")
        .setSource(mapping)
        .execute().actionGet();

    node.client().admin().indices()
        .preparePutMapping("test")
        .setType("type2")
        .setSource(mapping)
        .execute().actionGet();

    String bulkRequest = "{ \"create\" : { \"_index\" : \"test\", \"_type\" : \"type1\", \"_id\" : \"1\" } }\n"
        + "{ \"field1\" : \"value1\", \"permissions\" : [\"BCH\"] }\n"
        + "{ \"create\" : { \"_index\" : \"test\", \"_type\" : \"type2\", \"_id\" : \"2\" } }\n"
        + "{ \"field1\" : \"value1\", \"permissions\" : [\"BCH\"] }\n"
        + "{ \"create\" : { \"_index\" : \"test\", \"_type\" : \"type1\", \"_id\" : \"3\" } }\n"
        + "{ \"field1\" : \"value3\", \"permissions\" : [\"BCH\"] }\n";
    elasticIndexingClient.sendCUDRequest(bulkRequest);
    // Elasticsearch has near real-time search: document changes are not visible
    // to search immediately,
    // but will become visible within 1 second
    node.client().admin().indices().prepareRefresh().execute().actionGet();

    // When
    List<SearchResult> searchResults = new ArrayList<>(elasticSearchServiceConnector.search(null,
        "value1",
        null,
        0,
        10,
        null,
        null));

    // Then
    //Must get two results: one from type1 and one from type2
    assertEquals(2, searchResults.size());

  }

  @Test
  public void testSearchingDocument_NoIndexSpecify() {

    // Given
    assertEquals(0, documentNumber());

    elasticSearchServiceConnector = new ElasticSearchServiceConnector(getInitConnectorWithoutIndexParams(), elasticSearchingClient);

    //Create two types
    String mapping = "{ \"properties\" : " + "   {\"permissions\" : "
        + "       {\"type\" : \"string\", \"index\" : \"not_analyzed\"}" + "   }" + "}";

    CreateIndexRequestBuilder cirb = node.client().admin().indices().prepareCreate("test").addMapping("type1", mapping);
    cirb.execute().actionGet();
    CreateIndexRequestBuilder cirb2 = node.client().admin().indices().prepareCreate("test2").addMapping("type1", mapping);
    cirb2.execute().actionGet();

    String bulkRequest = "{ \"create\" : { \"_index\" : \"test\", \"_type\" : \"type1\", \"_id\" : \"1\" } }\n"
        + "{ \"field1\" : \"value1\", \"permissions\" : [\"BCH\"] }\n"
        + "{ \"create\" : { \"_index\" : \"test2\", \"_type\" : \"type1\", \"_id\" : \"2\" } }\n"
        + "{ \"field1\" : \"value1\", \"permissions\" : [\"BCH\"] }\n"
        + "{ \"create\" : { \"_index\" : \"test\", \"_type\" : \"type1\", \"_id\" : \"3\" } }\n"
        + "{ \"field1\" : \"value3\", \"permissions\" : [\"BCH\"] }\n";
    elasticIndexingClient.sendCUDRequest(bulkRequest);
    // Elasticsearch has near real-time search: document changes are not visible
    // to search immediately,
    // but will become visible within 1 second
    node.client().admin().indices().prepareRefresh().execute().actionGet();

    // When
    List<SearchResult> searchResults = new ArrayList<>(elasticSearchServiceConnector.search(null,
        "value1",
        null,
        0,
        10,
        null,
        null));

    // Then
    //Must get two results: one from test index and one from test2 index
    assertEquals(2, searchResults.size());

  }

  @Test
  public void testSearchingDocument_NoIndexAndTypeSpecify() {

    // Given
    assertEquals(0, documentNumber());

    elasticSearchServiceConnector = new ElasticSearchServiceConnector(getInitConnectorWithoutIndexAndTypeParams(), elasticSearchingClient);

    //Create two types
    String mapping = "{ \"properties\" : " + "   {\"permissions\" : "
        + "       {\"type\" : \"string\", \"index\" : \"not_analyzed\"}" + "   }" + "}";

    CreateIndexRequestBuilder cirb = node.client().admin().indices().prepareCreate("test").addMapping("type1", mapping);
    cirb.execute().actionGet();
    CreateIndexRequestBuilder cirb2 = node.client().admin().indices().prepareCreate("test2").addMapping("type2", mapping);
    cirb2.execute().actionGet();

    String bulkRequest = "{ \"create\" : { \"_index\" : \"test\", \"_type\" : \"type1\", \"_id\" : \"1\" } }\n"
        + "{ \"field1\" : \"value1\", \"permissions\" : [\"BCH\"] }\n"
        + "{ \"create\" : { \"_index\" : \"test2\", \"_type\" : \"type2\", \"_id\" : \"2\" } }\n"
        + "{ \"field2\" : \"value1\", \"permissions\" : [\"BCH\"] }\n"
        + "{ \"create\" : { \"_index\" : \"test\", \"_type\" : \"type1\", \"_id\" : \"3\" } }\n"
        + "{ \"field1\" : \"value3\", \"permissions\" : [\"BCH\"] }\n";
    elasticIndexingClient.sendCUDRequest(bulkRequest);
    // Elasticsearch has near real-time search: document changes are not visible
    // to search immediately,
    // but will become visible within 1 second
    node.client().admin().indices().prepareRefresh().execute().actionGet();

    // When
    List<SearchResult> searchResults = new ArrayList<>(elasticSearchServiceConnector.search(null,
        "value1",
        null,
        0,
        10,
        null,
        null));

    // Then
    //Must get two results: one from type1 and one from type2
    assertEquals(2, searchResults.size());

  }

  private InitParams getInitConnectorParams() {
    InitParams params = new InitParams();
    PropertiesParam constructorParams = new PropertiesParam();
    constructorParams.setName("constructor.params");
    constructorParams.setProperty("searchType", "type1");
    constructorParams.setProperty("type", "type1");
    constructorParams.setProperty("displayName", "test");
    constructorParams.setProperty("index", "test");
    constructorParams.setProperty("searchFields", "field1");
    params.addParam(constructorParams);
    return params;
  }

  private InitParams getInitConnectorWithoutIndexParams() {
    InitParams params = new InitParams();
    PropertiesParam constructorParams = new PropertiesParam();
    constructorParams.setName("constructor.params");
    constructorParams.setProperty("searchType", "type1");
    constructorParams.setProperty("type", "type1");
    constructorParams.setProperty("displayName", "test");
    constructorParams.setProperty("searchFields", "field1,field2");
    params.addParam(constructorParams);
    return params;
  }

  private InitParams getInitConnectorWithoutTypeParams() {
    InitParams params = new InitParams();
    PropertiesParam constructorParams = new PropertiesParam();
    constructorParams.setName("constructor.params");
    constructorParams.setProperty("searchType", "type1");
    constructorParams.setProperty("displayName", "test");
    constructorParams.setProperty("index", "test");
    constructorParams.setProperty("searchFields", "field1");
    params.addParam(constructorParams);
    return params;
  }

  private InitParams getInitConnectorWithoutIndexAndTypeParams() {
    InitParams params = new InitParams();
    PropertiesParam constructorParams = new PropertiesParam();
    constructorParams.setName("constructor.params");
    constructorParams.setProperty("searchType", "type1");
    constructorParams.setProperty("displayName", "test");
    constructorParams.setProperty("searchFields", "field1,field2");
    params.addParam(constructorParams);
    return params;
  }

}

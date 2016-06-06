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

import org.apache.commons.codec.binary.Base64;
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

import static org.junit.Assert.*;

/**
 *
 */
public class ElasticIndexingAttachmentIntegrationTest extends BaseIntegrationTest {

  private final static String MC23Quotes =
      "Some people want it to happen, some wish it would happen, others make it happen.";

  private ElasticSearchServiceConnector elasticSearchServiceConnector;

  @Before
  public void initServices() {
    super.setup();

    Identity identity = new Identity("TCL", Collections.singletonList(new MembershipEntry("BasketballPlayer")));
    ConversationState.setCurrent(new ConversationState(identity));
    elasticSearchServiceConnector = new ElasticSearchServiceConnector(getInitConnectorParams(), elasticSearchingClient);
  }

  private InitParams getInitConnectorParams() {
    InitParams params = new InitParams();
    PropertiesParam constructorParams = new PropertiesParam();
    constructorParams.setName("constructor.params");
    constructorParams.setProperty("searchType", "attachment");
    constructorParams.setProperty("displayName", "attachment");
    constructorParams.setProperty("index", "test");
    constructorParams.setProperty("searchFields", "file.content,title");
    params.addParam(constructorParams);
    return params;
  }

  @Test
  public void testCreateNewAttachmentType() {
    //Given
    elasticIndexingClient.sendCreateIndexRequest("test", "");
    assertFalse(typeExists("test", "attachment"));
    //When
    elasticIndexingClient.sendCreateTypeRequest("test", "attachment", getAttachmentMapping());
    //Then
    assertTrue(typeExists("test", "attachment"));

  }

  @Test
  public void testIndexAttachment() {
    //Given
    elasticIndexingClient.sendCreateIndexRequest("test", "");
    elasticIndexingClient.sendCreateTypeRequest("test", "attachment", getAttachmentMapping());
    assertEquals(0, documentNumber());
    String bulkRequest = "{ \"create\" : { \"_index\" : \"test\", \"_type\" : \"attachment\", \"_id\" : \"1\" } }\n" +
        "{ " +
        "\"title\" : \"Sample CV in English\"," +
        "\"file\" : \"" + new String(Base64.encodeBase64(MC23Quotes.getBytes())) + "\"" +
        " }\n";

    //When
    elasticIndexingClient.sendCUDRequest(bulkRequest);
    node.client().admin().indices().prepareRefresh().execute().actionGet();


    //Then
    assertEquals(1, documentNumber());

  }

  @Test
  public void testSearchAttachment() {
    //Given
    elasticIndexingClient.sendCreateIndexRequest("test", "");
    elasticIndexingClient.sendCreateTypeRequest("test", "attachment", getAttachmentMapping());
    assertEquals(0, documentNumber());
    String bulkRequest = "{ \"create\" : { \"_index\" : \"test\", \"_type\" : \"attachment\", \"_id\" : \"1\" } }\n" +
        "{ " +
        "\"title\" : \"Michael Jordan quotes\", " +
        "\"file\" : \""+ new String(Base64.encodeBase64(MC23Quotes.getBytes())) + "\", " +
        "\"permissions\" : [\"TCL\"]" +
            " }\n";

    //When
    elasticIndexingClient.sendCUDRequest(bulkRequest);
    node.client().admin().indices().prepareRefresh().execute().actionGet();
    List<SearchResult> searchResults = new ArrayList<>(elasticSearchServiceConnector.search(null,
        "people",
        null,
        0,
        10,
        null,
        null));

    //Then
    assertEquals(1, documentNumber());
    assertNotNull(searchResults);
    assertEquals(1, searchResults.size());
    assertEquals("... Some <strong>people</strong> want it to happen, some wish it would happen, others make it happen.\n", searchResults.get(0).getExcerpt());

  }

  private String getAttachmentMapping() {
    return "{\"properties\" : {\n" +
        "      \"file\" : {\n" +
        "        \"type\" : \"attachment\",\n" +
        "        \"fields\" : {\n" +
        "          \"title\" : { \"store\" : \"yes\" },\n" +
        "          \"content\" : { \"term_vector\":\"with_positions_offsets\", \"store\":\"yes\" }\n" +
        "        }\n" +
        "      },\n" +
        "      \"permissions\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\" }\n" +
        "    }" +
        "}";
  }

}


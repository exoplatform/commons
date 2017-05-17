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
package org.exoplatform.commons.search.integration;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.simple.JSONObject;

import org.exoplatform.commons.search.es.ElasticSearchServiceConnector;
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
public class ElasticIndexingAttachmentIT extends BaseElasticsearchIT {

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
    constructorParams.setProperty("searchFields", "attachment.content,title");
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
  public void testCreateNewPipeline() {
    //Given
    elasticIndexingClient.sendCreateIndexRequest("test", "");
    assertFalse(typeExists("test", "attachment"));
    elasticIndexingClient.sendCreateTypeRequest("test", "attachment", getAttachmentMapping());

    node.client().admin().indices().prepareRefresh().execute().actionGet();
    assertTrue(typeExists("test", "attachment"));
    //When
    elasticIndexingClient.sendCreateAttachmentPipelineRequest("test", "attachment", "attachment", getAttachmentProcessor());
    node.client().admin().indices().prepareRefresh().execute().actionGet();
    //Then
    assertTrue(pipelineExists("attachment"));
  }

  @Test
  public void testIndexAttachment() {
    //Given
    elasticIndexingClient.sendCreateIndexRequest("test", "");
    elasticIndexingClient.sendCreateTypeRequest("test", "attachment", getAttachmentMapping());
    elasticIndexingClient.sendCreateAttachmentPipelineRequest("test", "attachment", "attachment", getAttachmentProcessor());
    assertEquals(0, documentNumber());
    String attachmentDoc = "{ \"title\" : \"Sample CV in English\"," +
                          "\"file\" : \"" + new String(Base64.encodeBase64(MC23Quotes.getBytes())) + "\"" +
                          " }\n";

    //When
    elasticIndexingClient.sendCreateDocOnPipeline("test", "attachment", "1", "attachment", attachmentDoc);
    node.client().admin().indices().prepareRefresh().execute().actionGet();


    //Then
    assertEquals(1, documentNumber());

  }

  @Test
  public void testDeleteAllDocuments() {
    //Given
    elasticIndexingClient.sendCreateIndexRequest("test", "");
    elasticIndexingClient.sendCreateTypeRequest("test", "attachment", getAttachmentMapping());
    elasticIndexingClient.sendCreateAttachmentPipelineRequest("test", "attachment", "attachment", getAttachmentProcessor());
    assertEquals(0, documentNumber());
    String bulkRequest = "{ " +
        "\"title\" : \"Sample CV in English\"," +
        "\"file\" : \"" + new String(Base64.encodeBase64(MC23Quotes.getBytes())) + "\"" +
        " }\n";

    //When
    elasticIndexingClient.sendCreateDocOnPipeline("test", "attachment", "1", "attachment", bulkRequest);

    node.client().admin().indices().prepareRefresh().execute().actionGet();

    elasticIndexingClient.sendDeleteAllDocsOfTypeRequest("test", "attachment");
    node.client().admin().indices().prepareRefresh().execute().actionGet();

    // Then
    assertEquals(0, documentNumber());
  }

  @Test
  public void testSearchAttachment() {
    //Given
    elasticIndexingClient.sendCreateIndexRequest("test", "");
    elasticIndexingClient.sendCreateTypeRequest("test", "attachment", getAttachmentMapping());
    elasticIndexingClient.sendCreateAttachmentPipelineRequest("test", "attachment", "attachment", getAttachmentProcessor());
    assertEquals(0, documentNumber());

    String bulkRequest = "{\"title\" : \"Michael Jordan quotes\", " +
        "\"file\" : \""+ new String(Base64.encodeBase64(MC23Quotes.getBytes())) + "\", " +
        "\"permissions\" : [\"TCL\"]" +
        " }\n";

    //When
    elasticIndexingClient.sendCreateDocOnPipeline("test", "attachment", "1", "attachment", bulkRequest);

    node.client().admin().indices().prepareRefresh().execute().actionGet();

    List<SearchResult> searchResults = new ArrayList<>(elasticSearchServiceConnector.search(null,
        "people",
        null,
        0,
        10,
        null,
        null));

    node.client().admin().indices().prepareRefresh().execute().actionGet();

    //Then
    assertEquals(1, documentNumber());
    assertNotNull(searchResults);
    assertEquals(1, searchResults.size());
    assertEquals("... Some <strong>people</strong> want it to happen, some wish it would happen, others make it happen.", searchResults.get(0).getExcerpt());
  }

  private String getAttachmentMapping() {
    return "{\"properties\" : {\n" +
        "      \"file\" : {\n" +
        "        \"type\" : \"text\"\n," +
        "        \"index\" : \"false\"\n" +
        "      },\n" +
        "      \"title\" : { \n" +
        "         \"store\" : \"true\",\n" +
        "         \"type\" : \"text\"\n" +
        "      }\n," +
        "      \"attachment\" : {\n" +
        "        \"properties\" : {\n" +
        "          \"content\" : { \"term_vector\":\"with_positions_offsets\", \"store\":\"true\", \"type\" : \"text\"}\n" +
        "        }\n" +
        "      },\n" +
        "      \"permissions\" : {\"type\" : \"keyword\" }\n" +
        "    }" +
        "}";
  }

  private String getAttachmentProcessor() {
    JSONObject fieldJSON = new JSONObject();
    fieldJSON.put("field", "file");
    fieldJSON.put("indexed_chars", -1);
    fieldJSON.put("properties", new JSONArray(Collections.singleton("content")));

    JSONObject attachmentJSON = new JSONObject();
    attachmentJSON.put("attachment", fieldJSON);

    JSONObject processorJSON = new JSONObject();
    processorJSON.put("description", "Attachment processor");
    processorJSON.put("processors", new JSONArray(Collections.singleton(attachmentJSON)));
    return processorJSON.toJSONString();
  }

}
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
package org.exoplatform.addons.es.client;

import org.exoplatform.addons.es.domain.Document;
import org.exoplatform.addons.es.index.impl.ElasticIndexingServiceConnector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Created by The eXo Platform SAS Author : Thibault Clement
 * tclement@exoplatform.com 9/4/15
 */
@RunWith(MockitoJUnitRunner.class)
public class ElasticContentRequestBuilderTest {

  private ElasticContentRequestBuilder    elasticContentRequestBuilder;

  @Mock
  private ElasticIndexingServiceConnector elasticIndexingServiceConnector;

  @Mock
  private Document                        document;

  @Captor
  private ArgumentCaptor<String>          stringArgumentCaptor;

  @Before
  public void initMocks() throws ParseException {
    MockitoAnnotations.initMocks(this);
    elasticContentRequestBuilder = new ElasticContentRequestBuilder();
    initElasticServiceConnectorMock();
  }

  @Test
  public void getCreateIndexRequestContent_ifSimpleConnector_shouldReturnIndexJSONSettings() throws org.json.simple.parser.ParseException {
    // Given
    // When
    String request = elasticContentRequestBuilder.getCreateIndexRequestContent(elasticIndexingServiceConnector);
    // Then
    JSONObject parsedRequest = (JSONObject) JSONValue.parseWithException(request);
    JSONObject settings = (JSONObject) parsedRequest.get("settings");
    assertThat((String) settings.get("number_of_replicas"), is("2"));
    assertThat((String) settings.get("number_of_shards"), is("3"));
    assertNotNull(settings.get("analysis"));
    assertNotNull(((JSONObject) settings.get("analysis")).get("analyzer"));
    assertNotNull(((JSONObject) ((JSONObject) settings.get("analysis")).get("analyzer")).get("default"));
  }

  @Test
  public void getDeleteDocumentRequestContent_ifSimpleConnectorAndEntityId_shouldReturnDeleteQuery() throws org.json.simple.parser.ParseException {
    // Given
    // When
    String request = elasticContentRequestBuilder.getDeleteDocumentRequestContent(elasticIndexingServiceConnector, "1");
    // Then
    JSONObject parsedRequest = (JSONObject) JSONValue.parseWithException(request);
    JSONObject delete = (JSONObject) parsedRequest.get("delete");
    assertThat((String) delete.get("_type"), is("type1"));
    assertThat((String) delete.get("_id"), is("1"));
    assertThat((String) delete.get("_index"), is("test"));
  }

  @Test
  public void getCreateDocumentRequestContent_ifSimpleConnectorAndEntityId_shouldReturnCreateQuery() throws ParseException,
                                                                                                    org.json.simple.parser.ParseException {
    // Given
    initDocumentMock();
    // When
    String request = elasticContentRequestBuilder.getCreateDocumentRequestContent(elasticIndexingServiceConnector, "1");
    // Then
    String[] lines = request.split("\n");
    JSONObject parsedRequestLine1 = (JSONObject) JSONValue.parseWithException(lines[0]);
    JSONObject create = (JSONObject) parsedRequestLine1.get("create");
    assertThat((String) create.get("_type"), is("type1"));
    assertThat((String) create.get("_id"), is("1"));
    assertThat((String) create.get("_index"), is("test"));
    JSONObject parsedRequestLine2 = (JSONObject) JSONValue.parseWithException(lines[1]);
    assertThat((String) parsedRequestLine2.get("author"), is("Michael Jordan"));
    assertThat((String) parsedRequestLine2.get("quote"),
               is("I've missed more than 9000 shots in my career. I've lost almost 300 games. " +
                   "26 times, I've been trusted to take the game winning shot and missed. " +
                   "I've failed over and over and over again in my life. And that is why I succeed."));
    JSONArray permissions = (JSONArray) parsedRequestLine2.get("permissions");
    assertTrue(permissions.contains("vizir"));
    assertTrue(permissions.contains("goleador"));
    assertThat((Long) parsedRequestLine2.get("lastUpdatedDate"), is(601171200000L));
    assertThat((String) parsedRequestLine2.get("url"), is("MyUrlBaby"));
  }

  @Test
  public void getUpdateDocumentRequestContent_ifSimpleConnectorAndEntityId_shouldReturnUpdateQuery() throws ParseException, org.json.simple.parser.ParseException {
    // Given
    initDocumentMock();
    // When
    String request = elasticContentRequestBuilder.getUpdateDocumentRequestContent(elasticIndexingServiceConnector, "1");
    // Then
    String[] lines = request.split("\n");
    JSONObject parsedRequestLine1 = (JSONObject) JSONValue.parseWithException(lines[0]);
    JSONObject create = (JSONObject) parsedRequestLine1.get("index");
    assertThat((String) create.get("_type"), is("type1"));
    assertThat((String) create.get("_id"), is("1"));
    assertThat((String) create.get("_index"), is("test"));
    JSONObject parsedRequestLine2 = (JSONObject) JSONValue.parseWithException(lines[1]);
    assertThat((String) parsedRequestLine2.get("author"), is("Michael Jordan"));
    assertThat((String) parsedRequestLine2.get("quote"),
        is("I've missed more than 9000 shots in my career. I've lost almost 300 games. " +
            "26 times, I've been trusted to take the game winning shot and missed. " +
            "I've failed over and over and over again in my life. And that is why I succeed."));
    JSONArray permissions = (JSONArray) parsedRequestLine2.get("permissions");
    assertTrue(permissions.contains("vizir"));
    assertTrue(permissions.contains("goleador"));
    assertThat((Long) parsedRequestLine2.get("lastUpdatedDate"), is(601171200000L));
    assertThat((String) parsedRequestLine2.get("url"), is("MyUrlBaby"));
  }

  private void initElasticServiceConnectorMock() {
    when(elasticIndexingServiceConnector.getIndex()).thenReturn("test");
    when(elasticIndexingServiceConnector.getType()).thenReturn("type1");
    when(elasticIndexingServiceConnector.getReplicas()).thenReturn(2);
    when(elasticIndexingServiceConnector.getShards()).thenReturn(3);
    when(elasticIndexingServiceConnector.create("1")).thenReturn(document);
    when(elasticIndexingServiceConnector.update("1")).thenReturn(document);
  }

  private void initDocumentMock() throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    when(document.getLastUpdatedDate()).thenReturn(sdf.parse("19/01/1989"));
    when(document.getId()).thenReturn("1");
    when(document.getUrl()).thenReturn("MyUrlBaby");
    when(document.getPermissions()).thenReturn(new HashSet<String>(Arrays.asList("vizir", "goleador" )));
    Map<String, String> fields = new HashMap<>();
    fields.put("quote", "I've missed more than 9000 shots in my career. I've lost almost 300 games. "
        + "26 times, I've been trusted to take the game winning shot and missed. I've failed over and over "
        + "and over again in my life. And that is why I succeed.");
    fields.put("author", "Michael Jordan");
    when(document.getFields()).thenReturn(fields);
    when(document.toJSON()).thenCallRealMethod();
  }

}

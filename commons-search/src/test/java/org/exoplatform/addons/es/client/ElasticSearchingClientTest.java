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

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.exoplatform.commons.utils.PropertyManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 9/1/15
 */
@RunWith(MockitoJUnitRunner.class)
public class ElasticSearchingClientTest {
  private ElasticSearchingClient elasticSearchingClient;
  @Mock
  private HttpClient httpClient;
  @Mock
  private ElasticIndexingAuditTrail auditTrail;
  @Captor
  private ArgumentCaptor<HttpPost> httpPostRequestCaptor;

  @Before
  public void initMock() throws IOException {
    MockitoAnnotations.initMocks(this);
    PropertyManager.setProperty("exo.es.search.server.url", "http://127.0.0.1:9200");
    elasticSearchingClient = new ElasticSearchingClient(auditTrail);
    elasticSearchingClient.client = httpClient;
  }



  @Test
  public void sendSearchRequest_successRequest_callAuditTrailWithSuccessMessage() throws IOException {
    //Given
    String response = "{\n" +
        "  \"took\":7,\n" +
        "  \"timed_out\":false,\n" +
        "  \"_shards\":{\"total\":5,\"successful\":5,\"failed\":0},\n" +
        "  \"hits\":{\n" +
        "    \"total\":2,\n" +
        "    \"max_score\":1.0,\n" +
        "    \"hits\":[\n" +
        "      {\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"1\",\"_score\":1.0,\"_source\":{ \"field1\" : \"value1\" }},\n" +
        "      {\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"3\",\"_score\":1.0,\"_source\":{ \"field1\" : \"value3\" }}\n" +
        "    ]\n" +
        "  }\n" +
        "}";
    initClientMock(200, response);
    when(auditTrail.isFullLogEnabled()).thenReturn(true);
    //When
    elasticSearchingClient.sendRequest("mySearch", "test", "type1");
    //Then
    verify(auditTrail).isFullLogEnabled();
    verify(auditTrail).logAcceptedSearchOperation(eq("search_type"), eq("test"), eq("type1"), eq(HttpStatus.SC_OK), eq(response), anyLong());
    verifyNoMoreInteractions(auditTrail);
  }

  @Test
  public void sendSearchRequest_errorRequest_callAuditTrailWithErrorMessage() throws IOException {
    //Given
    String response = "{\n" +
        "  \"took\":7,\n" +
        "  \"timed_out\":false,\n" +
        "  \"_shards\":{\"total\":5,\"successful\":5,\"failed\":0},\n" +
        "  \"hits\":{\n" +
        "    \"total\":2,\n" +
        "    \"max_score\":1.0,\n" +
        "    \"hits\":[\n" +
        "      {\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"1\",\"_score\":1.0,\"_source\":{ \"field1\" : \"value1\" }},\n" +
        "      {\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"3\",\"_score\":1.0,\"_source\":{ \"field1\" : \"value3\" }}\n" +
        "    ]\n" +
        "  }\n" +
        "}";
    initClientMock(404, response);
    when(auditTrail.isFullLogEnabled()).thenReturn(true);
    //When
    elasticSearchingClient.sendRequest("mySearch", "test", "type1");
    //Then
    verify(auditTrail).logRejectedSearchOperation(eq("search_type"), eq("test"), eq("type1"), eq(HttpStatus.SC_NOT_FOUND), anyString(), anyLong());
    verifyNoMoreInteractions(auditTrail);
  }

  @Test
  public void sendSearchRequest_fullLogNotEnabledAndSuccessRequest_auditTrailNotCalled() throws IOException {
    //Given
    String response = "{\n" +
        "  \"took\":7,\n" +
        "  \"timed_out\":false,\n" +
        "  \"_shards\":{\"total\":5,\"successful\":5,\"failed\":0},\n" +
        "  \"hits\":{\n" +
        "    \"total\":2,\n" +
        "    \"max_score\":1.0,\n" +
        "    \"hits\":[\n" +
        "      {\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"1\",\"_score\":1.0,\"_source\":{ \"field1\" : \"value1\" }},\n" +
        "      {\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"3\",\"_score\":1.0,\"_source\":{ \"field1\" : \"value3\" }}\n" +
        "    ]\n" +
        "  }\n" +
        "}";
    initClientMock(200, response);
    when(auditTrail.isFullLogEnabled()).thenReturn(false);
    //When
    elasticSearchingClient.sendRequest("mySearch", "test", "type1");
    //Then
    verify(auditTrail).isFullLogEnabled();
    verifyNoMoreInteractions(auditTrail);
  }

  @Test
  public void sendSearchRequest_fullLogNotEnabledAndErrorRequest_auditTrailCalled() throws IOException {
    //Given
    String response = "{\n" +
        "  \"took\":7,\n" +
        "  \"timed_out\":false,\n" +
        "  \"_shards\":{\"total\":5,\"successful\":5,\"failed\":0},\n" +
        "  \"hits\":{\n" +
        "    \"total\":2,\n" +
        "    \"max_score\":1.0,\n" +
        "    \"hits\":[\n" +
        "      {\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"1\",\"_score\":1.0,\"_source\":{ \"field1\" : \"value1\" }},\n" +
        "      {\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"3\",\"_score\":1.0,\"_source\":{ \"field1\" : \"value3\" }}\n" +
        "    ]\n" +
        "  }\n" +
        "}";
    initClientMock(404, response);
    when(auditTrail.isFullLogEnabled()).thenReturn(false);
    //When
    elasticSearchingClient.sendRequest("mySearch", "test", "type1");
    //Then
    verify(auditTrail).logRejectedSearchOperation(eq("search_type"), eq("test"), eq("type1"), eq(HttpStatus.SC_NOT_FOUND), anyString(), anyLong());
    verifyNoMoreInteractions(auditTrail);
  }

  private void initClientMock(Integer postStatus, String postContent) throws IOException {
    // Post request
    final HttpResponse postResponse = mock(HttpResponse.class);
    StatusLine postStatusLine = mock(StatusLine.class);
    HttpEntity postHttpEntity = mock(HttpEntity.class);
    when(postResponse.getStatusLine()).thenReturn(postStatusLine);
    when(postStatusLine.getStatusCode()).thenReturn(postStatus);
    when(postResponse.getEntity()).thenReturn(postHttpEntity);
    when(postHttpEntity.getContent()).thenReturn(IOUtils.toInputStream(postContent, "UTF-8"));
    // Mock setting
    when(httpClient.execute(any(HttpPost.class))).thenAnswer(new Answer<HttpResponse>() {
      @Override
      public HttpResponse answer(InvocationOnMock invocation) throws Throwable {
        return postResponse;
      }
    });
  }
}


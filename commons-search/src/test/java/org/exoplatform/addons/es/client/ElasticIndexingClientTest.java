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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;

import org.exoplatform.commons.utils.PropertyManager;
import org.mockito.stubbing.Answer;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 9/1/15
 */
@RunWith(MockitoJUnitRunner.class)
public class ElasticIndexingClientTest {
  private ElasticIndexingClient elasticIndexingClient;
  @Mock
  private HttpClient httpClient;
  @Mock
  private ElasticIndexingAuditTrail auditTrail;
  @Captor
  private ArgumentCaptor<HttpPost> httpPostRequestCaptor;
  @Captor
  private ArgumentCaptor<HttpDelete> httpDeleteRequestCaptor;
  @Captor
  private ArgumentCaptor<HttpGet> httpGetRequestCaptor;


  @Before
  public void initMock() throws IOException {
    MockitoAnnotations.initMocks(this);
    PropertyManager.setProperty("exo.es.index.server.url", "http://127.0.0.1:9200");
    elasticIndexingClient = new ElasticIndexingClient(auditTrail);
    elasticIndexingClient.client = httpClient;
  }

  @Test
  public void sendCreateIndexRequest_IfCreateNewIndex_requestShouldBeSentToElastic() throws IOException {
    //Given
    initClientMock(404, "Not Found", 200, "Success");
    //When
    elasticIndexingClient.sendCreateIndexRequest("index", "fakeSettings");
    //Then
    verify(httpClient, times(2)).execute(any(HttpRequestBase.class));
  }

  @Test
  public void sendCreateTypeRequest_IfCreateNewType_requestShouldBeSentToElastic() throws IOException {
    //Given
    initClientMock(200, ElasticIndexingClient.EMPTY_JSON, 200, "Success");
    //When
    elasticIndexingClient.sendCreateTypeRequest("index", "type", "fakeMappings");
    //Then
    verify(httpClient, times(2)).execute(httpPostRequestCaptor.capture());
  }

  @Test
  public void sendDeleteTypeRequest_IfCreateNewType_deleteRequestShouldBeSentToElastic() throws IOException {
    //Given
    initClientMock(999, "", 200, "Success");
    //When
    elasticIndexingClient.sendDeleteAllDocsOfTypeRequest("index", "type");
    //Then
    verify(httpClient).execute(httpDeleteRequestCaptor.capture());
    assertEquals("http://127.0.0.1:9200/index/type/_query?q=*", httpDeleteRequestCaptor.getValue().getURI().toString());
  }

  @Test
  public void sendCUDRequest_IfCUDOperation_bulkRequestShouldBeSentToElastic() throws IOException {
    //Given
    String response = "{\"took\":15," +
        "\"errors\":true," +
        "\"items\":[" +
        "{\"index\":{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"1\",\"_version\":3,\"status\":200}}" +
        "]}";
    initClientMock(999, "", 200, response);
    //When
    elasticIndexingClient.sendCUDRequest("FakeBulkRequest");
    //Then
    verify(httpClient).execute(httpPostRequestCaptor.capture());
    assertEquals("http://127.0.0.1:9200/_bulk", httpPostRequestCaptor.getValue().getURI().toString());
    assertEquals("FakeBulkRequest", IOUtils.toString(httpPostRequestCaptor.getValue().getEntity().getContent()));

  }

  @Test
  public void sendBulkRequest_forEveryDocument_callAuditTrail() throws IOException {
    //Given
    String response = "{\"took\":15," +
        "\"errors\":true," +
        "\"items\":[" +
        "{\"index\":{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"1\",\"_version\":3,\"status\":200}}," +
        "{\"delete\":{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"2\",\"_version\":1,\"status\":404,\"found\":false}}," +
        "{\"create\":{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"3\",\"status\":409,\"error\":{\"reason\":\"DocumentAlreadyExistsException[[test][4] [type1][3]: document already exists]\"}}}," +
        "{\"update\":{\"_index\":\"index1\",\"_type\":\"type1\",\"_id\":\"1\",\"status\":404,\"error\":{\"reason\":\"DocumentMissingException[[index1][-1] [type1][1]: document missing]\"}}}" +
        "]}";
    initClientMock(999, "", 200, response);
    when(auditTrail.isFullLogEnabled()).thenReturn(true);
    //When
    elasticIndexingClient.sendCUDRequest("myBulk");
    //Then
    verify(auditTrail).isFullLogEnabled();
    verify(auditTrail).logAcceptedBulkOperation(eq("index"), eq("1"), eq("test"), eq("type1"), eq(HttpStatus.SC_OK), isNull(String.class), anyLong());
    verify(auditTrail).logRejectedDocumentBulkOperation(eq("delete"), eq("2"), eq("test"), eq("type1"), eq(HttpStatus.SC_NOT_FOUND), isNull(String.class), anyLong());
    verify(auditTrail).logRejectedDocumentBulkOperation(eq("create"), eq("3"), eq("test"), eq("type1"), eq(HttpStatus.SC_CONFLICT), eq("DocumentAlreadyExistsException[[test][4] [type1][3]: document already exists]"), anyLong());
    verify(auditTrail).logRejectedDocumentBulkOperation(eq("update"), eq("1"), eq("index1"), eq("type1"), eq(HttpStatus.SC_NOT_FOUND), eq("DocumentMissingException[[index1][-1] [type1][1]: document missing]"), anyLong());
    verifyNoMoreInteractions(auditTrail);
  }

  @Test
  public void createIndex_callAuditTrail() throws IOException {
    //Given
    String response = "{\"error\":\"IndexAlreadyExistsException[[profile] already exists]\",\"status\":400}";
    initClientMock(404, "Not Found", 400, response);
    //When
    elasticIndexingClient.sendCreateIndexRequest("profile", "mySettings");
    //Then
    verify(auditTrail).audit(eq("create_index"), isNull(String.class), eq("profile"), isNull(String.class), eq(HttpStatus.SC_BAD_REQUEST), eq("{\"error\":\"IndexAlreadyExistsException[[profile] already exists]\",\"status\":400}"), anyLong());
    verifyNoMoreInteractions(auditTrail);
  }

  @Test
  public void createType_callAuditTrail() throws IOException {
    //Given
    String response = "{\"error\":\"IndexMissingException[[profile] missing]\",\"status\":404}";
    initClientMock(200, ElasticIndexingClient.EMPTY_JSON,404, response);
    //When
    elasticIndexingClient.sendCreateTypeRequest("profile", "profile", "mySettings");
    //Then
    verify(auditTrail).audit(eq("create_type"), isNull(String.class), eq("profile"), eq("profile"), eq(HttpStatus.SC_NOT_FOUND), eq("{\"error\":\"IndexMissingException[[profile] missing]\",\"status\":404}"), anyLong());
    verifyNoMoreInteractions(auditTrail);
  }

  @Test
  public void deleteType_callAuditTrail() throws IOException {
    //Given
    String response = "{\"error\": \"TypeMissingException[[_all] type[[unknownType]] missing: No index has the type.]\",\"status\": 404}";
    initClientMock(999, "", 404, response);
    //When
    elasticIndexingClient.sendDeleteAllDocsOfTypeRequest("profile", "profile");
    //Then
    verify(auditTrail).audit(eq("delete_type"), isNull(String.class), eq("profile"), eq("profile"), eq(HttpStatus.SC_NOT_FOUND), eq("{\"error\": \"TypeMissingException[[_all] type[[unknownType]] missing: No index has the type.]\",\"status\": 404}"), anyLong());
    verifyNoMoreInteractions(auditTrail);
  }

  @Test
  public void sendBulkRequest_fullLogNotEnabled_auditTrailNotCalled() throws IOException {
    //Given
    String response = "{\"took\":15," +
        "\"errors\":false," +
        "\"items\":[" +
        "{\"index\":{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"1\",\"_version\":3,\"status\":200}}" +
        "]}";
    initClientMock(999, "", 200, response);
    when(auditTrail.isFullLogEnabled()).thenReturn(false);
    //When
    elasticIndexingClient.sendCUDRequest("myBulk");
    //Then
    verify(auditTrail).isFullLogEnabled();
    verifyNoMoreInteractions(auditTrail);
  }

  @Test(expected = ElasticClientAuthenticationException.class)
  public void createType_notAuthenticated_throwsException() throws IOException {
    //Given
    initClientMock(999, "", 401, "Authentication Required");
    //When
    elasticIndexingClient.sendCUDRequest("myBulk");
    //Then
    fail("ElasticClientAuthenticationException expected");
  }

  @Test
  public void createIndex_indexAlreadyExists_noRequestIsSentToES() throws IOException {
    //Given
    initClientMock(200, "{my existing Mapping}", 999, "");
    //When
    elasticIndexingClient.sendCreateIndexRequest("myIndex", "mySettings");
    //Then
    verify(httpClient, times(1)).execute(httpGetRequestCaptor.capture());
    assertEquals("http://127.0.0.1:9200/myIndex", httpGetRequestCaptor.getValue().getURI().toString());
    verifyNoMoreInteractions(httpClient);
  }

  @Test
  public void createIndex_indexNotExists_requestIsSentToES() throws IOException {
    //Given
    initClientMock(404, ElasticIndexingClient.EMPTY_JSON, 200, "Success");
    //When
    elasticIndexingClient.sendCreateIndexRequest("myIndex", "mySettings");
    //Then
    verify(httpClient, times(2)).execute(httpPostRequestCaptor.capture());
    assertEquals("http://127.0.0.1:9200/myIndex", httpPostRequestCaptor.getValue().getURI().toString());
    assertEquals("mySettings", IOUtils.toString(httpPostRequestCaptor.getValue().getEntity().getContent()));
    verifyNoMoreInteractions(httpClient);
  }

  private void initClientMock(Integer getStatus, String getContent, Integer postStatus, String postContent) throws IOException {
    // Get request
    final HttpResponse getResponse = mock(HttpResponse.class);
    StatusLine getStatusLine = mock(StatusLine.class);
    HttpEntity getHttpEntity = mock(HttpEntity.class);
    when(getResponse.getStatusLine()).thenReturn(getStatusLine);
    when(getStatusLine.getStatusCode()).thenReturn(getStatus);
    when(getResponse.getEntity()).thenReturn(getHttpEntity);
    when(getHttpEntity.getContent()).thenReturn(IOUtils.toInputStream(getContent, "UTF-8"));
    // Post request
    final HttpResponse postResponse = mock(HttpResponse.class);
    StatusLine postStatusLine = mock(StatusLine.class);
    HttpEntity postHttpEntity = mock(HttpEntity.class);
    when(postResponse.getStatusLine()).thenReturn(postStatusLine);
    when(postStatusLine.getStatusCode()).thenReturn(postStatus);
    when(postResponse.getEntity()).thenReturn(postHttpEntity);
    when(postHttpEntity.getContent()).thenReturn(IOUtils.toInputStream(postContent, "UTF-8"));
    // Mock setting
    when(httpClient.execute(any(HttpGet.class))).thenAnswer(new Answer<HttpResponse>() {
      @Override
      public HttpResponse answer(InvocationOnMock invocation) throws Throwable {
        Object[] args = invocation.getArguments();
        if (args[0] instanceof HttpGet) {
          return getResponse;
        }
        return postResponse;
      }
    });
  }
}


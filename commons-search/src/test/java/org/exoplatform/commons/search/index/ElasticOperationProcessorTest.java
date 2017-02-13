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
package org.exoplatform.commons.search.index;

import org.exoplatform.commons.search.es.client.ElasticContentRequestBuilder;
import org.exoplatform.commons.search.es.client.ElasticIndexingAuditTrail;
import org.exoplatform.commons.search.es.client.ElasticIndexingClient;
import org.exoplatform.commons.search.dao.IndexingOperationDAO;
import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.commons.search.domain.IndexingOperation;
import org.exoplatform.commons.search.domain.OperationType;
import org.exoplatform.commons.search.index.impl.ElasticIndexingOperationProcessor;
import org.exoplatform.commons.search.index.impl.ElasticIndexingServiceConnector;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.container.PortalContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 9/1/15
 */
@RunWith(MockitoJUnitRunner.class)
public class ElasticOperationProcessorTest {

  //Naming Convention Used: methodUnderTest_conditionEncounter_resultExpected

  private ElasticIndexingOperationProcessor elasticIndexingOperationProcessor;

  @Mock
  private IndexingOperationDAO indexingOperationDAO;
  @Mock
  private ElasticIndexingAuditTrail auditTrail;
  @Mock
  private ElasticIndexingClient elasticIndexingClient;

  @Mock
  private ElasticIndexingServiceConnector elasticIndexingServiceConnector;

  @Mock
  private ElasticContentRequestBuilder elasticContentRequestBuilder;

  private EntityManagerService entityManagerService;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);

    // Make sure a portal container is started
    PortalContainer.getInstance();

    entityManagerService = new EntityManagerService();
    entityManagerService.startRequest(null);
    elasticIndexingOperationProcessor = new ElasticIndexingOperationProcessor(indexingOperationDAO, elasticIndexingClient, elasticContentRequestBuilder, auditTrail, entityManagerService, null);
    initElasticServiceConnector();
  }

  private void initElasticServiceConnector() {
    when(elasticIndexingServiceConnector.getIndex()).thenReturn("blog");
    when(elasticIndexingServiceConnector.getType()).thenReturn("post");
    when(elasticIndexingServiceConnector.getReplicas()).thenReturn(1);
    when(elasticIndexingServiceConnector.getShards()).thenReturn(5);
  }

  @After
  public void clean() {
    elasticIndexingOperationProcessor.getConnectors().clear();
    entityManagerService.endRequest(null);
  }

  /*
  addConnector(ElasticIndexingServiceConnector elasticIndexingServiceConnector)
   */

  @Test
  public void addConnector_ifNewConnector_connectorAdded() {
    //Given
    assertEquals(0, elasticIndexingOperationProcessor.getConnectors().size());
    //When
    elasticIndexingOperationProcessor.addConnector(elasticIndexingServiceConnector);
    //Then
    assertEquals(1, elasticIndexingOperationProcessor.getConnectors().size());
  }

  @Test
  public void addConnector_ifConnectorAlreadyExist_connectorNotAdded() {
    //Given
    elasticIndexingOperationProcessor.getConnectors().put("post", elasticIndexingServiceConnector);
    assertEquals(1, elasticIndexingOperationProcessor.getConnectors().size());
    //When
    elasticIndexingOperationProcessor.addConnector(elasticIndexingServiceConnector);
    //Then
    assertEquals(1, elasticIndexingOperationProcessor.getConnectors().size());
  }

  @Test
  public void addConnectRor_ifNewConnector_initIndexingQueueCreated() {
    //Given
    IndexingOperation indexingOperation = new IndexingOperation(null,"post", OperationType.INIT);
    //When
    elasticIndexingOperationProcessor.addConnector(elasticIndexingServiceConnector);
    elasticIndexingOperationProcessor.start();
    //Then
    verify(indexingOperationDAO, times(1)).create(indexingOperation);
  }

  @Test
  public void addConnector_ifConnectorAlreadyExist_initIndexingQueueNotCreated() {
    //Given
    elasticIndexingOperationProcessor.getConnectors().put("post", elasticIndexingServiceConnector);
    IndexingOperation indexingOperation = new IndexingOperation(null,"post",OperationType.INIT);
    //When
    elasticIndexingOperationProcessor.addConnector(elasticIndexingServiceConnector);
    //Then
    verify(indexingOperationDAO, times(0)).create(indexingOperation);
  }

  /*
  process()
   */

  //test the order of the operation processing


  @Test
  public void process_ifAllOperationsInQueue_requestShouldBeSentInAnExpectedOrder() throws ParseException {
    //Given
    elasticIndexingOperationProcessor.getConnectors().put("post", elasticIndexingServiceConnector);
    IndexingOperation create = new IndexingOperation("1","post",OperationType.CREATE);
    create.setId(4L);
    IndexingOperation delete = new IndexingOperation("1","post",OperationType.DELETE);
    delete.setId(5L);
    IndexingOperation update = new IndexingOperation("1","post",OperationType.UPDATE);
    update.setId(2L);
    IndexingOperation deleteAll = new IndexingOperation(null,"post",OperationType.DELETE_ALL);
    deleteAll.setId(1L);
    IndexingOperation init = new IndexingOperation(null,"post",OperationType.INIT);
    init.setId(3L);
    List<IndexingOperation> indexingOperations = new ArrayList<>();
    indexingOperations.add(create);
    indexingOperations.add(delete);
    indexingOperations.add(update);
    indexingOperations.add(init);
    indexingOperations.add(deleteAll);
    Document document = new Document("post", "1", new Date());
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(indexingOperations);
    when(elasticIndexingServiceConnector.create("1")).thenReturn(document);
    when(elasticIndexingServiceConnector.update("1")).thenReturn(document);

    //When
    elasticIndexingOperationProcessor.process();

    //Then

    //Check Client invocation
    InOrder orderClient = inOrder(elasticIndexingClient);
    //Operation I
    orderClient.verify(elasticIndexingClient).sendCreateIndexRequest(elasticIndexingServiceConnector.getIndex(),
        elasticContentRequestBuilder.getCreateIndexRequestContent(elasticIndexingServiceConnector));
    orderClient.verify(elasticIndexingClient).sendCreateTypeRequest(elasticIndexingServiceConnector.getIndex(),
        elasticIndexingServiceConnector.getType(),
        elasticIndexingServiceConnector.getMapping());
    //Then Operation X
    orderClient.verify(elasticIndexingClient).sendDeleteAllDocsOfTypeRequest(elasticIndexingServiceConnector.getIndex(),
            elasticIndexingServiceConnector.getType());
    //Then Operation D, C and U
    orderClient.verify(elasticIndexingClient).sendCUDRequest(anyString());
    //Then no more interaction with client
    verifyNoMoreInteractions(elasticIndexingClient);
  }

  @Test
  public void process_ifAllOperationsInQueue_requestShouldBeCreatedInAnExpectedOrder() throws ParseException {
    //Given
    elasticIndexingOperationProcessor.getConnectors().put("post", elasticIndexingServiceConnector);
    elasticIndexingOperationProcessor.getConnectors().put("post1", elasticIndexingServiceConnector);
    elasticIndexingOperationProcessor.getConnectors().put("post2", elasticIndexingServiceConnector);
    elasticIndexingOperationProcessor.getConnectors().put("post3", elasticIndexingServiceConnector);
    IndexingOperation init = new IndexingOperation(null,"post",OperationType.INIT);
    init.setId(4L);
    IndexingOperation deleteAll = new IndexingOperation(null,"post",OperationType.DELETE_ALL);
    deleteAll.setId(5L);
    IndexingOperation delete = new IndexingOperation("1","post1",OperationType.DELETE);
    delete.setId(2L);
    IndexingOperation create = new IndexingOperation("2","post2",OperationType.CREATE);
    create.setId(1L);
    IndexingOperation update = new IndexingOperation("3","post3",OperationType.UPDATE);
    update.setId(3L);
    List<IndexingOperation> indexingOperations = new ArrayList<>();
    indexingOperations.add(create);
    indexingOperations.add(delete);
    indexingOperations.add(update);
    indexingOperations.add(init);
    indexingOperations.add(deleteAll);
    Document document = new Document("post", "1", new Date());
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(indexingOperations);
    when(elasticIndexingServiceConnector.create("2")).thenReturn(document);
    when(elasticIndexingServiceConnector.update("3")).thenReturn(document);

    //When
    elasticIndexingOperationProcessor.process();

    //Then

    //Check Request Builder invocation
    InOrder orderRequestBuilder = inOrder(elasticContentRequestBuilder);
    //Operation I
    orderRequestBuilder.verify(elasticContentRequestBuilder).getCreateIndexRequestContent(elasticIndexingServiceConnector);
    //Then Operation D, C and U
    orderRequestBuilder.verify(elasticContentRequestBuilder).getDeleteDocumentRequestContent(any(ElasticIndexingServiceConnector.class), anyString());
    orderRequestBuilder.verify(elasticContentRequestBuilder).getCreateDocumentRequestContent(elasticIndexingServiceConnector, "2");
    orderRequestBuilder.verify(elasticContentRequestBuilder).getUpdateDocumentRequestContent(elasticIndexingServiceConnector, "3");
    //Then no more interaction with builder
    verifyNoMoreInteractions(elasticContentRequestBuilder);
  }

  @Test
  public void process_ifAllOperationsInQueue_requestShouldContinueOnException() throws ParseException {
    //Given
    elasticIndexingOperationProcessor.getConnectors().put("post1", elasticIndexingServiceConnector);
    elasticIndexingOperationProcessor.getConnectors().put("post2", elasticIndexingServiceConnector);
    elasticIndexingOperationProcessor.getConnectors().put("post3", elasticIndexingServiceConnector);

    List<IndexingOperation> indexingOperations = new ArrayList<>();

    IndexingOperation delete = new IndexingOperation("1","post1",OperationType.DELETE);
    delete.setId(2L);
    indexingOperations.add(delete);

    IndexingOperation create = new IndexingOperation("2","post2",OperationType.CREATE);
    create.setId(1L);
    indexingOperations.add(create);

    IndexingOperation update = new IndexingOperation("3","post3",OperationType.UPDATE);
    update.setId(3L);
    indexingOperations.add(update);

    //When an exception is thrown during process
    doThrow(new RuntimeException("Fake error")).when(elasticContentRequestBuilder).getDeleteDocumentRequestContent(elasticIndexingServiceConnector, "1");
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(indexingOperations);
    elasticIndexingOperationProcessor.process();

    //Then Operation C and U was called
    verify(elasticContentRequestBuilder).getDeleteDocumentRequestContent(any(ElasticIndexingServiceConnector.class), eq("1"));
    verify(elasticContentRequestBuilder).getCreateDocumentRequestContent(any(ElasticIndexingServiceConnector.class), eq("2"));
    verify(elasticContentRequestBuilder).getUpdateDocumentRequestContent(any(ElasticIndexingServiceConnector.class), eq("3"));
  }

  //test the result of operation processing on the operation still in queue

  @Test
  public void process_ifDeleteAllOperation_allOldestCreateUpdateDeleteOperationsWithSameTypeStillInQueueShouldBeCanceled() throws ParseException {
    //Given
    elasticIndexingOperationProcessor.getConnectors().put("post", elasticIndexingServiceConnector);
    IndexingOperation deleteAll = new IndexingOperation(null,"post",OperationType.DELETE_ALL);
    deleteAll.setId(5L);
    //CUD operation are older than delete all
    IndexingOperation create = new IndexingOperation("1","post",OperationType.CREATE);
    create.setId(1L);
    IndexingOperation delete = new IndexingOperation("1","post",OperationType.DELETE);
    delete.setId(2L);
    IndexingOperation update = new IndexingOperation("1","post",OperationType.UPDATE);
    update.setId(3L);
    List<IndexingOperation> indexingOperations = new ArrayList<>();
    indexingOperations.add(create);
    indexingOperations.add(delete);
    indexingOperations.add(update);
    indexingOperations.add(deleteAll);
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(indexingOperations);

    //When
    elasticIndexingOperationProcessor.process();

    //Then
    InOrder orderClient = inOrder(elasticIndexingClient);
    //Remove and recreate type request
    orderClient.verify(elasticIndexingClient).sendDeleteAllDocsOfTypeRequest(elasticIndexingServiceConnector.getIndex(),
            elasticIndexingServiceConnector.getType());
    //No CUD request
    verifyNoMoreInteractions(elasticIndexingClient);
  }


  @Test
  public void process_ifDeleteOperation_allOldestCreateOperationsWithSameEntityIdStillInQueueShouldBeCanceled() throws ParseException {
    //Given
    elasticIndexingOperationProcessor.getConnectors().put("post", elasticIndexingServiceConnector);
    //Delete operation are older than create and update
    IndexingOperation delete = new IndexingOperation("1","post",OperationType.DELETE);
    delete.setId(2L);
    IndexingOperation create = new IndexingOperation("1","post",OperationType.CREATE);
    create.setId(1L);
    IndexingOperation update = new IndexingOperation("1","post",OperationType.UPDATE);
    update.setId(3L);
    List<IndexingOperation> indexingOperations = new ArrayList<>();
    indexingOperations.add(create);
    indexingOperations.add(delete);
    indexingOperations.add(update);
    Document document = new Document("post", "1", new Date());
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(indexingOperations);
    when(elasticIndexingServiceConnector.create("1")).thenReturn(document);
    when(elasticIndexingServiceConnector.update("1")).thenReturn(document);

    //When
    elasticIndexingOperationProcessor.process();

    //Then
    InOrder order = inOrder(elasticIndexingClient);
    //Only one delete request should be build
    verify(elasticContentRequestBuilder, times(1)).getDeleteDocumentRequestContent(elasticIndexingServiceConnector, "1");
    verifyNoMoreInteractions(elasticContentRequestBuilder);
    //Only one CUD request should be send
    order.verify(elasticIndexingClient, times(1)).sendCUDRequest(anyString());
    verifyNoMoreInteractions(elasticIndexingClient);
  }

  @Test
  public void process_ifCreateOperation_allOldestAndNewestUpdateOperationsWithSameEntityIdStillInQueueShouldBeCanceled() throws ParseException {
    //Given
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    elasticIndexingOperationProcessor.getConnectors().put("post", elasticIndexingServiceConnector);
    IndexingOperation create = new IndexingOperation("1","post",OperationType.CREATE);
    create.setId(1L);
    IndexingOperation oldUpdate = new IndexingOperation("1","post",OperationType.UPDATE);
    oldUpdate.setId(2L);
    IndexingOperation newUpdate = new IndexingOperation("1","post",OperationType.UPDATE);
    newUpdate.setId(3L);
    List<IndexingOperation> indexingOperations = new ArrayList<>();
    indexingOperations.add(create);
    indexingOperations.add(oldUpdate);
    indexingOperations.add(newUpdate);
    Document document = new Document("post", "1", sdf.parse("19/01/1989"));
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(indexingOperations);
    when(elasticIndexingServiceConnector.create("1")).thenReturn(document);
    when(elasticIndexingServiceConnector.update("1")).thenReturn(document);

    //When
    elasticIndexingOperationProcessor.process();

    //Then
    InOrder order = inOrder(elasticIndexingClient);
    //Only one create request should be build (update operations are canceled)
    verify(elasticContentRequestBuilder, times(1)).getCreateDocumentRequestContent(elasticIndexingServiceConnector, "1");
    verifyNoMoreInteractions(elasticContentRequestBuilder);
    //Only one CUD request should be send
    order.verify(elasticIndexingClient, times(1)).sendCUDRequest(anyString());
    verifyNoMoreInteractions(elasticIndexingClient);
  }

  @Test
  public void process_ifDeleteAllOperation_allNewestCreateDeleteOperationsStillInQueueShouldBeProcessed() throws ParseException {
    //Given
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    elasticIndexingOperationProcessor.getConnectors().put("post", elasticIndexingServiceConnector);
    IndexingOperation deleteAll = new IndexingOperation(null,"post",OperationType.DELETE_ALL);
    deleteAll.setId(1L);
    //CUD operation are newer than delete all
    IndexingOperation create = new IndexingOperation("1","post",OperationType.CREATE);
    create.setId(3L);
    IndexingOperation delete = new IndexingOperation("1","post",OperationType.DELETE);
    delete.setId(2L);
    List<IndexingOperation> indexingOperations = new ArrayList<>();
    indexingOperations.add(create);
    indexingOperations.add(delete);
    indexingOperations.add(deleteAll);
    Document document = new Document("post", "1", sdf.parse("19/01/1989"));
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(indexingOperations);
    when(elasticIndexingServiceConnector.create("1")).thenReturn(document);

    //When
    elasticIndexingOperationProcessor.process();

    //Then
    InOrder orderClient = inOrder(elasticIndexingClient);
    //Remove and recreate type request
    orderClient.verify(elasticIndexingClient).sendDeleteAllDocsOfTypeRequest(elasticIndexingServiceConnector.getIndex(),
            elasticIndexingServiceConnector.getType());
    //Create and Delete requests should be build
    verify(elasticContentRequestBuilder, times(1)).getCreateDocumentRequestContent(elasticIndexingServiceConnector, "1");
    verify(elasticContentRequestBuilder, times(1)).getDeleteDocumentRequestContent(elasticIndexingServiceConnector, "1");
    //Only one CUD request should be send
    orderClient.verify(elasticIndexingClient, times(1)).sendCUDRequest(anyString());
    verifyNoMoreInteractions(elasticIndexingClient);
  }

  @Test
  public void process_ifDeleteOperation_allNewestCreateOperationsStillInQueueShouldBeProcessed() throws ParseException {
    //Given
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    elasticIndexingOperationProcessor.getConnectors().put("post", elasticIndexingServiceConnector);
    //Delete operation are older than create
    IndexingOperation delete = new IndexingOperation("1","post",OperationType.DELETE);
    delete.setId(1L);
    IndexingOperation create = new IndexingOperation("1","post",OperationType.CREATE);
    create.setId(2L);
    List<IndexingOperation> indexingOperations = new ArrayList<>();
    indexingOperations.add(create);
    indexingOperations.add(delete);
    Document document = new Document("post", "1", sdf.parse("19/01/1989"));
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(indexingOperations);
    when(elasticIndexingServiceConnector.create("1")).thenReturn(document);

    //When
    elasticIndexingOperationProcessor.process();

    //Then
    InOrder order = inOrder(elasticContentRequestBuilder);
    //Only one delete and one create request should be build
    order.verify(elasticContentRequestBuilder, times(1)).getDeleteDocumentRequestContent(elasticIndexingServiceConnector, "1");
    order.verify(elasticContentRequestBuilder, times(1)).getCreateDocumentRequestContent(elasticIndexingServiceConnector, "1");
    verifyNoMoreInteractions(elasticContentRequestBuilder);
    //Only one CUD request should be send
    verify(elasticIndexingClient, times(1)).sendCUDRequest(anyString());
    verifyNoMoreInteractions(elasticIndexingClient);
  }

  @Test
  public void process_ifDeleteAllOperation_allNewestUpdateOperationsWithSameEntityTypeIdStillInQueueShouldBeCanceled() throws ParseException {
    //Given
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    elasticIndexingOperationProcessor.getConnectors().put("post", elasticIndexingServiceConnector);
    IndexingOperation deleteAll = new IndexingOperation(null,"post",OperationType.DELETE_ALL);
    deleteAll.setId(5L);
    //CUD operation are newer than delete all
    IndexingOperation update = new IndexingOperation("1","post",OperationType.UPDATE);
    update.setId(1L);
    List<IndexingOperation> indexingOperations = new ArrayList<>();
    indexingOperations.add(update);
    indexingOperations.add(deleteAll);
    Document document = new Document("post", "1", sdf.parse("19/01/1989"));
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(indexingOperations);
    when(elasticIndexingServiceConnector.update("1")).thenReturn(document);

    //When
    elasticIndexingOperationProcessor.process();

    //Then
    InOrder orderClient = inOrder(elasticIndexingClient);
    //Remove and recreate type request
    orderClient.verify(elasticIndexingClient).sendDeleteAllDocsOfTypeRequest(elasticIndexingServiceConnector.getIndex(),
            elasticIndexingServiceConnector.getType());
    //No CUD operation
    verifyNoMoreInteractions(elasticIndexingClient);
  }

  @Test
  public void process_ifDeleteOperation_allNewestUpdateOperationsWithSameEntityIdStillInQueueShouldBeCanceled() throws ParseException {
    //Given
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    elasticIndexingOperationProcessor.getConnectors().put("post", elasticIndexingServiceConnector);
    //Delete operation are older than update
    IndexingOperation delete = new IndexingOperation("1","post",OperationType.DELETE);
    delete.setId(1L);
    IndexingOperation update = new IndexingOperation("1","post",OperationType.UPDATE);
    update.setId(2L);
    List<IndexingOperation> indexingOperations = new ArrayList<>();
    indexingOperations.add(update);
    indexingOperations.add(delete);
    Document document = new Document("post", "1", sdf.parse("19/01/1989"));
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(indexingOperations);
    when(elasticIndexingServiceConnector.update("1")).thenReturn(document);

    //When
    elasticIndexingOperationProcessor.process();

    //Then
    //Only one delete request should be build
    verify(elasticContentRequestBuilder, times(1)).getDeleteDocumentRequestContent(elasticIndexingServiceConnector, "1");
    verifyNoMoreInteractions(elasticContentRequestBuilder);
    //Only one CUD request should be send
    verify(elasticIndexingClient, times(1)).sendCUDRequest(anyString());
    verifyNoMoreInteractions(elasticIndexingClient);
  }

  @Test
  public void process_ifBulkRequestReachedSizeLimit_requestIsSent() throws ParseException {
    //Given
    elasticIndexingOperationProcessor.setRequestSizeLimit(1);
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    elasticIndexingOperationProcessor.getConnectors().put("post", elasticIndexingServiceConnector);
    IndexingOperation create1 = new IndexingOperation("1","post",OperationType.CREATE);
    create1.setId(1L);
    IndexingOperation create2 = new IndexingOperation("2","post",OperationType.CREATE);
    create2.setId(2L);
    List<IndexingOperation> indexingOperations = new ArrayList<>();
    indexingOperations.add(create1);
    indexingOperations.add(create2);
    Document document1 = new Document("post", "1", sdf.parse("19/01/1989"));
    Document document2 = new Document("post", "2", sdf.parse("19/01/1989"));
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(indexingOperations);
    when(elasticIndexingServiceConnector.create("1")).thenReturn(document1);
    when(elasticIndexingServiceConnector.create("2")).thenReturn(document2);

    //When
    elasticIndexingOperationProcessor.process();

    //Then
    //Two CUD request should be send because the first create request will reached the limit size (= 1 byte)
    verify(elasticIndexingClient, times(2)).sendCUDRequest(anyString());
    verifyNoMoreInteractions(elasticIndexingClient);
  }

  @Test
  public void process_ifReindexAll_requestIsSent() throws ParseException {
    //Given
    elasticIndexingOperationProcessor.setReindexBatchSize(10);
    elasticIndexingOperationProcessor.getConnectors().put("post", elasticIndexingServiceConnector);
    IndexingOperation reindexAll = new IndexingOperation(null,"post",OperationType.REINDEX_ALL);
    reindexAll.setId(1L);
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(Collections.singletonList(reindexAll));
    when(elasticIndexingServiceConnector.getAllIds(0, 10)).thenReturn(Arrays.asList("1", "2"));
    //When
    elasticIndexingOperationProcessor.process();
    //Then
    ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
    InOrder orderClient = inOrder(indexingOperationDAO);
    orderClient.verify(indexingOperationDAO).create(new IndexingOperation(null, elasticIndexingServiceConnector.getType(), OperationType.DELETE_ALL));
    orderClient.verify(indexingOperationDAO).createAll(captor.capture());
    assertThat(captor.getValue().get(0), is(new IndexingOperation("1", elasticIndexingServiceConnector.getType(), OperationType.CREATE)));
    assertThat(captor.getValue().get(1), is(new IndexingOperation("2", elasticIndexingServiceConnector.getType(), OperationType.CREATE)));
  }

  @Test
  public void process_ifReindexAll_idsAreProcessedAsBatch() throws ParseException {
    //Given
    elasticIndexingOperationProcessor.setReindexBatchSize(2);
    elasticIndexingOperationProcessor.getConnectors().put("post", elasticIndexingServiceConnector);
    IndexingOperation reindexAll = new IndexingOperation(null,"post",OperationType.REINDEX_ALL);
    reindexAll.setId(3L);
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(Collections.singletonList(reindexAll));
    when(elasticIndexingServiceConnector.getAllIds(0, 2)).thenReturn(Arrays.asList("1", "2"));
    when(elasticIndexingServiceConnector.getAllIds(2, 2)).thenReturn(Collections.singletonList("3"));
    //When
    elasticIndexingOperationProcessor.process();
    //Then
    ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
    verify(indexingOperationDAO, times(2)).createAll(captor.capture());
    assertEquals(captor.getAllValues().get(0).size(), 2);
    assertEquals(captor.getAllValues().get(1).size(), 1);
  }

  @Test
  public void reindexAll_whateverTheResult_addToAuditTrail() throws IOException {
    //Given
    elasticIndexingOperationProcessor.getConnectors().put("post", elasticIndexingServiceConnector);
    IndexingOperation reindexAll = new IndexingOperation(null,"post",OperationType.REINDEX_ALL);
    reindexAll.setId(3L);
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(Collections.singletonList(reindexAll));
    //When
    elasticIndexingOperationProcessor.process();
    //Then
    verify(auditTrail).audit(eq("reindex_all"), isNull(String.class), isNull(String.class), eq("post"), isNull(Integer.class), isNull(String.class), anyLong());
    verifyNoMoreInteractions(auditTrail);
  }

  @Test
  public void deleteAll_whateverTheResult_addToAuditTrail() throws IOException {
    //Given
    elasticIndexingOperationProcessor.getConnectors().put("post", elasticIndexingServiceConnector);
    IndexingOperation deleteAll = new IndexingOperation(null,"post",OperationType.DELETE_ALL);
    deleteAll.setId(3L);
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(Collections.singletonList(deleteAll));
    //When
    elasticIndexingOperationProcessor.process();
    //Then
    verify(auditTrail).audit(eq("delete_all"), isNull(String.class), isNull(String.class), eq("post"), isNull(Integer.class), isNull(String.class), anyLong());
    verifyNoMoreInteractions(auditTrail);
  }

}


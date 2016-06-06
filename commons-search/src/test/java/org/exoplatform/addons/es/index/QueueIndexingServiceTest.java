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
package org.exoplatform.addons.es.index;

import org.exoplatform.addons.es.dao.IndexingOperationDAO;
import org.exoplatform.addons.es.domain.IndexingOperation;
import org.exoplatform.addons.es.domain.OperationType;
import org.exoplatform.addons.es.index.impl.QueueIndexingService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.ParseException;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 10/12/15
 */
@RunWith(MockitoJUnitRunner.class)
public class QueueIndexingServiceTest {

  //Naming Convention Used: methodUnderTest_conditionEncounter_resultExpected

  private QueueIndexingService queueIndexingService;
  @Mock
  private IndexingOperationDAO indexingOperationDAO;
  @Captor
  private ArgumentCaptor<String> stringCaptor;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
    queueIndexingService = new QueueIndexingService(indexingOperationDAO);
  }

  @After
  public void clean() {
    queueIndexingService.clearIndexingQueue();
  }

  /*
  indexing Method
   */

  @Test
  public void init_ifInitOperation_initIndexingQueueCreated() {
    //Given
    IndexingOperation indexingOperation = new IndexingOperation(null,"post",OperationType.INIT);
    //When
    queueIndexingService.init("post");
    //Then
    verify(indexingOperationDAO, times(1)).create(indexingOperation);
  }

  @Test
  public void unindexAll_ifDeleteAllOperation_deleteAllIndexingQueueCreated() {
    //Given
    IndexingOperation indexingOperation = new IndexingOperation(null,"post",OperationType.DELETE_ALL);
    //When
    queueIndexingService.unindexAll("post");
    //Then
    verify(indexingOperationDAO, times(1)).create(indexingOperation);
  }

  @Test
  public void unindex_ifDeleteOperation_deleteIndexingQueueCreated() {
    //Given
    IndexingOperation indexingOperation = new IndexingOperation("1","post",OperationType.DELETE);
    //When
    queueIndexingService.unindex("post", "1");
    //Then
    verify(indexingOperationDAO, times(1)).create(indexingOperation);
  }

  @Test
  public void reindex_ifUpdateOperation_updateIndexingQueueCreated() {
    //Given
    IndexingOperation indexingOperation = new IndexingOperation("1","post",OperationType.UPDATE);
    //When
    queueIndexingService.reindex("post", "1");
    //Then
    verify(indexingOperationDAO, times(1)).create(indexingOperation);
  }

  @Test
  public void reindexAll_commandsAreInsertedInIndexingQueue() throws ParseException {
    //Given
    IndexingOperation indexingOperation = new IndexingOperation(null,"post",OperationType.REINDEX_ALL);
    //When
    queueIndexingService.reindexAll("post");
    //Then
    verify(indexingOperationDAO, times(1)).create(indexingOperation);
  }

  @Test
  public void index_ifCreateOperation_createIndexingQueueCreated() {
    //Given
    IndexingOperation indexingOperation = new IndexingOperation("1","post",OperationType.CREATE);
    //When
    queueIndexingService.index("post", "1");
    //Then
    verify(indexingOperationDAO, times(1)).create(indexingOperation);
  }

  @Test(expected = IllegalArgumentException.class)
  public void index_ifEntityIdNull_IllegalArgumentException() {
    //Given
    //When
    queueIndexingService.index("post", null);
    //Then
    fail("Exception expected");
  }

  @Test(expected = IllegalArgumentException.class)
  public void index_ifEntityIdBlank_IllegalArgumentException() {
    //Given
    //When
    queueIndexingService.index("post", "");
    //Then
    fail("Exception expected");
  }

  @Test(expected = IllegalArgumentException.class)
  public void reindex_ifEntityIdNull_IllegalArgumentException() {
    //Given
    //When
    queueIndexingService.reindex("post", null);
    //Then
    fail("Exception expected");
  }

  @Test(expected = IllegalArgumentException.class)
  public void reindex_ifEntityIdBlank_IllegalArgumentException() {
    //Given
    //When
    queueIndexingService.reindex("post", "");
    //Then
    fail("Exception expected");
  }

  @Test(expected = IllegalArgumentException.class)
  public void unindex_ifEntityIdNull_IllegalArgumentException() {
    //Given
    //When
    queueIndexingService.unindex("post", null);
    //Then
    fail("Exception expected");
  }

  @Test(expected = IllegalArgumentException.class)
  public void unindex_ifEntityIdBlank_IllegalArgumentException() {
    //Given
    //When
    queueIndexingService.unindex("post", "");
    //Then
    fail("Exception expected");
  }
}


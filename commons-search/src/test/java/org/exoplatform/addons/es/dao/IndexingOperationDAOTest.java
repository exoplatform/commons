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
package org.exoplatform.addons.es.dao;

import org.exoplatform.addons.es.domain.IndexingOperation;
import org.exoplatform.addons.es.domain.OperationType;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.container.PortalContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 8/20/15
 */
public class IndexingOperationDAOTest extends AbstractDAOTest {

  private IndexingOperationDAO indexingOperationDAO;

  @Before
  public void setUp() {
    PortalContainer container = PortalContainer.getInstance();
    indexingOperationDAO = container.getComponentInstanceOfType(IndexingOperationDAO.class);
  }

  @After
  public void tearDown() {
    indexingOperationDAO.deleteAll();
  }

  @Test
  public void testIndexingQueueCreation() throws IllegalAccessException, NoSuchFieldException {

    //Given
    List<IndexingOperation> indexingOperations = indexingOperationDAO.findAll();
    assertEquals(indexingOperations.size(), 0);
    IndexingOperation indexingOperation = new IndexingOperation();
    indexingOperation.setEntityType("blog");
    indexingOperation.setOperation(OperationType.INIT);

    //When
    indexingOperationDAO.create(indexingOperation);

    //Then
    assertEquals(indexingOperationDAO.findAll().size(), 1);
    Field privateField = IndexingOperation.class.getDeclaredField("timestamp");
    privateField.setAccessible(true);
    Date timestamp = (Date) privateField.get(indexingOperation);
    assertNull(timestamp);
  }

  @Test
  public void testDatabaseAutoGeneratingTimestamp () throws NoSuchFieldException, IllegalAccessException {
    //Given
    IndexingOperation indexingOperation1 = new IndexingOperation();
    indexingOperation1.setEntityType("blog");
    indexingOperation1.setOperation(OperationType.INIT);
    indexingOperationDAO.create(indexingOperation1);
    PortalContainer container = PortalContainer.getInstance();
    entityMgrService = container.getComponentInstanceOfType(EntityManagerService.class);
    entityMgrService.getEntityManager().flush();
    entityMgrService.getEntityManager().refresh(indexingOperation1);
    //When
    indexingOperation1 = indexingOperationDAO.find(indexingOperation1.getId());
    //Then
    Field privateField = IndexingOperation.class.getDeclaredField("timestamp");
    privateField.setAccessible(true);
    Date timestamp = (Date) privateField.get(indexingOperation1);
    assertNotNull(timestamp);
  }

}


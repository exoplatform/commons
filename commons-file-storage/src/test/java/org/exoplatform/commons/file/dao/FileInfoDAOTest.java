/*
 * Copyright (C) 2016 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.commons.file.dao;

import org.exoplatform.commons.file.CommonsJPAIntegrationTest;
import org.exoplatform.commons.file.storage.entity.FileInfoEntity;
import org.exoplatform.commons.file.storage.entity.NameSpaceEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 */
public class FileInfoDAOTest extends CommonsJPAIntegrationTest {
  @Override
  @Before
  public void setUp() {
    super.setUp();
    fileInfoDAO.deleteAll();
    nameSpaceDAO.deleteAll();
  }

  @Override
  @After
  public void tearDown() {
    fileInfoDAO.deleteAll();
    nameSpaceDAO.deleteAll();
  }

  @Test
  public void testFileInfoEntity() {
    NameSpaceEntity nameSpaceEntity = nameSpaceDAO.create(new NameSpaceEntity("file", "default namespace"));
    FileInfoEntity entity = new FileInfoEntity();
    entity.setName("MyDoc.doc");
    entity.setNameSpaceEntity(nameSpaceEntity);
    entity.setChecksum("1225445455");
    entity.setDeleted(false);
    entity.setMimetype("application/doc");
    entity.setSize(1);
    entity.setUpdater("root");
    entity.setUpdatedDate(new Date());

    fileInfoDAO.create(entity);

    List<FileInfoEntity> list = fileInfoDAO.findAll();

    assertEquals(list.size(), 1);
    FileInfoEntity result = list.get(0);
    assertEquals(result.getChecksum(), "1225445455");
    assertEquals(result.getName(), "MyDoc.doc");
    assertEquals(result.isDeleted(), false);
  }

  public void testFindDeletedFile()
  {
    NameSpaceEntity nameSpaceEntity = nameSpaceDAO.create(new NameSpaceEntity("file", "default namespace"));
    FileInfoEntity entity1 = new FileInfoEntity();
    entity1.setName("MyDoc.doc");
    entity1.setNameSpaceEntity(nameSpaceEntity);
    entity1.setUpdatedDate(daysAgo(70));

    fileInfoDAO.create(entity1);

    FileInfoEntity entity2 = new FileInfoEntity();
    entity2.setName("MyDoc1.doc");
    entity2.setNameSpaceEntity(nameSpaceEntity);
    entity2.setDeleted(true);
    entity2.setUpdatedDate(daysAgo(60));

    fileInfoDAO.create(entity2);

    FileInfoEntity entity3 = new FileInfoEntity();
    entity3.setName("MyDoc2.doc");
    entity3.setNameSpaceEntity(nameSpaceEntity);
    entity3.setDeleted(true);
    entity3.setUpdatedDate(daysAgo(15));

    fileInfoDAO.create(entity3);

    List<FileInfoEntity> list = fileInfoDAO.findAll();

    assertEquals(list.size(), 3);

    List<FileInfoEntity> list1 = fileInfoDAO.findDeletedFiles(daysAgo(30));
    assertEquals(1,list1.size());

    FileInfoEntity fileInfoEntity=list1.get(0);
    assertEquals("MyDoc1.doc", fileInfoEntity.getName());
  }

  public void testFindFilesByPage()
  {
    NameSpaceEntity nameSpaceEntity = nameSpaceDAO.create(new NameSpaceEntity("file", "default namespace"));
    List<FileInfoEntity> list = new ArrayList<FileInfoEntity>();
    FileInfoEntity entity;
    for(int i = 0; i< 20; i++)
    {
      entity = new FileInfoEntity();
      entity.setName("MyDoc_"+i+".doc");
      entity.setNameSpaceEntity(nameSpaceEntity);
      entity.setUpdatedDate(daysAgo(70));
      list.add(entity);
    }

    fileInfoDAO.createAll(list);
    List<FileInfoEntity> result = fileInfoDAO.findAllByPage(15, 10);
    assertNotNull (result);
    assertEquals(5,result.size());
  }

  private static Date daysAgo(int days) {
    GregorianCalendar gc = new GregorianCalendar();
    gc.add(Calendar.DATE, -days);
    return gc.getTime();
  }
}

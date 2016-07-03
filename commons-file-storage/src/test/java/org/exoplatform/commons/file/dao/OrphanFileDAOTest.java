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
import org.exoplatform.commons.file.storage.entity.OrphanFileEntity;
import org.exoplatform.commons.file.storage.entity.FileInfoEntity;
import org.exoplatform.commons.file.storage.entity.NameSpaceEntity;
import org.junit.After;
import org.junit.Before;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 */
public class OrphanFileDAOTest extends CommonsJPAIntegrationTest {
  @Override
  @Before
  public void setUp() {
    super.setUp();
    orphanFileDAO.deleteAll();
    fileInfoDAO.deleteAll();
    nameSpaceDAO.deleteAll();
  }

  @Override
  @After
  public void tearDown() {
    orphanFileDAO.deleteAll();
    fileInfoDAO.deleteAll();
    nameSpaceDAO.deleteAll();
  }

  public void testFindDeletedFile() {
    NameSpaceEntity nameSpaceEntity = nameSpaceDAO.create(new NameSpaceEntity("file", "default namespace"));
    FileInfoEntity entity1 = new FileInfoEntity();
    entity1.setName("MyDoc.doc");
    entity1.setNameSpaceEntity(nameSpaceEntity);
    entity1.setUpdatedDate(daysAgo(70));

    fileInfoDAO.create(entity1);
    List<FileInfoEntity> list = fileInfoDAO.findAll();

    assertEquals(list.size(), 1);

    OrphanFileEntity deletedFileEntity = new OrphanFileEntity();
    deletedFileEntity.setDeletedDate(daysAgo(60));
    deletedFileEntity.setFileInfoEntity(entity1);
    deletedFileEntity.setChecksum("d41d8cd98f00b204e9800998ecf8427e");
    orphanFileDAO.create(deletedFileEntity);

    OrphanFileEntity deletedFileEntity1 = new OrphanFileEntity();
    deletedFileEntity1.setDeletedDate(daysAgo(10));
    deletedFileEntity1.setFileInfoEntity(entity1);
    deletedFileEntity1.setChecksum("d41d8cd98f00b204e9800998ecf88899");
    orphanFileDAO.create(deletedFileEntity1);

    List<OrphanFileEntity> list1 = orphanFileDAO.findDeletedFiles(daysAgo(30));
    assertEquals(1, list1.size());

    OrphanFileEntity deletedFileEntity2 = list1.get(0);
    assertEquals("d41d8cd98f00b204e9800998ecf8427e", deletedFileEntity2.getChecksum());
  }

  private static Date daysAgo(int days) {
    GregorianCalendar gc = new GregorianCalendar();
    gc.add(Calendar.DATE, -days);
    return gc.getTime();
  }

}

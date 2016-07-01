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

import java.util.Date;
import java.util.List;

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
  public void testCreateFileInfo() {
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
}

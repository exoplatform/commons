package org.exoplatform.commons.file.dao;

import org.exoplatform.commons.file.CommonsJPAIntegrationTest;
import org.exoplatform.commons.file.storage.entity.FileBinaryEntity;
import org.junit.After;
import org.junit.Before;

import java.util.Date;
import java.util.List;

/**
 * File Binary DAO test class.
 * Created by The eXo Platform SAS
 * Author : eXoPlatform exo@exoplatform.com
 */
public class FileBinaryDAOTest extends CommonsJPAIntegrationTest {
    @Override
    @Before
    public void setUp() {
        super.setUp();
        fileBinaryDAO.deleteAll();
    }

    @Override
    @After
    public void tearDown() {
        fileBinaryDAO.deleteAll();
    }

    public void testFileInfoEntity() {
        Date now = new Date();
        FileBinaryEntity fileBinaryEntity = new FileBinaryEntity();
        fileBinaryEntity.setName("myFile");
        fileBinaryEntity.setData("test".getBytes());
        fileBinaryEntity.setUpdatedDate(now);
        fileBinaryDAO.create(fileBinaryEntity);

        List<FileBinaryEntity> list = fileBinaryDAO.findAll();

        assertEquals(list.size(), 1);
        FileBinaryEntity result = list.get(0);
        assertEquals(result.getName(), "myFile");
        assertEquals(new String(result.getData()), "test");
        assertEquals(result.getUpdatedDate(), now);
    }

    public void testFindFileBlobByName(){
        FileBinaryEntity fileBinaryEntity1 = new FileBinaryEntity();
        fileBinaryEntity1.setName("file-1");

        FileBinaryEntity fileBinaryEntity2 = new FileBinaryEntity();
        fileBinaryEntity2.setName("file-2");

        fileBinaryDAO.create(fileBinaryEntity1);
        fileBinaryDAO.create(fileBinaryEntity2);

        FileBinaryEntity result = fileBinaryDAO.findFileBinaryByName("file-1");
        assertNotNull(result);
        assertEquals(result.getName(),"file-1");

        result = fileBinaryDAO.findFileBinaryByName("file-3");
        assertNull(result);
    }
}

package org.exoplatform.commons.file.services;

import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.model.NameSpace;
import org.exoplatform.commons.file.resource.FileSystemResourceProvider;
import org.exoplatform.commons.file.resource.ResourceProvider;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.impl.FileServiceImpl;
import org.exoplatform.commons.file.storage.DataStorage;
import org.exoplatform.commons.file.storage.dao.OrphanFileDAO;
import org.exoplatform.commons.file.storage.dao.FileInfoDAO;
import org.exoplatform.commons.file.storage.dao.NameSpaceDAO;
import org.exoplatform.commons.file.storage.entity.FileInfoEntity;
import org.exoplatform.commons.file.storage.entity.NameSpaceEntity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class FileServiceImplTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Mock
  private FileInfoDAO fileInfoDAO;

  @Mock
  private NameSpaceDAO nameSpaceDAO;

  @Mock
  private OrphanFileDAO orphanFileDAO;

  @Mock
  private DataStorage jpaDataStorage;

  @Mock
  private NameSpaceService nameSpaceService;

  @Test
  public void shouldReturnFile() throws Exception {
    ResourceProvider resourceProvider = new FileSystemResourceProvider(folder.getRoot().getAbsolutePath());
    // Given
    when(nameSpaceDAO.find(anyLong())).thenReturn(new NameSpaceEntity(1, "file", "Default NameSpace"));
    when(fileInfoDAO.find(anyLong())).thenReturn(new FileInfoEntity(1, "file1", null, 1, null, "", "d41d8cd98f00b204e9800998ecf8427e", false).setNameSpaceEntity(new NameSpaceEntity(1, "file", "Default NameSpace")));
    when(jpaDataStorage.getFileInfo(anyLong())).thenReturn(new FileInfo(1L,"file1",null,"file",1,null,"","d41d8cd98f00b204e9800998ecf8427e", false));
    FileService fileService = new FileServiceImpl(jpaDataStorage, resourceProvider, null);

    // When
    FileItem file = fileService.getFile(1);
    assertNotNull(file);
    assertEquals(1, file.getFileInfo().getId().longValue());
    assertEquals("file1", file.getFileInfo().getName());
  }

  @Test
  public void shouldWriteFile() throws Exception {
    ResourceProvider resourceProvider = new FileSystemResourceProvider(folder.getRoot().getAbsolutePath());
    // Given
    when(fileInfoDAO.create(any(FileInfoEntity.class))).thenReturn(new FileInfoEntity());
    when(jpaDataStorage.create(any(FileInfo.class), any(NameSpace.class))).thenReturn(new FileInfo(1L,"file1",null,"file",1,null,"","d41d8cd98f00b204e9800998ecf8427e", false));
    FileService fileService = new FileServiceImpl(jpaDataStorage, resourceProvider, null);

    // When
    fileService.writeFile(new FileItem(null, "file1", "", null,  1, new Date(), "", false, new ByteArrayInputStream("test".getBytes())));

    // Then
    verify(jpaDataStorage, times(1)).create(any(FileInfo.class),any(NameSpace.class));
  }

  @Test
  public void shouldRollbackFileWriteWhenSomethingGoesWrong() throws Exception {
    ResourceProvider resourceProvider = new FileSystemResourceProvider(folder.getRoot().getAbsolutePath());
    // Given
    when(fileInfoDAO.create(any(FileInfoEntity.class))).thenThrow(Exception.class);
    FileService fileService = new FileServiceImpl(jpaDataStorage,  resourceProvider, null);

    // When
    FileItem file = new FileItem(null, "file1", "plain/text", null,  1, new Date(), "john", false, new ByteArrayInputStream("test".getBytes()));
    FileItem createdFile = null;
    try {
      createdFile = fileService.writeFile(file);
    } catch(Exception e) {
      // expected exception
    }
    assertNull(createdFile);
  }
}
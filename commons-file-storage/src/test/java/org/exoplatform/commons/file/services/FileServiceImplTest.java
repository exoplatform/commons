package org.exoplatform.commons.file.services;

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.storage.FileInfoDAO;
import org.exoplatform.commons.file.storage.FileInfoEntity;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class FileServiceImplTest {

  @Mock
  private FileInfoDAO fileInfoDAO;

  @Mock
  private BinaryProvider binaryProvider;

  @Test
  public void shouldReturnFile() throws Exception {
    // Given
    when(fileInfoDAO.find(anyLong())).thenReturn(new FileInfoEntity(1, "file1", "", 1, null, "", "", false));
    when(binaryProvider.readBinary(any(FileInfo.class))).thenReturn(new ByteArrayInputStream(new byte[] {}));
    FileService fileService = new FileServiceImpl(fileInfoDAO, binaryProvider);

    // When
    FileItem file = fileService.getFile(1);
    assertNotNull(file);
    assertEquals(1, file.getFileInfo().getId().longValue());
    assertEquals("file1", file.getFileInfo().getName());
  }

  @Test
  public void shouldWriteFile() throws Exception {
    // Given
    when(fileInfoDAO.create(any(FileInfoEntity.class))).thenReturn(new FileInfoEntity());
    FileService fileService = new FileServiceImpl(fileInfoDAO, binaryProvider);

    // When
    fileService.writeFile(new FileItem(null, "file1", "", 1, new Date(), "", false, new ByteArrayInputStream("test".getBytes())));

    // Then
    verify(fileInfoDAO, times(1)).create(any(FileInfoEntity.class));
    verify(binaryProvider, times(1)).writeBinary(any(FileItem.class));
  }

  @Test
  public void shouldRollbackFileWriteWhenSomethingGoesWrong() throws Exception {
    // Given
    when(fileInfoDAO.create(any(FileInfoEntity.class))).thenThrow(Exception.class);
    FileService fileService = new FileServiceImpl(fileInfoDAO, binaryProvider);

    // When
    FileItem file = new FileItem(null, "file1", "plain/text", 1, new Date(), "john", false, new ByteArrayInputStream("test".getBytes()));
    FileItem createdFile = null;
    try {
      createdFile = fileService.writeFile(file);
    } catch(Exception e) {
      // expected exception
    }

    // Then
    verify(binaryProvider, times(1)).writeBinary(any(FileItem.class));
    verify(binaryProvider, times(1)).deleteBinary(any(FileInfo.class));
    assertNull(createdFile);
  }
}
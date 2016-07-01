package org.exoplatform.commons.file.services;

import org.exoplatform.commons.file.resource.FileSystemResourceProvider;
import org.exoplatform.commons.file.resource.ResourceProvider;
import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.util.FileChecksum;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

/**
 *
 */
public class FileSystemBinaryProviderTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Before
  public void setup() throws Exception {
    new FileChecksum();
  }

  @Test
  public void shouldReadBinary() throws Exception {

  }

  @Test
  public void shouldWriteBinary() throws Exception {
    // Given
      FileSystemResourceProvider fileResourceProvider = new FileSystemResourceProvider(folder.getRoot().getPath());
    // When
    FileItem file = new FileItem(1L, "file1", "", null,  1, null, "", false, new ByteArrayInputStream(new byte[]{}));
    fileResourceProvider.put(file.getFileInfo().getChecksum(), file.getAsStream());

    // Then
    java.io.File createdFile = fileResourceProvider.getFile(file.getFileInfo().getChecksum());
    assertTrue(createdFile.exists());
  }

  @Test
  public void shouldWriteBinaryWhenFileAlreadyExistsAndBinaryHasChanged() throws Exception {
    // Given
      FileSystemResourceProvider fileResourceProvider = new FileSystemResourceProvider(folder.getRoot().getPath());

    // When
    FileItem file = new FileItem(1L, "file1", "", null, 1, null, "", false, new ByteArrayInputStream("test".getBytes()));
    fileResourceProvider.put(file.getFileInfo().getChecksum(), file.getAsStream());
    java.io.File createdFile = fileResourceProvider.getFile(file.getFileInfo().getChecksum());
    assertTrue(createdFile.exists());
    file.setChecksum(new ByteArrayInputStream("test-updated".getBytes()));
    fileResourceProvider.put(file.getFileInfo().getChecksum(), file.getAsStream());

    // Then
    java.io.File updatedFile = fileResourceProvider.getFile(file.getFileInfo().getChecksum());
    assertNotEquals(updatedFile.getAbsolutePath(), createdFile.getAbsolutePath());
  }

  @Test
  public void shouldNotWriteBinaryWhenFileAlreadyExistsAndBinaryHasNotChanged() throws Exception {
    // Given
      FileSystemResourceProvider fileResourceProvider = new FileSystemResourceProvider(folder.getRoot().getPath());

    // When
    FileItem file = new FileItem(1L, "file1", "", null, 1, null, "", false, new ByteArrayInputStream("test".getBytes()));
    fileResourceProvider.put(file);
    java.io.File createdFile = new java.io.File(fileResourceProvider.getFilePath(file.getFileInfo()));
    assertTrue(createdFile.exists());
    fileResourceProvider.put(file);

    // Then
    java.io.File updatedFile = new java.io.File(fileResourceProvider.getFilePath(file.getFileInfo()));
    assertEquals(updatedFile.getAbsolutePath(), createdFile.getAbsolutePath());
    // TODO need to verify also that it does not effectively write
    //verify(fileInfoDataStorage, times(1)).update(any(FileInfoEntity.class));
  }

 @Test
  public void shouldDeleteBinary() throws Exception {
    // Given
     FileSystemResourceProvider fileResourceProvider = new FileSystemResourceProvider(folder.getRoot().getPath());

    // When
    FileItem file = new FileItem(1L, "file1", "", null, 1, null, "", false, new ByteArrayInputStream("test".getBytes()));
    fileResourceProvider.put(file);
    java.io.File createdFile = new java.io.File(fileResourceProvider.getFilePath(file.getFileInfo()));
    assertTrue(createdFile.exists());
    fileResourceProvider.remove(file.getFileInfo());

    // Then
    java.io.File deletedFile = new java.io.File(fileResourceProvider.getFilePath(file.getFileInfo()));
    assertFalse(deletedFile.exists());
  }

   @Test
  public void shouldThrowExceptionWhenDeletingABinaryWhichDoesNotExist() throws Exception {
    // Given
       FileSystemResourceProvider fileResourceProvider = new FileSystemResourceProvider(folder.getRoot().getPath());

    // When
    FileItem file = new FileItem(1L, "file1", "", null, 1, null, "", false, new ByteArrayInputStream("test".getBytes()));
    exception.expect(FileNotFoundException.class);
    fileResourceProvider.remove(file.getFileInfo());
  }

  @Test
  public void shouldReturnPathWhenChecksumIsValid() throws Exception {
    // Given
      FileSystemResourceProvider fileResourceProvider = new FileSystemResourceProvider(folder.getRoot().getPath());

    // When
    FileInfo fileInfo = new FileInfo(1L, "file1", "", null, 1, null, "", "d41d8cd98f00b204e9800998ecf8427e", false);
    String path = fileResourceProvider.getFilePath(fileInfo);

    // Then
    assertEquals(folder.getRoot().getPath() + "/d/4/1/d/8/c/d/9/d41d8cd98f00b204e9800998ecf8427e", path);
  }

 @Test
  public void shouldReturnNullWhenChecksumIsNotValid() throws Exception {
    // Given
    ResourceProvider fileResourceProvider = new FileSystemResourceProvider(folder.getRoot().getPath());

    // When
    FileInfo fileInfo = new FileInfo(1L, "file1", "", null, 1, null, "", "", false);
    String path = fileResourceProvider.getFilePath(fileInfo);

    // Then
    assertNull(path);
  }
}
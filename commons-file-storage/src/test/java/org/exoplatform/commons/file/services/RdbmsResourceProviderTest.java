package org.exoplatform.commons.file.services;

import org.exoplatform.commons.file.CommonsJPAIntegrationTest;
import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.resource.BinaryProvider;
import org.exoplatform.commons.file.resource.FileUtils;
import org.exoplatform.commons.file.resource.RdbmsResourceProvider;
import org.junit.After;
import org.junit.Before;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import static org.junit.Assert.*;

/**
 * Rdbms Resource Provider test class.
 * Created by The eXo Platform SAS
 * Author : eXoPlatform exo@exoplatform.com
 */
public class RdbmsResourceProviderTest extends CommonsJPAIntegrationTest {

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

  public void testWriteBinary() throws Exception {
    // Given
    RdbmsResourceProvider rdbmsResourceProvider = new RdbmsResourceProvider(fileBinaryDAO);
    // When
    FileItem file = new FileItem(1L, "file1", "", null, 1, null, "", false, new ByteArrayInputStream(new byte[] {}));
    rdbmsResourceProvider.put(file.getFileInfo().getChecksum(), file.getAsStream());

    // Then
    ByteArrayInputStream createdData = (ByteArrayInputStream) rdbmsResourceProvider.getStream(file.getFileInfo().getChecksum());
    assertNotNull(createdData);
  }

  public void testWriteBinaryWhenFileAlreadyExistsAndBinaryHasChanged() throws Exception {
    // Given
    RdbmsResourceProvider rdbmsResourceProvider = new RdbmsResourceProvider(fileBinaryDAO);

    // When
    FileItem file = new FileItem(1L, "file2", "", null, 1, null, "", false, new ByteArrayInputStream("test2".getBytes()));
    rdbmsResourceProvider.put(file);
    ByteArrayInputStream createdData = (ByteArrayInputStream) rdbmsResourceProvider.getStream(file.getFileInfo().getChecksum());
    assertNotNull(createdData);

    InputStream inputStream =new ByteArrayInputStream("test-updated".getBytes());
    file.setInputStream(inputStream);
    file.setChecksum(inputStream);
    rdbmsResourceProvider.put(file);

    // Then
    ByteArrayInputStream updateddData = (ByteArrayInputStream) rdbmsResourceProvider.getStream(file.getFileInfo().getChecksum());
    assertNotEquals(new String(FileUtils.readBytes(updateddData)), new String(FileUtils.readBytes(createdData)));
  }

  public void testFileAlreadyExistsAndBinaryHasNotChanged() throws Exception {
    // Given
    RdbmsResourceProvider rdbmsResourceProvider = new RdbmsResourceProvider(fileBinaryDAO);

    // When
    FileItem file = new FileItem(1L, "file3", "", null, 1, null, "", false, new ByteArrayInputStream("test3".getBytes()));
    rdbmsResourceProvider.put(file);
    String created = rdbmsResourceProvider.getFilePath(file.getFileInfo());
    try {
      rdbmsResourceProvider.put(file);
      fail();
    } catch (Throwable ex) {
      // Expected
    }

    // Then
    String updated = rdbmsResourceProvider.getFilePath(file.getFileInfo());
    assertEquals(updated, created);
  }

  public void testDeleteBinary() throws Exception {
    // Given
    RdbmsResourceProvider rdbmsResourceProvider = new RdbmsResourceProvider(fileBinaryDAO);

    // When
    FileItem file = new FileItem(1L, "file4", "", null, 1, null, "", false, new ByteArrayInputStream("test4".getBytes()));
    rdbmsResourceProvider.put(file);
    String created = rdbmsResourceProvider.getFilePath(file.getFileInfo());
    assertNotNull(created);
    rdbmsResourceProvider.remove(file.getFileInfo());

    // Then
    String deleted = rdbmsResourceProvider.getFilePath(file.getFileInfo());
    assertNull(deleted);
  }

  public void testDeletingABinaryWhichDoesNotExist() throws Exception {
    // Given
    RdbmsResourceProvider rdbmsResourceProvider = new RdbmsResourceProvider(fileBinaryDAO);

    // When
    FileItem file = new FileItem(1L, "file5", "", null, 1, null, "", false, new ByteArrayInputStream("test5".getBytes()));
    boolean deleted = rdbmsResourceProvider.remove(file.getFileInfo());

    // Then
    assertEquals(deleted, false);
  }

  public void shouldReturnNullWhenChecksumIsNotValid() throws Exception {
    // Given
    BinaryProvider binaryProvider = new RdbmsResourceProvider(fileBinaryDAO);

    // When
    FileInfo fileInfo = new FileInfo(1L, "file6", "", null, 1, null, "", "", false);
    String path = binaryProvider.getFilePath(fileInfo);

    // Then
    assertNull(path);
  }
}

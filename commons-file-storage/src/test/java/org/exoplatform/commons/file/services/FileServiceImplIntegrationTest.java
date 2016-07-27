package org.exoplatform.commons.file.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.resource.BinaryProvider;
import org.exoplatform.commons.testing.BaseExoTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;

/**
 * TODO do not use BaseExoTestCase to not be stuck with Junit 3
 */
@ConfiguredBy({ @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/files-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/test-configuration.xml") })
public class FileServiceImplIntegrationTest extends BaseExoTestCase {

  private ExecutorService executorService = Executors.newFixedThreadPool(3);

  private AtomicInteger   counter         = new AtomicInteger(0);

  protected void setUp() throws IOException {
    begin();
  }

  protected void tearDown() {
    end();
  }

  public void testShouldReturnFile() throws Exception {
    FileService fileService = getContainer().getComponentInstanceOfType(FileService.class);
    FileItem createdFile = fileService.writeFile(new FileItem(null,
                                                              "file1",
                                                              "plain/text",
                                                              null,
                                                              1,
                                                              new Date(),
                                                              "john",
                                                              false,
                                                              new ByteArrayInputStream("test".getBytes())));
    FileItem fetchedFile = fileService.getFile(createdFile.getFileInfo().getId());
    assertNotNull(fetchedFile);
    assertEquals("file1", fetchedFile.getFileInfo().getName());
    assertEquals("plain/text", fetchedFile.getFileInfo().getMimetype());
    assertEquals("john", fetchedFile.getFileInfo().getUpdater());
    assertEquals(false, fetchedFile.getFileInfo().isDeleted());
    assertEquals(1, fetchedFile.getFileInfo().getSize());
    assertEquals("file", fetchedFile.getFileInfo().getNameSpace());
    InputStream fileStream = fetchedFile.getAsStream();
    assertNotNull(fileStream);
    assertEquals("test", IOUtils.toString(fileStream));
  }

  public void testUpdateFile() throws Exception {
    FileService fileService = getContainer().getComponentInstanceOfType(FileService.class);
    FileItem createdFile = fileService.writeFile(new FileItem(null,
                                                              "file1",
                                                              "plain/text",
                                                              "file",
                                                              1,
                                                              new Date(),
                                                              "john",
                                                              false,
                                                              new ByteArrayInputStream("test".getBytes())));
    FileItem fetchedFile = fileService.getFile(createdFile.getFileInfo().getId());
    assertNotNull(fetchedFile);

    FileItem updatedSameFile = fileService.updateFile(new FileItem(fetchedFile.getFileInfo().getId(),
                                                                   "file1",
                                                                   "plain/text",
                                                                   "file",
                                                                   1,
                                                                   new Date(),
                                                                   "john",
                                                                   false,
                                                                   new ByteArrayInputStream("test".getBytes())));
    assertNotNull(updatedSameFile);
    assertEquals(fetchedFile.getFileInfo().getChecksum(), updatedSameFile.getFileInfo().getChecksum());

    FileItem updatedNewFile =
                            fileService.updateFile(new FileItem(fetchedFile.getFileInfo().getId(),
                                                                "file1",
                                                                "plain/text",
                                                                "file",
                                                                1,
                                                                new Date(),
                                                                "john",
                                                                false,
                                                                new ByteArrayInputStream("New test".getBytes())));
    assertNotNull(updatedNewFile);
    assertNotSame(fetchedFile.getFileInfo().getChecksum(), updatedNewFile.getFileInfo().getChecksum());
  }

  public void testConcurrentAddFile() throws Exception {
    FileService fileService = getContainer().getComponentInstanceOfType(FileService.class);
    BinaryProvider binaryProvider = getContainer().getComponentInstanceOfType(BinaryProvider.class);
    String text = "Concurrent add test" + System.currentTimeMillis();
    FileItem fileItem = fileService.writeFile(new FileItem(null,
                                                           "file1",
                                                           "plain/text",
                                                           "file",
                                                           1,
                                                           new Date(),
                                                           "john",
                                                           false,
                                                           new ByteArrayInputStream(text.getBytes())));
    for (int i = 0; i < 10; i++) {
      executorService.execute(new Runnable() {
        @Override
        public void run() {
          try {
            fileService.writeFile(new FileItem(null,
                                               "file1",
                                               "plain/text",
                                               "file",
                                               1,
                                               new Date(),
                                               "john",
                                               false,
                                               new ByteArrayInputStream(text.getBytes())));
          } catch (Exception e) {
            fail("Error while adding File: " + e.getMessage());
          } finally {
            counter.incrementAndGet();
          }
        }
      });
    }
    do {
      Thread.sleep(100);
    } while (counter.get() < 10);

    File file = new File(binaryProvider.getFilePath(fileItem.getFileInfo().getChecksum()));
    assertEquals(11, file.getParentFile().list().length);
  }

  public void testConcurrentUpdateFile() throws Exception {
    FileService fileService = getContainer().getComponentInstanceOfType(FileService.class);
    BinaryProvider binaryProvider = getContainer().getComponentInstanceOfType(BinaryProvider.class);
    String text = "Concurrent update test" + System.currentTimeMillis();
    FileItem fileItem = fileService.writeFile(new FileItem(null,
                                                           "file1",
                                                           "plain/text",
                                                           "file",
                                                           1,
                                                           new Date(),
                                                           "john",
                                                           false,
                                                           new ByteArrayInputStream(text.getBytes())));
    for (int i = 0; i < 10; i++) {
      executorService.execute(new Runnable() {
        @Override
        public void run() {
          try {
            fileService.updateFile(new FileItem(fileItem.getFileInfo().getId(),
                                                "file1",
                                                "plain/text",
                                                "file",
                                                1,
                                                new Date(),
                                                "john",
                                                false,
                                                new ByteArrayInputStream(text.getBytes())));
          } catch (Exception e) {
            fail("Error while adding File: " + e.getMessage());
          } finally {
            counter.incrementAndGet();
          }
        }
      });
    }
    do {
      Thread.sleep(100);
    } while (counter.get() < 10);

    File file = new File(binaryProvider.getFilePath(fileItem.getFileInfo().getChecksum()));
    assertEquals(1, file.getParentFile().list().length);
  }
}

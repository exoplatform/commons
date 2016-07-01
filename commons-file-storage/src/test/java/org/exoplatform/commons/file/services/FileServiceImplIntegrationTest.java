package org.exoplatform.commons.file.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import org.apache.commons.io.IOUtils;

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.testing.BaseExoTestCase;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;

/**
 * TODO do not use BaseExoTestCase to not be stuck with Junit 3
 */
@ConfiguredBy({
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/files-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/test-configuration.xml")
})
public class FileServiceImplIntegrationTest extends BaseExoTestCase {

  protected void setUp() throws IOException {
    begin();
  }

  protected void tearDown() {
    end();
  }

  public void testShouldReturnFile() throws Exception {
    FileService fileService = PortalContainer.getInstance().getComponentInstanceOfType(FileService.class);
    FileItem createdFile = fileService.writeFile(new FileItem(null, "file1", "plain/text", null, 1, new Date(), "john", false, new ByteArrayInputStream("test".getBytes())));
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
}

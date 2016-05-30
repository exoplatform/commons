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
    PropertyManager.setProperty("exo.jpa.datasource.name", "java:/comp/env/exo-jpa_portal");
    Path tempDirectory = Files.createTempDirectory("exo-files");
    // TODO this property is not well substituted in kernel component
    PropertyManager.setProperty("exo.files.dir", tempDirectory.toString());
    begin();
  }

  protected void tearDown() {
    end();
  }

  public void testShouldReturnFile() throws Exception {
    // Given
    FileService fileService = PortalContainer.getInstance().getComponentInstanceOfType(FileService.class);
    FileItem createdFile = fileService.writeFile(new FileItem(null, "file1", "plain/text", 1, new Date(), "john", false, new ByteArrayInputStream("test".getBytes())));

    // When
    FileItem fetchedFile = fileService.getFile(createdFile.getFileInfo().getId());
    assertNotNull(fetchedFile);
    assertEquals(1, fetchedFile.getFileInfo().getId().longValue());
    assertEquals("file1", fetchedFile.getFileInfo().getName());
    InputStream fileStream = fetchedFile.getStream();
    assertNotNull(fileStream);
    assertEquals("test", IOUtils.toString(fileStream));
  }
}

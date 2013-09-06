/*
 * Copyright (C) 2003-2010 eXo Product SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.info.test;

import java.io.*;
import java.util.Properties;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;

import org.exoplatform.commons.info.ProductInformations;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.test.BasicTestCase;

public class TestProductInformations extends BasicTestCase {
  private static final String OLD_PRODUCT_INFORMATIONS_FILE = "classpath:/conf/product_old.properties";
  private static final String NEW_PRODUCT_INFORMATIONS_FILE = "classpath:/conf/product_new.properties";

  private static final String OLD_VERSION = "1.0-old";
  private static final String NEW_VERSION = "1.0-new";

  protected final String REPO_NAME = "repository";

  protected final String COLLABORATION_WS = "collaboration";

  protected PortalContainer container;

  protected RepositoryService repositoryService;

  protected ProductInformations productInformations;

  protected ConfigurationManager configurationManager;

  public void setUp() throws Exception {
    container = PortalContainer.getInstance();
    repositoryService = getService(RepositoryService.class);
    productInformations = getService(ProductInformations.class);
    configurationManager = getService(ConfigurationManager.class);
    Session session = null;
    try {
      InputStream oldVersionsContentIS = configurationManager.getInputStream(OLD_PRODUCT_INFORMATIONS_FILE);
      byte[] binaries = new byte[oldVersionsContentIS.available()];
      oldVersionsContentIS.read(binaries);
      String oldVersionsContent = new String(binaries);

      session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);

      Node plfVersionDeclarationNode = getProductVersionNode(session);
      Node plfVersionDeclarationContentNode = plfVersionDeclarationNode.getNode("jcr:content");
      plfVersionDeclarationContentNode.setProperty("jcr:data", oldVersionsContent);

      session.save();
      session.refresh(true);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
    // invoke productInformations() explicitly to store the new version in the JCR
    productInformations.start();
  }

  public void testUpgradeVersion() throws Exception {
    Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);

    assertEquals(productInformations.getVersion(), NEW_VERSION);
    assertEquals(productInformations.getRevision(), NEW_VERSION);
    assertEquals(productInformations.getBuildNumber(), NEW_VERSION);
    assertEquals(productInformations.getVersion("org.exoplatform.ide"), NEW_VERSION);
    assertEquals(productInformations.getVersion("org.exoplatform.social"), NEW_VERSION);
    assertEquals(productInformations.getVersion("org.gatein.portal"), NEW_VERSION);
    assertEquals(productInformations.getVersion("org.exoplatform.ecms"), NEW_VERSION);
    assertEquals(productInformations.getVersion("org.exoplatform.ks"), NEW_VERSION);
    assertEquals(productInformations.getVersion("org.exoplatform.cs"), NEW_VERSION);
    assertEquals(productInformations.getCurrentProductGroupId(), "org.exoplatform.commons");

    assertEquals(productInformations.getPreviousVersion(), OLD_VERSION);
    assertEquals(productInformations.getPreviousRevision(), OLD_VERSION);
    assertEquals(productInformations.getPreviousBuildNumber(), OLD_VERSION);
    assertEquals(productInformations.getPreviousVersion("org.exoplatform.ide"), OLD_VERSION);
    assertEquals(productInformations.getPreviousVersion("org.exoplatform.social"), OLD_VERSION);
    assertEquals(productInformations.getPreviousVersion("org.gatein.portal"), OLD_VERSION);
    assertEquals(productInformations.getPreviousVersion("org.exoplatform.ecms"), OLD_VERSION);
    assertEquals(productInformations.getPreviousVersion("org.exoplatform.ks"), OLD_VERSION);
    assertEquals(productInformations.getPreviousVersion("org.exoplatform.cs"), OLD_VERSION);

    productInformations.storeProductsInformationsInJCR();

    Node plfVersionDeclarationNode = getProductVersionNode(session);
    assertTrue(plfVersionDeclarationNode.isNodeType(ProductInformations.MIX_VERSIONABLE));

    Version version = plfVersionDeclarationNode.getBaseVersion();
    assertNotNull(version);

    String[] versionLabels = plfVersionDeclarationNode.getVersionHistory().getVersionLabels(version);
    assertEquals(versionLabels.length, 1);
    assertEquals(versionLabels[0], OLD_VERSION);

    InputStream newVersionsContentIS = configurationManager.getInputStream(NEW_PRODUCT_INFORMATIONS_FILE);
    byte[] binaries = new byte[newVersionsContentIS.available()];
    newVersionsContentIS.read(binaries);
    Properties properties = new Properties();
    properties.load(new ByteArrayInputStream(binaries));
    String newVersionsContent = getPropertiesAsString(properties);
    newVersionsContent = newVersionsContent.split(ProductInformations.PRODUCT_GROUP_ID)[1];
    Node plfVersionDeclarationContentNode = plfVersionDeclarationNode.getNode("jcr:content");
    String storedVersion = plfVersionDeclarationContentNode.getProperty("jcr:data").getString();
    storedVersion = storedVersion.split(ProductInformations.PRODUCT_GROUP_ID)[1];
    assertEquals(newVersionsContent, storedVersion);
  }

  protected <T> T getService(Class<T> clazz) {
    return clazz.cast(container.getComponentInstanceOfType(clazz));
  }

  private Node getProductVersionNode(Session session) throws PathNotFoundException, RepositoryException {
    Node plfVersionDeclarationNodeContent = ((Node) session
        .getItem("/Application Data/" + ProductInformations.UPGRADE_PRODUCT_SERVICE_NODE_NAME + "/"
            + ProductInformations.PRODUCT_VERSION_DECLARATION_NODE_NAME));
    return plfVersionDeclarationNodeContent;
  }

  private String getPropertiesAsString(Properties properties) {
    StringWriter stringWriter = new StringWriter();
    try {
        properties.store(stringWriter,"ProductInformation");
    } catch (IOException ex) {

    }
    return stringWriter.toString();
  }
}

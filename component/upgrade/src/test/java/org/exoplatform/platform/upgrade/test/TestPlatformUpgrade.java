/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.platform.upgrade.test;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;

import org.exoplatform.component.product.ProductInformations;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.platform.upgrade.UpgradePlatformService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.test.BasicTestCase;

public class TestPlatformUpgrade extends BasicTestCase {
  private static final String OLD_VERSION = "1.0-old";

  protected final String REPO_NAME = "repository";

  protected final String COLLABORATION_WS = "collaboration";

  protected PortalContainer container;

  protected RepositoryService repositoryService;

  protected String currentVersion;

  public void setUp() throws Exception {
    container = PortalContainer.getInstance();
    repositoryService = getService(RepositoryService.class);
    ProductInformations productInformations = getService(ProductInformations.class);
    currentVersion = productInformations.getVersion();
    Session session = null;
    try {
      session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);

      Node plfVersionDeclarationNode = getPlatformVersionNode(session);
      Node plfVersionDeclarationContentNode = plfVersionDeclarationNode.getNode("jcr:content");
      plfVersionDeclarationContentNode.setProperty("jcr:data", OLD_VERSION);

      session.save();
      session.refresh(true);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
    // invoke upgradePlatformService.start() explicitly to store the new version in the JCR
    UpgradePlatformService upgradePlatformService = getService(UpgradePlatformService.class);
    upgradePlatformService.start();
  }

  public void testUpgradeVersion() throws Exception {
    Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);

    Node plfVersionDeclarationNode = getPlatformVersionNode(session);
    assertTrue(plfVersionDeclarationNode.isNodeType(UpgradePlatformService.MIX_VERSIONABLE));

    Version version = plfVersionDeclarationNode.getBaseVersion();
    assertNotNull(version);

    String[] versionLabels = plfVersionDeclarationNode.getVersionHistory().getVersionLabels(version);
    assertNotNull(versionLabels);
    assertEquals(versionLabels.length, 1);
    assertEquals(versionLabels[0], OLD_VERSION);

    Node plfVersionDeclarationContentNode = plfVersionDeclarationNode.getNode("jcr:content");
    String storedVersion = plfVersionDeclarationContentNode.getProperty("jcr:data").getString();
    assertEquals(currentVersion, storedVersion);
  }

  protected <T> T getService(Class<T> clazz) {
    return clazz.cast(container.getComponentInstanceOfType(clazz));
  }

  private Node getPlatformVersionNode(Session session) throws PathNotFoundException, RepositoryException {
    Node plfVersionDeclarationNodeContent = ((Node) session.getItem("/Application Data/"
        + UpgradePlatformService.UPGRADE_PLATFORM_SERVICE_NODE_NAME + "/"
        + UpgradePlatformService.PLATFORM_VERSION_DECLARATION_NODE_NAME));
    return plfVersionDeclarationNodeContent;
  }

}

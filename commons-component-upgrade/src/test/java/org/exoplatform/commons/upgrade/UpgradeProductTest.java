/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.upgrade;

import org.exoplatform.commons.testing.BaseCommonsTestCase;

import java.util.List;
import java.util.Arrays;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.commons.info.MissingProductInformationException;
import org.exoplatform.commons.info.ProductInformations;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com May
 * 31, 2012
 */
public class UpgradeProductTest extends BaseCommonsTestCase {

  private UpgradeProductService service;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.service = (UpgradeProductService) container.getComponentInstanceOfType(UpgradeProductService.class);

  }

  public void testPluginIsEnable() {
    PropertyManager.setProperty("commons.upgrade.portalPlugin.enable", "true");
    PropertyManager.setProperty("commons.upgrade.dummyPlugin.enable", "false");

    InitParams params;
    ValueParam param;

    // Create upgrade plugin for portal
    params = new InitParams();
    param = new ValueParam();
    param.setName("product.group.id");
    param.setValue("org.exoplatform.portal");
    params.addParameter(param);

    param = new ValueParam();
    param.setName("plugin.execution.order");
    param.setValue("1");
    params.addParameter(param);

    UpgradePluginFromVersionZERO upgradePortalPlugin = new UpgradePluginFromVersionZERO(params);
    upgradePortalPlugin.setName("portalUpgrade");

    // Create upgrade plugin for ECMS
    params = new InitParams();
    param = new ValueParam();
    param.setName("product.group.id");
    param.setValue("org.exoplatform.ecms");
    params.addParameter(param);

    param = new ValueParam();
    param.setName("plugin.execution.order");
    param.setValue("2");
    params.addParameter(param);

    UpgradePluginFromVersionX upgradeECMSPlugin = new UpgradePluginFromVersionX(params);
    upgradeECMSPlugin.setName("ecmsUpgrade");

    // Creare a dummy plugin: is not enabled
    UpgradeProductPlugin dummyPlugin = new UpgradeProductPlugin(params) {

      @Override
      public boolean shouldProceedToUpgrade(String previousVersion, String newVersion) {
        return true;
      }

      @Override
      public void processUpgrade(String oldVersion, String newVersion) {
        // Do Nothing
      }
    };
    dummyPlugin.setName("dummyPlugin");

    // Property enable = true
    assertTrue(upgradePortalPlugin.isEnabled());
    // Property enable = null
    assertTrue(upgradeECMSPlugin.isEnabled());
    // Set property enable = false
    assertFalse(dummyPlugin.isEnabled());

  }

  public void testProcessUpgrade() throws PathNotFoundException,
                                  RepositoryException,
                                  RepositoryConfigurationException,
                                  MissingProductInformationException {

    ProductInformations prodInfo = (ProductInformations) container.getComponentInstanceOfType(ProductInformations.class);

    String portalVersion = prodInfo.getVersion("org.gatein.portal");
    String portalPrevVersion = prodInfo.getPreviousVersion("org.gatein.portal");

    String ecmsVersion = prodInfo.getVersion("org.exoplatform.ecms");
    String ecmsPrevVersion = prodInfo.getPreviousVersion("org.exoplatform.ecms");

    // Node has been changed by upgrade-plugins
    Node upgradeNode = getUpgradeProductTestNode();

    // Get all version labels are set
    List<String> versionLabels = Arrays.asList(upgradeNode.getVersionHistory().getVersionLabels());

    // Verify upgrade portal plugin: only upgrade from version 0
    assertEquals(portalPrevVersion.equals("0"),
                 versionLabels.contains(portalVersion + "-ZERO-SNAPSHOT"));
    assertEquals(portalPrevVersion.equals("0"), versionLabels.contains(portalVersion + "-ZERO"));
    assertEquals(portalPrevVersion.equals("0"), upgradeNode.hasNode("upgradeFromZERO"));

    // Verify upgrade ecms plugin: only upgrade from version != 0
    assertEquals(!ecmsPrevVersion.equals("0"), versionLabels.contains(ecmsVersion + "-X-SNAPSHOT"));
    assertEquals(!ecmsPrevVersion.equals("0"), versionLabels.contains(ecmsVersion + "-X"));
    assertEquals(!ecmsPrevVersion.equals("0"), upgradeNode.hasNode("upgradeFrom" + ecmsPrevVersion));

  }

  @Override
  public void tearDown() {
  }

  private static Node getUpgradeProductTestNode() throws RepositoryException,
                                                 RepositoryConfigurationException {

    PortalContainer container = PortalContainer.getInstance();

    NodeHierarchyCreator nodeHierarchyCreator = (NodeHierarchyCreator) container.getComponentInstanceOfType(NodeHierarchyCreator.class);
    String upgradeNodePath = nodeHierarchyCreator.getJcrPath("upgradeProductTest");

    RepositoryService repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    Session session = repositoryService.getRepository("repository").getSystemSession("portal-test");
    return (Node) session.getItem(upgradeNodePath);

  }

  public static class UpgradePluginFromVersionZERO extends UpgradeProductPlugin {

    public UpgradePluginFromVersionZERO(InitParams initParams) {
      super(initParams);
    }

    @Override
    public void processUpgrade(String oldVersion, String newVersion) {
      try {
        Node upgradeNode = getUpgradeProductTestNode();
        addNodeVersion(upgradeNode, newVersion + "-ZERO-SNAPSHOT");
        upgradeNode.save();

        upgradeNode.addNode("upgradeFromZERO");
        upgradeNode.save();
        addNodeVersion(upgradeNode, newVersion + "-ZERO");
        upgradeNode.save();
        upgradeNode.getSession().save();
      } catch (RepositoryException e) {
        fail();
      } catch (RepositoryConfigurationException e) {
      }
    }

    @Override
    public boolean shouldProceedToUpgrade(String newVersion, String previousVersion) {
      if (previousVersion.equals("0"))
        return true;
      return false;
    }

  }

  public static class UpgradePluginFromVersionX extends UpgradeProductPlugin {

    public UpgradePluginFromVersionX(InitParams initParams) {
      super(initParams);
    }

    @Override
    public void processUpgrade(String oldVersion, String newVersion) {
      try {
        Node upgradeNode = getUpgradeProductTestNode();
        addNodeVersion(upgradeNode, newVersion + "-X-SNAPSHOT");
        upgradeNode.save();

        upgradeNode.addNode("upgradeFrom" + oldVersion);
        upgradeNode.save();
        addNodeVersion(upgradeNode, newVersion + "-X");
        upgradeNode.save();
        upgradeNode.getSession().save();

      } catch (RepositoryException e) {
        e.printStackTrace();
      } catch (RepositoryConfigurationException e) {
        e.printStackTrace();
      }
    }

    @Override
    public boolean shouldProceedToUpgrade(String newVersion, String previousVersion) {
      if (!previousVersion.equals("0"))
        return true;
      return false;
    }
  }

}

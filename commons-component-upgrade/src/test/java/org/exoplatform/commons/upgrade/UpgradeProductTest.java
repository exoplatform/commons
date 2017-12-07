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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.info.MissingProductInformationException;
import org.exoplatform.commons.info.ProductInformations;
import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com May
 * 31, 2012
 */
@ConfiguredBy({ @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/test-configuration.xml") })

public class UpgradeProductTest extends BaseCommonsTestCase {
  private static final String    OLD_PRODUCT_INFORMATIONS_FILE = "classpath:/conf/data/product_old.properties";

  private static final String    NEW_PRODUCT_INFORMATIONS_FILE = "classpath:/conf/data/product_new.properties";

  private static final String    NEWER_PRODUCT_INFORMATIONS_FILE = "classpath:/conf/data/product_newer.properties";

  protected RepositoryService    repositoryService;

  private ProductInformations    productInformations;

  private SettingService         settingService;

  private UpgradeProductService  upgradeService;

  protected ConfigurationManager configurationManager;

  public UpgradeProductTest() {
    setForceContainerReload(true);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.upgradeService = getContainer().getComponentInstanceOfType(UpgradeProductService.class);
    this.productInformations = getService(ProductInformations.class);
    this.settingService = getService(SettingService.class);
    this.repositoryService = getService(RepositoryService.class);
    this.configurationManager = getService(ConfigurationManager.class);
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

    productInformations.start();
    upgradeService.start();

    String portalVersion = productInformations.getVersion("org.gatein.portal");
    String portalPrevVersion = productInformations.getPreviousVersion("org.gatein.portal");

    String ecmsVersion = productInformations.getVersion("org.exoplatform.ecms");
    String ecmsPrevVersion = productInformations.getPreviousVersion("org.exoplatform.ecms");

    // Node has been changed by upgrade-plugins
    Node upgradeNode = getUpgradeProductTestNode();

    assertTrue("Node is not versionnable", upgradeNode.isNodeType("mix:versionable"));

    // Get all version labels are set
    List<String> versionLabels = Arrays.asList(upgradeNode.getVersionHistory().getVersionLabels());

    // Verify upgrade portal plugin: only upgrade from version 0
    assertEquals(portalPrevVersion.equals("0"), versionLabels.contains(portalVersion + "-ZERO-SNAPSHOT"));
    assertEquals(portalPrevVersion.equals("0"), versionLabels.contains(portalVersion + "-ZERO"));
    assertEquals(portalPrevVersion.equals("0"), upgradeNode.hasNode("upgradeFromZERO"));

    // Verify upgrade ecms plugin: only upgrade from version != 0
    assertEquals(!ecmsPrevVersion.equals("0"), versionLabels.contains(ecmsVersion + "-X-SNAPSHOT"));
    assertEquals(!ecmsPrevVersion.equals("0"), versionLabels.contains(ecmsVersion + "-X"));
    assertEquals(!ecmsPrevVersion.equals("0"), upgradeNode.hasNode("upgradeFrom" + ecmsPrevVersion));
  }

  public void testUpgradeWithTargetVersion() {
    productInformations.setFirstRun(false);

    // Create upgrade plugin for ECMS
    InitParams params = new InitParams();
    ValueParam param = new ValueParam();
    param.setName("product.group.id");
    param.setValue("org.exoplatform.social");
    params.addParameter(param);

    param = new ValueParam();
    param.setName("plugin.execution.order");
    param.setValue("2");
    params.addParameter(param);

    param = new ValueParam();
    param.setName(UpgradeProductPlugin.UPGRADE_PLUGIN_TARGET_PARAMETER);
    param.setValue("1.0-M3");
    params.addParameter(param);

    UpgradePluginWithTargetVersion upgradeProductPlugin = new UpgradePluginWithTargetVersion(params);
    upgradeProductPlugin.setName("UpgradePluginWithTargetVersion");

    assertEquals("1.0-M3", upgradeProductPlugin.getTargetVersion());

    upgradeService.addUpgradePlugin(upgradeProductPlugin);

    try {
      resetPreviousProductInformation(OLD_PRODUCT_INFORMATIONS_FILE);
    } catch (Exception e) {
      fail(e);
    }

    // invoke productInformations() explicitly to store the new version in the
    // JCR
    productInformations.start();
    upgradeService.start();

    assertEquals(1, UpgradePluginWithTargetVersion.COUNT.get());
    assertTrue(UpgradePluginWithTargetVersion.PROCESSED);

    UpgradePluginWithTargetVersion.PROCESSED = false;

    // invoke productInformations() explicitly to store the new version in the
    // JCR
    productInformations.start();
    upgradeService.start();

    assertFalse(UpgradePluginWithTargetVersion.PROCESSED);
  }

  public void testUpgradeAsynchronous() {
    productInformations.setFirstRun(false);

    // Create upgrade plugin for ECMS
    InitParams params = new InitParams();
    ValueParam param = new ValueParam();
    param.setName("product.group.id");
    param.setValue("org.exoplatform.social");
    params.addParameter(param);

    param = new ValueParam();
    param.setName("plugin.execution.order");
    param.setValue("2");
    params.addParameter(param);

    UpgradePluginAsynchronous upgradeProductPlugin = new UpgradePluginAsynchronous(params);

    assertFalse(upgradeProductPlugin.isAsyncUpgradeExecution());

    param = new ValueParam();
    param.setName(UpgradeProductPlugin.UPGRADE_PLUGIN_ASYNC);
    param.setValue("true");
    params.addParameter(param);
    upgradeProductPlugin = new UpgradePluginAsynchronous(params);

    assertTrue(upgradeProductPlugin.isAsyncUpgradeExecution());

    upgradeProductPlugin.setName("UpgradePluginAsynchronous");
    upgradeService.addUpgradePlugin(upgradeProductPlugin);

    try {
      resetPreviousProductInformation(OLD_PRODUCT_INFORMATIONS_FILE);
    } catch (Exception e) {
      fail(e);
    }

    // Lock current thread twice to wait until Upgrade plugin has finished its
    // execution
    upgradeProductPlugin.executeParentThread.lock();

    // Interrupt Upgrade plugin execution
    upgradeProductPlugin.executePluginLock.lock();
    try {
      // invoke productInformations() explicitly to store the new version in the
      // JCR
      productInformations.start();
      upgradeService.start();

      assertFalse(UpgradePluginAsynchronous.PROCESSED);
    } finally {
      // Proceed Upgrade plugin execution
      upgradeProductPlugin.executePluginLock.unlock();
    }

    // Force wait until the Upgrade Task finishes its execution
    upgradeProductPlugin.executeParentThread.lock();
    upgradeProductPlugin.executeParentThread.unlock();

    assertEquals(1, UpgradePluginAsynchronous.COUNT.get());
    assertTrue(UpgradePluginAsynchronous.PROCESSED);

    // Start test for the second Upgrade Plugin execution

    UpgradePluginAsynchronous.PROCESSED = false;
    updateNewProductionInformations(NEWER_PRODUCT_INFORMATIONS_FILE);

    // Lock current thread twice to wait until Upgrade plugin has finished its
    // execution
    upgradeProductPlugin.executeParentThread.lock();

    // Interrupt Upgrade plugin execution
    upgradeProductPlugin.executePluginLock.lock();

    try {
      // invoke productInformations() explicitly to store the new version in the
      // JCR
      productInformations.start();
      upgradeService.start();
    } finally {
      // Proceed Upgrade plugin execution
      upgradeProductPlugin.executePluginLock.unlock();
    }

    upgradeProductPlugin.executeParentThread.lock();
    upgradeProductPlugin.executeParentThread.unlock();

    assertEquals(2, UpgradePluginAsynchronous.COUNT.get());
    assertTrue(UpgradePluginAsynchronous.PROCESSED);
  }

  public void testUpgradeExecutedOnce() {
    productInformations.setFirstRun(false);

    // Create upgrade plugin for ECMS
    InitParams params = new InitParams();
    ValueParam param = new ValueParam();
    param.setName("product.group.id");
    param.setValue("org.exoplatform.social");
    params.addParameter(param);

    param = new ValueParam();
    param.setName("plugin.execution.order");
    param.setValue("2");
    params.addParameter(param);

    UpgradePluginExecutedOnce upgradeProductPlugin = new UpgradePluginExecutedOnce(params);
    assertFalse(upgradeProductPlugin.isExecuteOnlyOnce());

    param = new ValueParam();
    param.setName(UpgradeProductPlugin.UPGRADE_PLUGIN_EXECUTE_ONCE_PARAMETER);
    param.setValue("true");
    params.addParameter(param);
    upgradeProductPlugin = new UpgradePluginExecutedOnce(params);

    assertTrue(upgradeProductPlugin.isExecuteOnlyOnce());

    upgradeProductPlugin.setName("UpgradePluginExecutedOnce");
    upgradeService.addUpgradePlugin(upgradeProductPlugin);

    try {
      resetPreviousProductInformation(OLD_PRODUCT_INFORMATIONS_FILE);
    } catch (Exception e) {
      fail(e);
    }

    // invoke productInformations() explicitly to store the new version in the
    // JCR
    productInformations.start();
    upgradeService.start();

    assertTrue(UpgradePluginExecutedOnce.PROCESSED);

    UpgradePluginExecutedOnce.PROCESSED = false;

    // invoke productInformations() explicitly to store the new version in the
    // JCR
    productInformations.start();
    upgradeService.start();

    assertFalse(UpgradePluginExecutedOnce.PROCESSED);
  }

  public void testUpgradeErrorFirstCallWithTargetVersion() {
    productInformations.setFirstRun(false);

    // Create upgrade plugin for ECMS
    InitParams params = new InitParams();
    ValueParam param = new ValueParam();
    param.setName("product.group.id");
    param.setValue("org.exoplatform.social");
    params.addParameter(param);

    param = new ValueParam();
    param.setName("plugin.execution.order");
    param.setValue("2");
    params.addParameter(param);

    param = new ValueParam();
    param.setName(UpgradeProductPlugin.UPGRADE_PLUGIN_EXECUTE_ONCE_PARAMETER);
    param.setValue("true");
    params.addParameter(param);

    param = new ValueParam();
    param.setName(UpgradeProductPlugin.UPGRADE_PLUGIN_TARGET_PARAMETER);
    param.setValue("1.0-M5");
    params.addParameter(param);

    UpgradePluginErrorFirstCall.PROCESSED = false;
    UpgradePluginErrorFirstCall.COUNT = new AtomicLong(0);

    UpgradePluginErrorFirstCall upgradeProductPlugin = new UpgradePluginErrorFirstCall(params);
    upgradeProductPlugin.setName("UpgradePluginErrorFirstCallWithTargetVersion");
    upgradeService.addUpgradePlugin(upgradeProductPlugin);

    try {
      resetPreviousProductInformation(OLD_PRODUCT_INFORMATIONS_FILE);
    } catch (Exception e) {
      fail(e);
    }

    // invoke productInformations() explicitly to store the new version in the
    // JCR
    productInformations.start();
    upgradeService.start();

    assertFalse(UpgradePluginErrorFirstCall.PROCESSED);
    assertEquals(1, UpgradePluginErrorFirstCall.COUNT.get());

    // invoke productInformations() explicitly to store the new version in the
    // JCR
    productInformations.start();
    upgradeService.start();

    assertEquals(2, UpgradePluginErrorFirstCall.COUNT.get());
    assertTrue(UpgradePluginErrorFirstCall.PROCESSED);
  }

  public void testUpgradeErrorFirstCall() {
    productInformations.setFirstRun(false);

    // Create upgrade plugin for ECMS
    InitParams params = new InitParams();
    ValueParam param = new ValueParam();
    param.setName("product.group.id");
    param.setValue("org.exoplatform.social");
    params.addParameter(param);

    param = new ValueParam();
    param.setName("plugin.execution.order");
    param.setValue("2");
    params.addParameter(param);

    param = new ValueParam();
    param.setName(UpgradeProductPlugin.UPGRADE_PLUGIN_EXECUTE_ONCE_PARAMETER);
    param.setValue("true");
    params.addParameter(param);

    UpgradePluginErrorFirstCall.PROCESSED = false;
    UpgradePluginErrorFirstCall.COUNT = new AtomicLong(0);

    UpgradePluginErrorFirstCall upgradeProductPlugin = new UpgradePluginErrorFirstCall(params);
    upgradeProductPlugin.setName("UpgradePluginErrorFirstCall");
    upgradeService.addUpgradePlugin(upgradeProductPlugin);

    try {
      resetPreviousProductInformation(OLD_PRODUCT_INFORMATIONS_FILE);
    } catch (Exception e) {
      fail(e);
    }

    // invoke productInformations() explicitly to store the new version in the
    // JCR
    productInformations.start();
    upgradeService.start();

    assertFalse(UpgradePluginErrorFirstCall.PROCESSED);
    assertEquals(1, UpgradePluginErrorFirstCall.COUNT.get());

    // invoke productInformations() explicitly to store the new version in the
    // JCR
    productInformations.start();
    upgradeService.start();

    assertEquals(2, UpgradePluginErrorFirstCall.COUNT.get());
    assertTrue(UpgradePluginErrorFirstCall.PROCESSED);
  }

  public void testUpgradeStatus() {
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
    param.setValue("org.exoplatform.platform");
    params.addParameter(param);

    SettingService settingService = container.getComponentInstanceOfType(SettingService.class);
    assertNotNull("SettingService is not configured", settingService);

    UpgradePluginStatus upgradeStatus = new UpgradePluginStatus(settingService, params);
    upgradeStatus.setName("statusUpgrade");

    assertTrue("Status should be != COMPLETED", upgradeStatus.shouldProceedToUpgrade("", ""));
    upgradeStatus.processUpgrade("", "");
    assertTrue("Status should be != COMPLETED even after upgrade completion AND UpdateStatusAfterUpgrade = false",
               upgradeStatus.shouldProceedToUpgrade("", ""));
    upgradeStatus.setUpdateStatusAfterUpgrade(true);
    upgradeStatus.processUpgrade("", "");
    assertFalse("Status should be == COMPLETED after upgrade completion AND UpdateStatusAfterUpgrade = true",
                upgradeStatus.shouldProceedToUpgrade("", ""));
  }

  @Override
  public void tearDown() {
    try {
      Session session = repositoryService.getCurrentRepository().getSystemSession(productInformations.getWorkspaceName());
      Node plfVersionDeclarationNode = getProductVersionNode(session);
      plfVersionDeclarationNode.remove();
      session.save();

      Node upgradeProductTestNode = getUpgradeProductTestNode();
      Session testSession = upgradeProductTestNode.getSession();
      upgradeProductTestNode.remove();
      testSession.save();

      updateNewProductionInformations(NEW_PRODUCT_INFORMATIONS_FILE);
      settingService.remove(UpgradeProductService.UPGRADE_PRODUCT_CONTEXT);
    } catch (Exception e) {
      fail(e);
    }
  }

  private static Node getUpgradeProductTestNode() throws RepositoryException, RepositoryConfigurationException {
    PortalContainer container = PortalContainer.getInstance();

    ProductInformations productInformations = container.getComponentInstanceOfType(ProductInformations.class);
    NodeHierarchyCreator nodeHierarchyCreator =
                                              (NodeHierarchyCreator) container.getComponentInstanceOfType(NodeHierarchyCreator.class);
    String upgradeNodePath = nodeHierarchyCreator.getJcrPath("upgradeProductTest");

    RepositoryService repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    Session session = repositoryService.getCurrentRepository().getSystemSession(productInformations.getWorkspaceName());
    return (Node) session.getItem(upgradeNodePath);
  }

  public static class UpgradePluginAsynchronous extends UpgradeProductPlugin {

    public static AtomicLong COUNT               = new AtomicLong(0);

    public static boolean    PROCESSED;

    public Lock              executeParentThread = new Lock();

    public Lock              executePluginLock   = new Lock();

    public UpgradePluginAsynchronous(InitParams initParams) {
      super(initParams);
    }

    @Override
    public void processUpgrade(String oldVersion, String newVersion) {
      executePluginLock.lock();
      try {
        PROCESSED = true;
        COUNT.incrementAndGet();
      } finally {
        executePluginLock.unlock();
        executeParentThread.unlock();
      }
    }
  }

  public static class UpgradePluginWithTargetVersion extends UpgradeProductPlugin {

    public static final AtomicLong COUNT = new AtomicLong(0);

    public static boolean          PROCESSED;

    public UpgradePluginWithTargetVersion(InitParams initParams) {
      super(initParams);
    }

    @Override
    public void processUpgrade(String oldVersion, String newVersion) {
      PROCESSED = true;
      COUNT.incrementAndGet();
    }
  }

  public static class UpgradePluginExecutedOnce extends UpgradeProductPlugin {

    public static boolean PROCESSED;

    public UpgradePluginExecutedOnce(InitParams initParams) {
      super(initParams);
    }

    @Override
    public void processUpgrade(String oldVersion, String newVersion) {
      PROCESSED = true;
    }
  }

  public static class UpgradePluginErrorFirstCall extends UpgradeProductPlugin {

    public static AtomicLong COUNT = new AtomicLong(0);

    public static boolean          PROCESSED;

    public UpgradePluginErrorFirstCall(InitParams initParams) {
      super(initParams);
    }

    @Override
    public void processUpgrade(String oldVersion, String newVersion) {
      if (COUNT.incrementAndGet() == 1) {
        throw new RuntimeException("EXPECTED EXCEPTION");
      }
      PROCESSED = true;
    }
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
        fail(e);
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

  public static class UpgradePluginFromVersionONE extends UpgradeProductPlugin {

    public UpgradePluginFromVersionONE(InitParams initParams) {
      super(initParams);
    }

    @Override
    public void processUpgrade(String oldVersion, String newVersion) {
      try {
        Node upgradeNode = getUpgradeProductTestNode();
        addNodeVersion(upgradeNode, newVersion + "-ONE-SNAPSHOT");
        upgradeNode.save();

        upgradeNode.addNode("upgradeFromONE");
        upgradeNode.save();
        addNodeVersion(upgradeNode, newVersion + "-ONE");
        upgradeNode.save();
        upgradeNode.getSession().save();
      } catch (RepositoryException e) {
        fail(e);
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

  public static class UpgradePluginStatus extends UpgradeProductPlugin {

    private static final String MIGRATION_STATUS_COMPLETED = "COMPLETED";

    private static final String MIGRATION_STATUS           = "Migration_STATUS";

    boolean                     updateStatusAfterUpgrade   = false;

    public UpgradePluginStatus(SettingService settingService, InitParams initParams) {
      super(settingService, initParams);
    }

    @Override
    public void processUpgrade(String oldVersion, String newVersion) {
      try {
        if (updateStatusAfterUpgrade) {
          storeValueForPlugin(MIGRATION_STATUS, MIGRATION_STATUS_COMPLETED);
        }
      } catch (Exception e) {
        fail(e);
      }
    }

    @Override
    public boolean shouldProceedToUpgrade(String newVersion, String previousVersion) {
      String migrationstatus = getValue(MIGRATION_STATUS);
      return StringUtils.isBlank(migrationstatus) || !MIGRATION_STATUS_COMPLETED.equals(migrationstatus);
    }

    public void setUpdateStatusAfterUpgrade(boolean updateStatusAfterUpgrade) {
      this.updateStatusAfterUpgrade = updateStatusAfterUpgrade;
    }

    public boolean isUpdateStatusAfterUpgrade() {
      return updateStatusAfterUpgrade;
    }
  }

  public static class Lock {
    private boolean locked = false;

    public synchronized void lock() {
      try {
        while (locked) {
          wait();
        }
      } catch (InterruptedException e) {
        throw new RuntimeException("Wait was interrupted unexpectly", e);
      }
      locked = true;
    }

    public synchronized void unlock() {
      locked = false;
      notify();
    }

    public boolean isLocked() {
      return locked;
    }
  }

  private void resetPreviousProductInformation(String filePath) throws Exception {
    Session session = null;
    try {
      InputStream oldVersionsContentIS = configurationManager.getInputStream(filePath);
      byte[] binaries = new byte[oldVersionsContentIS.available()];
      oldVersionsContentIS.read(binaries);
      String oldVersionsContent = new String(binaries);

      session = repositoryService.getCurrentRepository().getSystemSession(productInformations.getWorkspaceName());

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
  }

  private void updateNewProductionInformations(String filePath) {
    try {
      InputStream newVersionsContentIS = configurationManager.getInputStream(filePath);
      byte[] binaries = new byte[newVersionsContentIS.available()];
      newVersionsContentIS.read(binaries);
      Properties properties = new Properties();
      properties.load(new ByteArrayInputStream(binaries));
      productInformations.setProductInformationProperties(properties);
    } catch (Exception e) {
      fail(e);
    }
  }

  private Node getProductVersionNode(Session session) throws PathNotFoundException, RepositoryException {
    Node plfVersionDeclarationNodeContent = ((Node) session.getItem(productInformations.getProductVersionDeclarationNodePath()));
    return plfVersionDeclarationNodeContent;
  }
}

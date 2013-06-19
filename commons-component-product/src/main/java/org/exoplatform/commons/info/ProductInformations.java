/**
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.commons.info;

import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

import javax.jcr.*;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import java.io.*;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author <a href="mailto:anouar.chattouna@exoplatform.com">Anouar
 *         Chattouna</a>
 * @version $Revision$
 */
public class ProductInformations implements Startable {
  public static final String ENTERPRISE_EDITION = "ENTERPRISE";

  public static final String EXPRESS_EDITION = "EXPRESS";

  public static final String EDITION = "edition";

  public static final String KEY_GENERATION_DATE = "key generation date";

  public static final String DELAY = "delay";

  public static final String PRODUCT_CODE = "productCode";

  public static final String PRODUCT_KEY = "productKey";

  public static final String NB_USERS = "number of users";

  public static final String PRODUCT_GROUP_ID = "product.groupId";

  public static final String PRODUCT_REVISION = "product.revision";

  public static final String PRODUCT_BUILD_NUMBER = "product.buildNumber";

  public static final String PRODUCT_VERSIONS_DECLARATION_FILE = "product.versions.declaration.file";

  public static final String PROCEED_UPGRADE_FIRST_TIME_KEY = "proceedUpgradeFirstTime";

  public static final String WORKING_WORSPACE_NAME = "working.worspace.name";

  
  /**
   * Constant that will be used in nodeHierarchyCreator.getJcrPath: it
   * represents the Application data root node Alias
   */
  public static final String EXO_APPLICATIONS_DATA_NODE_ALIAS = "exoApplicationDataNode";

  /**
   * MixinType that will activate the versioning on a selected node
   */
  public static final String MIX_VERSIONABLE = "mix:versionable";

  /**
   * Service application data node name
   */
  public static final String UPGRADE_PRODUCT_SERVICE_NODE_NAME = "ProductInformationsService";

  /**
   * node name where the Product version declaration is
   */
  public static final String PRODUCT_VERSION_DECLARATION_NODE_NAME = "productVersionDeclarationNode";

  private static final Log LOG = ExoLogger.getLogger(ProductInformations.class);

  private Properties productInformationProperties = new Properties();
  private Properties previousProductInformationProperties = new Properties();

  private String workspaceName = null;
  private String applicationDataRootNodePath = null;
  private String productVersionDeclarationNodePath = null;
  private boolean firstRun = false;

  private RepositoryService repositoryService = null;
  private SessionProviderService sessionProviderService = null;

  public ProductInformations(RepositoryService repositoryService, SessionProviderService sessionProviderService,
                             NodeHierarchyCreator nodeHierarchyCreator, ConfigurationManager cmanager, InitParams initParams) {
    this.repositoryService = repositoryService;
    this.sessionProviderService = sessionProviderService;
    if (!initParams.containsKey(PRODUCT_VERSIONS_DECLARATION_FILE)) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Couldn't find the init value param: " + PRODUCT_VERSIONS_DECLARATION_FILE);
      }
      return;
    }
    String filePath = initParams.getValueParam(PRODUCT_VERSIONS_DECLARATION_FILE).getValue();
    try {
      if (LOG.isInfoEnabled()) {
        LOG.info("Read products versions from " + filePath);
      }
      InputStream inputStream = cmanager.getInputStream(filePath);
      productInformationProperties.load(inputStream);
    } catch (IOException exception) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Couldn't parse the file " + filePath, exception);
      }
      return;
    } catch (Exception exception) {
      //ConfigurationManager.getInputStream() throws Exception().
      //It's from another project and we cannot modify it. So we have to catch Exception
      if (LOG.isErrorEnabled()) {
        LOG.error("Error occured while reading the file " + filePath, exception);
      }
      return;
    }

    // The workspace name where products informations are stored
    if (initParams.containsKey(WORKING_WORSPACE_NAME)) {
      workspaceName = initParams.getValueParam(WORKING_WORSPACE_NAME).getValue();
    }

    applicationDataRootNodePath = nodeHierarchyCreator.getJcrPath(EXO_APPLICATIONS_DATA_NODE_ALIAS);
    if (applicationDataRootNodePath == null) {
      if (LOG.isErrorEnabled()) {
        LOG.error(EXO_APPLICATIONS_DATA_NODE_ALIAS + " wasn't found as 'NodeHierarchyCreator' alias");
      }
      return;
    }
    if (applicationDataRootNodePath.indexOf("/") == 0) {
      applicationDataRootNodePath = applicationDataRootNodePath.replaceFirst("/", "");
    }
    productVersionDeclarationNodePath = "/" + applicationDataRootNodePath + "/" + UPGRADE_PRODUCT_SERVICE_NODE_NAME + "/"
            + PRODUCT_VERSION_DECLARATION_NODE_NAME;
  }

  public String getEdition() throws MissingProductInformationException {
    return previousProductInformationProperties.getProperty(EDITION);
  }

  public String getNumberOfUsers() throws MissingProductInformationException {
    return previousProductInformationProperties.getProperty(NB_USERS);
  }

  public String getDateOfLicence() throws MissingProductInformationException {
    return previousProductInformationProperties.getProperty(KEY_GENERATION_DATE);
  }

  public String getDuration() throws MissingProductInformationException {
    return previousProductInformationProperties.getProperty(DELAY);
  }

  public String getProductCode() throws MissingProductInformationException {
    return previousProductInformationProperties.getProperty(PRODUCT_CODE);
  }

  public String getProductKey() throws MissingProductInformationException {
    return previousProductInformationProperties.getProperty(PRODUCT_KEY);
  }



  /**
   * @return This method returns the current product's version.
   */
  public String getVersion() throws MissingProductInformationException {
    return getVersion(getCurrentProductGroupId());
  }

  /**
   * @return This method return the product version, selected by its maven
   *         groupId.
   */
  public String getVersion(String productGroupId) throws MissingProductInformationException {
    if (!productInformationProperties.containsKey(productGroupId)) {
      throw new MissingProductInformationException(productGroupId);
    }
    return productInformationProperties.getProperty(productGroupId);
  }

  /**
   * @return the product.buildNumber property value.
   */
  public String getBuildNumber() throws MissingProductInformationException {
    if (!productInformationProperties.containsKey(PRODUCT_BUILD_NUMBER)) {
      throw new MissingProductInformationException(PRODUCT_BUILD_NUMBER);
    }
    return productInformationProperties.getProperty(PRODUCT_BUILD_NUMBER);
  }

  /**
   * @return the product.revision property value.
   */
  public String getRevision() throws MissingProductInformationException {
    if (!productInformationProperties.containsKey(PRODUCT_REVISION)) {
      throw new MissingProductInformationException(PRODUCT_REVISION);
    }
    return productInformationProperties.getProperty(PRODUCT_REVISION);
  }

  /**
   * @return the current product's maven group id.
   */
  public String getCurrentProductGroupId() throws MissingProductInformationException {
    if (!productInformationProperties.containsKey(PRODUCT_GROUP_ID)) {
      throw new MissingProductInformationException(PRODUCT_GROUP_ID);
    }
    return productInformationProperties.getProperty(PRODUCT_GROUP_ID);
  }

  /**
   * @return the platform.version property. This method return the platform
   *         version.
   */
  public String getPreviousVersion() throws MissingProductInformationException {
    return getPreviousVersion(getCurrentProductGroupId());
  }

  /**
   * @return the platform.version property. This method return the platform
   *         version.
   */
  public String getPreviousVersion(String productGroupId) throws MissingProductInformationException {
    if (!previousProductInformationProperties.containsKey(productGroupId)) {
      throw new MissingProductInformationException(productGroupId);
    }
    return previousProductInformationProperties.getProperty(productGroupId);
  }

  /**
   * @return an empty string if the properties file is not found, otherwise
   *         the platform.buildNumber property. This method return the
   *         build number of the platform.
   */
  public String getPreviousBuildNumber() throws MissingProductInformationException {
    if (!previousProductInformationProperties.containsKey(PRODUCT_BUILD_NUMBER)) {
      throw new MissingProductInformationException(PRODUCT_BUILD_NUMBER);
    }
    return previousProductInformationProperties.getProperty(PRODUCT_BUILD_NUMBER);
  }

  /**
   * @return the value of product.revision property. This method return the
   *         current revison of the platform.
   */
  public String getPreviousRevision() throws MissingProductInformationException {
    if (!previousProductInformationProperties.containsKey(PRODUCT_REVISION)) {
      throw new MissingProductInformationException(PRODUCT_REVISION);
    }
    return previousProductInformationProperties.getProperty(PRODUCT_REVISION);
  }

  public boolean isFirstRun() {
    return this.firstRun;
  }

  /**
   * This method loads from the JCR the stored products versions. If it's
   * the first server start up, then store the declared one.
   */
  public void start() {
    if (LOG.isDebugEnabled()) {
      LOG.debug("start method begin");
    }
    if (workspaceName == null || workspaceName.equals("")) {
      try {
        workspaceName = repositoryService.getDefaultRepository().getConfiguration().getDefaultWorkspaceName();
        if (LOG.isInfoEnabled()){
          LOG.info("Workspace wasn't specified, use '" + workspaceName + "' as default workspace of this repository.");
        }
      } catch (RepositoryException exception) {
        LOG.error("Error occured while getting default workspace name.", exception);
        return;
      } catch (RepositoryConfigurationException exception) {
        LOG.error("Error occured while getting default workspace name.", exception);
        return;
      }
    }
    Session session = null;
    try {
      // Get a JCR Session
      session = getSession();

      // get "Application Data" node
      Node applicationDataNode = null;
      if (session.getRootNode().hasNode(applicationDataRootNodePath)) {
        applicationDataNode = session.getRootNode().getNode(applicationDataRootNodePath);
      } else {
        if (LOG.isDebugEnabled()) {
          LOG.debug("'Application Data' doesn't exist, creating it ... ");
        }
        applicationDataNode = session.getRootNode().addNode(applicationDataRootNodePath, "nt:unstructured");
        applicationDataNode.addMixin("exo:hiddenable");
      }

      // This node's path is
      // "WS_NAME:/exo;services/ProductInformationsService/productVersionDeclarationNode"
      Node productVersionDeclarationNode = null;
      if (applicationDataNode.hasNode(UPGRADE_PRODUCT_SERVICE_NODE_NAME)) {// reads
        // from
        // the
        // JCR
        // the
        // previous
        // version
        productVersionDeclarationNode = applicationDataNode.getNode(UPGRADE_PRODUCT_SERVICE_NODE_NAME + "/"
                + PRODUCT_VERSION_DECLARATION_NODE_NAME);
        // get the previous product informations, stored in a JCR property,
        // this will be by example:
        // "WS_NAME:/exo:services/ProductInformationsService/productVersionDeclarationNode/jcr:content/jcr:data"
        String previousVersionData = ((Property) session.getItem(productVersionDeclarationNode.getPath()
                + "/jcr:content/jcr:data")).getString();
        previousProductInformationProperties = new Properties();
        previousProductInformationProperties.load(new ByteArrayInputStream(previousVersionData.getBytes()));
      } else {// This is the first time that this Service starts up
        if (LOG.isDebugEnabled()) {
          LOG.debug("Product server first run: setup product Version Declaration Node");
        }
        firstRun = true;
        Node UpgradeProductServiceNode = applicationDataNode.addNode(UPGRADE_PRODUCT_SERVICE_NODE_NAME, "nt:unstructured");
        productVersionDeclarationNode = UpgradeProductServiceNode.addNode(PRODUCT_VERSION_DECLARATION_NODE_NAME, "nt:file");
        Node productVersionDeclarationNodeContent = productVersionDeclarationNode.addNode("jcr:content", "nt:resource");
        productVersionDeclarationNodeContent.setProperty("jcr:encoding", "UTF-8");
        productVersionDeclarationNodeContent.setProperty("jcr:mimeType", "text/plain");
        productVersionDeclarationNodeContent.setProperty("jcr:data", getCurrentProductInformationsAsString());
        productVersionDeclarationNodeContent.setProperty("jcr:lastModified", new Date().getTime());
        if (!productVersionDeclarationNode.isNodeType(MIX_VERSIONABLE)) {
          productVersionDeclarationNode.addMixin(MIX_VERSIONABLE);
        }
        session.save();
        session.refresh(true);
        previousProductInformationProperties = (Properties)productInformationProperties.clone();
      }
    } catch (LoginException exception) {
      LOG.error("Can't load product informations from the JCR: Error when getting JCR session.", exception);
      return;
    } catch (NoSuchWorkspaceException exception) {
      LOG.error("Can't load product informations from the JCR: Error when getting JCR session.", exception);
      return;
    } catch (RepositoryException exception) {
      LOG.error("Can't load product informations from the JCR!", exception);
      return;
    } catch (IOException exception) {
      LOG.error("Can't load product informations from the JCR: the data stored in the JCR couldn't be parsed.", exception);
      return;
    } finally {
      if (session != null) {
        session.logout();
      }
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("start method end");
    }
  }

  /**
   * This method is called by eXo Kernel when stopping the parent
   * ExoContainer
   */
  public void stop() {}

  public void storeUnlockInformation(){
    Session session = null;
    try {
      session = getSession();
      Node productVersionDeclarationNode = (Node) session.getItem(productVersionDeclarationNodePath);
      Node productVersionDeclarationNodeContent = productVersionDeclarationNode.getNode("jcr:content");
      productVersionDeclarationNodeContent.setProperty("jcr:data", getPreviousProductInformationsAsString() );
      productVersionDeclarationNodeContent.setProperty("jcr:lastModified", new Date().getTime());
      productVersionDeclarationNode.getSession().save();
    } catch (LoginException exception) {
      LOG.error("Can't store informations in the JCR: Error when getting JCR session.", exception);
    } catch (NoSuchWorkspaceException exception) {
      LOG.error("Can't store informations in the JCR: Error when getting JCR session.", exception);
    } catch (RepositoryException exception) {
      LOG.error("Can't store informations in the JCR!", exception);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  public void storeProductsInformationsInJCR() {
    Session session = null;
    try {
      session = getSession();
      Node productVersionDeclarationNode = (Node) session.getItem(productVersionDeclarationNodePath);

      // Add a version of product informations node, with the previous
      // informations
          Version version = productVersionDeclarationNode.checkin();
      productVersionDeclarationNode.getSession().save();
      VersionHistory versionHistory = productVersionDeclarationNode.getVersionHistory();
      String  versionLabel = null;
      if(versionHistory.hasVersionLabel(getPreviousVersion())) {

          versionLabel = getPreviousVersion()+"-"+currentFlag();

      } else {

          versionLabel = getPreviousVersion();
      }
      versionHistory.addVersionLabel(version.getName(), versionLabel , false);

      productVersionDeclarationNode.checkout();

      // Update the content of the product informations node, with the new
      // content
      Node productVersionDeclarationNodeContent = productVersionDeclarationNode.getNode("jcr:content");
      productVersionDeclarationNodeContent.setProperty("jcr:data", getCurrentProductInformationsAsString());
      productVersionDeclarationNodeContent.setProperty("jcr:lastModified", new Date().getTime());
      productVersionDeclarationNode.getSession().save();
    } catch (LoginException exception) {
      LOG.error("Can't store informations in the JCR: Error when getting JCR session.", exception);
    } catch (NoSuchWorkspaceException exception) {
      LOG.error("Can't store informations in the JCR: Error when getting JCR session.", exception);
    } catch (RepositoryException exception) {
      LOG.error("Can't store informations in the JCR!", exception);
    } catch (MissingProductInformationException exception) {
      LOG.error("Can't read current product version!", exception);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  private String getCurrentProductInformationsAsString() {
    StringWriter stringWriter = new StringWriter();
    String info = "";
    try {
      if(previousProductInformationProperties.containsKey(PRODUCT_KEY))
        productInformationProperties.setProperty(PRODUCT_KEY,getProductKey());
      if(previousProductInformationProperties.containsKey(PRODUCT_CODE))
        productInformationProperties.setProperty(PRODUCT_CODE, getProductCode());
      if(previousProductInformationProperties.containsKey(EDITION))
        productInformationProperties.setProperty(EDITION, getEdition());
      if(previousProductInformationProperties.containsKey(KEY_GENERATION_DATE))
        productInformationProperties.setProperty(KEY_GENERATION_DATE, getDateOfLicence());
      if(previousProductInformationProperties.containsKey(DELAY))
        productInformationProperties.setProperty(DELAY, getDuration());
      if(previousProductInformationProperties.containsKey(NB_USERS))
        productInformationProperties.setProperty(NB_USERS, getNumberOfUsers());
    } catch (MissingProductInformationException e) {
      LOG.info("it's a locked instance!");
    }
      try {
          productInformationProperties.store(stringWriter,"ProductInformation");
          info = stringWriter.toString();

      }   catch (IOException ex) {
          LOG.error("cannot load properties");

      }

      return info;
  }

  private String getPreviousProductInformationsAsString() {
    StringWriter stringWriter = new StringWriter();
      String info = "";
      if(previousProductInformationProperties.containsKey("--")) previousProductInformationProperties.remove("--");
      try {
          previousProductInformationProperties.store(stringWriter,"ProductInformation");
          info = stringWriter.toString();

      } catch (IOException ex) {
          LOG.error("cannot load properties");
      }

      return info;
  }

  public void setUnlockInformation(Properties unlockInformation) {
    previousProductInformationProperties.setProperty(EDITION, (String) unlockInformation.get(EDITION));
    previousProductInformationProperties.setProperty(NB_USERS, (String) unlockInformation.get(NB_USERS));
    previousProductInformationProperties.setProperty(PRODUCT_KEY, (String) unlockInformation.get(PRODUCT_KEY));
    previousProductInformationProperties.setProperty(PRODUCT_CODE, (String) unlockInformation.get(PRODUCT_CODE));
    previousProductInformationProperties.setProperty(DELAY, (String) unlockInformation.get(DELAY));
    previousProductInformationProperties.setProperty(KEY_GENERATION_DATE, (String) unlockInformation.get(KEY_GENERATION_DATE));
  }

  public String getWorkspaceName() {
    return workspaceName;
  }

  private Session getSession() throws RepositoryException, LoginException, NoSuchWorkspaceException {
    SessionProvider sessionProvider = sessionProviderService.getSystemSessionProvider(null);
    ManageableRepository repository = repositoryService.getCurrentRepository();
    return sessionProvider.getSession(workspaceName, repository);
  }

  public String getProductVersionDeclarationNodePath() {
    return this.productVersionDeclarationNodePath;
  }

  public void setPreviousVersionsIfFirstRun(String defaultVersion) {
    if (isFirstRun()) {
      previousProductInformationProperties = (Properties)productInformationProperties.clone();
      Set<?> keys = previousProductInformationProperties.keySet();
      for (Object key : keys) {
        previousProductInformationProperties.setProperty((String) key, defaultVersion);
      }
    }
  }

  private static int currentFlag () {

      return Calendar.getInstance(TimeZone.getDefault()).get(Calendar.YEAR);

  }

}

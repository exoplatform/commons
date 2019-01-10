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

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.settings.jpa.entity.SettingsEntity;
import org.picocontainer.Startable;

import javax.jcr.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:anouar.chattouna@exoplatform.com">Anouar
 *         Chattouna</a>
 * @version $Revision$
 */
public class ProductInformations implements Startable {

  public static final String     product_Information                   = "ProductInformation";

  public static final String     ENTERPRISE_EDITION                    = "ENTERPRISE";

  public static final String     EXPRESS_EDITION                       = "EXPRESS";

  public static final String     EDITION                               = "edition";

  public static final String     KEY_GENERATION_DATE                   = "key generation date";

  public static final String     DELAY                                 = "delay";

  public static final String     PRODUCT_CODE                          = "productCode";

  public static final String     PRODUCT_KEY                           = "productKey";

  public static final String     NB_USERS                              = "number of users";

  public static final String     PRODUCT_GROUP_ID                      = "product.groupId";

  public static final String     PRODUCT_REVISION                      = "product.revision";

  public static final String     PRODUCT_BUILD_NUMBER                  = "product.buildNumber";

  public static final String     WORKING_WORSPACE_NAME                 = "working.worspace.name";

  public static final String     PRODUCT_VERSIONS_DECLARATION_FILE     = "product.versions.declaration.file";

  /**
   * Constant that will be used in nodeHierarchyCreator.getJcrPath: it represents
   * the Application data root node Alias
   */
  public static final String     EXO_APPLICATIONS_DATA_NODE_ALIAS      = "exoApplicationDataNode";

  /**
   * Service application data node name
   */
  public static final String     UPGRADE_PRODUCT_SERVICE_NODE_NAME     = "ProductInformationsService";

  /**
   * node name where the Product version declaration is
   */
  public static final String     PRODUCT_VERSION_DECLARATION_NODE_NAME = "productVersionDeclarationNode";

  private static final Log       LOG                                   = ExoLogger.getLogger(ProductInformations.class);

  public String                  applicationDataRootNodePath           = null;

  private String                 productVersionDeclarationNodePath     = null;

  private String                 workspaceName                         = null;

  private Properties             productInformationProperties          = new Properties();

  private Map<String, String>    productInformation                    = new HashMap<>();

  private boolean                firstRun                              = false;

  private SettingService         settingService;

  private NodeHierarchyCreator   nodeHierarchyCreator;

  private RepositoryService      repositoryService                     = null;

  private SessionProviderService sessionProviderService                = null;

  public ProductInformations(ConfigurationManager cmanager,
                             NodeHierarchyCreator nodeHierarchyCreator,
                             InitParams initParams,
                             SettingService settingService,
                             SessionProviderService sessionProviderService,
                             RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
    this.sessionProviderService = sessionProviderService;
    this.settingService = settingService;
    this.nodeHierarchyCreator = nodeHierarchyCreator;
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
      // ConfigurationManager.getInputStream() throws Exception().
      // It's from another project and we cannot modify it. So we have to catch
      // Exception
      if (LOG.isErrorEnabled()) {
        LOG.error("Error occured while reading the file " + filePath, exception);
      }
      return;
    }
    if (initParams.containsKey(WORKING_WORSPACE_NAME)) {
      workspaceName = initParams.getValueParam(WORKING_WORSPACE_NAME).getValue();
    }
  }

  public String getEdition() throws MissingProductInformationException {
    return productInformation.get(EDITION);
  }

  public String getNumberOfUsers() throws MissingProductInformationException {
    return productInformation.get(NB_USERS);
  }

  public String getDateOfLicence() throws MissingProductInformationException {
    return productInformation.get(KEY_GENERATION_DATE);
  }

  public String getDuration() throws MissingProductInformationException {
    return productInformation.get(DELAY);
  }

  public String getProductCode() throws MissingProductInformationException {
    return productInformation.get(PRODUCT_CODE);
  }

  public String getProductKey() throws MissingProductInformationException {
    return productInformation.get(PRODUCT_KEY);
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
    if (!productInformation.containsKey(productGroupId)) {
      throw new MissingProductInformationException(productGroupId);
    }
    return productInformation.get(productGroupId);
  }

  /**
   * @return an empty string if the properties file is not found, otherwise the
   *         platform.buildNumber property. This method return the build number of
   *         the platform.
   */
  public String getPreviousBuildNumber() throws MissingProductInformationException {
    if (!productInformation.containsKey(PRODUCT_BUILD_NUMBER)) {
      throw new MissingProductInformationException(PRODUCT_BUILD_NUMBER);
    }
    return productInformation.get(PRODUCT_BUILD_NUMBER);
  }

  /**
   * @return the value of product.revision property. This method return the
   *         current revison of the platform.
   */
  public String getPreviousRevision() throws MissingProductInformationException {
    if (!productInformation.containsKey(PRODUCT_REVISION)) {
      throw new MissingProductInformationException(PRODUCT_REVISION);
    }
    return productInformation.get(PRODUCT_REVISION);
  }

  public boolean isFirstRun() {
    return this.firstRun;
  }

  /**
   * This method migrate from the JCR the stored products versions to JPA. If it's
   * the first server start up, then store the declared one.
   */
  public void start() {
    try {
      migrateProductInformation();
      // Load product information from DB
      Map<String, SettingValue> productInformationSettings =
                                                           settingService.getSettingsByContextAndScope(Context.GLOBAL.getName(),
                                                                                                       Context.GLOBAL.getId(),
                                                                                                       Scope.APPLICATION.getName(),
                                                                                                       product_Information);
      if (productInformationSettings != null && !productInformationSettings.isEmpty()) {
        productInformationSettings.entrySet()
                                  .stream()
                                  .forEach(e -> productInformation.put(e.getKey(), e.getValue().getValue().toString()));

      } else {// This is the first time that this Service starts up
        LOG.info("Platform first run - init and store product Information");
        firstRun = true;
        // Store product information properties in DB
        initProductInformation(productInformationProperties);
        storeProductInformation(productInformation);
      }
    } catch (Exception e) {
      LOG.error("Error while starting product information service - Cause : " + e.getMessage(), e);
    }
  }

  /**
   * This method is called by eXo Kernel when stopping the parent ExoContainer
   */
  public void stop() {
  }

  public void storeProductInformation(Map<String, String> map) {
    try {
      for (Map.Entry<String, String> entry : map.entrySet()) {
        settingService.set(Context.GLOBAL,
                           Scope.APPLICATION.id(product_Information),
                           entry.getKey(),
                           SettingValue.create(entry.getValue()));
      }
    } catch (Exception e) {
      LOG.error("Error while storing product informations    - Cause : " + e.getMessage(), e);
    }
  }

  public void initProductInformation(Properties properties) {
    properties.entrySet().stream().forEach(entry -> productInformation.put((String) entry.getKey(), (String) entry.getValue()));
  }

  /**
   * Migrate product information from JCR to RDBMS
   */
  public void migrateProductInformation() {
    if (workspaceName == null || workspaceName.equals("")) {
      try {
        workspaceName = repositoryService.getCurrentRepository().getConfiguration().getDefaultWorkspaceName();
        if (LOG.isInfoEnabled()) {
          LOG.info("Workspace wasn't specified, use '" + workspaceName + "' as default workspace of this repository.");
        }
      } catch (RepositoryException exception) {
        LOG.error("Error occured while getting default workspace name.", exception);
        return;
      }
    }
    Session session = null;
    try {
      session = getSession();
      applicationDataRootNodePath = nodeHierarchyCreator.getJcrPath(EXO_APPLICATIONS_DATA_NODE_ALIAS);
      productVersionDeclarationNodePath = applicationDataRootNodePath + "/" + UPGRADE_PRODUCT_SERVICE_NODE_NAME + "/"
          + PRODUCT_VERSION_DECLARATION_NODE_NAME;
      if (session.itemExists(productVersionDeclarationNodePath)) {
        Node productVersionDeclarationNode = (Node) session.getItem(productVersionDeclarationNodePath);
        Node productVersionDeclarationNodeContent = productVersionDeclarationNode.getNode("jcr:content");
        String data = productVersionDeclarationNodeContent.getProperty("jcr:data").getString();
        Properties jcrInformation = new Properties();
        jcrInformation.load(new ByteArrayInputStream(data.getBytes()));
        initProductInformation(jcrInformation);
        storeProductInformation(productInformation);
        productVersionDeclarationNode.getParent().remove();
        LOG.info("productVersionDeclaration node removed!");
        session.save();
      } else {
        LOG.info("No product information to migrate from JCR to RDBMS");
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
    }
  }

  public void setUnlockInformation(Properties unlockInformation) {
    productInformation.put(EDITION, (String) unlockInformation.get(EDITION));
    productInformation.put(NB_USERS, (String) unlockInformation.get(NB_USERS));
    productInformation.put(PRODUCT_KEY, (String) unlockInformation.get(PRODUCT_KEY));
    productInformation.put(PRODUCT_CODE, (String) unlockInformation.get(PRODUCT_CODE));
    productInformation.put(DELAY, (String) unlockInformation.get(DELAY));
    productInformation.put(KEY_GENERATION_DATE, (String) unlockInformation.get(KEY_GENERATION_DATE));
  }

  public void setPreviousVersionsIfFirstRun(String defaultVersion) {
    if (isFirstRun()) {
      initProductInformation(productInformationProperties);
      productInformation.forEach((key, value) -> productInformation.put(key, defaultVersion));
    }
  }

  public String getWorkspaceName() {
    return workspaceName;
  }

  public String getProductVersionDeclarationNodePath() {
    return this.productVersionDeclarationNodePath;
  }

  private static String currentFlag() {
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    return dateFormat.format(Calendar.getInstance(TimeZone.getDefault()).getTime());
  }

  public void setProductInformationProperties(Properties productInformationProperties) {
    this.productInformationProperties = productInformationProperties;
  }

  public Properties getProductInformationProperties() {
    return productInformationProperties;
  }

  public Map<String, String> getProductInformation() {
    return productInformation;
  }

  public void setFirstRun(boolean firstRun) {
    this.firstRun = firstRun;
  }

  private Session getSession() throws RepositoryException, LoginException, NoSuchWorkspaceException {
    SessionProvider sessionProvider = sessionProviderService.getSystemSessionProvider(null);
    ManageableRepository repository = repositoryService.getCurrentRepository();
    return sessionProvider.getSession(workspaceName, repository);
  }

}

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

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.picocontainer.Startable;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * @author <a href="mailto:anouar.chattouna@exoplatform.com">Anouar
 *         Chattouna</a>
 * @version $Revision$
 */
public class ProductInformations implements Startable {
  public static final String  PRODUCT_INFORMATION                   = "ProductInformation";

  public static final String  ENTERPRISE_EDITION                    = "ENTERPRISE";

  public static final String  EXPRESS_EDITION                       = "EXPRESS";

  public static final String  EDITION                               = "edition";

  public static final String  KEY_GENERATION_DATE                   = "key generation date";

  public static final String  DELAY                                 = "delay";

  public static final String  PRODUCT_CODE                          = "productCode";

  public static final String  PRODUCT_KEY                           = "productKey";

  public static final String  NB_USERS                              = "number of users";

  public static final String  PRODUCT_GROUP_ID                      = "product.groupId";

  public static final String  PRODUCT_REVISION                      = "product.revision";

  public static final String  PRODUCT_BUILD_NUMBER                  = "product.buildNumber";

  public static final String  WORKING_WORSPACE_NAME                 = "working.worspace.name";

  public static final String  PRODUCT_VERSIONS_DECLARATION_FILE     = "product.versions.declaration.file";

  /**
   * Service application data node name
   */
  public static final String  UPGRADE_PRODUCT_SERVICE_NODE_NAME     = "ProductInformationsService";

  /**
   * node name where the Product version declaration is
   */
  public static final String  PRODUCT_VERSION_DECLARATION_NODE_NAME = "productVersionDeclarationNode";

  private static final Log    LOG                                   = ExoLogger.getLogger(ProductInformations.class);

  private Properties          productInformationProperties          = new Properties();

  private Map<String, String> productInformation                    = new HashMap<>();

  private boolean             firstRun                              = false;

  private SettingService      settingService;

  public ProductInformations(ConfigurationManager cmanager,
                             InitParams initParams,
                             SettingService settingService) {
    this.settingService = settingService;
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
    } catch (Exception exception) {
      // ConfigurationManager.getInputStream() throws Exception().
      // It's from another project and we cannot modify it. So we have to catch
      // Exception
      if (LOG.isErrorEnabled()) {
        LOG.error("Error occured while reading the file " + filePath, exception);
      }
    }
  }

  public String getEdition() {
    return productInformation.get(EDITION);
  }

  public String getNumberOfUsers() {
    return productInformation.get(NB_USERS);
  }

  public String getDateOfLicence() {
    return productInformation.get(KEY_GENERATION_DATE);
  }

  public String getDuration() {
    return productInformation.get(DELAY);
  }

  public String getProductCode() {
    return productInformation.get(PRODUCT_CODE);
  }

  public String getProductKey() {
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
   *         platform.buildNumber property. This method return the build number
   *         of the platform.
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
   * This service will store the declared product versions at startup.
   */
  @SuppressWarnings("rawtypes")
  public void start() {
    try {
      // Load product information from DB
      Map<String, SettingValue> productInformationSettings = settingService.getSettingsByContextAndScope(Context.GLOBAL.getName(),
                                                                                                         Context.GLOBAL.getId(),
                                                                                                         Scope.APPLICATION.getName(),
                                                                                                         PRODUCT_INFORMATION);
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
    // NOthing to stop
  }

  public void storeProductInformation(Map<String, String> map) {
    try {
      for (Map.Entry<String, String> entry : map.entrySet()) {
        settingService.set(Context.GLOBAL,
                           Scope.APPLICATION.id(PRODUCT_INFORMATION),
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

}

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

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.codec.binary.StringUtils;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.info.ProductInformations;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.test.BasicTestCase;

public class TestProductInformations extends BasicTestCase {

  private static final String    OLD_PRODUCT_INFORMATIONS_FILE = "classpath:/conf/product_old.properties";

  private static final String    OLD_VERSION                   = "1.0";

  private static final String    NEW_VERSION                   = "2.0";

  protected PortalContainer      container;

  protected ProductInformations  productInformations;

  protected ConfigurationManager configurationManager;

  protected SettingService       settingService;

  @Override
  public void setUp() {
    container = PortalContainer.getInstance();
    configurationManager = getService(ConfigurationManager.class);
    settingService = getService(SettingService.class);

    InitParams initParams = new InitParams();
    ValueParam fileParam = new ValueParam();
    fileParam.setName(ProductInformations.PRODUCT_VERSIONS_DECLARATION_FILE);
    fileParam.setValue("classpath:/conf/product_new.properties");
    initParams.addParam(fileParam);
    ValueParam wsParam = new ValueParam();
    wsParam.setName(ProductInformations.WORKING_WORSPACE_NAME);
    wsParam.setValue("portal-test");
    initParams.addParam(wsParam);

    productInformations = new ProductInformations(configurationManager,
                                                  initParams,
                                                  settingService);

  }

  public void testUnlockInformation() {
    // Given
    Properties p = new Properties();
    p.setProperty(ProductInformations.EDITION, "edition");
    p.setProperty(ProductInformations.NB_USERS, "1");
    p.setProperty(ProductInformations.KEY_GENERATION_DATE, "0");
    p.setProperty(ProductInformations.DELAY, "0");
    p.setProperty(ProductInformations.PRODUCT_CODE, "0");
    p.setProperty(ProductInformations.PRODUCT_KEY, "0");

    // When
    productInformations.setUnlockInformation(p);

    // Then
    assertEquals(productInformations.getEdition(), "edition");
    assertEquals(productInformations.getNumberOfUsers(), "1");
    assertEquals(productInformations.getDateOfLicence(), "0");
    assertEquals(productInformations.getDuration(), "0");
    assertEquals(productInformations.getProductCode(), "0");
    assertEquals(productInformations.getProductKey(), "0");
  }

  @SuppressWarnings("rawtypes")
  public void testShouldUpdateProductInformationMapWhenProductInformationExistsInDB() throws Exception {
    // Given
    Properties properties = new Properties();
    InputStream oldVersionsContent = configurationManager.getInputStream(OLD_PRODUCT_INFORMATIONS_FILE);
    properties.load(oldVersionsContent);
    productInformations.initProductInformation(properties);
    productInformations.storeProductInformation(productInformations.getProductInformation());

    // When
    productInformations.start();

    // Then
    Map<String, SettingValue> productInformationSettings = getProductInformationFromJPA();
    assertFalse(productInformations.isFirstRun());
    assertNotNull(productInformationSettings);
    assertFalse(productInformationSettings.isEmpty());
    productInformationSettings.entrySet()
                              .stream()
                              .filter(entry -> entry.getKey().startsWith("org.exoplatform"))
                              .forEach(entry -> assertEquals("'" + entry.getKey() + "' doesn't have an expected value",
                                                             OLD_VERSION,
                                                             entry.getValue().getValue()));

    assertEquals(productInformations.getPreviousVersion(), OLD_VERSION);
    assertEquals(productInformations.getPreviousRevision(), OLD_VERSION);
    assertEquals(productInformations.getPreviousBuildNumber(), OLD_VERSION);
    assertEquals(productInformations.getPreviousVersion("org.exoplatform.social"), OLD_VERSION);
    assertEquals(productInformations.getPreviousVersion("org.gatein.portal"), OLD_VERSION);

    assertEquals(productInformations.getVersion(), NEW_VERSION);
    assertEquals(productInformations.getRevision(), NEW_VERSION);
    assertEquals(productInformations.getBuildNumber(), NEW_VERSION);
    assertEquals(productInformations.getVersion("org.exoplatform.social"), NEW_VERSION);
    assertEquals(productInformations.getVersion("org.gatein.portal"), NEW_VERSION);
    assertEquals(productInformations.getCurrentProductGroupId(), "org.exoplatform.commons");

    // clean environment
    settingService.remove(Context.GLOBAL, Scope.APPLICATION.id(ProductInformations.PRODUCT_INFORMATION));
    Map<String, SettingValue> productInformationStgs = getProductInformationFromJPA();
    assertTrue(productInformationStgs.isEmpty());
  }

  @SuppressWarnings("rawtypes")
  public void testShouldStoreProductInformationInJPAWhenStartedWithEmptyDB() throws Exception { // NOSONAR
    // Given

    // When
    productInformations.start();

    // Then
    Map<String, SettingValue> productInformationSettings = getProductInformationFromJPA();
    assertTrue(productInformations.isFirstRun());
    assertNotNull(productInformationSettings);
    assertFalse(productInformationSettings.isEmpty());
    productInformationSettings.entrySet()
                              .stream()
                              .filter(entry -> !"product.groupId".equals(entry.getKey()))
                              .forEach(entry -> assertEquals(entry.getValue().getValue(), NEW_VERSION));

    assertEquals(productInformations.getPreviousVersion(), NEW_VERSION);
    assertEquals(productInformations.getPreviousRevision(), NEW_VERSION);
    assertEquals(productInformations.getPreviousBuildNumber(), NEW_VERSION);
    assertEquals(productInformations.getPreviousVersion("org.exoplatform.social"), NEW_VERSION);
    assertEquals(productInformations.getPreviousVersion("org.gatein.portal"), NEW_VERSION);

    // Assert productInformation loaded from properties file
    assertEquals(productInformations.getVersion(), NEW_VERSION);
    assertEquals(productInformations.getRevision(), NEW_VERSION);
    assertEquals(productInformations.getBuildNumber(), NEW_VERSION);
    assertEquals(productInformations.getVersion("org.exoplatform.social"), NEW_VERSION);
    assertEquals(productInformations.getVersion("org.gatein.portal"), NEW_VERSION);
    assertEquals(productInformations.getCurrentProductGroupId(), "org.exoplatform.commons");

    // clean environment
    settingService.remove(Context.GLOBAL, Scope.APPLICATION.id(ProductInformations.PRODUCT_INFORMATION));
    Map<String, SettingValue> productInformationStgs = getProductInformationFromJPA();
    assertTrue(productInformationStgs.isEmpty());

  }

  protected <T> T getService(Class<T> clazz) {
    return clazz.cast(container.getComponentInstanceOfType(clazz));
  }

  @SuppressWarnings("rawtypes")
  private Map<String, SettingValue> getProductInformationFromJPA() {
    return settingService.getSettingsByContextAndScope(Context.GLOBAL.getName(),
                                                       Context.GLOBAL.getId(),
                                                       Scope.APPLICATION.getName(),
                                                       ProductInformations.PRODUCT_INFORMATION);
  }
}

package org.exoplatform.jpa.impl;

import org.exoplatform.commons.api.persistence.DataInitializer;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.settings.jpa.dao.SettingContextDAO;
import org.exoplatform.settings.jpa.dao.SettingScopeDAO;
import org.exoplatform.settings.jpa.dao.SettingsDAO;

/**
 * Created by exo on 3/10/17.
 */
@ConfiguredBy({ @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/test-jpa-configuration.xml") })
public class CommonsDAOJPAImplTest extends BaseTest {
  protected SettingContextDAO settingContextDAO;

  protected SettingScopeDAO settingScopeDAO;

  protected SettingsDAO settingsDAO;


  public void setUp() {
    super.setUp();

    // make sure data are well initialized for each test

    DataInitializer dataInitializer = getService(DataInitializer.class);
    dataInitializer.initData();

    // Init DAO
    settingContextDAO = getService(SettingContextDAO.class);
    settingScopeDAO = getService(SettingScopeDAO.class);
    settingsDAO = getService(SettingsDAO.class);

    // Clean Data
    cleanDB();
  }

  public void testInit() {
    assertNotNull(settingContextDAO);
    assertNotNull(settingScopeDAO);
    assertNotNull(settingsDAO);
  }

  public void tearDown() {
    // Clean Data
    cleanDB();
    super.tearDown();
  }

  private void cleanDB() {
    settingContextDAO.deleteAll(settingContextDAO.findAll());
    settingScopeDAO.deleteAll(settingScopeDAO.findAll());
    settingsDAO.deleteAll(settingsDAO.findAll());
  }
}
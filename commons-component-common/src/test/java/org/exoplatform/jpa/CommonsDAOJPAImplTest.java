package org.exoplatform.jpa;

import org.exoplatform.commons.api.persistence.DataInitializer;
import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;

/**
 * Created by exo on 3/10/17.
 */
@ConfiguredBy({ @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/test-jpa-configuration.xml") })
public class CommonsDAOJPAImplTest extends BaseCommonsTestCase {


  public void setUp() throws Exception {
    super.setUp();

    // make sure data are well initialized for each test

    DataInitializer dataInitializer = getService(DataInitializer.class);
    dataInitializer.initData();

    // Init DAO

    // Clean Data
    cleanDB();
  }

  public void testInit() {
  }

  public void tearDown() throws Exception {
    // Clean Data
    cleanDB();
    super.tearDown();
  }

  private void cleanDB() {
  }
}
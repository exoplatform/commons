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
package org.exoplatform.commons.testing;

import org.junit.BeforeClass;

import org.exoplatform.component.test.*;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.services.idgenerator.IDGeneratorService;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform hailt@exoplatform.com
 * May 22, 2012
 */
@ConfiguredBy({
    @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.commons.component.core-dependencies-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.commons.component.core-configuration.xml") })
public abstract class BaseCommonsTestCase extends AbstractKernelTest {

  protected static IDGeneratorService idGeneratorService;

  protected PortalContainer           container;

  protected ConfigurationManager      configurationManager;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    begin();
    container = PortalContainer.getInstance();
    configurationManager = getService(ConfigurationManager.class);

    System.setProperty("gatein.email.domain.url", "http://localhost:8080");
  }

  @Override
  protected void tearDown() throws Exception {
    end();
    super.tearDown();
  }

  @BeforeClass
  @Override
  protected void beforeRunBare() {
    if (System.getProperty("gatein.test.output.path") == null) {
      System.setProperty("gatein.test.output.path", System.getProperty("java.io.tmpdir"));
    }
    super.beforeRunBare();
  }

  protected <T> T getService(Class<T> clazz) {
    return clazz.cast(container.getComponentInstanceOfType(clazz));
  }

  public static IDGeneratorService getIdGeneratorService() {
    if (idGeneratorService == null) {
      idGeneratorService = PortalContainer.getInstance().getComponentInstanceOfType(IDGeneratorService.class);
    }
    return idGeneratorService;
  }

  public static String generate() {
    return getIdGeneratorService().generateStringID(Long.toString(System.currentTimeMillis()));
  }
}

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

import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform hailt@exoplatform.com
 * May 22, 2012
 */
@ConfiguredBy({ @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/test-root-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/test-portal-configuration.xml") })
public abstract class BaseCommonsTestCase extends AbstractKernelTest {
  private static final Log       LOG            = ExoLogger.getLogger(BaseCommonsTestCase.class);

  protected final String         REPO_NAME      = "repository";

  protected final String         WORKSPACE_NAME = "portal-test";

  protected PortalContainer      container;

  protected RepositoryService    repositoryService;

  protected ConfigurationManager configurationManager;

  protected Session              session;

  protected Node                 root;

  public void setUp() throws Exception {
    // Set random folder name for JCR index, swap & lock
    System.setProperty("exo.test.random.name", "" + Math.random());
    super.setUp();
    begin();
    container = PortalContainer.getInstance();
    repositoryService = getService(RepositoryService.class);
    configurationManager = getService(ConfigurationManager.class);

    session = repositoryService.getRepository(REPO_NAME).getSystemSession(WORKSPACE_NAME);
    root = session.getRootNode();
    System.setProperty("gatein.email.domain.url", "http://localhost:8080");
  }

  protected void tearDown() throws Exception {
    NodeIterator iter = root.getNodes();
    while (iter.hasNext()) {
      Node node = iter.nextNode();
      node.remove();
    }
    session.save();
    end();
    super.tearDown();
  }

  @BeforeClass
  @Override
  protected void beforeRunBare() {
    if(System.getProperty("gatein.test.output.path") == null) {
      System.setProperty("gatein.test.output.path", System.getProperty("java.io.tmpdir"));
    }
    super.beforeRunBare();
  }

  protected <T> T getService(Class<T> clazz) {
    return clazz.cast(container.getComponentInstanceOfType(clazz));
  }
}
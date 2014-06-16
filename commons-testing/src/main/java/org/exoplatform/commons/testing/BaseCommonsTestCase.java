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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.test.BasicTestCase;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform hailt@exoplatform.com
 * May 22, 2012
 */
public abstract class BaseCommonsTestCase extends BasicTestCase {
  protected final String         REPO_NAME      = "repository";

  protected final String         WORKSPACE_NAME = "portal-test";

  protected PortalContainer      container;

  protected RepositoryService    repositoryService;

  protected ConfigurationManager configurationManager;

  protected Session              session;

  protected Node                 root;

  public void setUp() throws Exception {
    container = PortalContainer.getInstance();
    repositoryService = getService(RepositoryService.class);
    configurationManager = getService(ConfigurationManager.class);

    session = repositoryService.getRepository(REPO_NAME).getSystemSession(WORKSPACE_NAME);
    root = session.getRootNode();
    System.setProperty("gatein.email.domain.url", "http://localhost:8080");
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    NodeIterator iter = root.getNodes();
    while (iter.hasNext()) {
      Node node = iter.nextNode();
      node.remove();
    }
    session.save();
    session.logout();
  }

  protected <T> T getService(Class<T> clazz) {
    return clazz.cast(container.getComponentInstanceOfType(clazz));
  }
}

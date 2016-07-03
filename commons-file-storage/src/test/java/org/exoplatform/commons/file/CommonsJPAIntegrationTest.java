/*
 * Copyright (C) 2016 eXo Platform SAS.
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
package org.exoplatform.commons.file;

import org.exoplatform.commons.api.persistence.DataInitializer;
import org.exoplatform.commons.file.storage.dao.OrphanFileDAO;
import org.exoplatform.commons.file.storage.dao.FileInfoDAO;
import org.exoplatform.commons.file.storage.dao.NameSpaceDAO;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 */
@ConfiguredBy({
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/files-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/test-configuration.xml")
})
public class CommonsJPAIntegrationTest extends BaseTest {
  protected FileInfoDAO    fileInfoDAO;

  protected NameSpaceDAO   nameSpaceDAO;

  protected OrphanFileDAO orphanFileDAO;

  public void setUp() {
    super.setUp();

    // make sure data are well initialized for each test
    DataInitializer dataInitializer = PortalContainer.getInstance().getComponentInstanceOfType(DataInitializer.class);
    dataInitializer.initData();

    // Init DAO
    fileInfoDAO = PortalContainer.getInstance().getComponentInstanceOfType(FileInfoDAO.class);
    nameSpaceDAO = PortalContainer.getInstance().getComponentInstanceOfType(NameSpaceDAO.class);
    orphanFileDAO = PortalContainer.getInstance().getComponentInstanceOfType(OrphanFileDAO.class);

    // Clean Data
    cleanDB();
  }

  public void testInit()
  {
    assertNotNull(fileInfoDAO);
    assertNotNull(nameSpaceDAO);
    assertNotNull(orphanFileDAO);
  }

  public void tearDown() {
    // Clean Data
    cleanDB();
    super.tearDown();
  }

  private void cleanDB() {
    orphanFileDAO.deleteAll();
    fileInfoDAO.deleteAll();
    nameSpaceDAO.deleteAll();
  }
}

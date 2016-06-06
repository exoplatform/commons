/* 
* Copyright (C) 2003-2015 eXo Platform SAS.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see http://www.gnu.org/licenses/ .
*/
package org.exoplatform.addons.es.dao;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.persistence.EntityManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 8/20/15
 */
public abstract class AbstractDAOTest {
  private static Connection conn;
  private static Liquibase liquibase;

  EntityManagerService entityMgrService;

  @BeforeClass
  public static void startDB () throws ClassNotFoundException, SQLException, LiquibaseException {

    Class.forName("org.hsqldb.jdbcDriver");
    conn = DriverManager.getConnection("jdbc:hsqldb:file:target/hsql-db", "sa", "");

    Database database = DatabaseFactory.getInstance()
        .findCorrectDatabaseImplementation(new JdbcConnection(conn));

    //Create Table
    liquibase = new Liquibase("./src/main/resources/db/changelog/exo-search.db.changelog-1.0.0.xml",
        new FileSystemResourceAccessor(), database);
    liquibase.update((String) null);

  }

  @AfterClass
  public static void stopDB () throws LiquibaseException, SQLException {

    liquibase.rollback(1000, null);
    conn.close();

  }

  @Before
  public void initializeContainerAndStartRequestLifecycle() {
    PortalContainer container = PortalContainer.getInstance();

    RequestLifeCycle.begin(container);

    entityMgrService = container.getComponentInstanceOfType(EntityManagerService.class);
    entityMgrService.getEntityManager().getTransaction().begin();
  }

  @After
  public void endRequestLifecycle() {

    entityMgrService.getEntityManager().getTransaction().commit();

    RequestLifeCycle.end();
  }

  protected EntityManager getEntityManager() {
    return entityMgrService.getEntityManager();
  }

}


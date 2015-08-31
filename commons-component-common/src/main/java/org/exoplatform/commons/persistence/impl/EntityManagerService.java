/*
 * Copyright (C) 2015 eXo Platform SAS.
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
package org.exoplatform.commons.persistence.impl;

import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.apache.commons.lang.StringUtils;
import org.hibernate.ejb.HibernatePersistence;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * This service is responsible to create a single EntityManagerFactory, with the
 * persistence unit name <code>exo-pu</code>.
 * <p>
 * The service is also bound to use of the RequestLifecycle that there is only
 * one EntityManager will be created at beginning of the request lifecycle. The
 * EntityManager instance will be maintained open and shared with all
 * applications relying on this through to the end of lifecycle.
 *
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public class EntityManagerService implements ComponentRequestLifecycle {
  private final static Log            LOGGER                  = ExoLogger.getLogger(EntityManagerService.class);
  private static final String[]       HIBERNATE_PROPERTIES    = new String[] {"hibernate.show_sql",
          "hibernate.format_sql",
          "hibernate.use_sql_comments"};
  private static final String         EXO_JPA_DATASOURCE_NAME = "exo.jpa.datasource_name";
  private static final String         EXO_JPA_DEFAULT_DATASOURCE_NAME = "java:/comp/env/exo-jpa_portal";
  private static final String         PERSISTENCE_UNIT_NAME   = "exo-pu";
  private static EntityManagerFactory entityManagerFactory;

  private ThreadLocal<EntityManager>  instance                = new ThreadLocal<>();

  public EntityManagerService() {
    final Properties properties = new Properties();
    // Setting datasource JNDI name
    String datasourceName = PropertyManager.getProperty(EXO_JPA_DATASOURCE_NAME);
    if (StringUtils.isBlank(datasourceName)) {
      datasourceName = EXO_JPA_DEFAULT_DATASOURCE_NAME;
    }
    properties.put(HibernatePersistence.NON_JTA_DATASOURCE, datasourceName);

    // Get Hibernate properties in eXo global properties
    for (String propertyName : HIBERNATE_PROPERTIES) {
      String propertyValue = PropertyManager.getProperty(propertyName);
      if (StringUtils.isNotBlank(propertyValue)) {
        properties.put(propertyName, propertyValue);
        LOGGER.info("Setting [" + propertyName + "] to [" + propertyValue + "]");
      }
    }

    entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Created EntityManagerFactory instance: {} with datasource {}", PERSISTENCE_UNIT_NAME, datasourceName);
    }
  }

  /**
   * @return the EntityManager instance available in current request lifecycle.
   *         Otherwise, it returns NULL.
   */
  public EntityManager getEntityManager() {
    if (instance.get() == null) {
      return null;
    } else {
      return instance.get();
    }
  }

  /**
   * Return a completely new instance of EntityManager. The EntityManager
   * instance is put in the threadLocal for further use.
   *
   * @return return a completely new instance of EntityManager.
   */
  EntityManager createEntityManager() {
    EntityManager em = entityManagerFactory.createEntityManager();
    instance.set(em);
    return em;
  }

  @Override
  public void startRequest(ExoContainer container) {
    createEntityManager();
  }

  @Override
  public void endRequest(ExoContainer container) {
    closeEntityManager();
  }

  void closeEntityManager() {
    EntityManager em = getEntityManager();
    EntityTransaction tx = null;
    try {
      tx = em.getTransaction();
      if (tx.isActive()) {
        tx.commit();
      }
    } catch (RuntimeException e) {
      if (tx != null && tx.isActive()) {
        if (LOGGER.isErrorEnabled()) {
          LOGGER.error("Failed to commit transaction.", e);
        }

        tx.rollback();
      }
    } finally {
      em.close();
      instance.set(null);

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Ended a request lifecycle of {} component service", EntityManagerService.class.getName());
      }
    }
  }
}
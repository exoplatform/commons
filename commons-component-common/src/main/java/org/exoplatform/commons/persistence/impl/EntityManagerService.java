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

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.ejb.HibernatePersistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
  public static final String          PERSISTENCE_UNIT_NAME       = "exo-pu";
  private static final Log            LOGGER                      = ExoLogger.getLogger(EntityManagerService.class);
  private static final String         EXO_JPA_DATASOURCE_NAME     = "exo.jpa.datasource_name";
  private static final String         EXO_PREFIX_FOR_HIB_SETTINGS = "exo.jpa.";
  private static EntityManagerFactory entityManagerFactory;

  private ThreadLocal<EntityManager>  instance                    = new ThreadLocal<>();

  private final Properties properties;

  public EntityManagerService() {
    properties = new Properties();

    // Setting datasource JNDI name. Get it directly from eXo global properties so it is not overridable by addons.
    String datasourceName = PropertyManager.getProperty(EXO_JPA_DATASOURCE_NAME);
    if (StringUtils.isNotBlank(datasourceName)) {
      properties.put(HibernatePersistence.NON_JTA_DATASOURCE, datasourceName);
      LOGGER.info("EntityManagerFactory [{}] - Creating with datasource {}.", PERSISTENCE_UNIT_NAME, datasourceName);
    } else {
      LOGGER.info("EntityManagerFactory [{}] - Creating with default datasource.", PERSISTENCE_UNIT_NAME);
    }

    // Get Hibernate properties in eXo global properties
    for (String propertyName : getHibernateAvailableSettings()) {
      String propertyValue = PropertyManager.getProperty(EXO_PREFIX_FOR_HIB_SETTINGS + propertyName);
      if (StringUtils.isNotBlank(propertyValue)) {
        properties.put(propertyName, propertyValue);
        LOGGER.info("EntityManagerFactory [{}] - Setting [{}] to [{}]", PERSISTENCE_UNIT_NAME, propertyName, propertyValue);
      }
    }

    entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
    LOGGER.info("EntityManagerFactory [{}] - Created.", PERSISTENCE_UNIT_NAME);
  }

  public String getDatasourceName() {
    return (String) properties.get(HibernatePersistence.NON_JTA_DATASOURCE);
  }

  public void setDatasourceName(String datasourceName) {
    properties.put(HibernatePersistence.NON_JTA_DATASOURCE, datasourceName);
  }

  private List<String> getHibernateAvailableSettings() {
    List<String> result = new ArrayList<>();
    for (Field field : AvailableSettings.class.getDeclaredFields()) {
      try {
        result.add((String) field.get(null));
      } catch (IllegalAccessException e) {
        // Noting to do: we log and continue
        LOGGER.error("Error while getting Hibernate available settings.", e);
      }
    }
    return result;
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

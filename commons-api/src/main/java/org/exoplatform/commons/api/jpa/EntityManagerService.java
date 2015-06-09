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
package org.exoplatform.commons.api.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * This service is responsible to create a single EntityManagerFactory, with the
 * persistence unit name passed from service init-params
 * <code>persistence.unit.name</code>.
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

  protected static Log log = ExoLogger.getLogger(EntityManagerService.class);

  private static EntityManagerFactory entityManagerFactory;

  private ThreadLocal<EntityManager> instance = new ThreadLocal<EntityManager>();

  private String persistenceName;

  public EntityManagerService(InitParams params) {
    ValueParam value = params.getValueParam("persistence.unit.name");
    persistenceName = value.getValue();
    entityManagerFactory = Persistence.createEntityManagerFactory(persistenceName);
    if (log.isInfoEnabled()) {
      log.info("Created EntityManagerFactory instance: {}", persistenceName);;
    }
  }

  /**
   * @return the EntityManager instance available in current request lifecycle.
   *         Otherwise, it returns NULL.
   */
  public EntityManager getEntityManager() {
    if (instance.get() == null) {
      if (log.isErrorEnabled()) {
        log.error("No EntityManager is available!!! It must be in a request lifecycle.");
      }
      return null;
    } else {
      return instance.get();
    }
  }

  /**
   * Return a completely new instance of EntityManager. The EntityManager
   * instance will not be managed by the service, so the caller needs to manage
   * it himself.
   * 
   * @return return a completely new instance of EntityManager.
   */
  public EntityManager createEntityManager() {
    return entityManagerFactory.createEntityManager();
  }

  @Override
  public void startRequest(ExoContainer container) {
    EntityManager em = entityManagerFactory.createEntityManager();
    instance.set(em);
  }

  @Override
  public void endRequest(ExoContainer container) {
    EntityManager em = getEntityManager();
    EntityTransaction tx = null;
    try {
      tx = em.getTransaction();
      if (tx.isActive()) {
        tx.commit();
      }
    } catch (RuntimeException e) {
      if ( tx != null && tx.isActive() ) {
        if (log.isErrorEnabled()) {
          log.error("Failed to commit transaction.", e);
        }

        tx.rollback();
      }
    } finally {
      em.close();
      instance.set(null);

      if (log.isDebugEnabled()) {
        log.debug("Ended a request lifecycle of {} component service", EntityManagerService.class.getName());
      }
    }
  }
}

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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.commons.api.persistence.Transactional;

import javax.persistence.EntityManager;

/**
 * Process the Transactional annotation. The only propagation implemented yet is
 * REQUIRED. Support a current transaction, create a new one if none exists.
 * Analogous to EJB or Spring transaction attribute of the same name.
 *
 * @see org.exoplatform.commons.api.persistence.Transactional
 * @author <a href="bdechateauvieux@exoplatform.org">Benoit de Chateauvieux</a>
 * @version $Revision$
 */
@Aspect
public class TransactionalAspect {
  private static final Log LOG = ExoLogger.getLogger(TransactionalAspect.class);

  @Around("execution(* *(..)) && @annotation(Transactional)")
  public Object around(ProceedingJoinPoint point) throws Throwable {
    EntityManager entityManager;
    boolean begunTx = false;
    boolean emStarted = false;

    // Do we need to start EntityManager ?
    EntityManagerService service = PortalContainer.getInstance().getComponentInstanceOfType(EntityManagerService.class);
    entityManager = service.getEntityManager();
    if (entityManager == null) {
      LOG.debug("Injecting new PersistenceContext");
      entityManager = service.createEntityManager();
      emStarted = true;
    } else {
      LOG.debug("Using existing PersistenceContext");
    }

    // Do we need to start Transaction ?
    if (entityManager.getTransaction().isActive()) {
      LOG.debug("Using current transaction");
    } else {
      LOG.debug("Starting new transaction");
      entityManager.getTransaction().begin();
      begunTx = true;
    }
    try {
      Object result = point.proceed();
      return result;
    } finally {
      // Do we need to end Transaction ?
      if ((begunTx) && (entityManager.getTransaction().isActive())) {
        try {
          LOG.debug("Committing current transaction");
          entityManager.getTransaction().commit();
        } catch (RuntimeException e) {
          LOG.error("Failed to commit current transaction. The transaction is rolled back.", e);
          entityManager.getTransaction().rollback();
        }
      }

      // Do we need to close EntityManager ?
      if (emStarted && (entityManager != null)) {
        service.closeEntityManager();
      }
    }
  }

}

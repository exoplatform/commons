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

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.persistence.EntityManager;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Proxy that returns an EntityManager instance. If there is an entityManager
 * already started, it will return it. If no entityManager is found in the
 * context, it will create a new one and close it at the end of the execution of
 * the method.
 *
 * @author <a href="bdechateauvieux@exoplatform.org">Benoit de Chateauvieux</a>
 * @version $Revision$
 */
public class EntityManagerHolder {
  private static final Log           LOG      = ExoLogger.getLogger(EntityManagerHolder.class);

  private final static EntityManager EM_PROXY = (EntityManager) Proxy.newProxyInstance(
          ExoEntityManagerInvocationHandler.class.getClassLoader(),
          new Class[] { EntityManager.class },
          new ExoEntityManagerInvocationHandler());

  public static EntityManager get() {
    return EM_PROXY;
  }

  private static class ExoEntityManagerInvocationHandler implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      boolean emStarted = false;
      EntityManager entityManager = null;

      EntityManagerService service = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(EntityManagerService.class);
      try {
        entityManager = service.getEntityManager();
        if (entityManager == null) {
          LOG.debug("Injecting new EntityManager");
          entityManager = service.createEntityManager();
          emStarted = true;
        } else {
          LOG.debug("Using existing EntityManager");
        }
        return method.invoke(entityManager, args);

      } finally {
        if (emStarted && (entityManager != null) && (!entityManager.getTransaction().isActive())) {
          LOG.debug("Closing new EntityManager");
          service.closeEntityManager();
        }
      }
    }
  }
}

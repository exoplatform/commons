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
package org.exoplatform.commons.persistence.impl;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.commons.api.persistence.ExoTransactional;

/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 * @param <E> Entity type
 * @param <ID> Identity of the entity
 */
public class GenericDAOJPAImpl<E, ID extends Serializable> implements GenericDAO<E, ID> {

  protected Class<E> modelClass;

  public GenericDAOJPAImpl() {
    ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
    this.modelClass = (Class<E>) genericSuperclass.getActualTypeArguments()[0];
  }

  @Override
  public Long count() {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Long> query = cb.createQuery(Long.class);

    Root<E> entity = query.from(modelClass);

    //Selecting the count
    query.select(cb.count(entity));

    return getEntityManager().createQuery(query).getSingleResult();
  }

  @Override
  public E find(ID id) {
    return (E) getEntityManager().find(modelClass, id);
  }

  /**
   * This method makes 2 calls to getEntityManager():
   * 1- The first one to get the CriteriaBuilder
   * 2- The second one to create the query
   * If there is no EntityManager in the threadLocal (i.e: EntityManagerService.getEntityManager() returns null),
   * the EntityManagerHolder will return 2 distinct EntityManager instances.
   * This will result in a org.hibernate.SessionException: Session is closed!.
   *
   * Thus, this method shall always be invoked with an EntityManager in the ThreadLocal
   * (for example, from a request managed by the portal lifecycle or from a method annotated with  @ExoTransactional)
   */
  //Another option is to implement something similar to Spring's DeferredQueryInvocationHandler
  @Override
  public List<E> findAll() {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<E> query = cb.createQuery(modelClass);

    Root<E> entity = query.from(modelClass);

    //Selecting the entity
    query.select(entity);

    return getEntityManager().createQuery(query).getResultList();
  }

  @Override
  @ExoTransactional
  public E create(E entity) {
    EntityManager em = getEntityManager();
    em.persist(entity);
    return entity;
  }

  @Override
  @ExoTransactional
  public void createAll(List<E> entities) {
    EntityManager em = getEntityManager();
    for (E entity : entities) {
      em.persist(entity);
    }
  }

  @Override
  @ExoTransactional
  public E update(E entity) {
    getEntityManager().merge(entity);
    return entity;
  }

  @Override
  @ExoTransactional
  public void updateAll(List<E> entities) {
    for (E entity : entities) {
      getEntityManager().merge(entity);
    }
  }

  @Override
  @ExoTransactional
  public void delete(E entity) {
    EntityManager em = getEntityManager();
    em.remove(em.merge(entity));
  }

  @Override
  @ExoTransactional
  public void deleteAll(List<E> entities) {
    EntityManager em = getEntityManager();
    for (E entity : entities) {
      em.remove(entity);
    }
  }

  @Override
  @ExoTransactional
  public void deleteAll() {
    List<E> entities = findAll();

    EntityManager em = getEntityManager();
    for (E entity : entities) {
      em.remove(entity);
    }
  }

  /**
   * Return an EntityManager instance.
   * @return An EntityManger instance.
   */
  protected EntityManager getEntityManager() {
    return EntityManagerHolder.get();
  }
}


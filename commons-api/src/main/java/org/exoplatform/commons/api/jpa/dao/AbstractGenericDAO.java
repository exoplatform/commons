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
package org.exoplatform.commons.api.jpa.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 * @param <E> Entity type
 * @param <ID> Identity of the entity
 */
abstract public class AbstractGenericDAO<E, ID extends Serializable> implements GenericDAO<E, ID> {

  protected Class<E> modelClass;

  public AbstractGenericDAO() {
    ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
    this.modelClass = (Class<E>) genericSuperclass.getActualTypeArguments()[0];
  }

  /**
   * Return an EntityManager instance.
   * @return An EntityManger instance.
   */
  public abstract EntityManager getEntityManager();

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
  public E create(E entity) {
    EntityManager em = getEntityManager();
    em.persist(entity);
    return entity;
  }

  @Override
  public void createAll(List<E> entities) {
    EntityManager em = getEntityManager();
    for (E entity : entities) {
      em.persist(entity);
    }
  }

  @Override
  public E update(E entity) {
    getEntityManager().merge(entity);
    return entity;
  }

  @Override
  public void updateAll(List<E> entities) {
    for (E entity : entities) {
      getEntityManager().merge(entity);
    }
  }

  @Override
  public void delete(E entity) {
    EntityManager em = getEntityManager();
    em.remove(entity);
  }

  @Override
  public void deleteAll(List<E> entities) {
    EntityManager em = getEntityManager();
    for (E entity : entities) {
      em.remove(entity);
    }
  }

  @Override
  public void deleteAll() {
    List<E> entities = findAll();

    EntityManager em = getEntityManager();
    for (E entity : entities) {
      em.remove(entity);
    }
  }
}


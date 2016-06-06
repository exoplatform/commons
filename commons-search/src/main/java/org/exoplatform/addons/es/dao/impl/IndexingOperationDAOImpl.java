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
package org.exoplatform.addons.es.dao.impl;

import org.exoplatform.addons.es.dao.IndexingOperationDAO;
import org.exoplatform.addons.es.domain.IndexingOperation;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 7/29/15
 */
public class IndexingOperationDAOImpl extends GenericDAOJPAImpl<IndexingOperation, Long> implements IndexingOperationDAO {

  @Override
  @ExoTransactional
  public List<IndexingOperation> findAllFirst(Integer maxResults) {
      return getEntityManager()
              .createNamedQuery("IndexingOperation.findAll", IndexingOperation.class)
              .setMaxResults(maxResults)
              .getResultList();
  }

  @Override
  @ExoTransactional
  public void deleteAllIndexingOperationsHavingIdLessThanOrEqual(long id) {
    getEntityManager()
        .createNamedQuery("IndexingOperation.deleteAllIndexingOperationsHavingIdLessThanOrEqual")
        .setParameter("id", id)
        .executeUpdate();
  }

  @Override
  public List<IndexingOperation> findAll(int offset, int limit) {
    return getEntityManager()
        .createNamedQuery("IndexingOperation.findAll", IndexingOperation.class)
        .setFirstResult(offset)
        .setMaxResults(limit)
        .getResultList();
  }
}


package org.exoplatform.settings.jpa.dao;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.settings.jpa.entity.ContextEntity;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Created by exo on 3/8/17.
 */
public class SettingContextDAO extends GenericDAOJPAImpl<ContextEntity, Long> {
  @ExoTransactional
  public ContextEntity getContext(ContextEntity contextEntity) {
    TypedQuery<ContextEntity> query = getEntityManager().createNamedQuery("commons.getContext", ContextEntity.class)
        .setParameter("name", contextEntity.getName())
        .setParameter("contextType", contextEntity.getType());
    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @ExoTransactional
  public List<ContextEntity> getContextsOfType(String contextType) {
    TypedQuery<ContextEntity> query = getEntityManager().createNamedQuery("commons.getContextofType", ContextEntity.class)
        .setParameter("contextType", contextType);
    try {
      return query.getResultList();
    } catch (NoResultException e) {
      return null;
    }
  }

  @ExoTransactional
  public List<ContextEntity> getContextsOfType(String contextType, int offset, int limit) {
    TypedQuery<ContextEntity> query = getEntityManager().createNamedQuery("commons.getContextofType", ContextEntity.class)
        .setFirstResult(offset)
        .setMaxResults(limit)
        .setParameter("contextType", contextType);
    try {
      return query.getResultList();
    } catch (NoResultException e) {
      return null;
    }
  }
}

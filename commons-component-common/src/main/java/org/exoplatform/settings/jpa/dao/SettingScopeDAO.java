package org.exoplatform.settings.jpa.dao;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.settings.jpa.entity.ScopeEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

/**
 * Created by exo on 3/8/17.
 */
public class SettingScopeDAO extends GenericDAOJPAImpl<ScopeEntity, Long> {
  @ExoTransactional
  public ScopeEntity getScope(ScopeEntity scopeEntity) {
    TypedQuery<ScopeEntity> query;
      query = getEntityManager().createNamedQuery("commons.getScope", ScopeEntity.class)
          .setParameter("name", scopeEntity.getName())
          .setParameter("scopeType", scopeEntity.getType());
    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  public ScopeEntity getScopeOfType(String scopeType) {
    TypedQuery<ScopeEntity> query = getEntityManager().createNamedQuery("commons.getScopeOfType", ScopeEntity.class)
        .setParameter("scopeType", scopeType);
    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }
}

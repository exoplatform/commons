package org.exoplatform.settings.jpa.dao;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.settings.jpa.entity.ScopeEntity;

public class SettingScopeDAO extends GenericDAOJPAImpl<ScopeEntity, Long> {
  @ExoTransactional
  public ScopeEntity getScopeByTypeAndName(String scopeType, String scopeName) {
    TypedQuery<ScopeEntity> query;
    if (StringUtils.isBlank(scopeName)) {
      query = getEntityManager().createNamedQuery("SettingsScopeEntity.getScopeWithNullName", ScopeEntity.class)
                                .setParameter("scopeType", scopeType);
    } else {
      query = getEntityManager().createNamedQuery("SettingsScopeEntity.getScope", ScopeEntity.class)
                                .setParameter("scopeName", scopeName)
                                .setParameter("scopeType", scopeType);
    }
    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }
}

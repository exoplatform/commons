package org.exoplatform.settings.jpa.dao;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.settings.jpa.entity.ContextEntity;

public class SettingContextDAO extends GenericDAOJPAImpl<ContextEntity, Long> {
  private static final Log LOG = ExoLogger.getLogger(SettingContextDAO.class);

  @ExoTransactional
  public ContextEntity getContextByTypeAndName(String contextType, String contextName) {
    TypedQuery<ContextEntity> query;
    if (StringUtils.isBlank(contextName)) {
      query = getEntityManager().createNamedQuery("SettingsContextEntity.getContextByTypeWithNullName", ContextEntity.class)
                                .setParameter("contextType", contextType);
    } else {
      query = getEntityManager().createNamedQuery("SettingsContextEntity.getContextByTypeAndName", ContextEntity.class)
                                .setParameter("contextName", contextName)
                                .setParameter("contextType", contextType);
    }
    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    } catch (NonUniqueResultException e1) {
      LOG.error("Non unique result for settings context of type {} and name {}. First result will be returned",
                contextType,
                contextName);
      return query.getResultList().get(0);
    }
  }

  @ExoTransactional
  public List<ContextEntity> getEmptyContextsByScopeAndContextType(String contextType,
                                                                   String scopeType,
                                                                   String scopeName,
                                                                   int offset,
                                                                   int limit) {
    TypedQuery<ContextEntity> query;
    if (StringUtils.isBlank(scopeName)) {
      query =
            getEntityManager().createNamedQuery("SettingsContextEntity.getEmptyContextsByScopeWithNullNameAndContextType", ContextEntity.class)
                              .setParameter("contextType", contextType)
                              .setParameter("scopeType", scopeType);
    } else {
      query = getEntityManager().createNamedQuery("SettingsContextEntity.getEmptyContextsByScopeAndContextType", ContextEntity.class)
                                .setParameter("contextType", contextType)
                                .setParameter("scopeType", scopeType)
                                .setParameter("scopeName", scopeName);
    }
    if (limit != 0) {
      query.setMaxResults(limit).setFirstResult(offset);
    }
    return query.getResultList();
  }

  @ExoTransactional
  public List<ContextEntity> getContextsByTypeAndSettingNameAndScope(String contextType,
                                                                     String scopeType,
                                                                     String scopeName,
                                                                     String settingName,
                                                                     int offset,
                                                                     int limit) {
    TypedQuery<ContextEntity> query;
    if (StringUtils.isBlank(scopeName)) {
      query = getEntityManager()
                                .createNamedQuery("SettingsContextEntity.getContextsByTypeAndScopeWithNullNameAndSettingName",
                                                  ContextEntity.class)
                                .setFirstResult(offset)
                                .setMaxResults(limit)
                                .setParameter("contextType", contextType)
                                .setParameter("scopeType", scopeType)
                                .setParameter("settingName", settingName);
    } else {
      query = getEntityManager().createNamedQuery("SettingsContextEntity.getContextsByTypeAndScopeAndSettingName", ContextEntity.class)
                                .setFirstResult(offset)
                                .setMaxResults(limit)
                                .setParameter("contextType", contextType)
                                .setParameter("scopeType", scopeType)
                                .setParameter("scopeName", scopeName)
                                .setParameter("settingName", settingName);
    }
    return query.getResultList();
  }
}

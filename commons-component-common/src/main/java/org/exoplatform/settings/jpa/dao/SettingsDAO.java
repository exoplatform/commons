package org.exoplatform.settings.jpa.dao;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.settings.jpa.EntityConverter;
import org.exoplatform.settings.jpa.entity.ContextEntity;
import org.exoplatform.settings.jpa.entity.ScopeEntity;
import org.exoplatform.settings.jpa.entity.SettingsEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Created by exo on 3/8/17.
 */
public class SettingsDAO extends GenericDAOJPAImpl<SettingsEntity, Long> {

  @ExoTransactional
  public List<Long> findAllIds(int offset, int limit) {
    return getEntityManager().createNamedQuery("commons.getAllSettingsIds").setFirstResult(offset).setMaxResults(limit).getResultList();
  }

  @ExoTransactional
  public List<SettingsEntity> getSettingsByUser(String user) {
    TypedQuery<SettingsEntity> query = getEntityManager().createNamedQuery("commons.getSettingsByUser", SettingsEntity.class)
        .setParameter("user", user);
    return query.getResultList();
  }

  @ExoTransactional
  public List<SettingsEntity> getSettingsByContext(Context context) {
    TypedQuery<SettingsEntity> query = getEntityManager().createNamedQuery("commons.getSettingsByContext", SettingsEntity.class)
        .setParameter("contextType", EntityConverter.convertContextToContextEntity(context).getType())
        .setParameter("contextName", EntityConverter.convertContextToContextEntity(context).getName());
    return query.getResultList();
  }

  @ExoTransactional
  public List<SettingsEntity> getSettingsByScope(Scope scope) {
    TypedQuery<SettingsEntity> query = getEntityManager().createNamedQuery("commons.getSettingsByScope", SettingsEntity.class)
        .setParameter("scopeType", EntityConverter.convertScopeToScopeEntity(scope).getType())
        .setParameter("scopeName", EntityConverter.convertScopeToScopeEntity(scope).getName());
    return query.getResultList();
  }

  @ExoTransactional
  public List<SettingsEntity> getSettingsByContextAndScope(Context context, Scope scope) {
    TypedQuery<SettingsEntity> query;
      query = getEntityManager().createNamedQuery("commons.getSettingsByContextAndScope", SettingsEntity.class)
          .setParameter("contextType", EntityConverter.convertContextToContextEntity(context).getType())
          .setParameter("contextName", EntityConverter.convertContextToContextEntity(context).getName())
          .setParameter("scopeType", EntityConverter.convertScopeToScopeEntity(scope).getType())
          .setParameter("scopeName", EntityConverter.convertScopeToScopeEntity(scope).getName());
    return query.getResultList();
  }

  @ExoTransactional
  public SettingsEntity getSetting(ContextEntity contextEntity, ScopeEntity scopeEntity, String key) {
    TypedQuery<SettingsEntity> query;
      query =  getEntityManager().createNamedQuery("commons.getSetting", SettingsEntity.class)
          .setParameter("name", key)
          .setParameter("contextType", contextEntity.getType())
          .setParameter("contextName", contextEntity.getName())
          .setParameter("scopeType", scopeEntity.getType())
          .setParameter("scopeName", scopeEntity.getName());
    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @ExoTransactional
  public List<SettingsEntity> getUserSettingsWithDeactivate(String user, String isActive, String isEnabled) {
    TypedQuery<SettingsEntity> query = getEntityManager().createNamedQuery("commons.getUserSettingsWithDeactivate", SettingsEntity.class)
        .setParameter("user", user)
        .setParameter("isActive", isActive)
        .setParameter("isEnabled", isEnabled);
    return query.getResultList();
  }
}

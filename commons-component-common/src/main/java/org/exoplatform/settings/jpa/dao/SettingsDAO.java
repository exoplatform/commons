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
  public List<SettingsEntity> getSettingsByUser(String user) {
    TypedQuery<SettingsEntity> query = getEntityManager().createNamedQuery("commons.getSettingsByUser", SettingsEntity.class)
        .setParameter("user", user);
    return query.getResultList();
  }

  @ExoTransactional
  public List<SettingsEntity> getSettingsByContext(Context context) {
    ContextEntity contextEntity = EntityConverter.convertContextToContextEntity(context);
    TypedQuery<SettingsEntity> query = getEntityManager().createNamedQuery("commons.getSettingsByContext", SettingsEntity.class)
        .setParameter("contextType", contextEntity.getType())
        .setParameter("contextName", contextEntity.getName());
    return query.getResultList();
  }

  @ExoTransactional
  public List<SettingsEntity> getSettingsByScope(Scope scope) {
    ScopeEntity scopeEntity = EntityConverter.convertScopeToScopeEntity(scope);
    TypedQuery<SettingsEntity> query = getEntityManager().createNamedQuery("commons.getSettingsByScope", SettingsEntity.class)
        .setParameter("scopeType", scopeEntity.getType())
        .setParameter("scopeName", scopeEntity.getName());
    return query.getResultList();
  }

  @ExoTransactional
  public List<SettingsEntity> getSettingsByContextAndScope(Context context, Scope scope) {
    ContextEntity contextEntity = EntityConverter.convertContextToContextEntity(context);
    ScopeEntity scopeEntity = EntityConverter.convertScopeToScopeEntity(scope);
    TypedQuery<SettingsEntity> query;
      query = getEntityManager().createNamedQuery("commons.getSettingsByContextAndScope", SettingsEntity.class)
          .setParameter("contextType", contextEntity.getType())
          .setParameter("contextName", contextEntity.getName())
          .setParameter("scopeType", scopeEntity.getType())
          .setParameter("scopeName", scopeEntity.getName());
    return query.getResultList();
  }

  @ExoTransactional
  public SettingsEntity getSettingByContextAndScopeAndKey(ContextEntity contextEntity, ScopeEntity scopeEntity, String key) {
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

  public int getNumber(ScopeEntity scopeEntity, String key, String value) {
    TypedQuery<Long> query;
    query =  getEntityManager().createNamedQuery("commons.getSettingsNumber", Long.class)
        .setParameter("name", key)
        .setParameter("valueParam", value)
        .setParameter("scopeType", scopeEntity.getType())
        .setParameter("scopeName", scopeEntity.getName());
    try {
      return query.getSingleResult().intValue();
    } catch (NoResultException e) {
      return 0;
    }
  }
}

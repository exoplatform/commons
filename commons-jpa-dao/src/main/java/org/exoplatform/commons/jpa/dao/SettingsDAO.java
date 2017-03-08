package org.exoplatform.commons.jpa.dao;

import org.exoplatform.commons.jpa.entity.SettingsEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Created by exo on 3/8/17.
 */
public class SettingsDAO extends GenericDAOJPAImpl<SettingsEntity, Long> {
  public List<SettingsEntity> getSettingsByUser(String user) {

    TypedQuery<SettingsEntity> query = getEntityManager().createNamedQuery("commons.getSettingsByUser", SettingsEntity.class);
        //.setParameter("user", user);

    return query.getResultList();
  }

  public List<SettingsEntity> getSettingsByApplication(String user, String app) {

    TypedQuery<SettingsEntity> query = getEntityManager().createNamedQuery("commons.getSettingsByApplication", SettingsEntity.class);
//        .setParameter("user", user)
//        .setParameter("application", app);

    return query.getResultList();
  }

  public List<SettingsEntity> getSettingOfUser(String user, String setting) {

    TypedQuery<SettingsEntity> query = getEntityManager().createNamedQuery("commons.getSettingOfUser", SettingsEntity.class);
//        .setParameter("user", user)
//        .setParameter("setting", setting);

    return query.getResultList();
  }
}

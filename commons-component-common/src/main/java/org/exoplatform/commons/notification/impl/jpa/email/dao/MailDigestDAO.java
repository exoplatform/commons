package org.exoplatform.commons.notification.impl.jpa.email.dao;

import java.util.Set;

import javax.persistence.Query;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailDigestEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

public class MailDigestDAO extends GenericDAOJPAImpl<MailDigestEntity, Long> {
  @ExoTransactional
  public void deleteAllDigestsOfType(String type) {
    Query query = getEntityManager().createNamedQuery("NotificationsMailDigestEntity.deleteAllDigestsOfType").setParameter("digestType", type);
    query.executeUpdate();
  }

  @ExoTransactional
  public void deleteDigestsOfTypeByNotificationsIds(Set<Long> mailNotifsIds, String type) {
    Query query = getEntityManager().createNamedQuery("NotificationsMailDigestEntity.deleteDigestsOfTypeByNotificationsIds")
        .setParameter("digestType", type)
        .setParameter("notificationIds", mailNotifsIds);
    query.executeUpdate();
  }

  @ExoTransactional
  public void deleteAllDigests() {
    Query query = getEntityManager().createNamedQuery("NotificationsMailDigestEntity.deleteAllDigests");
    query.executeUpdate();
  }
}

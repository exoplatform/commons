package org.exoplatform.commons.notification.impl.jpa.email.dao;

import org.exoplatform.commons.notification.impl.jpa.email.entity.MailDigestEntity;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailNotifEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

/**
 * Created by exo on 3/8/17.
 */
public class MailDigestDAO extends GenericDAOJPAImpl<MailDigestEntity, Long> {

  public boolean isDigestDailySent(MailNotifEntity mailNotif) {
    try {
      TypedQuery<Long> queryDaily = getEntityManager().createNamedQuery("commons.countDigestByNotifAndType", Long.class)
          .setParameter("digestType", "daily")
          .setParameter("notifId", mailNotif);
      return (queryDaily.getSingleResult().intValue() == 0);
    } catch (NoResultException e) {
      return true;
    }
  }

  public boolean isDigestWeeklySent(MailNotifEntity mailNotif) {
    try {
      TypedQuery<Long> queryWeekly = getEntityManager().createNamedQuery("commons.countDigestByNotifAndType", Long.class)
          .setParameter("digestType", "weekly")
          .setParameter("notifId", mailNotif);
      return (queryWeekly.getSingleResult().intValue() == 0);
    } catch (NoResultException e) {
      return true;
    }
  }

  public MailDigestEntity getDigest(MailNotifEntity mailNotif, String type) {
    try {
      TypedQuery<MailDigestEntity> query = getEntityManager().createNamedQuery("commons.findDigestByNotifAndType", MailDigestEntity.class)
          .setParameter("digestType", type)
          .setParameter("notifId", mailNotif);
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }
}

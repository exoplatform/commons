package org.exoplatform.commons.notification.impl.jpa.email.dao;

import org.exoplatform.commons.notification.impl.jpa.email.entity.MailDigestEntity;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailNotifEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

import javax.persistence.TypedQuery;

/**
 * Created by exo on 3/8/17.
 */
public class MailDigestDAO extends GenericDAOJPAImpl<MailDigestEntity, Long> {

  public boolean isDigestSent(MailNotifEntity mailNotif) {
    TypedQuery<MailDigestEntity> queryDaily = getEntityManager().createNamedQuery("commons.findDigestByNotifAndType", MailDigestEntity.class)
        .setParameter("digestType", "daily")
        .setParameter("notifId", mailNotif);
    TypedQuery<MailDigestEntity> queryWeekly = getEntityManager().createNamedQuery("commons.findDigestByNotifAndType", MailDigestEntity.class)
        .setParameter("digestType", "weekly")
        .setParameter("notifId", mailNotif);
    return ((queryDaily.getResultList().size() > 0) && (queryWeekly.getResultList().size() > 0));

  }
}

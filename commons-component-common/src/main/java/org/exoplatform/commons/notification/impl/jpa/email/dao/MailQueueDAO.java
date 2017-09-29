package org.exoplatform.commons.notification.impl.jpa.email.dao;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailQueueEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

import java.util.List;

public class MailQueueDAO extends GenericDAOJPAImpl<MailQueueEntity, Long> {
  @ExoTransactional
  public List<MailQueueEntity> findAll(int offset, int limit) {
    return getEntityManager().createNamedQuery("NotificationsMailQueueEntity.getMessagesInQueue", MailQueueEntity.class).setFirstResult(offset)
        .setMaxResults(limit).getResultList();
  }
}

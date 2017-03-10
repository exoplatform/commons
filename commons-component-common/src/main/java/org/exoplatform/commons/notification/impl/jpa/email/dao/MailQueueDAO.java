package org.exoplatform.commons.notification.impl.jpa.email.dao;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailQueueEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

import java.util.List;

/**
 * Created by exo on 3/27/17.
 */
public class MailQueueDAO extends GenericDAOJPAImpl<MailQueueEntity, Long> {
  @ExoTransactional
  public List<MailQueueEntity> findAll(int offset, int limit) {
    return getEntityManager().createNamedQuery("commons.getMessagesInQueue").setFirstResult(offset)
        .setMaxResults(limit).getResultList();
  }
}

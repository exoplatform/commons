package org.exoplatform.commons.notification.impl.jpa.web.dao;

import org.exoplatform.commons.notification.impl.jpa.web.entity.WebNotifEntity;
import org.exoplatform.commons.notification.impl.jpa.web.entity.WebUsersEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Created by exo on 3/8/17.
 */
public class WebUsersDAO extends GenericDAOJPAImpl<WebUsersEntity, Long> {

  public List<WebUsersEntity> findWebNotifsByUserAndRead(String userId, Boolean isRead, int offset, int limit) {
    return getEntityManager().createNamedQuery("commons.findWebNotifsByUserAndRead")
        .setParameter("userId", userId)
        .setParameter("isRead", isRead)
        .setFirstResult(offset)
        .setMaxResults(limit)
        .getResultList();
  }

  public List<WebUsersEntity> findWebNotifsByUserAndRead(String userId, Boolean isRead) {
    return getEntityManager().createNamedQuery("commons.findWebNotifsByUserAndRead")
        .setParameter("userId", userId)
        .setParameter("isRead", isRead)
        .getResultList();
  }

  public int getNumberOnBadge(String userId) {
    TypedQuery<Long> query;
    query =  getEntityManager().createNamedQuery("commons.getNumberOnBadge", Long.class)
        .setParameter("userId", userId);
    try {
      return query.getSingleResult().intValue();
    } catch (NoResultException e) {
      return 0;
    }
  }

  public List<WebUsersEntity> findWebNotifsByUser(String userId) {
    return getEntityManager().createNamedQuery("commons.findWebNotifsByUser")
        .setParameter("userId", userId)
        .getResultList();
  }
}

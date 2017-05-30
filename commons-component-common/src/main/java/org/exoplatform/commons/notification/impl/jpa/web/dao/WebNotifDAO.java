package org.exoplatform.commons.notification.impl.jpa.web.dao;

import org.exoplatform.commons.notification.impl.jpa.web.entity.WebNotifEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

import javax.persistence.NoResultException;
import java.util.Date;
import java.util.List;

/**
 * Created by exo on 3/8/17.
 */
public class WebNotifDAO extends GenericDAOJPAImpl<WebNotifEntity, Long> {

  public List<WebNotifEntity> findWebNotifsByFilter(String pluginId, String userId, Boolean isOnPopover, int offset, int limit) {
    return getEntityManager().createNamedQuery("commons.findWebNotifsByPluginFilter")
        .setParameter("pluginId", pluginId)
        .setParameter("userId", userId)
        .setParameter("isOnPopover", isOnPopover)
        .setFirstResult(offset)
        .setMaxResults(limit)
        .getResultList();
  }

  public List<WebNotifEntity> findWebNotifsByFilter(String userId, int offset, int limit) {
    return getEntityManager().createNamedQuery("commons.findWebNotifsByUserFilter")
        .setParameter("userId", userId)
        .setFirstResult(offset)
        .setMaxResults(limit)
        .getResultList();
  }

  public List<WebNotifEntity> findWebNotifsByFilter(String userId, boolean isOnPopover, int offset, int limit) {
    return getEntityManager().createNamedQuery("commons.findWebNotifsByPopoverFilter")
        .setParameter("userId", userId)
        .setParameter("isOnPopover", isOnPopover)
        .setFirstResult(offset)
        .setMaxResults(limit)
        .getResultList();
  }

  public List<WebNotifEntity> findWebNotifsByUser(String userId, Boolean isRead, int offset, int limit) {
    return getEntityManager().createNamedQuery("commons.findNewWebNotifsByUser")
        .setParameter("userId", userId)
        .setParameter("isRead", isRead)
        .setFirstResult(offset)
        .setMaxResults(limit)
        .getResultList();
  }

  public List<WebNotifEntity> findWebNotifsByUser(String userId, Boolean isRead) {
    return getEntityManager().createNamedQuery("commons.findNewWebNotifsByUser")
        .setParameter("userId", userId)
        .setParameter("isRead", isRead)
        .getResultList();
  }
  public List<WebNotifEntity> findWebNotifsByLastUpdatedDate(Date delayTime) {
    return getEntityManager().createNamedQuery("commons.findWebNotifsByLastUpdatedDate")
        .setParameter("delayTime", delayTime)
        .getResultList();
  }

  public List<WebNotifEntity> findWebNotifsOfUserByLastUpdatedDate(String userId, Date calendar) {
    return getEntityManager().createNamedQuery("commons.findWebNotifsOfUserByLastUpdatedDate")
        .setParameter("userId", userId)
        .setParameter("calendar", calendar)
        .getResultList();
  }

  public List<WebNotifEntity> findUnreadNotification(String pluginId, String owner, String activityId, Date date) {
    return getEntityManager().createNamedQuery("commons.findUnreadNotification")
        .setParameter("pluginId", pluginId)
        .setParameter("owner", owner)
        .setParameter("activityId", activityId)
        .setParameter("activityIdParamName", "activityId")
        .setParameter("calendar", date)
        .setFirstResult(0)
        .setMaxResults(1)
        .getResultList();
  }

  public WebNotifEntity findWebNotifsOfUserByParam(String to, String type, String paramValue, String paramName) {
    try {
      return (WebNotifEntity) getEntityManager().createNamedQuery("commons.findWebNotifsOfUserByParam")
          .setParameter("owner", to)
          .setParameter("pluginId", type)
          .setParameter("paramName", paramName)
          .setParameter("paramValue", paramValue)
          .getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }
}

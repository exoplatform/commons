package org.exoplatform.commons.notification.impl.jpa.web.dao;

import java.util.Calendar;
import java.util.List;

import javax.persistence.TypedQuery;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.notification.impl.jpa.web.entity.WebUsersEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

public class WebUsersDAO extends GenericDAOJPAImpl<WebUsersEntity, Long> {

  @ExoTransactional
  public List<WebUsersEntity> findWebNotifsByFilter(String pluginId, String userId, Boolean isOnPopover, int offset, int limit) {
    TypedQuery<WebUsersEntity> query = getEntityManager().createNamedQuery("NotificationsWebUsersEntity.findWebNotifsByPluginFilter", WebUsersEntity.class)
        .setParameter("pluginId", pluginId)
        .setParameter("userId", userId)
        .setParameter("isOnPopover", isOnPopover)
        .setFirstResult(offset);
    if (limit >= 0) {
      return query.setMaxResults(limit).getResultList();
    } else {
      return query.getResultList();
    }
  }

  @ExoTransactional
  public List<WebUsersEntity> findWebNotifsByFilter(String userId, int offset, int limit) {
    TypedQuery<WebUsersEntity> query = getEntityManager().createNamedQuery("NotificationsWebUsersEntity.findWebNotifsByUserFilter", WebUsersEntity.class)
        .setParameter("userId", userId)
        .setFirstResult(offset);
    if (limit >= 0) {
      return query.setMaxResults(limit).getResultList();
    } else {
      return query.getResultList();
    }
  }

  @ExoTransactional
  public List<WebUsersEntity> findWebNotifsByFilter(String userId, boolean isOnPopover, int offset, int limit) {
    TypedQuery<WebUsersEntity> query = getEntityManager().createNamedQuery("NotificationsWebUsersEntity.findWebNotifsByPopoverFilter", WebUsersEntity.class)
        .setParameter("userId", userId)
        .setParameter("isOnPopover", isOnPopover)
        .setFirstResult(offset);
    if (limit >= 0) {
      return query.setMaxResults(limit).getResultList();
    } else {
      return query.getResultList();
    }
  }

  @ExoTransactional
  public List<WebUsersEntity> findWebNotifsOfUserByLastUpdatedDate(String userId, Calendar calendar) {
    return getEntityManager().createNamedQuery("NotificationsWebUsersEntity.findWebNotifsOfUserByLastUpdatedDate", WebUsersEntity.class)
                             .setParameter("userId", userId)
                             .setParameter("calendar", calendar)
                             .getResultList();
  }

  @ExoTransactional
  public List<WebUsersEntity> findUnreadNotification(String pluginId, String userId, String paramName, String paramValue) {
    return getEntityManager().createNamedQuery("NotificationsWebUsersEntity.findUnreadNotification", WebUsersEntity.class)
                             .setParameter("pluginId", pluginId)
                             .setParameter("userId", userId)
                             .setParameter("paramName", paramName)
                             .setParameter("paramValue", paramValue)
                             .getResultList();
  }

  @ExoTransactional
  public void markAllRead(String userId) {
      getEntityManager().createNamedQuery("NotificationsWebUsersEntity.markWebNotifsAsReadByUser")
      .setParameter("userId", userId)
      .executeUpdate();
  }

  @ExoTransactional
  public List<WebUsersEntity> findWebNotifsByLastUpdatedDate(Calendar fiveDaysAgo) {
    return getEntityManager().createNamedQuery("NotificationsWebUsersEntity.findWebNotifsByLastUpdatedDate", WebUsersEntity.class)
                             .setParameter("calendar", fiveDaysAgo)
                             .getResultList();
  }

  @ExoTransactional
  public int getNumberOnBadge(String userId) {
    TypedQuery<Long> query =  getEntityManager().createNamedQuery("NotificationsWebUsersEntity.getNumberOnBadge", Long.class)
        .setParameter("userId", userId);
    return query.getSingleResult().intValue();
  }

  @ExoTransactional
  public List<WebUsersEntity> findNotifsWithBadge(String userId) {
    return getEntityManager().createNamedQuery("NotificationsWebUsersEntity.findNotifsWithBadge", WebUsersEntity.class)
        .setParameter("userId", userId)
        .getResultList();
  }

  @ExoTransactional
  public List<WebUsersEntity> findNotificationsByTypeAndParams(String pluginType, String paramName, String paramValue, String receiver, int offset, int limit) {
    TypedQuery<WebUsersEntity> query = getEntityManager()
        .createNamedQuery("NotificationsWebUsersEntity.findNotificationsByTypeAndParams", WebUsersEntity.class)
        .setParameter("pluginType", pluginType)
        .setParameter("paramName", paramName)
        .setParameter("paramValue", paramValue)
        .setParameter("receiver", receiver)
        .setFirstResult(offset);
    if (limit >= 0) {
          return query.setMaxResults(limit).getResultList();
    } else {
      return query.getResultList();
    }
  }
}

package org.exoplatform.commons.notification.impl.jpa.email.dao;

import java.util.Calendar;
import java.util.List;

import javax.persistence.TypedQuery;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailNotifEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

public class MailNotifDAO extends GenericDAOJPAImpl<MailNotifEntity, Long> {

  @ExoTransactional
  public List<MailNotifEntity> getNotifsByPluginAndDay(String pluginId, String dayName) {
    Calendar cal = Calendar.getInstance();
    TypedQuery<MailNotifEntity> query = getEntityManager()
                                                          .createNamedQuery("NotificationsMailNotifEntity.getNotifsByPluginAndDay",
                                                                            MailNotifEntity.class)
                                                          .setParameter("day", Integer.parseInt(dayName))
                                                          .setParameter("month", cal.get(Calendar.MONTH) + 1)
                                                          .setParameter("year", cal.get(Calendar.YEAR))
                                                          .setParameter("pluginId", pluginId);
    return query.getResultList();
  }

  @ExoTransactional
  public List<MailNotifEntity> getNotifsByPluginAndWeek(String pluginId, Calendar oneWeekAgo) {
    TypedQuery<MailNotifEntity> query = getEntityManager()
                                                          .createNamedQuery("NotificationsMailNotifEntity.getNotifsByPluginAndWeek",
                                                                            MailNotifEntity.class)
                                                          .setParameter("date", oneWeekAgo)
                                                          .setParameter("pluginId", pluginId);
    return query.getResultList();
  }

  @ExoTransactional
  public List<MailNotifEntity> getAllNotificationsWithoutDigests(int offset, int limit) {
    TypedQuery<MailNotifEntity> query = getEntityManager()
                                                          .createNamedQuery("NotificationsMailNotifEntity.getAllNotificationsWithoutDigests",
                                                                            MailNotifEntity.class)
                                                          .setFirstResult(offset)
                                                          .setMaxResults(limit);
    return query.getResultList();
  }
}

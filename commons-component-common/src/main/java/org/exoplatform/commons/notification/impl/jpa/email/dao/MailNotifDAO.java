package org.exoplatform.commons.notification.impl.jpa.email.dao;


import org.exoplatform.commons.notification.impl.jpa.email.entity.MailNotifEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

import javax.persistence.TypedQuery;
import java.util.Calendar;
import java.util.List;

/**
 * Created by exo on 3/8/17.
 */
public class MailNotifDAO extends GenericDAOJPAImpl<MailNotifEntity, Long> {

  public List<MailNotifEntity> getNotifsByPluginAndDay(String pluginId, String dayName) {
    Calendar cal = Calendar.getInstance();
    TypedQuery<MailNotifEntity> query = getEntityManager().createNamedQuery("commons.getNotifsByPluginAndDay", MailNotifEntity.class)
        .setParameter("day", Integer.parseInt(dayName))
        .setParameter("month", cal.get(Calendar.MONTH) +1)
        .setParameter("year", cal.get(Calendar.YEAR))
        .setParameter("pluginId", pluginId);
    return query.getResultList();
  }

  public List<MailNotifEntity> getNotifsByPluginAndWeek(String pluginId, Calendar oneWeekAgo) {
    TypedQuery<MailNotifEntity> query = getEntityManager().createNamedQuery("commons.getNotifsByPluginAndWeek", MailNotifEntity.class)
        .setParameter("date", oneWeekAgo)
        .setParameter("pluginId", pluginId);
    return query.getResultList();
  }
}

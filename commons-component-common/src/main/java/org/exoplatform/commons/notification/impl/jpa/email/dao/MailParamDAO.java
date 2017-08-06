package org.exoplatform.commons.notification.impl.jpa.email.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailNotifEntity;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailParamEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

public class MailParamDAO extends GenericDAOJPAImpl<MailParamEntity, Long> {

  @ExoTransactional
  public void deleteParamsOfNotifications(List<MailNotifEntity> allNotificationsWithoutDigests) {
    List<Long> ids = new ArrayList<>();
    for (MailNotifEntity mailNotifEntity : allNotificationsWithoutDigests) {
      ids.add(mailNotifEntity.getId());
    }
    Query query = getEntityManager().createNamedQuery("NotificationsMailParamsEntity.deleteParamsOfNotifications").setParameter("notifications", ids);
    query.executeUpdate();
  }

}

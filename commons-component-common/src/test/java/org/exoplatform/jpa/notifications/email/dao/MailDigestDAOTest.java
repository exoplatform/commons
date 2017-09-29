package org.exoplatform.jpa.notifications.email.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.notification.impl.jpa.email.JPAMailNotificationStorage;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailDigestEntity;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailNotifEntity;
import org.exoplatform.commons.notification.job.NotificationJob;
import org.exoplatform.commons.persistence.impl.EntityManagerHolder;
import org.exoplatform.jpa.CommonsDAOJPAImplTest;

public class MailDigestDAOTest extends CommonsDAOJPAImplTest {
  private JPAMailNotificationStorage notificationDataStorage;
  @Before
  public void setUp() throws Exception {
    super.setUp();
    mailDigestDAO.deleteAll();
    notificationDataStorage = getService(JPAMailNotificationStorage.class);
  }

  @After
  public void tearDown()  {
    mailDigestDAO.deleteAll();
  }

  @Test
  public void testIsDigestSent() throws Exception {
    MailNotifEntity mailNotifEntity1 = new MailNotifEntity();
    MailNotifEntity mailNotifEntity2 = new MailNotifEntity();

    //Given
    mailNotifEntity1 = mailNotifDAO.create(mailNotifEntity1);
    mailNotifEntity2 = mailNotifDAO.create(mailNotifEntity2);

    mailDigestDAO.create(new MailDigestEntity().setNotification(mailNotifEntity1).setType("daily"));
    mailDigestDAO.create(new MailDigestEntity().setNotification(mailNotifEntity1).setType("weekly"));
    mailDigestDAO.create(new MailDigestEntity().setNotification(mailNotifEntity2).setType("daily"));
    mailDigestDAO.create(new MailDigestEntity().setNotification(mailNotifEntity2).setType("weekly"));

    EntityManagerHolder.get().clear();

    mailNotifEntity1 = mailNotifDAO.find(mailNotifEntity1.getId());
    mailNotifEntity2 = mailNotifDAO.find(mailNotifEntity2.getId());

    //Then
    assertNotNull(mailNotifEntity1);
    assertNotNull(mailNotifEntity2);
    assertEquals(2, mailNotifEntity1.getDigests().size());
    assertEquals(2, mailNotifEntity2.getDigests().size());

    //when
    NotificationContext context = NotificationContextImpl.cloneInstance();
    context.append(NotificationJob.JOB_DAILY, true);
    notificationDataStorage.removeMessageAfterSent(context);
    EntityManagerHolder.get().clear();
    mailNotifEntity1 = mailNotifDAO.find(mailNotifEntity1.getId());
    mailNotifEntity2 = mailNotifDAO.find(mailNotifEntity2.getId());

    //then
    assertNotNull(mailNotifEntity1);
    assertNotNull(mailNotifEntity2);
    assertEquals(1, mailNotifEntity1.getDigests().size());
    assertEquals(1, mailNotifEntity2.getDigests().size());

    //when
    context = NotificationContextImpl.cloneInstance();
    context.append(NotificationJob.JOB_WEEKLY, true);
    notificationDataStorage.removeMessageAfterSent(context);
    EntityManagerHolder.get().clear();
    mailNotifEntity1 = mailNotifDAO.find(mailNotifEntity1.getId());
    mailNotifEntity2 = mailNotifDAO.find(mailNotifEntity2.getId());

    //then
    assertNull(mailNotifEntity1);
    assertNull(mailNotifEntity2);
  }
}

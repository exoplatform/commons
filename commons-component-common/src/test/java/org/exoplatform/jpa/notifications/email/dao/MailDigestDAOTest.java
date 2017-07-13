package org.exoplatform.jpa.notifications.email.dao;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.notification.impl.jpa.email.JPANotificationDataStorage;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailDigestEntity;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailNotifEntity;
import org.exoplatform.commons.notification.job.NotificationJob;
import org.exoplatform.jpa.CommonsDAOJPAImplTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by exo on 3/8/17.
 */
public class MailDigestDAOTest extends CommonsDAOJPAImplTest {
  private JPANotificationDataStorage notificationDataStorage;
  @Before
  public void setUp() throws Exception {
    super.setUp();
    mailDigestDAO.deleteAll();
    notificationDataStorage = getService(JPANotificationDataStorage.class);
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
    mailDigestDAO.create(new MailDigestEntity().setNotification(mailNotifEntity1).setType("daily"));
    mailDigestDAO.create(new MailDigestEntity().setNotification(mailNotifEntity1).setType("weekly"));
    mailDigestDAO.create(new MailDigestEntity().setNotification(mailNotifEntity2).setType("daily"));
    mailDigestDAO.create(new MailDigestEntity().setNotification(mailNotifEntity2).setType("weekly"));
    //Then
    assertThat(mailDigestDAO.isDigestDailySent(mailNotifEntity1), is(false));
    assertThat(mailDigestDAO.isDigestWeeklySent(mailNotifEntity1), is(false));
    assertThat(mailDigestDAO.isDigestDailySent(mailNotifEntity2), is(false));
    assertThat(mailDigestDAO.isDigestWeeklySent(mailNotifEntity2), is(false));

    //when
    NotificationContext context = NotificationContextImpl.cloneInstance();
    context.append(NotificationJob.JOB_DAILY, true);
    notificationDataStorage.removeMessageAfterSent(context);
    //then
    assertThat(mailDigestDAO.isDigestDailySent(mailNotifEntity1), is(true));
    assertThat(mailDigestDAO.isDigestDailySent(mailNotifEntity2), is(true));
    assertThat(mailDigestDAO.isDigestWeeklySent(mailNotifEntity2), is(false));

    //when
    NotificationContext context2 = NotificationContextImpl.cloneInstance();
    context2.append(NotificationJob.JOB_WEEKLY, true);
    notificationDataStorage.removeMessageAfterSent(context2);
    //then
    assertThat(mailDigestDAO.isDigestDailySent(mailNotifEntity1), is(true));
    assertThat(mailDigestDAO.isDigestWeeklySent(mailNotifEntity1), is(true));
    assertThat(mailDigestDAO.isDigestDailySent(mailNotifEntity2), is(true));
    assertThat(mailDigestDAO.isDigestWeeklySent(mailNotifEntity2), is(true));
  }
}

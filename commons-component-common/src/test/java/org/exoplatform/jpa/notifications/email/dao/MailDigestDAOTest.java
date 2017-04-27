package org.exoplatform.jpa.notifications.email.dao;

import org.exoplatform.commons.notification.impl.jpa.email.entity.MailDigestEntity;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailNotifEntity;
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
  @Before
  public void setUp() throws Exception {
    super.setUp();
    mailDigestDAO.deleteAll();
  }

  @After
  public void tearDown()  {
    mailDigestDAO.deleteAll();
  }

  @Test
  public void testIsDigestSent() {
    MailNotifEntity mailNotifEntity1 = new MailNotifEntity();
    MailNotifEntity mailNotifEntity2 = new MailNotifEntity();
    //Given
    mailDigestDAO.create(new MailDigestEntity().setNotification(mailNotifEntity1).setType("daily"));
    mailDigestDAO.create(new MailDigestEntity().setNotification(mailNotifEntity2).setType("daily"));
    mailDigestDAO.create(new MailDigestEntity().setNotification(mailNotifEntity2).setType("weekly"));

    //Then
    assertThat(mailDigestDAO.isDigestSent(mailNotifEntity1), is(false));
    assertThat(mailDigestDAO.isDigestSent(mailNotifEntity2), is(true));
  }
}

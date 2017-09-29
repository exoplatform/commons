package org.exoplatform.jpa.notifications.email.dao;

import org.exoplatform.commons.notification.impl.jpa.email.entity.MailQueueEntity;
import org.exoplatform.jpa.CommonsDAOJPAImplTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MailQueueDAOTest extends CommonsDAOJPAImplTest {
  @Before
  public void setUp() throws Exception {
    super.setUp();
    mailQueueDAO.deleteAll();
  }

  @After
  public void tearDown()  {
    mailQueueDAO.deleteAll();
  }

  @Test
  public void testFindAllByOffsetAndLimit() {
    //Given
    mailQueueDAO.create(new MailQueueEntity().setBody("1"));
    mailQueueDAO.create(new MailQueueEntity().setBody("2"));
    mailQueueDAO.create(new MailQueueEntity().setBody("3"));
    mailQueueDAO.create(new MailQueueEntity().setBody("4"));
    mailQueueDAO.create(new MailQueueEntity().setBody("5"));

    //When
    List<MailQueueEntity> mailQueueEntities1 = mailQueueDAO.findAll(0, 10);
    List<MailQueueEntity> mailQueueEntities2 = mailQueueDAO.findAll(2, 4);

    //Then
    assertThat(mailQueueEntities1.size(), is(5));
    assertEquals(mailQueueEntities2.get(0).getBody(), "3");
  }
}

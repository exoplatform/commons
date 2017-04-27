package org.exoplatform.jpa.notifications.email.dao;


import org.exoplatform.commons.notification.impl.jpa.email.entity.MailNotifEntity;
import org.exoplatform.jpa.CommonsDAOJPAImplTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by exo on 3/8/17.
 */
public class MailNotifDAOTest extends CommonsDAOJPAImplTest {
  @Before
  public void setUp() throws Exception {
    super.setUp();
    mailNotifDAO.deleteAll();
  }

  @After
  public void tearDown()  {
    mailNotifDAO.deleteAll();
  }

  @Test
  public void testGetNotifsByPluginAndDay() {
    //today at current time
    Calendar calendar = Calendar.getInstance();
    //today at a previous time
    Calendar calendar2 = Calendar.getInstance();
    calendar2.setTime(new Date(calendar2.getTime().getTime() - 100));
    //tomorrow
    Calendar calendar3 = Calendar.getInstance();
    calendar3.set(Calendar.DAY_OF_MONTH, calendar3.get(Calendar.DAY_OF_MONTH)+1);

    //Given
    mailNotifDAO.create(new MailNotifEntity().setCreationDate(calendar).setType("plugin1"));
    mailNotifDAO.create(new MailNotifEntity().setCreationDate(calendar2).setType("plugin1"));
    mailNotifDAO.create(new MailNotifEntity().setCreationDate(calendar2).setType("plugin2"));
    mailNotifDAO.create(new MailNotifEntity().setCreationDate(calendar3).setType("plugin1"));

    //When
    List<MailNotifEntity> mailNotifEntities1 = mailNotifDAO.getNotifsByPluginAndDay("plugin1", String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));
    List<MailNotifEntity> mailNotifEntities2 = mailNotifDAO.getNotifsByPluginAndDay("plugin2", String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));

    //Then
    assertThat(mailNotifEntities1.size(), is(2));
    assertThat(mailNotifEntities2.size(), is(1));
  }

  @Test
  public void testGetNotifsByPluginAndWeek() {
    //today
    Calendar today = Calendar.getInstance();
    //yesterday
    Calendar yesterday = Calendar.getInstance();
    yesterday.set(Calendar.DAY_OF_MONTH, yesterday.get(Calendar.DAY_OF_MONTH)-1);
    //4 days ago
    Calendar fourDaysAgo = Calendar.getInstance();
    fourDaysAgo.set(Calendar.DAY_OF_MONTH, fourDaysAgo.get(Calendar.DAY_OF_MONTH)-4);
    //8 days ago
    Calendar eightDaysAgo = Calendar.getInstance();
    eightDaysAgo.set(Calendar.DAY_OF_MONTH, eightDaysAgo.get(Calendar.DAY_OF_MONTH)-8);

    //Given
    mailNotifDAO.create(new MailNotifEntity().setCreationDate(today).setType("plugin1"));
    mailNotifDAO.create(new MailNotifEntity().setCreationDate(yesterday).setType("plugin1"));
    mailNotifDAO.create(new MailNotifEntity().setCreationDate(fourDaysAgo).setType("plugin1"));
    mailNotifDAO.create(new MailNotifEntity().setCreationDate(fourDaysAgo).setType("plugin2"));
    mailNotifDAO.create(new MailNotifEntity().setCreationDate(eightDaysAgo).setType("plugin1"));

    //When
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.WEEK_OF_YEAR, calendar.get(Calendar.WEEK_OF_YEAR) - 1);
    List<MailNotifEntity> mailNotifEntities1 = mailNotifDAO.getNotifsByPluginAndWeek("plugin1", calendar);
    List<MailNotifEntity> mailNotifEntities2 = mailNotifDAO.getNotifsByPluginAndWeek("plugin2", calendar);

    //Then
    assertThat(mailNotifEntities1.size(), is(3));
    assertThat(mailNotifEntities2.size(), is(1));
  }
}

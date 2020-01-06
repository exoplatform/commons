package org.exoplatform.jpa.notifications.web.dao;

import java.util.Calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.exoplatform.commons.notification.impl.jpa.web.entity.WebNotifEntity;
import org.exoplatform.commons.notification.impl.jpa.web.entity.WebParamsEntity;
import org.exoplatform.commons.notification.impl.jpa.web.entity.WebUsersEntity;
import org.exoplatform.commons.persistence.impl.EntityManagerHolder;
import org.exoplatform.jpa.CommonsDAOJPAImplTest;

public class WebUsersDAOTest extends CommonsDAOJPAImplTest {

  @Before
  public void setUp() throws Exception {
    super.setUp();
    webUsersDAO.deleteAll();
    webParamsDAO.deleteAll();
    webNotifDAO.deleteAll();
  }

  @After
  public void tearDown()  {
    webUsersDAO.deleteAll();
    webParamsDAO.deleteAll();
    webNotifDAO.deleteAll();
  }

  @Test
  public void testFindWebNotifsByFilter() {
    WebNotifEntity webNotifEntity1 = new WebNotifEntity();
    webNotifEntity1.setType("plugin1");
    webNotifEntity1 = webNotifDAO.create(webNotifEntity1);

    WebUsersEntity webUsersEntity1 = new WebUsersEntity();
    webUsersEntity1.setShowPopover(true);
    webUsersEntity1.setRead(false);
    webUsersEntity1.setReceiver("user1");
    webUsersEntity1.setNotification(webNotifEntity1);
    webUsersEntity1 = webUsersDAO.create(webUsersEntity1);

    EntityManagerHolder.get().clear();

    //Then
    assertEquals(1, webUsersDAO.findWebNotifsByFilter("plugin1", "user1", true, 0, 10).size());
    assertEquals(0, webUsersDAO.findWebNotifsByFilter("plugin2", "user1", true, 0, 10).size());

    WebNotifEntity webNotifEntity2 = new WebNotifEntity();
    webNotifEntity2.setType("plugin2");
    webNotifEntity2 = webNotifDAO.create(webNotifEntity2);

    WebUsersEntity webUsersEntity2 = new WebUsersEntity();
    webUsersEntity2.setShowPopover(true);
    webUsersEntity2.setRead(true);
    webUsersEntity2.setReceiver("user1");
    webUsersEntity2.setNotification(webNotifEntity2);
    webUsersEntity2 = webUsersDAO.create(webUsersEntity2);

    EntityManagerHolder.get().clear();

    assertEquals(1, webUsersDAO.findWebNotifsByFilter("plugin2", "user1", true, 0, 10).size());
    assertEquals(0, webUsersDAO.findWebNotifsByFilter("plugin1", "user2", true, 0, 10).size());
    assertEquals(0, webUsersDAO.findWebNotifsByFilter("plugin1", "user2", false, 0, 10).size());
    assertEquals(2, webUsersDAO.findWebNotifsByFilter("user1", 0, 10).size());
    assertEquals(2, webUsersDAO.findWebNotifsByFilter("user1", true, 0, 10).size());

    webUsersEntity2.setShowPopover(false);
    webUsersDAO.update(webUsersEntity2);

    EntityManagerHolder.get().clear();

    assertEquals(1, webUsersDAO.findWebNotifsByFilter("user1", false, 0, 10).size());
  }

  @Test
  public void testgetNotificationsByTypeAndParams() {
    WebNotifEntity webNotifEntity1 = new WebNotifEntity();
    webNotifEntity1.setType("plugin1");
    webNotifEntity1 = webNotifDAO.create(webNotifEntity1);

    WebUsersEntity webUsersEntity1 = new WebUsersEntity();
    webUsersEntity1.setReceiver("user1");
    webUsersEntity1.setNotification(webNotifEntity1);
    webUsersDAO.create(webUsersEntity1);
    WebParamsEntity webParamsEntity1 = new WebParamsEntity();
    webParamsEntity1.setName("toto");
    webParamsEntity1.setValue("titi");
    webParamsEntity1.setNotification(webNotifEntity1);
    webParamsDAO.create(webParamsEntity1);

    EntityManagerHolder.get().clear();

    //Then
    assertEquals(1, webUsersDAO.findNotificationsByTypeAndParams("plugin1", "toto", "titi", "user1", 0, 10).size());
    assertEquals(0, webUsersDAO.findNotificationsByTypeAndParams("plugin2", "toto", "titi", "user1", 0, 10).size());
    assertEquals(0, webUsersDAO.findNotificationsByTypeAndParams("plugin1", "toto", "tata", "user1", 0, 10).size());
    assertEquals(0, webUsersDAO.findNotificationsByTypeAndParams("plugin1", "tata", "titi", "user1", 0, 10).size());
    assertEquals(0, webUsersDAO.findNotificationsByTypeAndParams("plugin1", "toto", "titi", "user2", 0, 10).size());
  }

  @Test
  public void testFindWebNotifsByLastUpdatedDate() {
    //yesterday
    Calendar today = Calendar.getInstance();
    Calendar yesterday = (Calendar) today.clone();
    yesterday.add(Calendar.DATE, -1);

    //4 days ago
    Calendar fourDaysAgo = (Calendar) today.clone();
    fourDaysAgo.add(Calendar.DATE, -4);

    //5 days ago
    Calendar fiveDaysAgo = (Calendar) today.clone();
    fiveDaysAgo.add(Calendar.DATE, -5);

    //today
    WebNotifEntity webNotifEntity1 = new WebNotifEntity();
    webNotifEntity1.setCreationDate(today);
    webNotifEntity1 = webNotifDAO.create(webNotifEntity1);
    
    WebNotifEntity webNotifEntity2 = new WebNotifEntity();
    webNotifEntity2.setCreationDate(yesterday);
    webNotifEntity2 = webNotifDAO.create(webNotifEntity2);
    
    WebNotifEntity webNotifEntity3 = new WebNotifEntity();
    webNotifEntity3.setCreationDate(fourDaysAgo);
    webNotifEntity3 = webNotifDAO.create(webNotifEntity3);

    WebUsersEntity webUsersEntity1 = new WebUsersEntity();
    webUsersEntity1.setUpdateDate(today);
    webUsersEntity1.setReceiver("user1");
    webUsersEntity1.setNotification(webNotifEntity1);
    webUsersEntity1 = webUsersDAO.create(webUsersEntity1);

    WebUsersEntity webUsersEntity2 = new WebUsersEntity();
    webUsersEntity2.setUpdateDate(yesterday);
    webUsersEntity2.setReceiver("user1");
    webUsersEntity2.setNotification(webNotifEntity2);
    webUsersEntity2 = webUsersDAO.create(webUsersEntity2);

    WebUsersEntity webUsersEntity3 = new WebUsersEntity();
    webUsersEntity3.setUpdateDate(fourDaysAgo);
    webUsersEntity3.setReceiver("user1");
    webUsersEntity3.setNotification(webNotifEntity3);
    webUsersEntity3 = webUsersDAO.create(webUsersEntity3);

    end();
    begin();
    EntityManagerHolder.get().clear();

    //Then
    assertEquals(0, webUsersDAO.findWebNotifsByLastUpdatedDate(fiveDaysAgo).size());
    assertEquals(3, webUsersDAO.findWebNotifsByLastUpdatedDate(Calendar.getInstance()).size());
    assertEquals(1, webUsersDAO.findWebNotifsByLastUpdatedDate(yesterday).size());

    assertEquals(3, webUsersDAO.findWebNotifsOfUserByLastUpdatedDate("user1", Calendar.getInstance()).size());
    assertEquals(0, webUsersDAO.findWebNotifsOfUserByLastUpdatedDate("user2", Calendar.getInstance()).size());
    assertEquals(1, webUsersDAO.findWebNotifsOfUserByLastUpdatedDate("user1", yesterday).size());
    assertEquals(0, webUsersDAO.findWebNotifsOfUserByLastUpdatedDate("user2", yesterday).size());
    assertEquals(0, webUsersDAO.findWebNotifsOfUserByLastUpdatedDate("user1", fiveDaysAgo).size());
  }

  @Test
  public void testFindUnreadNotification() {
    WebNotifEntity webNotifEntity1 = new WebNotifEntity();
    webNotifEntity1.setCreationDate(Calendar.getInstance());
    webNotifEntity1.setType("plugin1");
    webNotifEntity1 = webNotifDAO.create(webNotifEntity1);

    WebNotifEntity webNotifEntity2 = new WebNotifEntity();
    webNotifEntity2.setCreationDate(Calendar.getInstance());
    webNotifEntity2.setType("plugin1");
    webNotifEntity2 = webNotifDAO.create(webNotifEntity2);

    WebNotifEntity webNotifEntity3 = new WebNotifEntity();
    webNotifEntity3.setCreationDate(Calendar.getInstance());
    webNotifEntity3.setType("plugin1");
    webNotifEntity3 = webNotifDAO.create(webNotifEntity3);

    WebNotifEntity webNotifEntity4 = new WebNotifEntity();
    webNotifEntity4.setCreationDate(Calendar.getInstance());
    webNotifEntity4.setType("plugin2");
    webNotifEntity4 = webNotifDAO.create(webNotifEntity4);

    WebParamsEntity webParamsEntity1 = new WebParamsEntity();
    webParamsEntity1.setName("activityId");
    webParamsEntity1.setValue("1");
    webParamsEntity1.setNotification(webNotifEntity1);
    webParamsEntity1 = webParamsDAO.create(webParamsEntity1);

    WebUsersEntity webUsersEntity1 = new WebUsersEntity();
    webUsersEntity1.setRead(false);
    webUsersEntity1.setReceiver("user1");
    webUsersEntity1.setUpdateDate(Calendar.getInstance());
    webUsersEntity1.setNotification(webNotifEntity1);
    webUsersEntity1 = webUsersDAO.create(webUsersEntity1);

    WebParamsEntity webParamsEntity2 = new WebParamsEntity();
    webParamsEntity2.setName("activityId");
    webParamsEntity2.setValue("1");
    webParamsEntity2.setNotification(webNotifEntity2);
    webParamsEntity2 = webParamsDAO.create(webParamsEntity2);

    WebUsersEntity webUsersEntity2 = new WebUsersEntity();
    webUsersEntity2.setRead(true);
    webUsersEntity2.setReceiver("user1");
    webUsersEntity2.setUpdateDate(Calendar.getInstance());
    webUsersEntity2.setNotification(webNotifEntity2);
    webUsersEntity2 = webUsersDAO.create(webUsersEntity2);

    WebParamsEntity webParamsEntity3 = new WebParamsEntity();
    webParamsEntity3.setName("activityId");
    webParamsEntity3.setValue("1");
    webParamsEntity3.setNotification(webNotifEntity3);
    webParamsEntity3 = webParamsDAO.create(webParamsEntity3);

    WebUsersEntity webUsersEntity3 = new WebUsersEntity();
    webUsersEntity3.setRead(false);
    webUsersEntity3.setReceiver("user2");
    webUsersEntity3.setUpdateDate(Calendar.getInstance());
    webUsersEntity3.setNotification(webNotifEntity3);
    webUsersEntity3 = webUsersDAO.create(webUsersEntity3);

    WebParamsEntity webParamsEntity4 = new WebParamsEntity();
    webParamsEntity4.setName("activityId");
    webParamsEntity4.setValue("1");
    webParamsEntity4.setNotification(webNotifEntity4);
    webParamsEntity4 = webParamsDAO.create(webParamsEntity4);

    WebUsersEntity webUsersEntity4 = new WebUsersEntity();
    webUsersEntity4.setRead(false);
    webUsersEntity4.setReceiver("user1");
    webUsersEntity4.setUpdateDate(Calendar.getInstance());
    webUsersEntity4.setNotification(webNotifEntity4);
    webUsersEntity4 = webUsersDAO.create(webUsersEntity4);

    //yesterday
    Calendar yesterday = Calendar.getInstance();
    yesterday.add(Calendar.DATE, -1);

    //Then
    assertEquals(1, webUsersDAO.findUnreadNotification("plugin1", "user2", "activityId", "1").size());
    assertEquals(1, webUsersDAO.findUnreadNotification("plugin2", "user1", "activityId", "1").size());
    assertEquals(1, webUsersDAO.findUnreadNotification("plugin1", "user2", "activityId", "1").size());
    assertEquals(0, webUsersDAO.findUnreadNotification("plugin1", "user2", "activityId", "2").size());
  }
}

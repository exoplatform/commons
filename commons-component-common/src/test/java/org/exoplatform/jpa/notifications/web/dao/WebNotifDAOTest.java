package org.exoplatform.jpa.notifications.web.dao;

import org.exoplatform.commons.notification.impl.jpa.web.entity.WebNotifEntity;
import org.exoplatform.commons.notification.impl.jpa.web.entity.WebParamsEntity;
import org.exoplatform.commons.notification.impl.jpa.web.entity.WebUsersEntity;
import org.exoplatform.jpa.CommonsDAOJPAImplTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by exo on 3/8/17.
 */
public class WebNotifDAOTest extends CommonsDAOJPAImplTest {
  @Before
  public void setUp() throws Exception {
    super.setUp();
    webNotifDAO.deleteAll();
  }

  @After
  public void tearDown()  {
    webNotifDAO.deleteAll();
  }

  @Test
  public void testFindWebNotifsByFilter() {
    WebUsersEntity webUsersEntity1 = new WebUsersEntity();
    webUsersEntity1.setShowPopover(true);
    webUsersEntity1.setRead(false);
    webUsersEntity1.setReceiver("user1");
    WebNotifEntity webNotifEntity1 = new WebNotifEntity();
    webNotifEntity1.addReceiver(webUsersEntity1);
    webNotifEntity1.setType("plugin1");
    webUsersEntity1.setNotification(webNotifEntity1);

    WebUsersEntity webUsersEntity2 = new WebUsersEntity();
    webUsersEntity2.setShowPopover(true);
    webUsersEntity2.setRead(true);
    webUsersEntity2.setReceiver("user1");
    WebNotifEntity webNotifEntity2 = new WebNotifEntity();
    webNotifEntity2.addReceiver(webUsersEntity2);
    webNotifEntity2.setType("plugin2");
    webUsersEntity2.setNotification(webNotifEntity2);

    //Given
    webNotifDAO.create(webNotifEntity1);

    //Then
    assertEquals(webNotifDAO.findWebNotifsByFilter("plugin1", "user1", true, 0, 10).size(), 1);
    assertEquals(webNotifDAO.findWebNotifsByFilter("plugin2", "user1", true, 0, 10).size(), 0);
    webNotifDAO.create(webNotifEntity2);
    assertEquals(webNotifDAO.findWebNotifsByFilter("plugin2", "user1", true, 0, 10).size(), 1);
    assertEquals(webNotifDAO.findWebNotifsByFilter("plugin1", "user2", true, 0, 10).size(), 0);
    assertEquals(webNotifDAO.findWebNotifsByFilter("plugin1", "user2", false, 0, 10).size(), 0);
    assertEquals(webNotifDAO.findWebNotifsByFilter("user1", 0, 10).size(), 2);
    assertEquals(webNotifDAO.findWebNotifsByFilter("user1", true, 0, 10).size(), 2);
    webUsersEntity2.setShowPopover(false);
    webNotifDAO.update(webNotifEntity2);
    assertEquals(webNotifDAO.findWebNotifsByFilter("user1", false, 0, 10).size(), 1);
  }

  @Test
  public void testFindWebNotifsByLastUpdatedDate() {
    //today
    WebUsersEntity webUsersEntity1 = new WebUsersEntity();
    webUsersEntity1.setUpdateDate(Calendar.getInstance().getTime());
    webUsersEntity1.setReceiver("user1");
    WebNotifEntity webNotifEntity1 = new WebNotifEntity();
    webNotifEntity1.addReceiver(webUsersEntity1);
    webUsersEntity1.setNotification(webNotifEntity1);

    //yesterday
    Calendar yesterday = Calendar.getInstance();
    yesterday.set(Calendar.DAY_OF_MONTH, yesterday.get(Calendar.DAY_OF_MONTH)-1);
    WebUsersEntity webUsersEntity2 = new WebUsersEntity();
    webUsersEntity2.setUpdateDate(yesterday.getTime());
    webUsersEntity2.setReceiver("user1");
    WebNotifEntity webNotifEntity2 = new WebNotifEntity();
    webNotifEntity2.addReceiver(webUsersEntity2);
    webUsersEntity2.setNotification(webNotifEntity2);

    //4 days ago
    Calendar fourDaysAgo = Calendar.getInstance();
    fourDaysAgo.set(Calendar.DAY_OF_MONTH, fourDaysAgo.get(Calendar.DAY_OF_MONTH)-4);
    WebUsersEntity webUsersEntity3 = new WebUsersEntity();
    webUsersEntity3.setUpdateDate(fourDaysAgo.getTime());
    webUsersEntity3.setReceiver("user1");
    WebNotifEntity webNotifEntity3 = new WebNotifEntity();
    webNotifEntity3.addReceiver(webUsersEntity3);
    webUsersEntity3.setNotification(webNotifEntity3);

    //Given
    webNotifDAO.create(webNotifEntity1);
    webNotifDAO.create(webNotifEntity2);
    webNotifDAO.create(webNotifEntity3);

    //5 days ago
    Calendar fiveDaysAgo = Calendar.getInstance();
    fiveDaysAgo.set(Calendar.DAY_OF_MONTH, fiveDaysAgo.get(Calendar.DAY_OF_MONTH)-5);

    //Then
    assertEquals(webNotifDAO.findWebNotifsByLastUpdatedDate(Calendar.getInstance().getTime()).size(), 3);
    assertEquals(webNotifDAO.findWebNotifsByLastUpdatedDate(yesterday.getTime()).size(), 1);
    assertEquals(webNotifDAO.findWebNotifsByLastUpdatedDate(fiveDaysAgo.getTime()).size(), 0);
    assertEquals(webNotifDAO.findWebNotifsOfUserByLastUpdatedDate("user1", Calendar.getInstance().getTime()).size(), 3);
    assertEquals(webNotifDAO.findWebNotifsOfUserByLastUpdatedDate("user2", Calendar.getInstance().getTime()).size(), 0);
    assertEquals(webNotifDAO.findWebNotifsOfUserByLastUpdatedDate("user1", yesterday.getTime()).size(), 1);
    assertEquals(webNotifDAO.findWebNotifsOfUserByLastUpdatedDate("user2", yesterday.getTime()).size(), 0);
    assertEquals(webNotifDAO.findWebNotifsOfUserByLastUpdatedDate("user1", fiveDaysAgo.getTime()).size(), 0);
  }

  @Test
  public void testFindUnreadNotification() {
    WebParamsEntity webParamsEntity1 = new WebParamsEntity();
    webParamsEntity1.setName("activityId");
    webParamsEntity1.setValue("1");
    Set<WebParamsEntity> params1 = new HashSet<WebParamsEntity>();
    params1.add(webParamsEntity1);
    WebUsersEntity webUsersEntity1 = new WebUsersEntity();
    webUsersEntity1.setRead(false);
    webUsersEntity1.setReceiver("user1");
    webUsersEntity1.setUpdateDate(Calendar.getInstance().getTime());
    WebNotifEntity webNotifEntity1 = new WebNotifEntity();
    webNotifEntity1.addReceiver(webUsersEntity1);
    webNotifEntity1.setParameters(params1);
    webNotifEntity1.setType("plugin1");
    webUsersEntity1.setNotification(webNotifEntity1);
    webParamsEntity1.setNotification(webNotifEntity1);


    WebParamsEntity webParamsEntity2 = new WebParamsEntity();
    webParamsEntity2.setName("activityId");
    webParamsEntity2.setValue("1");
    Set<WebParamsEntity> params2 = new HashSet<WebParamsEntity>();
    params2.add(webParamsEntity2);
    WebUsersEntity webUsersEntity2 = new WebUsersEntity();
    webUsersEntity2.setRead(true);
    webUsersEntity2.setReceiver("user1");
    webUsersEntity2.setUpdateDate(Calendar.getInstance().getTime());
    WebNotifEntity webNotifEntity2 = new WebNotifEntity();
    webNotifEntity2.addReceiver(webUsersEntity2);
    webNotifEntity2.setType("plugin1");
    webNotifEntity2.setParameters(params2);
    webUsersEntity2.setNotification(webNotifEntity2);
    webParamsEntity2.setNotification(webNotifEntity2);

    WebParamsEntity webParamsEntity3 = new WebParamsEntity();
    webParamsEntity3.setName("activityId");
    webParamsEntity3.setValue("1");
    Set<WebParamsEntity> params3 = new HashSet<WebParamsEntity>();
    params3.add(webParamsEntity3);
    WebUsersEntity webUsersEntity3 = new WebUsersEntity();
    webUsersEntity3.setRead(false);
    webUsersEntity3.setReceiver("user2");
    webUsersEntity3.setUpdateDate(Calendar.getInstance().getTime());
    WebNotifEntity webNotifEntity3 = new WebNotifEntity();
    webNotifEntity3.addReceiver(webUsersEntity3);
    webNotifEntity3.setType("plugin1");
    webNotifEntity3.setParameters(params3);
    webUsersEntity3.setNotification(webNotifEntity3);
    webParamsEntity3.setNotification(webNotifEntity3);

    WebParamsEntity webParamsEntity4 = new WebParamsEntity();
    webParamsEntity4.setName("activityId");
    webParamsEntity4.setValue("1");
    Set<WebParamsEntity> params4 = new HashSet<WebParamsEntity>();
    params4.add(webParamsEntity4);
    WebUsersEntity webUsersEntity4 = new WebUsersEntity();
    webUsersEntity4.setRead(false);
    webUsersEntity4.setReceiver("user1");
    webUsersEntity4.setUpdateDate(Calendar.getInstance().getTime());
    WebNotifEntity webNotifEntity4 = new WebNotifEntity();
    webNotifEntity4.addReceiver(webUsersEntity4);
    webNotifEntity4.setType("plugin2");
    webNotifEntity4.setParameters(params4);
    webUsersEntity4.setNotification(webNotifEntity4);
    webParamsEntity4.setNotification(webNotifEntity4);

    //Given
    webNotifDAO.create(webNotifEntity1);
    webNotifDAO.create(webNotifEntity2);
    webNotifDAO.create(webNotifEntity3);
    webNotifDAO.create(webNotifEntity4);

    //yesterday
    Calendar yesterday = Calendar.getInstance();
    yesterday.set(Calendar.DAY_OF_MONTH, yesterday.get(Calendar.DAY_OF_MONTH)-1);
    //Then
    assertEquals(webNotifDAO.findUnreadNotification("plugin1", "user1", "1", Calendar.getInstance().getTime()).size(), 0);
    assertEquals(webNotifDAO.findUnreadNotification("plugin2", "user1", "1", yesterday.getTime()).size(), 1);
    assertEquals(webNotifDAO.findUnreadNotification("plugin1", "user2", "1", yesterday.getTime()).size(), 1);
    assertEquals(webNotifDAO.findUnreadNotification("plugin1", "user2", "2", yesterday.getTime()).size(), 0);
  }

  @Test
  public void testFindWebNotifsOfUserByParam() {
    WebParamsEntity webParamsEntity1 = new WebParamsEntity();
    webParamsEntity1.setName("param1");
    webParamsEntity1.setValue("value1");
    Set<WebParamsEntity> params1 = new HashSet<WebParamsEntity>();
    params1.add(webParamsEntity1);
    WebUsersEntity webUsersEntity1 = new WebUsersEntity();
    webUsersEntity1.setReceiver("user1");
    WebNotifEntity webNotifEntity1 = new WebNotifEntity();
    webNotifEntity1.addReceiver(webUsersEntity1);
    webNotifEntity1.setParameters(params1);
    webNotifEntity1.setType("plugin1");
    webUsersEntity1.setNotification(webNotifEntity1);
    webParamsEntity1.setNotification(webNotifEntity1);


    WebParamsEntity webParamsEntity2 = new WebParamsEntity();
    webParamsEntity2.setName("param2");
    webParamsEntity2.setValue("value2");
    Set<WebParamsEntity> params2 = new HashSet<WebParamsEntity>();
    params2.add(webParamsEntity2);
    WebUsersEntity webUsersEntity2 = new WebUsersEntity();
    webUsersEntity2.setReceiver("user1");
    WebNotifEntity webNotifEntity2 = new WebNotifEntity();
    webNotifEntity2.addReceiver(webUsersEntity2);
    webNotifEntity2.setType("plugin1");
    webNotifEntity2.setParameters(params2);
    webUsersEntity2.setNotification(webNotifEntity2);
    webParamsEntity2.setNotification(webNotifEntity2);

    WebParamsEntity webParamsEntity3 = new WebParamsEntity();
    webParamsEntity3.setName("param1");
    webParamsEntity3.setValue("value3");
    Set<WebParamsEntity> params3 = new HashSet<WebParamsEntity>();
    params3.add(webParamsEntity3);
    WebUsersEntity webUsersEntity3 = new WebUsersEntity();
    webUsersEntity3.setReceiver("user1");
    WebNotifEntity webNotifEntity3 = new WebNotifEntity();
    webNotifEntity3.addReceiver(webUsersEntity3);
    webNotifEntity3.setType("plugin1");
    webNotifEntity3.setParameters(params3);
    webUsersEntity3.setNotification(webNotifEntity3);
    webParamsEntity3.setNotification(webNotifEntity3);

    WebParamsEntity webParamsEntity4 = new WebParamsEntity();
    webParamsEntity4.setName("param1");
    webParamsEntity4.setValue("value1");
    Set<WebParamsEntity> params4 = new HashSet<WebParamsEntity>();
    params4.add(webParamsEntity4);
    WebUsersEntity webUsersEntity4 = new WebUsersEntity();
    webUsersEntity4.setReceiver("user1");
    WebNotifEntity webNotifEntity4 = new WebNotifEntity();
    webNotifEntity4.addReceiver(webUsersEntity4);
    webNotifEntity4.setType("plugin2");
    webNotifEntity4.setParameters(params4);
    webUsersEntity4.setNotification(webNotifEntity4);
    webParamsEntity4.setNotification(webNotifEntity4);

    //Given
    webNotifDAO.create(webNotifEntity1);
    webNotifDAO.create(webNotifEntity2);
    webNotifDAO.create(webNotifEntity3);
    webNotifDAO.create(webNotifEntity4);

    //Then
    assertNotNull(webNotifDAO.findWebNotifsOfUserByParam("user1", "plugin1", "value1", "param1"));
    assertNotNull(webNotifDAO.findWebNotifsOfUserByParam("user1", "plugin1", "value2", "param2"));
    assertNotNull(webNotifDAO.findWebNotifsOfUserByParam("user1", "plugin1", "value3", "param1"));
    assertNull(webNotifDAO.findWebNotifsOfUserByParam("user1", "plugin2", "value2", "param2"));
    assertNull(webNotifDAO.findWebNotifsOfUserByParam("user2", "plugin1", "value1", "param1"));
  }
}

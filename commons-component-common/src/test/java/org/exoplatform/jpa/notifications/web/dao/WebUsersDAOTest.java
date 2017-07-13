package org.exoplatform.jpa.notifications.web.dao;

import org.exoplatform.commons.notification.impl.jpa.web.entity.WebNotifEntity;
import org.exoplatform.commons.notification.impl.jpa.web.entity.WebUsersEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.jpa.CommonsDAOJPAImplTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by exo on 3/8/17.
 */
public class WebUsersDAOTest extends CommonsDAOJPAImplTest {

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
  public void testFindWebNotifsByUser() {
    WebUsersEntity webUsersEntity1 = new WebUsersEntity();
    webUsersEntity1.setRead(true);
    webUsersEntity1.setReceiver("user1");
    WebNotifEntity webNotifEntity1 = new WebNotifEntity();
    webNotifEntity1.addReceiver(webUsersEntity1);
    webUsersEntity1.setNotification(webNotifEntity1);

    WebUsersEntity webUsersEntity2 = new WebUsersEntity();
    webUsersEntity2.setRead(true);
    webUsersEntity2.setReceiver("user1");
    WebNotifEntity webNotifEntity2 = new WebNotifEntity();
    webNotifEntity2.addReceiver(webUsersEntity2);
    webUsersEntity2.setNotification(webNotifEntity2);

    WebUsersEntity webUsersEntity3 = new WebUsersEntity();
    webUsersEntity3.setRead(false);
    webUsersEntity3.setReceiver("user1");
    WebNotifEntity webNotifEntity3 = new WebNotifEntity();
    webNotifEntity3.addReceiver(webUsersEntity3);
    webUsersEntity3.setNotification(webNotifEntity3);

    //Given
    webNotifDAO.create(webNotifEntity1);
    webNotifDAO.create(webNotifEntity2);

    //Then
    assertEquals(webUsersDAO.findWebNotifsByUser("user1").size(), 2);
  }

}

package org.exoplatform.jpa.notifications.web.dao;

import org.junit.After;
import org.junit.Before;

import org.exoplatform.jpa.CommonsDAOJPAImplTest;

public class WebNotifDAOTest extends CommonsDAOJPAImplTest {
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

}

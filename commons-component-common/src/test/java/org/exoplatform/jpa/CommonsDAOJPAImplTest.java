package org.exoplatform.jpa;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.exoplatform.commons.api.persistence.DataInitializer;
import org.exoplatform.commons.notification.impl.jpa.email.dao.MailDigestDAO;
import org.exoplatform.commons.notification.impl.jpa.email.dao.MailNotifDAO;
import org.exoplatform.commons.notification.impl.jpa.email.dao.MailParamDAO;
import org.exoplatform.commons.notification.impl.jpa.email.dao.MailQueueDAO;
import org.exoplatform.commons.notification.impl.jpa.web.dao.WebNotifDAO;
import org.exoplatform.commons.notification.impl.jpa.web.dao.WebParamsDAO;
import org.exoplatform.commons.notification.impl.jpa.web.dao.WebUsersDAO;
import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.settings.jpa.dao.SettingContextDAO;
import org.exoplatform.settings.jpa.dao.SettingScopeDAO;
import org.exoplatform.settings.jpa.dao.SettingsDAO;

public class CommonsDAOJPAImplTest extends BaseCommonsTestCase {
  protected SettingContextDAO settingContextDAO;

  protected SettingScopeDAO   settingScopeDAO;

  protected SettingsDAO       settingsDAO;

  protected MailDigestDAO     mailDigestDAO;

  protected MailNotifDAO      mailNotifDAO;

  protected MailParamDAO      mailParamsDAO;

  protected MailQueueDAO      mailQueueDAO;

  protected WebNotifDAO       webNotifDAO;

  protected WebParamsDAO      webParamsDAO;

  protected WebUsersDAO       webUsersDAO;

  public void setUp() throws Exception {
    super.setUp();

    // make sure data are well initialized for each test

    DataInitializer dataInitializer = getService(DataInitializer.class);
    dataInitializer.initData();

    // Init DAO
    settingContextDAO = getService(SettingContextDAO.class);
    settingScopeDAO = getService(SettingScopeDAO.class);
    settingsDAO = getService(SettingsDAO.class);
    mailDigestDAO = getService(MailDigestDAO.class);
    mailNotifDAO = getService(MailNotifDAO.class);
    mailParamsDAO = getService(MailParamDAO.class);
    mailQueueDAO = getService(MailQueueDAO.class);
    webNotifDAO = getService(WebNotifDAO.class);
    webParamsDAO = getService(WebParamsDAO.class);
    webUsersDAO = getService(WebUsersDAO.class);

    // Clean Data
    cleanDB();
  }

  public void testInit() {
    assertNotNull(settingContextDAO);
    assertNotNull(settingScopeDAO);
    assertNotNull(settingsDAO);
    assertNotNull(mailDigestDAO);
    assertNotNull(mailNotifDAO);
    assertNotNull(mailParamsDAO);
    assertNotNull(mailQueueDAO);
    assertNotNull(webNotifDAO);
    assertNotNull(webParamsDAO);
    assertNotNull(webUsersDAO);
  }

  public void tearDown() throws Exception {
    // Clean Data
    cleanDB();
    super.tearDown();
  }

  @BeforeClass
  @Override
  protected void beforeRunBare() {
    if (System.getProperty("gatein.test.output.path") == null) {
      System.setProperty("gatein.test.output.path", System.getProperty("java.io.tmpdir"));
    }
    super.beforeRunBare();
  }

  @AfterClass
  @Override
  protected void afterRunBare() {
    super.afterRunBare();
  }

  private void cleanDB() {
    settingsDAO.deleteAll();
    settingScopeDAO.deleteAll();
    settingContextDAO.deleteAll();

    mailParamsDAO.deleteAll();
    mailDigestDAO.deleteAll();
    mailNotifDAO.deleteAll();

    webParamsDAO.deleteAll();
    webUsersDAO.deleteAll();
    webNotifDAO.deleteAll();

    mailQueueDAO.deleteAll();
  }
}

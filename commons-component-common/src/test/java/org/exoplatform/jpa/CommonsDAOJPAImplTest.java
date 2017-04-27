package org.exoplatform.jpa;

import org.exoplatform.commons.api.persistence.DataInitializer;
import org.exoplatform.commons.notification.impl.jpa.email.dao.MailDigestDAO;
import org.exoplatform.commons.notification.impl.jpa.email.dao.MailNotifDAO;
import org.exoplatform.commons.notification.impl.jpa.email.dao.MailParamsDAO;
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

/**
 * Created by exo on 3/10/17.
 */
@ConfiguredBy({ @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/test-jpa-configuration.xml") })
public class CommonsDAOJPAImplTest extends BaseCommonsTestCase {
  protected SettingContextDAO settingContextDAO;

  protected SettingScopeDAO settingScopeDAO;

  protected SettingsDAO settingsDAO;

  protected MailDigestDAO mailDigestDAO;

  protected MailNotifDAO mailNotifDAO;

  protected MailParamsDAO mailParamsDAO;

  protected MailQueueDAO mailQueueDAO;

  protected WebNotifDAO webNotifDAO;

  protected WebParamsDAO webParamsDAO;

  protected WebUsersDAO webUsersDAO;


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
    mailParamsDAO = getService(MailParamsDAO.class);
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

  private void cleanDB() {
    settingContextDAO.deleteAll(settingContextDAO.findAll());
    settingScopeDAO.deleteAll(settingScopeDAO.findAll());
    settingsDAO.deleteAll(settingsDAO.findAll());
    mailDigestDAO.deleteAll(mailDigestDAO.findAll());
    mailNotifDAO.deleteAll(mailNotifDAO.findAll());
    mailParamsDAO.deleteAll(mailParamsDAO.findAll());
    mailQueueDAO.deleteAll(mailQueueDAO.findAll());
    webNotifDAO.deleteAll(webNotifDAO.findAll());
    webParamsDAO.deleteAll(webParamsDAO.findAll());
    webUsersDAO.deleteAll(webUsersDAO.findAll());
  }
}
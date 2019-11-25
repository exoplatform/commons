package org.exoplatform.jpa.notifications.web.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.notification.BaseNotificationTestCase;
import org.exoplatform.commons.notification.impl.jpa.web.JPAWebNotificationStorage;
import org.exoplatform.commons.notification.impl.jpa.web.dao.WebNotifDAO;
import org.exoplatform.commons.notification.impl.jpa.web.dao.WebParamsDAO;
import org.exoplatform.commons.notification.impl.jpa.web.dao.WebUsersDAO;
import org.exoplatform.commons.notification.plugin.PluginTest;
import org.exoplatform.commons.persistence.impl.EntityManagerHolder;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

@ConfiguredBy({ @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/test-root-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/commons-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/test-portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/test-jpa-configuration.xml") })
public class JPAWebNotificationStorageTest extends BaseNotificationTestCase {

  private JPAWebNotificationStorage webNotificationStorage;
  private WebNotifDAO webNotifDAO;
  private WebUsersDAO webUsersDAO;
  private WebParamsDAO webParamsDAO;
  protected List<String> userIds;

  public JPAWebNotificationStorageTest() {
    setForceContainerReload(true);
  }

  @Override
  public void setUp() throws Exception  {
    super.setUp();
    webNotificationStorage = getService(JPAWebNotificationStorage.class);
    webNotifDAO = getService(WebNotifDAO.class);
    webUsersDAO = getService(WebUsersDAO.class);
    webParamsDAO = getService(WebParamsDAO.class);
    userIds = new ArrayList<String>();
  }

  @Override
  public void tearDown() throws Exception  {
    webParamsDAO.deleteAll();
    webUsersDAO.deleteAll();
    webNotifDAO.deleteAll();
    super.tearDown();
  }

  public void testSaveWebNotification() throws Exception {
    String userId = "root";
    userIds.add(userId);
    NotificationInfo info = makeWebNotificationInfo(userId);
    webNotificationStorage.save(info);
    //
    assertEquals(1, webNotificationStorage.get(new WebNotificationFilter(userId), 0, 10).size());
  }

  public void testMarkRead() throws Exception {
    String userId = "root";
    userIds.add(userId);
    NotificationInfo info = makeWebNotificationInfo(userId);
    webNotificationStorage.save(info);
    List<NotificationInfo> list = webNotificationStorage.get(new WebNotificationFilter(userId), 0, 10);
    assertEquals(1, list.size());
    NotificationInfo notif = list.get(0);
    assertFalse(Boolean.valueOf(notif.getOwnerParameter().get(NotificationMessageUtils.READ_PORPERTY.getKey())));
    //
    webNotificationStorage.markRead(notif.getId());
    //
    notif = webNotificationStorage.get(notif.getId());
    assertTrue(Boolean.valueOf(notif.getOwnerParameter().get(NotificationMessageUtils.READ_PORPERTY.getKey())));
  }

  public void testMarkReadAll() throws Exception {
    String userId = "demo";
    userIds.add(userId);
    for (int i = 0; i < 10; i++) {
      NotificationInfo info = makeWebNotificationInfo(userId);
      webNotificationStorage.save(info);
    }
    List<NotificationInfo> list = webNotificationStorage.get(new WebNotificationFilter(userId), 0, 10);
    assertEquals(10, list.size());
    for(NotificationInfo notif : list) {
      assertFalse(Boolean.valueOf(notif.getOwnerParameter().get(NotificationMessageUtils.READ_PORPERTY.getKey())));
    }
    ConversationState.setCurrent(new ConversationState(new Identity(userId)));
    //
    webNotificationStorage.markAllRead(userId);
    //
    EntityManagerHolder.get().clear();
    list = webNotificationStorage.get(new WebNotificationFilter(userId), 0, 10);
    assertEquals(10, list.size());
    //
    for(NotificationInfo notif : list) {
      assertTrue(Boolean.valueOf(notif.getValueOwnerParameter(NotificationMessageUtils.READ_PORPERTY.getKey())));
    }
  }

  public void testUpdateNotification() throws Exception {
    String userId = "john";
    NotificationInfo info = makeWebNotificationInfo(userId);
    String notifId = info.getId();
    webNotificationStorage.save(info);
    NotificationInfo got = webNotificationStorage.get(info.getId());
    assertEquals("The title", got.getTitle());
    long lastUpdatedTime = got.getLastModifiedDate();

    //update and move top, the lastUpdatedTime will be modified
    got = makeWebNotificationInfo(userId);
    got.setId(notifId);
    got.setTitle("new title");
    got.setId(info.getId());
    webNotificationStorage.update(got, true);
    got = webNotificationStorage.get(got.getId());
    assertEquals("new title", got.getTitle());
    assertFalse(lastUpdatedTime == got.getLastModifiedDate());

    //update but don't move top, the lastUpdatedTime will not be modified
    lastUpdatedTime = got.getLastModifiedDate();
    got.setTitle("new new title");
    webNotificationStorage.update(got, false);
    got = webNotificationStorage.get(got.getId());
    assertEquals("new new title", got.getTitle());
    assertTrue(lastUpdatedTime == got.getLastModifiedDate());
  }

  public void testRemoveByJob() throws Exception {
    // Create data for old notifications
    /* Example:
     *  PastTime is 1/12/2014
     *  Today is 15/12/2014
     *  Create notification for:
     *   + 04/12/2014
     *   + 06/12/2014
     *   + 08/12/2014
     *   + 10/12/2014
     *   + 12/12/2014
     *  Case 1: Delay time 9 days, remove all web notification on days:
     *   + 04/12/2014
     *   + 06/12/2014
     *  Expected: remaining is 30 notifications / 3 days
     *  Case 2: Delay time 3 days, remove all web notification on days:
     *   + 08/12/2014
     *   + 10/12/2014
     *   + 12/12/2014
     *  Expected: remaining is 0 notification
    */
    String userId = "demo";
    Calendar cal = Calendar.getInstance();
    long t = 86400000l;
    long current = cal.getTimeInMillis();
    for (int i = 12; i > 3; i = i - 2) {
      cal.setTimeInMillis(current - i * t);
      for (int j = 0; j < 10; j++) {
        NotificationInfo info = makeWebNotificationInfo(userId).setDateCreated(cal);
        //
        webNotificationStorage.save(info);
      }
    }
    webNotifDAO.findAll();
    // check data
    //getWebUserDateNode
//    SessionProvider sProvider = SessionProvider.createSystemProvider();
//    Node parentNode = getOrCreateChannelNode(sProvider, userId);
//    assertEquals(5, webNotifDAO.findAll());
//    //
//    NodeIterator iter = null;
//    for (int i = 4; i < 13; i = i + 2) {
//      cal.setTimeInMillis(current - i * t);
//      Node node = getOrCreateWebDateNode(sProvider, cal, userId);
//      iter = node.getNodes();
//      assertEquals(10, iter.getSize());
//    }
    //
//    storage.remove(userId, 9 * daySeconds);
//    //
//    assertEquals(3, parentNode.getNodes().getSize());
//    //
//    storage.remove(userId, 3 * daySeconds);
//    assertEquals(0, parentNode.getNodes().getSize());
  }

  public void testGetNewMessage() throws Exception  {
    assertEquals(8, NotificationMessageUtils.getMaxItemsInPopover());
    //
    String userId = "root";
    userIds.add(userId);
    webNotificationStorage.save(makeWebNotificationInfo(userId));
    //
    assertEquals(1, webNotificationStorage.getNumberOnBadge(userId));
    webNotificationStorage.save(makeWebNotificationInfo(userId));
    assertEquals(2, webNotificationStorage.getNumberOnBadge(userId));
    for (int i = 0; i < 10; ++i) {
      webNotificationStorage.save(makeWebNotificationInfo(userId));
    }
    //
    List<NotificationInfo> list = webNotificationStorage.get(new WebNotificationFilter(userId), 0, 15);
    assertEquals(12, list.size());
    //
    assertEquals(12, webNotificationStorage.getNumberOnBadge(userId));
    //
    webNotificationStorage.resetNumberOnBadge(userId);
    //
    assertEquals(0, webNotificationStorage.getNumberOnBadge(userId));
  }

  public void testSpecialUserNameToGetMessage() throws Exception {
    //Test with methods: getUnreadNotification, getNewMessage and remove
    String userId = "don't_blink_polarity";
    userIds.add(userId);
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(cal.getTimeInMillis() - 3 * 86400000l);
    //
    webNotificationStorage.save(makeWebNotificationInfo(userId).setDateCreated(cal).setLastModifiedDate(cal));

    EntityManagerHolder.get().clear();

    NotificationInfo unreadNotification = webNotificationStorage.getUnreadNotification(PluginTest.ID, "TheActivityId", userId);
    assertNotNull(unreadNotification);
    assertEquals(PluginTest.ID, unreadNotification.getKey().getId());
    assertEquals(1, webNotificationStorage.getNumberOnBadge(userId));
    assertTrue(webNotificationStorage.remove(userId, 86400));
    //
    assertEquals(0, webNotificationStorage.getNumberOnBadge(userId));
  }

  public void testRemoveByLiveTime() throws Exception {
    // Create data for old notifications
    /* Example:
     *  PastTime is 1/12/2014
     *  Today is 15/12/2014
     *  Create notification for:
     *   + 04/12/2014
     *   + 06/12/2014
     *   + 08/12/2014
     *   + 10/12/2014
     *   + 12/12/2014
     *  Case 1: Delay time 9 days, remove all web notification on days:
     *   + 04/12/2014
     *   + 06/12/2014
     *  Expected: remaining is 30 notifications / 3 days
     *  Case 2: Delay time 3 days, remove all web notification on days:
     *   + 08/12/2014
     *   + 10/12/2014
     *   + 12/12/2014
     *  Expected: remaining is 0 notification
    */
    String userId = "demo";
    Calendar cal = Calendar.getInstance();
    long t = 86400000l;
    long current = cal.getTimeInMillis();
    for (int i = 12; i > 3; i = i - 2) {
      cal.setTimeInMillis(current - i * t);
      for (int j = 0; j < 10; j++) {
        NotificationInfo info = makeWebNotificationInfo(userId).setDateCreated(cal);
        //
        webNotificationStorage.save(info);
      }
    }
    webNotifDAO.findAll();
//    // check data
//    SessionProvider sProvider = SessionProvider.createSystemProvider();
//    Node parentNode = getOrCreateChannelNode(sProvider, userId);
//    assertEquals(5, parentNode.getNodes().getSize());
//    //
//    webNotificationStorage.remove(userId, 9 * daySeconds);
//    //
//    assertEquals(3, parentNode.getNodes().getSize());
//    //
//    webNotificationStorage.remove(userId, 3 * daySeconds);
//    assertEquals(0, parentNode.getNodes().getSize());
  }

  public void testGetNotificationsByTypeAndParams() {
    String userId = "toto";
    userIds.add(userId);
    NotificationInfo info = makeWebNotificationInfo(userId);
    webNotificationStorage.save(info);
    WebNotificationFilter referenceFilter = new WebNotificationFilter(userId);
    referenceFilter.setParameter("activityId", "TheActivityId");
    referenceFilter.setPluginKey(new PluginKey(PluginTest.ID));
    List<NotificationInfo> gotList = webNotificationStorage.get(referenceFilter, 0, 10);
    //
    // Test fake parameter value
    WebNotificationFilter fakeParameterFilter = new WebNotificationFilter(userId);
    fakeParameterFilter.setParameter("activityId", "fake");
    PluginKey pluginKey = new PluginKey(PluginTest.ID);
    fakeParameterFilter.setPluginKey(pluginKey);
    // Test fake plugin key
    WebNotificationFilter fakePluginFilter = new WebNotificationFilter(userId);
    fakePluginFilter.setParameter("activityId", "TheActivityId");
    fakePluginFilter.setPluginKey(new PluginKey("FakePluginId"));

    // not found because of fake plugin key
    assertEquals(0, webNotificationStorage.get(fakePluginFilter, 0, 10).size());
    // not removed because of fake parameter value
    assertEquals(0, webNotificationStorage.get(fakeParameterFilter, 0, 10).size());
    // success get
    assertEquals(1, webNotificationStorage.get(referenceFilter, 0, 10).size());
    assertEquals(info.getId(), gotList.get(0).getId());
  }
}
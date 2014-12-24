package org.exoplatform.commons.notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.plugin.PluginTest;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

public class WebNotificationStorageTestCase extends BaseNotificationTestCase {

  public WebNotificationStorageTestCase() {
  }
  private static final String NOTIFICATION = "notification";
  private static final String NT_UNSTRUCTURED = "nt:unstructured";
  private WebNotificationStorage   webStorage;
  private final String WORKSPACE_COLLABORATION = "collaboration";
  private NodeHierarchyCreator nodeHierarchyCreator;
  private List<String> userIds;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    nodeHierarchyCreator = getService(NodeHierarchyCreator.class);
    webStorage = getService(WebNotificationStorage.class);
    assertNotNull(webStorage);
    //
    ManageableRepository repo = repositoryService.getRepository(REPO_NAME);
    repo.getConfiguration().setDefaultWorkspaceName(WORKSPACE_COLLABORATION);
    session = repo.getSystemSession(WORKSPACE_COLLABORATION);
    root = session.getRootNode();
    //
    userIds = new ArrayList<String>();
  }

  @Override
  public void tearDown() throws Exception {
    for (String userId : userIds) {
      Node userNodeApp = nodeHierarchyCreator.getUserApplicationNode(SessionProvider.createSystemProvider(), userId);
      userNodeApp.getNode(NOTIFICATION).remove();
    }
    session.save();
    //
    super.tearDown();
  }

  private NotificationInfo makeWebNotificationInfo(String userId) {
    NotificationInfo info = NotificationInfo.instance();
    info.key(new PluginKey(PluginTest.ID));
    info.setTitle("The title");
    info.setFrom("mary");
    info.setTo(userId);
    info.with(AbstractService.NTF_SHOW_POPOVER, "true")
        .with(AbstractService.NTF_READ, "false")
        .with("activityId", "TheActivityId")
        .with("accessLink", "http://fsdfsdf.com/fsdfsf");
    return info;
  }
  
  private String getDateName(Calendar cal) {
    return new SimpleDateFormat(AbstractService.DATE_NODE_PATTERN).format(cal.getTime());
  }
  
  private Node getWebUserCurrentDateNode(String userId) throws Exception {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    Calendar cal = Calendar.getInstance();
    return getWebUserDateNode(sProvider, cal, userId);
  }

  private Node getWebUserDateNode(SessionProvider sProvider, Calendar cal, String userId) throws Exception {
    String dateNodeName = getDateName(cal);
    Node userNode = getOrCreateWebUserNode(sProvider, userId);
    if (userNode.hasNode(dateNodeName)) {
      return userNode.getNode(dateNodeName);
    }
    return null;
  }

  private Node getOrCreateWebUserNode(SessionProvider sProvider, String userId) throws Exception {
    Node userNodeApp = nodeHierarchyCreator.getUserApplicationNode(sProvider, userId);
    Node parentNode = null;
    if (userNodeApp.hasNode(NOTIFICATION)) {
      parentNode = userNodeApp.getNode(NOTIFICATION);
    } else {
      parentNode = userNodeApp.addNode(NOTIFICATION, NT_UNSTRUCTURED);
    }
    if (parentNode.hasNode(AbstractService.WEB_CHANNEL)) {
      parentNode = parentNode.getNode(AbstractService.WEB_CHANNEL);
    } else {
      parentNode = parentNode.addNode(AbstractService.WEB_CHANNEL, AbstractService.NTF_CHANNEL);
    }
    return parentNode;
  }

  public void testSaveWebNotification() throws Exception {
    String userId = "root";
    userIds.add(userId);
    NotificationInfo info = makeWebNotificationInfo(userId);
    webStorage.save(info);
    //
    assertTrue(getWebUserCurrentDateNode(userId).getNodes().getSize() == 1);
  }

  public void testMarkRead() throws Exception {
    String userId = "root";
    userIds.add(userId);
    NotificationInfo info = makeWebNotificationInfo(userId);
    webStorage.save(info);
    Node notifiNode = getWebUserCurrentDateNode(userId).getNodes().nextNode();
    assertTrue(notifiNode.hasProperty(AbstractService.NTF_READ));
    assertFalse(notifiNode.getProperty(AbstractService.NTF_READ).getBoolean());
    //
    System.out.println(info.getValueOwnerParameter("UUID"));
    webStorage.markRead(info.getValueOwnerParameter("UUID"));
    //
    notifiNode = session.getNodeByUUID(info.getValueOwnerParameter("UUID"));
    assertTrue(notifiNode.getProperty(AbstractService.NTF_READ).getBoolean());
  }

  public void testMarkReadAll() throws Exception {
    String userId = "demo";
    userIds.add(userId);
    for (int i = 0; i < 10; i++) {
      NotificationInfo info = makeWebNotificationInfo(userId);
      webStorage.save(info);
    }
    NodeIterator iter = getWebUserCurrentDateNode(userId).getNodes();
    assertEquals(10, iter.getSize());
    while (iter.hasNext()) {
      Node node = iter.nextNode();
      assertFalse(node.getProperty(AbstractService.NTF_READ).getBoolean());
    }
    //
    webStorage.markReadAll(userId);
    //
    iter = getWebUserCurrentDateNode(userId).getNodes();
    while (iter.hasNext()) {
      Node node = iter.nextNode();
      assertTrue(node.getProperty(AbstractService.NTF_READ).getBoolean());
    }
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
        webStorage.save(info);
      }
    }
    // check data
    //getWebUserDateNode
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    Node parentNode = getOrCreateWebUserNode(sProvider, userId);
    assertEquals(5, parentNode.getNodes().getSize());
    //
    NodeIterator iter = null;
    for (int i = 4; i < 13; i = i + 2) {
      cal.setTimeInMillis(current - i * t);
      Node node = getWebUserDateNode(sProvider, cal, userId);
      iter = node.getNodes();
      assertEquals(10, iter.getSize());
    }
    //
    webStorage.remove(userId, 9*86400);
    //
    assertEquals(3, parentNode.getNodes().getSize());
    //
    webStorage.remove(userId, 3*86400);
    assertEquals(0, parentNode.getNodes().getSize());
  }
}
package org.exoplatform.commons.notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;

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
  
  private String getDateName() {
    return new SimpleDateFormat(AbstractService.DATE_NODE_PATTERN).format(Calendar.getInstance().getTime());
  }

  private Node getUserNotificationNode(String userId) throws Exception {
    Node userNodeApp = nodeHierarchyCreator.getUserApplicationNode(SessionProvider.createSystemProvider(), userId);
    Node parentNode;
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
    String dateNodeName = getDateName();
    if (parentNode.hasNode(dateNodeName)) {
      return parentNode.getNode(dateNodeName);
    }
    //
    Node dateNode = parentNode.addNode(dateNodeName, AbstractService.NTF_NOTIF_DATE);
    userNodeApp.getSession().save();

    return dateNode;
  }

  public void testSaveWebNotification() throws Exception {
    String userId = "root";
    userIds.add(userId);
    NotificationInfo info = makeWebNotificationInfo(userId);
    webStorage.save(info);
    //
    assertTrue(getUserNotificationNode(userId).getNodes().getSize() == 1);
  }

  public void testMarkRead() throws Exception {
    String userId = "root";
    userIds.add(userId);
    NotificationInfo info = makeWebNotificationInfo(userId);
    webStorage.save(info);
    Node notifiNode = getUserNotificationNode(userId).getNodes().nextNode();
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
    NodeIterator iter = getUserNotificationNode(userId).getNodes();
    assertEquals(10, iter.getSize());
    while (iter.hasNext()) {
      Node node = iter.nextNode();
      assertFalse(node.getProperty(AbstractService.NTF_READ).getBoolean());
    }
    //
    webStorage.markReadAll(userId);
    //
    iter = getUserNotificationNode(userId).getNodes();
    while (iter.hasNext()) {
      Node node = iter.nextNode();
      
      PropertyIterator iterator = node.getProperties();
      System.out.println("node: " + node.getName()  + " {");
      while (iterator.hasNext()) {
        Property p = iterator.nextProperty();
        if (p.getName().indexOf("jcr:") == 0) {
          continue;
        }
        try {
          System.out.println(" +" + p.getName() + " : " + p.getString());
        } catch (Exception e) {
          System.out.println(" +" +p.getName() + " : " + p.getValue().toString());
        }
      }
      System.out.println("}");
      assertTrue(node.getProperty(AbstractService.NTF_READ).getBoolean());
    }
  }
}

















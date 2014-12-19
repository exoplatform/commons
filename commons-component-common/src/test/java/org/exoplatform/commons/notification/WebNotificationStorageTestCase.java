package org.exoplatform.commons.notification;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.plugin.PluginTest;

public class WebNotificationStorageTestCase extends BaseNotificationTestCase {

  public WebNotificationStorageTestCase() {
  }
  private WebNotificationStorage   webStorage;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    webStorage = getService(WebNotificationStorage.class);
    assertNotNull(webStorage);
    //
    Node nftNode = null;
    if(!session.getRootNode().hasNode("eXoNotification")) {
      nftNode = session.getRootNode().addNode(AbstractService.NOTIFICATION_HOME_NODE, AbstractService.NTF_NOTIFICATION);
      nftNode.addNode(AbstractService.WEB_CHANNEL, AbstractService.NTF_CHANNEL);
    } else if(!session.getRootNode().hasNode("eXoNotification/web")) {
      session.getRootNode().getNode(AbstractService.NOTIFICATION_HOME_NODE)
             .addNode(AbstractService.WEB_CHANNEL, AbstractService.NTF_CHANNEL);
    }
    session.save();
  }
  
  @Override
  public void tearDown() throws Exception {
    Node homeNode = (Node) session.getItem("/eXoNotification/web");
    NodeIterator iterator = homeNode.getNodes();
    while (iterator.hasNext()) {
      Node node = (iterator.nextNode());
      node.remove();
    }
    session.save();
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
  
  private Node getUserNode(String userId) throws Exception {
    String dateName = new SimpleDateFormat(AbstractService.DATE_NODE_PATTERN).format(Calendar.getInstance().getTime());
    Node homeNode = (Node) session.getItem("/eXoNotification/web");
    assertTrue(homeNode.hasNode(dateName));
    assertTrue(homeNode.getNode(dateName).hasNode(userId));
    return homeNode.getNode(dateName + "/" + userId);
  }

  public void testSaveWebNotification() throws Exception {
    NotificationInfo info = makeWebNotificationInfo("root");
    webStorage.save(info);
    //
    assertTrue(getUserNode("root").getNodes().getSize() == 1);
  }
  
  public void testMarkRead() throws Exception {
    NotificationInfo info = makeWebNotificationInfo("root");
    webStorage.save(info);
    Node notifiNode = getUserNode("root").getNodes().nextNode();
    assertTrue(notifiNode.hasProperty(AbstractService.NTF_READ));
    assertFalse(notifiNode.getProperty(AbstractService.NTF_READ).getBoolean());
    //
    webStorage.markRead(info.getValueOwnerParameter("UUID"));
    //
    assertTrue(notifiNode.getProperty(AbstractService.NTF_READ).getBoolean());
  }
  
  public void testMarkReadAll() throws Exception {
    String userId = "demo";
    for (int i = 0; i < 10; i++) {
      NotificationInfo info = makeWebNotificationInfo(userId);
      webStorage.save(info);
    }
    NodeIterator iter = getUserNode(userId).getNodes();
    assertEquals(10, iter.getSize());
    while (iter.hasNext()) {
      Node node = iter.nextNode();
      assertFalse(node.getProperty(AbstractService.NTF_READ).getBoolean());
    }
    //
    webStorage.markReadAll(userId);
    //
    iter = getUserNode(userId).getNodes();
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

















package org.exoplatform.commons.notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.ObservationManager;

import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.impl.service.storage.WebNotificationStorageImpl;
import org.exoplatform.commons.notification.mock.AddNodeEventListener;
import org.exoplatform.commons.notification.plugin.PluginTest;
import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

@ConfiguredBy({ @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/test-root-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/commons-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/test-portal-configuration.xml") })
public abstract class BaseNotificationTestCase extends BaseCommonsTestCase {

  protected static final String NOTIFICATIONS = "notifications";
  protected static final String NT_UNSTRUCTURED = "nt:unstructured";
  protected final String WORKSPACE_COLLABORATION = "collaboration";
  protected List<String> userIds;
  protected WebNotificationStorageImpl storage;
  protected NodeHierarchyCreator nodeHierarchyCreator;
  protected Session collaborationSession;
  
  private boolean mustInitCollaborationWorkspace = false;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    if (!root.hasNode("settings")) {
      root.addNode("settings", "stg:settings");
      session.save();
    }
    if (this.mustInitCollaborationWorkspace) {
      nodeHierarchyCreator = getService(NodeHierarchyCreator.class);
      storage = getService(WebNotificationStorageImpl.class);
      //
      ManageableRepository repo = repositoryService.getRepository(REPO_NAME);
      repo.getConfiguration().setDefaultWorkspaceName(WORKSPACE_COLLABORATION);
      collaborationSession = repo.getSystemSession(WORKSPACE_COLLABORATION);
      addNodeEventListener(collaborationSession);
      root = collaborationSession.getRootNode();
      userIds = new ArrayList<String>();
    } else {
      //Adds the LastModifiedDate property to the added node.
      addNodeEventListener(session);
    }
  }
  
  protected void initCollaborationWorkspace() {
    this.mustInitCollaborationWorkspace = true;
  }
  
  @Override
  protected void tearDown() throws Exception {
    if (this.mustInitCollaborationWorkspace) {
      SessionProvider sessionProvider = SessionProvider.createSystemProvider();
      for (String userId : userIds) {
        Node userNodeApp = nodeHierarchyCreator.getUserApplicationNode(sessionProvider, userId);
        if (userNodeApp.hasNode(NOTIFICATIONS)) {
          userNodeApp.getNode(NOTIFICATIONS).remove();
          userNodeApp.save();
        }
      }
    }
    this.mustInitCollaborationWorkspace = false;
    session.logout();
  }
  
  private String convertDateToNodeName(Calendar cal) {
    return new SimpleDateFormat(AbstractService.DATE_NODE_PATTERN).format(cal.getTime());
  }
  
  /**
   * Gets or create the Web Date Node on Collaboration workspace.
   * 
   * For example: The web date node has the path as bellow:
   * User1: /Users/U___/Us___/Use___/User1/ApplicationData/notifications/web/20141224/
   * 
   * @param sProvider
   * @param notification
   * @return
   * @throws Exception
   */
  protected Node getOrCreateWebDateNode(SessionProvider sProvider, Calendar cal, String userId) throws Exception {
    String dateNodeName = convertDateToNodeName(cal);
    Node channelNode = getOrCreateChannelNode(sProvider, userId);
    if (channelNode.hasNode(dateNodeName)) {
      return channelNode.getNode(dateNodeName);
    } else {
      Node dateNode = channelNode.addNode(dateNodeName, AbstractService.NTF_NOTIF_DATE);
      dateNode.setProperty(AbstractService.NTF_LAST_MODIFIED_DATE, cal.getTimeInMillis());
      channelNode.getSession().save();
      return dateNode;
    }
  }

  /**
   * Gets or create the Channel Node by NodeHierarchyCreator on Collaboration workspace.
   * 
   * For example: The channel node has the path as bellow:
   * User1: /Users/U___/Us___/Use___/User1/ApplicationData/notifications/web/20141224/
   * 
   * @param sProvider
   * @param userId the remoteId
   * @return the channel node
   * @throws Exception
   */
  protected Node getOrCreateChannelNode(SessionProvider sProvider, String userId) throws Exception {
    Node userNodeApp = nodeHierarchyCreator.getUserApplicationNode(sProvider, userId);
    Node parentNode = null;
    if (userNodeApp.hasNode(NOTIFICATIONS)) {
      parentNode = userNodeApp.getNode(NOTIFICATIONS);
    } else {
      parentNode = userNodeApp.addNode(NOTIFICATIONS, NT_UNSTRUCTURED);
    }
    Node channelNode = null;
    if (parentNode.hasNode(AbstractService.WEB_CHANNEL)) {
      channelNode = parentNode.getNode(AbstractService.WEB_CHANNEL);
    } else {
      channelNode = parentNode.addNode(AbstractService.WEB_CHANNEL, AbstractService.NTF_CHANNEL);
    }
    return channelNode;
  }
  
  protected NotificationInfo makeWebNotificationInfo(String userId) {
    NotificationInfo info = NotificationInfo.instance();
    info.key(new PluginKey(PluginTest.ID));
    info.setTitle("The title");
    info.setFrom("mary");
    info.setTo(userId);
    info.with(NotificationMessageUtils.SHOW_POPOVER_PROPERTY.getKey(), "true")
        .with(NotificationMessageUtils.READ_PORPERTY.getKey(), "false")
        .with("activityId", "TheActivityId")
        .with("accessLink", "http://fsdfsdf.com/fsdfsf");
    return info;
  }

  /**
   * Adds the LastModifiedDate property to the added node.
   * @throws Exception
   */
  protected void addNodeEventListener(Session mySession) throws Exception {
    try {
      ObservationManager observation = mySession.getWorkspace().getObservationManager();
      AddNodeEventListener addNodeListener = new AddNodeEventListener();
      addNodeListener.setSession(mySession);
      observation.addEventListener(addNodeListener, Event.NODE_ADDED, "/", true, null, new String[] {"nt:base"}, false);
    } catch (Exception e) {}
  }
}

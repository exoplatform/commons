package org.exoplatform.commons.notification.impl.service.storage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.impl.NotificationSessionManager;
import org.exoplatform.commons.notification.impl.service.storage.cache.CachedWebNotificationStorage;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionManager;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionMode;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class WebNotificationStorageImpl extends AbstractService implements WebNotificationStorage {
  private static final Log LOG = ExoLogger.getLogger(WebNotificationStorageImpl.class);
  private static final String NOTIFICATIONS = "notifications";
  private static final String NT_UNSTRUCTURED = "nt:unstructured";

  private final ReentrantLock lock = new ReentrantLock();
  private final NodeHierarchyCreator nodeHierarchyCreator;
  
  private final UserSettingService userSettingService;
  private final DataDistributionManager distributionManager;
  private WebNotificationStorage webNotificationStorage;
  
  public WebNotificationStorageImpl(NodeHierarchyCreator nodeHierarchyCreator, DataDistributionManager distributionManager,
                                    UserSettingService userSettingService) {
    this.nodeHierarchyCreator = nodeHierarchyCreator;
    this.distributionManager = distributionManager;
    this.userSettingService = userSettingService;
  }

  private Session getSession(SessionProvider sProvider) throws Exception {
    ManageableRepository repository = CommonsUtils.getRepository();
    return sProvider.getSession(repository.getConfiguration().getDefaultWorkspaceName(), repository);
  }
  
  private String converDateToNodeName(Calendar cal) {
    return new SimpleDateFormat(DATE_NODE_PATTERN).format(cal.getTime());
  }

  /**
   * Gets or create the Web Date Node on Collaboration workspace.
   */
  private Node getOrCreateDataNode(Node rootNode, String nodeName, String nodeType) throws RepositoryException {
    return distributionManager.getDataDistributionType(DataDistributionMode.NONE)
        .getOrCreateDataNode(rootNode, nodeName, nodeType);
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
  private Node getOrCreateWebDateNode(SessionProvider sProvider, NotificationInfo notification) throws Exception {
    String dateNodeName = converDateToNodeName(notification.getDateCreated());
    Node channelNode = getOrCreateChannelNode(sProvider, notification.getTo());
    try {
      return channelNode.getNode(dateNodeName);
    } catch (PathNotFoundException e) {
      Node dateNode = getOrCreateDataNode(channelNode, dateNodeName, NTF_NOTIF_DATE);
      dateNode.setProperty(NTF_LAST_MODIFIED_DATE, notification.getDateCreated().getTimeInMillis());
      channelNode.save();
      return dateNode;
    }
  }

  /**
   * Gets or create the Channel Node by NodeHierarchyCreator on Collaboration workspace.
   * 
   * For example: The channel node has the path as bellow:
   * User1: /Users/U___/Us___/Use___/User1/ApplicationData/notifications/web
   * 
   * @param sProvider
   * @param userId the remoteId
   * @return the channel node
   * @throws Exception
   */
  private Node getOrCreateChannelNode(SessionProvider sProvider, String userId) throws Exception {
    Node userNodeApp = nodeHierarchyCreator.getUserApplicationNode(sProvider, userId);
    Node parentNode = getOrCreateDataNode(userNodeApp, NOTIFICATIONS, NT_UNSTRUCTURED);
    Node channelNode = getOrCreateDataNode(parentNode, WEB_CHANNEL, NTF_CHANNEL);
    return channelNode;
  }

  private void addMixinCountItemOnPopover(Node notifyNode, String userId) throws Exception {
    if (!notifyNode.isNodeType(MIX_NEW_NODE)) {
      notifyNode.addMixin(MIX_NEW_NODE);
    }
  }

  @Override
  public void save(NotificationInfo notification) {
    save(notification, true);
  }

  /**
   * Creates the notification message to the specified user.
   * 
   * @param notification The notification to save
   * @param isCountOnPopover The status to update count on Popover or not
   */
  private void save(NotificationInfo notification, boolean isCountOnPopover) {
    boolean created = NotificationSessionManager.createSystemProvider();
    SessionProvider sProvider = NotificationSessionManager.getSessionProvider();
    try {
      lock.lock();
      //
      Node userNode = getOrCreateWebDateNode(sProvider, notification);
      Node notifyNode = getOrCreateDataNode(userNode, notification.getId(), NTF_NOTIF_INFO);
      //
      notifyNode.setProperty(NTF_PLUGIN_ID, notification.getKey().getId());
      notifyNode.setProperty(NTF_TEXT, notification.getTitle());
      notifyNode.setProperty(NTF_SENDER, notification.getFrom());
      notifyNode.setProperty(NTF_OWNER, notification.getTo());
      notifyNode.setProperty(NTF_LAST_MODIFIED_DATE, notification.getLastModifiedDate());
      //NTF_NAME_SPACE
      Map<String, String> ownerParameter = notification.getOwnerParameter();
      if(ownerParameter != null && !ownerParameter.isEmpty()) {
        for (String key : ownerParameter.keySet()) {
          String propertyName = (key.indexOf(NTF_NAME_SPACE) != 0) ? NTF_NAME_SPACE + key : key;
          notifyNode.setProperty(propertyName, ownerParameter.get(key));
        }
      }
      //
      if (isCountOnPopover) {
        addMixinCountItemOnPopover(notifyNode, notification.getTo());
      }
      //
      userNode.getSession().save();
    } catch (Exception e) {
      LOG.error("Failed to save the notificaton.", e);
    } finally {
      NotificationSessionManager.closeSessionProvider(created);
      lock.unlock();
    }
  }
  
  @Override
  public List<NotificationInfo> get(WebNotificationFilter filter, int offset, int limit) {
    List<NotificationInfo> result = new ArrayList<NotificationInfo>();
    SessionProvider sProvider = CommonsUtils.getSystemSessionProvider();
    try {
      Node userWebNotificationNode = getOrCreateChannelNode(sProvider, filter.getUserId());
      NotificationIterator notificationIterator = new NotificationIterator(userWebNotificationNode, offset, limit);
      //nodes order by lastUpdated DESC
      List<Node> nodes = notificationIterator.nodes();
      //
      for(Node node : nodes) {
        result.add(getWebNotificationStorage().get(node.getName()));
      }
    } catch (Exception e) {
      LOG.error("Notifications not found by filter: " + filter.toString(), e);
    }
    return result;
  }
  @Override
  public NotificationInfo get(String id) {
    try {
      return fillModel(getNodeNotification(CommonsUtils.getSystemSessionProvider(), id));
    } catch (Exception e) {
      LOG.error("Notification not found by id: " + id, e);
      return null;
    }
  }


  @Override
  public boolean remove(String notificationId) {
    boolean created = NotificationSessionManager.createSystemProvider();
    SessionProvider sProvider = NotificationSessionManager.getSessionProvider();
    try {
      Node node = getNodeNotification(sProvider, notificationId);
      if (node != null) {
        distributionManager.getDataDistributionType(DataDistributionMode.NONE)
                           .removeDataNode(node.getParent(), notificationId);
        return true;
      }
    } catch (Exception e) {
      LOG.error("Failed to remove the notification id: " + notificationId, e);
    } finally {
      NotificationSessionManager.closeSessionProvider(created);
    }
    return false;
  }

  @Override
  public boolean remove(String userId, long seconds) {
    boolean created = NotificationSessionManager.createSystemProvider();
    SessionProvider sProvider = NotificationSessionManager.getSessionProvider();
    try {
      Node userNode = getOrCreateChannelNode(sProvider, userId);
      Session session = userNode.getSession();
      long delayTime = System.currentTimeMillis() - (seconds * 1000);
      StringBuilder strQuery = new StringBuilder("SELECT * FROM ");
      strQuery.append(NTF_NOTIF_DATE).append(" WHERE (").append("jcr:path LIKE '").append(userNode.getPath())
              .append("/%' AND NOT jcr:path LIKE '").append(userNode.getPath()).append("/%/%'").append(") AND (")
              .append(NTF_LAST_MODIFIED_DATE).append(" < ").append(delayTime).append(")");
      QueryManager qm = session.getWorkspace().getQueryManager();
      Query query = qm.createQuery(strQuery.toString(), Query.SQL);
      NodeIterator it = query.execute().getNodes();
      while (it.hasNext()) {
        Node node = it.nextNode();
        node.remove();
        //
        session.save();
      }
    } catch (Exception e) {
      LOG.error("Failed to remove all notifications for the user id: " + userId, e);
      return false;
    } finally {
      NotificationSessionManager.closeSessionProvider(created);
    }
    return false;
  }

  @Override
  public void markRead(String notificationId) {
    Node node = getNodeNotification(CommonsUtils.getSystemSessionProvider(), notificationId);
    if (node != null) {
      Session session;
      try {
        session = node.getSession();
        node.setProperty(NTF_READ, "true");
        session.save();
      } catch (Exception e) {
        LOG.error("Failed to update the read notification Id: " + notificationId, e);
      }
    }
  }

  @Override
  public void hidePopover(String notificationId) {
    Node node = getNodeNotification(CommonsUtils.getSystemSessionProvider(), notificationId);
    if (node != null) {
      Session session;
      try {
        session = node.getSession();
        node.setProperty(NTF_SHOW_POPOVER, "false");
        session.save();
      } catch (Exception e) {
        LOG.error("Failed to hide the notification Id: " + notificationId + " on the popover list.", e);
      }
    }
  }

  @Override
  public void markAllRead(String userId) {
    try {
      //
      userSettingService.saveLastReadDate(userId, System.currentTimeMillis());
      //
      if (getWebNotificationStorage() instanceof CachedWebNotificationStorage) {
        CachedWebNotificationStorage cacheStorage = (CachedWebNotificationStorage) getWebNotificationStorage();
        cacheStorage.updateAllRead(userId);
      }
    } catch (Exception e) {
      LOG.error("Failed to update the all read for userId:" + userId, e);
    }
  }

  /**
   * Fill data from JCR node to model object
   * @param node
   * @return
   * @throws Exception
   */
  private NotificationInfo fillModel(Node node) throws Exception {
    if(node == null) return null;
    NotificationInfo notifiInfo = NotificationInfo.instance()
      .setTo(node.getProperty(NTF_OWNER).getString()) // owner of notification NTF_OWNER
      .setFrom(node.getProperty(NTF_SENDER).getString()) // user make event of notification
      .key(node.getProperty(NTF_PLUGIN_ID).getString())//pluginId
      .setTitle(node.getProperty(NTF_TEXT).getString())
      .setOnPopOver(node.getProperty(NTF_SHOW_POPOVER).getBoolean())
      //
      .setLastModifiedDate(node.getProperty(NTF_LAST_MODIFIED_DATE).getLong())
      .setId(node.getName())
      .end();
    if (node.hasProperty(EXO_DATE_CREATED)) {
      notifiInfo.setDateCreated(node.getProperty(EXO_DATE_CREATED).getDate());
    }
    List<String> ignoreProperties = Arrays.asList(NTF_PLUGIN_ID, NTF_TEXT, NTF_OWNER, NTF_LAST_MODIFIED_DATE);
    PropertyIterator iterator = node.getProperties();
    while (iterator.hasNext()) {
      Property p = iterator.nextProperty();
      if (p.getName().indexOf(NTF_NAME_SPACE) == 0) {
        if (ignoreProperties.contains(p.getName())) {
          continue;
        }
        try {
          notifiInfo.with(p.getName().replace(NTF_NAME_SPACE, ""), p.getString());
        } catch (Exception e) {
          LOG.error("Failed to get the property value.", e);
        }
      }
    }
    /**
     * Comparison the read time point to decide the read status of message.
     * + If less than the read time point, Read = TRUE
     * + Else depends on the the status of the message
     */
    long lastReadDate = getLastReadDateOfUser(notifiInfo.getTo());
    if (notifiInfo.getLastModifiedDate() <= lastReadDate) {
      notifiInfo.with(NotificationMessageUtils.READ_PORPERTY.getKey(), "true");
    }
    //
    return notifiInfo;
  }
  
  private Node getNodeNotification(SessionProvider sProvider, String notificationId) {
    try {
      Session session = getSession(sProvider);
      StringBuilder strQuery = new StringBuilder("SELECT * FROM ");
      strQuery.append(NTF_NOTIF_INFO).append(" WHERE fn:name() = '").append(notificationId).append("'");
      QueryManager qm = session.getWorkspace().getQueryManager();
      QueryImpl query = (QueryImpl) qm.createQuery(strQuery.toString(), Query.SQL);
      query.setOffset(0);
      query.setLimit(1);
      NodeIterator it = query.execute().getNodes();
      if (it.hasNext()) {
        return it.nextNode();
      }
    } catch (Exception e) {
      LOG.error("Failed to get web notification node: " + notificationId, e);
    }
    return null;
  }

  /**
   * Gets {@link WebNotificationStorage}
   * @return
   */
  private WebNotificationStorage getWebNotificationStorage() {
    if (webNotificationStorage == null) {
      webNotificationStorage = CommonsUtils.getService(WebNotificationStorage.class);
    }
    return webNotificationStorage;
  }

  @Override
  public NotificationInfo getUnreadNotification(String pluginId, String activityId, String owner) {
    boolean created = NotificationSessionManager.createSystemProvider();
    SessionProvider sProvider = NotificationSessionManager.getSessionProvider();
    try {
      String userNodePath = getOrCreateChannelNode(sProvider, owner).getPath();
      StringBuilder strQuery = new StringBuilder("SELECT * FROM ").append(NTF_NOTIF_INFO);
      strQuery.append(" WHERE jcr:path LIKE '").append(userNodePath).append("/%'")
              .append(" AND ntf:pluginId = '").append(pluginId).append("'")
              .append(" AND ntf:activityId = '").append(activityId).append("'")
              .append(" AND ntf:read = 'false'");
      Session session = getSession(sProvider);
      QueryManager qm = session.getWorkspace().getQueryManager();
      QueryImpl query = (QueryImpl) qm.createQuery(strQuery.toString(), Query.SQL);
      query.setOffset(0);
      query.setLimit(1);
      NodeIterator iter = query.execute().getNodes();
      if (iter.hasNext()) {
        return getWebNotificationStorage().get(iter.nextNode().getName());
      }
    } catch (Exception e) {
      LOG.debug("Failed to getUnreadNotification ", e);
    } finally {
      NotificationSessionManager.closeSessionProvider(created);
    }
    return null;
  }

  @Override
  public void update(NotificationInfo notification, boolean moveTop) {
    try {
      lock.lock();
      //
      remove(notification.getId());
      // if moveTop == true, the number on badge will increase
      // else the number on badge will not increase
      save(notification, moveTop);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public int getNumberOnBadge(String userId) {
    try {
      SessionProvider sProvider = CommonsUtils.getSystemSessionProvider();
      NodeIterator iter = getNewMessage(sProvider, userId, 0);
      return (int) iter.getSize();
    } catch (Exception e) {
      LOG.error("Failed to getNumberOnBadge() ", e);
    }
    return 0;
  }

  @Override
  public void resetNumberOnBadge(String userId) {
    try {
      SessionProvider sProvider = CommonsUtils.getSystemSessionProvider();
      NodeIterator iter = getNewMessage(sProvider, userId, 0);
      while (iter.hasNext()) {
        Node node = iter.nextNode();
        node.removeMixin(MIX_NEW_NODE);
      }
      getSession(sProvider).save();
    } catch (Exception e) {
      LOG.error("Failed to resetNumberOnBadge() ", e);
    }
  }
  
  private NodeIterator getNewMessage(SessionProvider sProvider, String userId, int limit) throws Exception {
    Session session = getSession(sProvider);
    String path = getOrCreateChannelNode(sProvider, userId).getPath();
    StringBuilder strQuery = new StringBuilder("SELECT * FROM ").append(MIX_NEW_NODE)
        .append(" WHERE jcr:path LIKE '").append(path).append("/%' ");
    QueryManager qm = session.getWorkspace().getQueryManager();
    QueryImpl query = (QueryImpl) qm.createQuery(strQuery.toString(), Query.SQL);
    if (limit > 0) {
      query.setOffset(0);
      query.setLimit(limit);
    }
    //
    return query.execute().getNodes();
  }

  /**
   * @param userId
   * @return
   */
  private long getLastReadDateOfUser(String userId) {
    return userSettingService.get(userId).getLastReadDate();
  }
}

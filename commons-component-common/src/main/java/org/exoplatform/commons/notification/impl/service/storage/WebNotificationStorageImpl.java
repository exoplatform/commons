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
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.impl.NotificationSessionManager;
import org.exoplatform.commons.notification.impl.service.storage.cache.CachedWebNotificationStorage;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
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
  
  private WebNotificationStorage webNotificationStorage;
  
  public WebNotificationStorageImpl(NodeHierarchyCreator nodeHierarchyCreator) {
    this.nodeHierarchyCreator = nodeHierarchyCreator;
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
    if (channelNode.hasNode(dateNodeName)) {
      return channelNode.getNode(dateNodeName);
    } else {
      Node dateNode = channelNode.addNode(dateNodeName, NTF_NOTIF_DATE);
      dateNode.setProperty(NTF_LAST_MODIFIED_DATE, notification.getDateCreated().getTimeInMillis());
      channelNode.getSession().save();
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
    Node parentNode = null;
    if (userNodeApp.hasNode(NOTIFICATIONS)) {
      parentNode = userNodeApp.getNode(NOTIFICATIONS);
    } else {
      parentNode = userNodeApp.addNode(NOTIFICATIONS, NT_UNSTRUCTURED);
    }
    Node channelNode = null;
    if (parentNode.hasNode(WEB_CHANNEL)) {
      channelNode = parentNode.getNode(WEB_CHANNEL);
    } else {
      channelNode = parentNode.addNode(WEB_CHANNEL, NTF_CHANNEL);
    }
    return channelNode;
  }

  private void addMixinCountItemOnPopover(Node notifyNode, String userId) throws Exception {
    if (!notifyNode.isNodeType(MIX_NEW_NODE)) {
      int currentNewMessage = getWebNotificationStorage().getNumberOnBadge(userId);
      if (currentNewMessage < NotificationMessageUtils.getMaxItemsInPopover() + 1 && notifyNode.canAddMixin(MIX_NEW_NODE)) {
        notifyNode.addMixin(MIX_NEW_NODE);
      }
    }
  }

  @Override
  public void save(NotificationInfo notification) {
    boolean created = NotificationSessionManager.createSystemProvider();
    SessionProvider sProvider = NotificationSessionManager.getSessionProvider();
    final ReentrantLock localLock = lock;
    try {
      localLock.lock();
      Node userNode = getOrCreateWebDateNode(sProvider, notification);
      Node notifyNode = null;
      if (userNode.hasNode(notification.getId())) {
        notifyNode = userNode.getNode(notification.getId());
      } else {
        notifyNode = userNode.addNode(notification.getId(), NTF_NOTIF_INFO);
      }
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
      addMixinCountItemOnPopover(notifyNode, notification.getTo());
      //
      getSession(sProvider).save();
    } catch (Exception e) {
      LOG.error("Failed to save the notificaton.", e);
    } finally {
      NotificationSessionManager.closeSessionProvider(created);
      localLock.unlock();
    }
  }
  
  @Override
  public List<NotificationInfo> get(WebNotificationFilter filter, int offset, int limit) {
    List<NotificationInfo> result = new ArrayList<NotificationInfo>();
    SessionProvider sProvider = CommonsUtils.getSystemSessionProvider();
    try {
      NodeIterator it = get(sProvider, filter, offset, limit);
      while (it.hasNext()) {
        result.add(getWebNotificationStorage().get(it.nextNode().getName()));
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

  /**
   * Gets notifications node by filter and offset, limit 
   * 
   * @param sProvider The SessionProvider
   * @param filter The WebNotificationFilter
   * @param offset The offset
   * @param limit  The limit, if limit <= 0, do not apply offset, limit
   * @return
   * @throws Exception
   */
  private NodeIterator get(SessionProvider sProvider, WebNotificationFilter filter, int offset, int limit) throws Exception {
    Session session = getSession(sProvider);
    StringBuilder strQuery = new StringBuilder("SELECT * FROM ");
    strQuery.append(NTF_NOTIF_INFO).append(" WHERE ");
    String path = getOrCreateChannelNode(sProvider, filter.getUserId()).getPath();
    strQuery.append("jcr:path LIKE '").append(path).append("/%' ");
    if (filter.isOnPopover()) {
      strQuery.append("AND ").append(NTF_SHOW_POPOVER).append("= 'true' ");
    }
    if (filter.getPluginKey() != null) {
      strQuery.append("AND ").append(NTF_PLUGIN_ID).append("='").append(filter.getPluginKey().getId()).append("' ");
    }
    if (filter.isRead() != null) {
      strQuery.append("AND ").append(NTF_READ).append("='").append(filter.isRead()).append("' ");
    }
    if (filter.getLimitDay() > 0) {
      long time = System.currentTimeMillis() - filter.getLimitDay() * 86400000;
      strQuery.append("AND ").append(NTF_LAST_MODIFIED_DATE).append(">='").append(time).append("' ");
    }
    if (filter.isOrder()) {
      strQuery.append("ORDER BY ").append(NTF_LAST_MODIFIED_DATE).append(" DESC");
    }
    //
    LOG.debug(" The query get web notification:\n" + strQuery);
    QueryManager qm = session.getWorkspace().getQueryManager();
    QueryImpl query = (QueryImpl) qm.createQuery(strQuery.toString(), Query.SQL);
    if (limit > 0) {
      query.setLimit(limit);
      query.setOffset(offset);
    }
    return query.execute().getNodes();
  }

  @Override
  public boolean remove(String notificationId) {
    boolean created = NotificationSessionManager.createSystemProvider();
    SessionProvider sProvider = NotificationSessionManager.getSessionProvider();
    try {
      Node node = getNodeNotification(sProvider, notificationId);
      if (node != null) {
        Session session = node.getSession();
        node.remove();
        session.save();
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
    SessionProvider sProvider = CommonsUtils.getSystemSessionProvider();
    WebNotificationFilter filter = new WebNotificationFilter(userId).setRead(false).setOrder(false);
    try {
      CachedWebNotificationStorage cacheStorage = null;
      if (getWebNotificationStorage() instanceof CachedWebNotificationStorage) {
        cacheStorage = (CachedWebNotificationStorage) getWebNotificationStorage();
      }
      NodeIterator it = get(sProvider, filter, 0, 0);
      while (it.hasNext()) {
        Node node = it.nextNode();
        node.setProperty(NTF_READ, "true");
        //
        if (cacheStorage != null) {
          cacheStorage.updateRead(node.getName(), true);
        }
      }
      getSession(sProvider).save();
    } catch (Exception e) {
      LOG.error("Failed to update the all read for userId:" + userId, e);
    }
  }

  
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
    //
    return notifiInfo;
  }
  
  private Node getNodeNotification(SessionProvider sProvider, String notificationId) {
    try {
      Session session = getSession(sProvider);
      StringBuilder strQuery = new StringBuilder("SELECT * FROM ");
      strQuery.append(NTF_NOTIF_INFO).append(" WHERE fn:name() = '").append(notificationId).append("'");
      QueryManager qm = session.getWorkspace().getQueryManager();
      Query query = qm.createQuery(strQuery.toString(), Query.SQL);
      NodeIterator it = query.execute().getNodes();
      if (it.getSize() > 0) {
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
    save(notification);
  }

  @Override
  public int getNumberOnBadge(String userId) {
    try {
      SessionProvider sProvider = CommonsUtils.getSystemSessionProvider();
      int limit = NotificationMessageUtils.getMaxItemsInPopover() + 1;
      NodeIterator iter = getNewMessage(sProvider, userId, limit);
      return (int) iter.getSize();
    } catch (Exception e) {
      LOG.debug("Failed to clearNewMessageNumber ", e);
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
      LOG.debug("Failed to clearNewMessageNumber ", e);
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
}

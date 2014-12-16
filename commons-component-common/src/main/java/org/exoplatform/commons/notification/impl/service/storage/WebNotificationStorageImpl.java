package org.exoplatform.commons.notification.impl.service.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebFilter;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.notification.NotificationConfiguration;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.impl.NotificationSessionManager;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class WebNotificationStorageImpl extends AbstractService implements WebNotificationStorage {
  private static final Log LOG = ExoLogger.getLogger(WebNotificationStorageImpl.class);

  private final ReentrantLock lock = new ReentrantLock();

  private final String workspace;
  public WebNotificationStorageImpl(NotificationConfiguration configuration) {
    this.workspace = configuration.getWorkspace();
  }

  @Override
  public void save(NotificationInfo notification) {
    SessionProvider sProvider = NotificationSessionManager.getOrCreateSessionProvider();
    final ReentrantLock localLock = lock;
    try {
      localLock.lock();
      String owner = notification.getTo();
      Node userNode = getOrCreateWebCurrentUserNode(sProvider, workspace, owner);
      Node notifyNode = null;
      if(userNode.hasNode(notification.getId())) {
        notifyNode = userNode.getNode(notification.getId());
      } else {
        notifyNode = userNode.addNode(notification.getId(), NTF_NOTIF_INFO);
      }
      notifyNode.setProperty(NTF_PLUGIN_ID, notification.getKey().getId());
      notifyNode.setProperty(NTF_TEXT, notification.getTitle());
      notifyNode.setProperty(NTF_SENDER, notification.getFrom());
      notifyNode.setProperty(NTF_OWNER, owner);
      notifyNode.setProperty(NTF_LAST_MODIFIED_DATE, notification.getLastModifiedDate());
      //NTF_NAME_SPACE
      Map<String, String> ownerParameter = notification.getOwnerParameter();
      if(ownerParameter != null && !ownerParameter.isEmpty()) {
        for (String key : ownerParameter.keySet()) {
          String propertyName = (key.indexOf(NTF_NAME_SPACE) != 0) ? NTF_NAME_SPACE + key : key;
          notifyNode.setProperty(propertyName, ownerParameter.get(key));
        }
      }
      notifyNode.getSession().save();
      //
      notification.with("UUID", notifyNode.getUUID());
    } catch (Exception e) {
      LOG.error("Failed to save the NotificationMessage", e);
    } finally {
      localLock.unlock();
    }
  }

  @Override
  public List<NotificationInfo> get(WebFilter filter) {
    List<NotificationInfo> notificationInfos = new ArrayList<NotificationInfo>();
    SessionProvider sProvider = NotificationSessionManager.getOrCreateSessionProvider();
    try {
      NodeIterator it = get(sProvider, filter);
      while (it.hasNext()) {
        Node node = it.nextNode();
        notificationInfos.add(fillModel(node));
      }
    } catch (Exception e) {
      LOG.warn("Can not get web notifications by filter: " + filter.toString(), e);
    }
    return notificationInfos;
  }
  
  private NodeIterator get(SessionProvider sProvider, WebFilter filter) throws Exception {
    Session session = getSession(sProvider, workspace);
    StringBuilder strQuery = new StringBuilder("SELECT * FROM ");
    strQuery.append(NTF_NOTIF_INFO).append(" WHERE ");
    if (filter.getJcrPath() == null || filter.getJcrPath().isEmpty()) {
      filter.setJcrPath(session.getItem("/" + NOTIFICATION_HOME_NODE + "/" + EMAIL_CHANNEL).getPath());
    }
    strQuery.append("jcr:path LIKE '").append(filter.getJcrPath()).append("/%' ");
    if (filter.getUserId() != null && !filter.getUserId().isEmpty()) {
      strQuery.append("AND ").append(NTF_OWNER).append("='").append(filter.getUserId()).append("' ");
    }
    if (filter.isOnPopover()) {
      strQuery.append("AND ").append(NTF_SHOW_POPOVER).append("='true' ");
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
    LOG.info(" The query get web notification:\n" + strQuery);
    QueryManager qm = session.getWorkspace().getQueryManager();
    QueryImpl query = (QueryImpl) qm.createQuery(strQuery.toString(), Query.SQL);
    if (filter.getLimit() > 0) {
      query.setLimit(filter.getLimit());
      query.setOffset(filter.getOffset());
    }
    return query.execute().getNodes();
  }

  @Override
  public boolean remove(String notificationId) {
    Node node = getNodeNotification(NotificationSessionManager.getOrCreateSessionProvider(), notificationId);
    if (node != null) {
      Session session;
      try {
        session = node.getSession();
        node.remove();
        session.save();
        return true;
      } catch (Exception e) {
        LOG.warn("Can not remove web notification node: " + notificationId, e);
      }
    }
    return false;
  }

  @Override
  public boolean remove(int days) {
    return false;
  }

  @Override
  public void markRead(String notificationId) {
    Node node = getNodeNotification(NotificationSessionManager.getOrCreateSessionProvider(), notificationId);
    if (node != null) {
      Session session;
      try {
        session = node.getSession();
        node.setProperty(NTF_READ, "true");
        session.save();
      } catch (Exception e) {
        LOG.warn("Can not save read on popover for web notification node: " + notificationId, e);
      }
    }
  }

  @Override
  public void hidePopover(String notificationId) {
    Node node = getNodeNotification(NotificationSessionManager.getOrCreateSessionProvider(), notificationId);
    if (node != null) {
      Session session;
      try {
        session = node.getSession();
        node.setProperty(NTF_SHOW_POPOVER, "false");
        session.save();
      } catch (Exception e) {
        LOG.warn("Can not save hidden on popover for web notification node: " + notificationId, e);
      }
    }
  }

  @Override
  public void markReadAll(String userId) {
    SessionProvider sProvider = NotificationSessionManager.getOrCreateSessionProvider();
    WebFilter filter = new WebFilter(userId, 0, 0).setRead(false).setOrder(false);
    try {
      NodeIterator it = get(sProvider, filter);
      while (it.hasNext()) {
        Node node = it.nextNode();
        node.setProperty(NTF_READ, "true");
      }
      getSession(sProvider, workspace).save();
    } catch (Exception e) {
      LOG.warn("Can not get web notifications by filter: " + filter.toString(), e);
    }
  }

  
  private NotificationInfo fillModel(Node node) throws Exception {
    if(node == null) return null;
    NotificationInfo notifiInfo = NotificationInfo.instance()
      .setTo(node.getParent().getName()) // owner of notification NTF_OWNER
      .setFrom(node.getProperty(NTF_SENDER).getString()) // user make event of notification
      .key(node.getProperty(NTF_PLUGIN_ID).getString())//pluginId
      .setTitle(node.getProperty(NTF_TEXT).getString())
      //
      .setLastModifiedDate(node.getProperty(NTF_LAST_MODIFIED_DATE).getLong())
      .with("UUID", node.getUUID())
      .setId(node.getName())
      .end();
    List<String> ignoreProperties = Arrays.asList(NTF_SENDER, NTF_PLUGIN_ID, NTF_TEXT, NTF_OWNER, NTF_LAST_MODIFIED_DATE);
    PropertyIterator iterator = node.getProperties();
    while (iterator.hasNext()) {
      Property p = iterator.nextProperty();
      if (p.getName().indexOf(NTF_NAME_SPACE) == 0) {
        if (ignoreProperties.contains(p.getName())) {
          continue;
        }
        try {
          notifiInfo.with(p.getName(), p.getString());
          notifiInfo.with(p.getName().replace(NTF_NAME_SPACE, ""), p.getString());
        } catch (Exception e) {}
      }
    }
    //
    return notifiInfo;
  }
  
  private Node getNodeNotification(SessionProvider sProvider, String notificationId) {
    try {
      Session session = getSession(sProvider, workspace);
      try {
        return session.getNodeByUUID(notificationId);
      } catch (ItemNotFoundException e) {
        StringBuilder strQuery = new StringBuilder("SELECT * FROM ");
        strQuery.append(NTF_NOTIF_INFO).append(" WHERE fn:name() = '").append(notificationId).append("'");
        QueryManager qm = session.getWorkspace().getQueryManager();
        Query query = qm.createQuery(strQuery.toString(), Query.SQL);
        NodeIterator it = query.execute().getNodes();
        if (it.getSize() > 0) {
          return it.nextNode();
        }
      }
    } catch (Exception e) {
      LOG.warn("Failed to get web notification node: " + notificationId, e);
    }
    return null;
  }
}

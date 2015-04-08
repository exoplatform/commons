/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.notification.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RangeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.ItemImpl;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public abstract class AbstractService {

  private static final Log LOG = ExoLogger.getLogger(AbstractService.class);

  public static final String STG_SCOPE                = "stg:scope";

  public static final String STG_CONTEXT              = "stg:context";

  public static final String STG_SUBCONTEXT           = "stg:subcontext";

  public static final String STG_SIMPLE_CONTEXT       = "stg:simplecontext";

  public static final String EXO_IS_ACTIVE            = "exo:isActive";

  public static final String EXO_INSTANTLY            = "exo:instantly";
  
  public static final String EXO_DAILY                = "exo:daily";

  public static final String EXO_LAST_READ_DATE       = "exo:lastReadDate";

  public static final String EXO_WEEKLY               = "exo:weekly";

  public static final String NTF_TO                   = "ntf:to";

  public static final String NTF_FROM                 = "ntf:from";

  public static final String NTF_TEXT                 = "ntf:text";

  public static final String NTF_READ                 = "ntf:read";

  public static final String NTF_ORDER                = "ntf:order";

  public static final String NTF_SENDER               = "ntf:sender";

  public static final String NTF_OWNER                = "ntf:owner";

  public static final String NTF_CHANNEL              = "ntf:channel";

  public static final String NTF_MESSAGE              = "ntf:message";

  public static final String NTF_PARENT_ID            = "ntf:parentId";

  public static final String NTF_PLUGIN_ID            = "ntf:pluginId";

  public static final String NTF_NOTIF_USER           = "ntf:notifUser";

  public static final String NTF_NOTIF_DATE           = "ntf:notifDate";

  public static final String NTF_NOTIF_INFO           = "ntf:notifInfo";

  public static final String NTF_SHOW_POPOVER         = "ntf:showPopover";

  public static final String NTF_MESSAGE_INFO         = "ntf:messageInfo";

  public static final String NTF_SEND_TO_DAILY        = "ntf:sendToDaily";

  public static final String NTF_MESSAGE_HOME         = "ntf:messageHome";

  public static final String NTF_SEND_TO_WEEKLY       = "ntf:sendToWeekly";

  public static final String NTF_NOTIFICATION         = "ntf:notification";

  public static final String NTF_PROVIDER_TYPE        = "ntf:providerType";

  public static final String NTF_SEND_TO_MONTHLY      = "ntf:sendToMonthly";

  public static final String NTF_LAST_MODIFIED_DATE   = "ntf:lastModifiedDate";

  public static final String MIX_DEFAULT_SETTING      = "mix:defaultSetting";

  public static final String MIX_SUB_MESSAGE_HOME     = "mix:subMessageHome";

  public static final String MIX_NEW_NODE             = "mix:newNode";

  public static final String NTF_OWNER_PARAMETER      = "ntf:ownerParameter";

  public static final String NTF_MESSAGE_INFO_HOME    = "ntf:messageInfoHome";

  public static final String EXO_DATE_CREATED         = "exo:dateCreated";

  public static final String EXO_LAST_MODIFIED_DATE   = "exo:lastModifiedDate";

  public static final String JCR_ROOT                 = "/jcr:root";

  public static final String ASCENDING                = " ASC";

  public static final String DESCENDING               = " DESC";

  public static final String WORKSPACE_PARAM          = "workspace";

  public static final String DEFAULT_WORKSPACE_NAME   = "portal-system";

  public static final String NOTIFICATION_HOME_NODE   = "eXoNotification";
  
  public static final String MESSAGE_HOME_NODE        = "messageHome";
  
  public static final String MESSAGE_INFO_HOME_NODE   = "messageInfoHome";

  public static final String WEB_CHANNEL              = "web";

  public static final String SETTING_NODE             = "settings";

  public static final String SETTING_USER_NODE        = "user";

  public static final String SETTING_USER_PATH        = "settings/user";

  public static final String NOTIFICATION_PARENT_PATH = "/";

  public static final String VALUE_PATTERN            = "{VALUE}";

  public static final String DATE_NODE_PATTERN        = "yyyyMMdd";

  public static final String NTF_NAME_SPACE           = "ntf:";

  /** Defines the prefix of the parent message node such as d20 */
  public static final String DAY                      = "d";

  protected static Node getNotificationHomeNode(SessionProvider sProvider, String workspace) throws Exception {
    Session session = getSession(sProvider, workspace);
    Node notificationHome, rootNode = session.getRootNode();
    if (rootNode.hasNode(NOTIFICATION_HOME_NODE)) {
      notificationHome = rootNode.getNode(NOTIFICATION_HOME_NODE);
    } else {
      notificationHome = rootNode.addNode(NOTIFICATION_HOME_NODE, NTF_NOTIFICATION);
      session.save();
    }
    return notificationHome;
  }

  /**
   * Get the home node of MessageInfo node
   * 
   * @param sProvider
   * @param workspace
   * @return
   * @throws Exception
   */
  protected Node getMessageInfoHomeNode(SessionProvider sProvider, String workspace) throws Exception {
    Node ntfHomeNode = getNotificationHomeNode(sProvider, workspace);
    if (ntfHomeNode.hasNode(MESSAGE_INFO_HOME_NODE) == false) {
      Node messageHome = ntfHomeNode.addNode(MESSAGE_INFO_HOME_NODE, NTF_MESSAGE_INFO_HOME);
      sessionSave(ntfHomeNode);
      return messageHome;
    }
    return ntfHomeNode.getNode(MESSAGE_INFO_HOME_NODE);
  }

  /**
   * Makes the node path for MessageHome node
   * "/eXoNotification/messageHome/<pluginId>/<DAY_OF_MONTH>/<HOUR_OF_DAY>/"
   * 
   * @param sProvider
   * @param pluginId
   * @return
   * @throws Exception
   */
  protected Node getOrCreateMessageParent(SessionProvider sProvider, String workspace, String pluginId) throws Exception {
    Node providerNode = getMessageNodeByPluginId(sProvider, workspace, pluginId);
    String dayName = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    Node dayNode = getOrCreateMessageNode(providerNode, DAY + dayName);
    return dayNode;
  }
  
 

  /**
   * Makes the node path for MessageHome node
   * 
   * @param sProvider
   * @param workspace
   * @param pluginId
   * @return
   * @throws Exception
   */
  protected Node getMessageNodeByPluginId(SessionProvider sProvider, String workspace, String pluginId) throws Exception {
    Node root = getNotificationHomeNode(sProvider, workspace);
    // rootPath = "/eXoNotification/messageHome/"
    Node messageHome = getOrCreateMessageNode(root, MESSAGE_HOME_NODE);
    // pluginPath = /eXoNotification/messageHome/<pluginId>/
    return getOrCreateMessageNode(messageHome, pluginId);
  }
  
  /**
   * 
   * @param parent
   * @param nodeName
   * @return
   * @throws Exception
   */
  protected Node getOrCreateMessageNode(Node parent, String nodeName) throws Exception {
    if (parent.hasNode(nodeName) == false) {
      Node messageHome = parent.addNode(nodeName, NTF_MESSAGE_HOME);
      messageHome.addMixin(MIX_SUB_MESSAGE_HOME);
      sessionSave(messageHome);
      return messageHome;
    }
    return parent.getNode(nodeName);
  }

  public static Session getSession(SessionProvider sProvider, String workspace) {
    if (workspace == null || workspace.length() == 0) {
      workspace = DEFAULT_WORKSPACE_NAME;
    }
    try {
      return sProvider.getSession(workspace, CommonsUtils.getRepository());
    } catch (RepositoryException e) {
      LOG.error("Failed to get session for workspace " + workspace, e);
      return null;
    }
  }

  public static String getCurrentTenantName() {
    try {
      return CommonsUtils.getRepository().getConfiguration().getName();
    } catch (Exception e) {
      return "defaultTenantName";
    }
  }

  protected static void sessionSave(Node node) throws Exception {
    node.getSession().save();
  }
  
  protected static String getValue(String value) {
    if (value == null || value.isEmpty()) {
      return "";
    }
    return VALUE_PATTERN.replace("VALUE", value);
  }

  protected static String getValues(String values) {
    if (values == null || values.isEmpty()) {
      return "";
    }
    return values.replace("{", "").replace("}", "");
  }
  
  /**
   * Gets the NodeIterator with list of nodes order by DESC lastUpdated value
   * 
   * @param parentNode
   * @return
   */
  public static NodeIterator getNodeIteratorOrderDESC(Node parentNode) throws RepositoryException {
    SessionImpl session = (SessionImpl) parentNode.getSession();
    NodeData data = (NodeData) ((NodeImpl) parentNode).getData();
    SessionDataManager dataManager = ((SessionImpl) session).getTransientNodesManager();
    List<NodeData> storedNodes = new ArrayList<NodeData>(dataManager.getChildNodesData(data));
    Collections.sort(storedNodes, new NodeDataComparatorDESC());
    return new LazyNodeIterator(session, data, storedNodes);
  }
  
  /**
   * Defines the comparator with order by DESC of NodeData
   * @author thanhvc
   *
   */
  private static class NodeDataComparatorDESC implements Comparator<NodeData> {
    public int compare(NodeData n1, NodeData n2) {
      return n2.getOrderNumber() - n1.getOrderNumber();
    }
  }
  
  /**
   * Defines the LazyNodeIterator
   * @author thanhvc
   *
   */
  private static class LazyNodeIterator extends LazyItemsIterator implements NodeIterator {
    
    LazyNodeIterator(SessionImpl session, NodeData parentData, List<NodeData> nodes) throws RepositoryException {
      super(session, parentData, nodes);
    }
    /**
     * {@inheritDoc}
     */
    public Node nextNode() {
      return (Node) nextItem();
    }
  }
  
  private static abstract class LazyItemsIterator implements RangeIterator {

    protected Iterator<NodeData>           iter;

    protected int                          size = -1;

    protected NodeImpl                     next;

    protected int                          pos  = 0;
    
    private final SessionImpl session;
    private final NodeData parentData;

    LazyItemsIterator(SessionImpl session, NodeData parentData, List<NodeData> items) throws RepositoryException {
      this.iter = items.iterator();
      this.session = session;
      this.parentData = parentData;
      fetchNext();
    }

    protected void fetchNext() throws RepositoryException {
      // We use a while loop instead of re-calling fetchNext if canRead(item)
      // returns false to avoid affecting the call stack because if we have
      // a lot of items and we have canRead(item) that returns false too
      // many consecutive times, we will get a StackOverflowError like in
      // JCR-2283
      while (iter.hasNext()) {
        NodeData item = iter.next();

        // check read conditions
        if (canRead(item)) {
          next = new NodeImpl(item, this.parentData, session);
          return;
        }
      }
      next = null;
    }

    protected boolean canRead(ItemData item) {
      return session.getAccessManager().hasPermission(item.isNode() ? ((NodeData) item).getACL() : this.parentData.getACL(),
                                                      new String[] { PermissionType.READ },
                                                      session.getUserState().getIdentity());
    }

    public ItemImpl nextItem() {
      if (next != null) {
        try {
          ItemImpl i = next;
          fetchNext();
          pos++;
          // fire action post-READ
          session.getActionHandler().postRead(i);
          return i;
        } catch (RepositoryException e) {
          LOG.error("An exception occured: " + e.getMessage());
          throw new NoSuchElementException(e.toString());
        }
      }

      throw new NoSuchElementException();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
      return next != null;
    }

    /**
     * {@inheritDoc}
     */
    public Object next() {
      return nextItem();
    }

    /**
     * {@inheritDoc}
     */
    public void skip(long skipNum) {
      trySkip(skipNum, true);
    }

    /**
     * Tries to skip some elements.
     * 
     * @param throwException indicates to throw an NoSuchElementException if
     *          can't skip defined count of elements
     * @return how many elememnts remained to skip
     */
    protected long trySkip(long skipNum, boolean throwException) {
      pos += skipNum;
      while (skipNum-- > 1) {
        try {
          iter.next();
        } catch (NoSuchElementException e) {
          if (throwException) {
            throw e;
          }
          return skipNum;
        }
      }

      try {
        fetchNext();
      } catch (RepositoryException e) {
        LOG.error("An exception occured: " + e.getMessage());
        throw new NoSuchElementException(e.toString());
      }

      return 0;
    }

    /**
     * {@inheritDoc}
     */
    public long getSize() {
      if (size == -1) {
        // calc size
        int sz = pos + (next != null ? 1 : 0);
        if (iter.hasNext()) {
          List<NodeData> itemsLeft = new ArrayList<NodeData>();
          do {
            NodeData item = iter.next();
            if (canRead(item)) {
              itemsLeft.add(item);
              sz++;
            }
          } while (iter.hasNext());

          iter = itemsLeft.iterator();
        }
        size = sz;
      }

      return size;
    }

    /**
     * {@inheritDoc}
     */
    public long getPosition() {
      return pos;
    }

    /**
     * {@inheritDoc}
     */
    public void remove() {
      LOG.warn("Remove not supported");
    }
  }
  
}

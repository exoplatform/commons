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

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

public abstract class AbstractService {

  public static final String STG_SCOPE                = "stg:scope";

  public static final String STG_CONTEXT              = "stg:context";

  public static final String STG_SUBCONTEXT           = "stg:subcontext";

  public static final String STG_SIMPLE_CONTEXT       = "stg:simplecontext";

  public static final String EXO_IS_ACTIVE            = "exo:isActive";

  public static final String EXO_INSTANTLY            = "exo:instantly";

  public static final String EXO_DAILY                = "exo:daily";

  public static final String EXO_WEEKLY               = "exo:weekly";

  public static final String NTF_FROM                 = "ntf:from";

  public static final String NTF_ORDER                = "ntf:order";

  public static final String NTF_MESSAGE              = "ntf:message";

  public static final String NTF_SEND_TO_DAILY        = "ntf:sendToDaily";

  public static final String NTF_SEND_TO_WEEKLY       = "ntf:sendToWeekly";

  public static final String NTF_MESSAGE_HOME         = "ntf:messageHome";

  public static final String NTF_NOTIFICATION         = "ntf:notification";

  public static final String NTF_PROVIDER_TYPE        = "ntf:providerType";

  public static final String MIX_SUB_MESSAGE_HOME     = "mix:subMessageHome";

  public static final String MIX_DEFAULT_SETTING      = "mix:defaultSetting";

  public static final String NTF_OWNER_PARAMETER      = "ntf:ownerParameter";

  public static final String EXO_LAST_MODIFIED_DATE   = "exo:lastModifiedDate";

  public static final String JCR_ROOT                 = "/jcr:root";

  public static final String ASCENDING                = " ascending";

  public static final String DESCENDING               = " descending";

  public static final String WORKSPACE_PARAM          = "workspace";

  public static final String DEFAULT_WORKSPACE_NAME   = "portal-system";

  public static final String NOTIFICATION_HOME_NODE   = "eXoNotification";
  
  public static final String PREFIX_MESSAGE_HOME_NODE = "messageHome";
  
  public static final String PROVIDER_HOME_NODE       = "providerHome";

  public static final String SETTING_NODE             = "settings";

  public static final String SETTING_USER_NODE        = "user";

  public static final String SETTING_USER_PATH        = "settings/user";

  public static final String NOTIFICATION_PARENT_PATH = "/";

  private static final String DAY                     = "d";

  private static final String HOUR                    = "h";
  
  protected static Node getNotificationHomeNode(SessionProvider sProvider, String workspace) throws Exception {
    Node homeNode = getSession(sProvider, workspace).getRootNode();
    if (NOTIFICATION_PARENT_PATH.equals(homeNode.getPath()) == false) {
      homeNode = homeNode.getNode(NOTIFICATION_PARENT_PATH);
    }
    Node notificationHome;
    try {
      notificationHome = homeNode.getNode(NOTIFICATION_HOME_NODE);
    } catch (Exception e) {
      notificationHome = homeNode.addNode(NOTIFICATION_HOME_NODE, NTF_NOTIFICATION);
      homeNode.getSession().save();
    }
    return notificationHome;
  }

  /**
   * Makes the node path for MessageHome node
   * "/eXoNotification/messageHome/<providerId>/<DAY_OF_MONTH>/<HOUR_OF_DAY>/"
   * 
   * @param sProvider
   * @param providerId
   * @return
   * @throws Exception
   */
  protected Node getOrCreateMessageParent(SessionProvider sProvider, String workspace, String providerId) throws Exception {
    Node root = getNotificationHomeNode(sProvider, workspace);
    // rootPath = "/eXoNotification/messageHome/"
    Node messageHome = getOrCreateNode(root, PREFIX_MESSAGE_HOME_NODE);
    // providerPath = /eXoNotification/messageHome/<providerId>/
    Node providerNode = getOrCreateNode(messageHome, providerId);
    String dayName = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    Node dayNode = getOrCreateNode(providerNode, DAY + dayName);
    String hourName = String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
    Node messageParentNode = getOrCreateNode(dayNode, HOUR + hourName);
    return messageParentNode;
  }

  private Node getOrCreateNode(Node parent, String nodeName) throws Exception {
    if (parent.hasNode(nodeName) == false) {
      Node messageHome = parent.addNode(nodeName, NTF_MESSAGE_HOME);
      messageHome.addMixin(MIX_SUB_MESSAGE_HOME);
      return messageHome;
    }
    return parent.getNode(nodeName);
  }

  public static Session getSession(SessionProvider sProvider, String workspace) {
    try {
      if (workspace == null || workspace.length() == 0) {
        workspace = DEFAULT_WORKSPACE_NAME;
      }
      return sProvider.getSession(workspace, CommonsUtils.getRepository());
    } catch (Exception e) {
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

  protected static SessionProvider getSystemProvider() {
    return CommonsUtils.getSystemSessionProvider();
  }

  protected static void sessionSave(Node node) throws Exception {
    node.getSession().save();
  }

}

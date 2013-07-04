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
package org.exoplatform.commons.notification;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

public abstract class AbstractService {

  public static final String STG_SCOPE                = "stg:scope";

  public static final String NTF_FROM                 = "ntf:from";

  public static final String NTF_TYPE                 = "ntf:type";

  public static final String NTF_NAME                 = "ntf:name";

  public static final String NTF_ORDER                = "ntf:order";

  public static final String NTF_PARAMS               = "ntf:params";

  public static final String NTF_MESSAGE              = "ntf:message";

  public static final String NTF_SUBJECTS             = "ntf:subjects";

  public static final String NTF_IS_ACTIVE            = "ntf:isActive";

  public static final String NTF_PROVIDER             = "ntf:provider";

  public static final String NTF_TEMPLATES            = "ntf:templates";

  public static final String NTF_DIGESTERS            = "ntf:digesters";

  public static final String NTF_SEND_TO_DAILY        = "ntf:sendToDaily";

  public static final String NTF_MESSAGE_HOME         = "ntf:messageHome";

  public static final String NTF_SEND_TO_WEEKLY       = "ntf:sendToWeekly";

  public static final String NTF_NOTIFICATION         = "ntf:notification";

  public static final String NTF_PROVIDER_HOME        = "ntf:providerHome";

  public static final String NTF_PROVIDER_TYPE        = "ntf:providerType";

  public static final String NTF_SEND_TO_MONTHLY      = "ntf:sendToMonthly";

  public static final String MIX_SUB_MESSAGE_HOME     = "mix:subMessageHome";

  public static final String MIX_DEFAULT_SETTING      = "mix:defaultSetting";

  public static final String NTF_OWNER_PARAMETER      = "ntf:ownerParameter";

  public static final String ASCENDING                = " ascending";

  public static final String JCR_ROOT                 = "/jcr:root";

  public static final String DESCENDING               = " descending";

  public static final String WORKSPACE_PARAM          = "workspace";

  public static final String DEFAULT_WORKSPACE_NAME   = "portal-system";

  public static final String NOTIFICATION_HOME_NODE   = "eXoNotification";
  
  public static final String PREFIX_MESSAGE_HOME_NODE = "messageHome";
  
  public static final String PROVIDER_HOME_NODE       = "providerHome";

  public static final String SETTING_USER_PATH        = "settings/user";

  public static final String NOTIFICATION_PARENT_PATH = "/";
  
  public static Node getNotificationHomeNode(SessionProvider sProvider, String workspace) throws Exception {
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
  
  public static SessionProvider createSystemProvider() {
    return CommonsUtils.getSystemSessionProvider();
  }
}

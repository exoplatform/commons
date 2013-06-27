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

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;


public class NotificationUtils implements NotificationProperties{
  
  public static final String WORKSPACE_PARAM          = "workspace";

  public static final String DEFAULT_WORKSPACE_NAME   = "portal-system";

  public static final String NOTIFICATION_HOME_NODE   = "eXoNotification";

  public static final String NOTIFICATION_PARENT_PATH = "/";

  public static final String PREFIX_MESSAGE_HOME_NODE = "messageHome";

  public static final String PROVIDER_HOME_NODE       = "providerHome";
  
  public static String listToString(List<String> list) {
    if (list == null || list.size() == 0) {
      return "";
    }
    StringBuffer values = new StringBuffer();
    for (String str : list) {
      if (values.length() > 0) {
        values.append(",");
      }
      values.append(str);
    }
    return values.toString();
  }
  
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
    RepositoryService repositoryService = CommonsUtils.getService(RepositoryService.class);
    try {
      ManageableRepository manageableRepository = repositoryService.getCurrentRepository();
      return sProvider.getSession(workspace, manageableRepository);//"portal-system"
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  
  public static SessionProvider createSystemProvider() {
    SessionProviderService sessionProviderService = CommonsUtils.getService(SessionProviderService.class);
    return sessionProviderService.getSystemSessionProvider(null);
  }
}

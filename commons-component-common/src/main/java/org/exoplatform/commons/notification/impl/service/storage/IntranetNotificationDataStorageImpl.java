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
package org.exoplatform.commons.notification.impl.service.storage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.jcr.Node;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.api.notification.service.storage.IntranetNotificationDataStorage;
import org.exoplatform.commons.notification.NotificationConfiguration;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.notification.impl.NotificationSessionManager;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class IntranetNotificationDataStorageImpl extends AbstractService implements IntranetNotificationDataStorage {
  private static final Log                    LOG         = ExoLogger.getLogger(IntranetNotificationDataStorageImpl.class);
  private String                              workspace;
  private int                                 MENU_LIMIT  = 13;
  private final ReentrantLock                 lock        = new ReentrantLock();
  // userId + list object
  private Map<String, List<NotificationInfo>>  tempStorage = new ConcurrentHashMap<String, List<NotificationInfo>>();

  public IntranetNotificationDataStorageImpl(NotificationConfiguration configuration) {
    this.workspace = configuration.getWorkspace();
  }

  @Override
  public void save(NotificationInfo notificationInfo) throws Exception {
    SessionProvider sProvider = NotificationSessionManager.getOrCreateSessionProvider();
    final ReentrantLock localLock = lock;
    try {
      localLock.lock();
      Node notificationHome = getNotificationHomeNode(sProvider, workspace);
      //
      List<NotificationInfo> current = tempStorage.get(notificationInfo.getTo());
      if (current == null) {
        current = new LinkedList<NotificationInfo>();
        tempStorage.put(notificationInfo.getTo(), current);
      }
      if (current.contains(notificationInfo)) {
        current.remove(notificationInfo);
      }
      ((LinkedList<NotificationInfo>) current).addFirst(notificationInfo);
    } catch (Exception e) {
      LOG.error("Failed to save the NotificationMessage", e);
    } finally {
      localLock.unlock();
    }
  }
  
  private NotificationInfo fillModel(Node node) throws Exception {
    if(node == null) return null;
    NotificationInfo message = NotificationInfo.instance()
      .setFrom(node.getProperty(NTF_FROM).getString()) // user make event of notification
      .setTo(node.getProperty(NTF_TO).getString()) // owner of notification
      .key(node.getProperty(NTF_PROVIDER_TYPE).getString())//pluginId
      .setOwnerParameter(node.getProperty(NTF_OWNER_PARAMETER).getValues())
      .setHasRead(node.getProperty(NTF_OWNER_PARAMETER).getBoolean())
      .setLastModifiedDate(node.getProperty(EXO_LAST_MODIFIED_DATE).getDate())
      .setId(node.getName())
      .end();
    //
    return message;
  }

  @Override
  public void saveRead(String userId, String id) throws Exception {
    List<NotificationInfo> current = tempStorage.get(userId);
    if (current != null) {
      for (NotificationInfo info : current) {
        if (info.getId().equals(id)) {
          info.setHasRead(true);
          break;
        }
      }
    }
  }

  @Override
  public void saveReadAll(String userId) throws Exception {
    List<NotificationInfo> current = tempStorage.get(userId);
    if (current != null) {
      for (NotificationInfo info : current) {
        info.setHasRead(true);
      }
    }
  }

  @Override
  public List<NotificationInfo> get(String userId, int limit) throws Exception {
    List<NotificationInfo> notificationInfos = new ArrayList<NotificationInfo>();
    List<NotificationInfo> current = tempStorage.get(userId);
    if(current != null) {
      if(current.size() > limit) {
        notificationInfos.addAll(current.subList(0, limit));
      } else {
        notificationInfos.addAll(current);
      }
    }
    return notificationInfos;
  }
  
  @Override
  public List<String> getNotificationContent(String userId, boolean isOnMenu) throws Exception {
    List<String> notifications = new ArrayList<String>();
    List<NotificationInfo> notificationInfos = get(userId, MENU_LIMIT);
    for (NotificationInfo notificationInfo : notificationInfos) {
      notifications.add(buildUIMessage(notificationInfo));
    }
    return notifications;
  }

  @Override
  public String buildUIMessage(NotificationInfo notificationInfo) throws Exception {
    NotificationContext nCtx = NotificationContextImpl.cloneInstance();
    AbstractNotificationPlugin plugin = nCtx.getPluginContainer().getPlugin(notificationInfo.getKey());
    if (plugin == null) {
      return "";
    }
    try {
      return plugin.buildUIMessage(nCtx.setNotificationInfo(notificationInfo));
    } catch (Exception e) {
      LOG.error("Failed to build UIMessage", e);
    }
    return "";
  }

  @Override
  public boolean remove(String userId, String id) throws Exception {
    List<NotificationInfo> current = tempStorage.get(userId);
    if (current != null) {
      List<NotificationInfo> tmp = new ArrayList<NotificationInfo>(current);
      for (NotificationInfo info : tmp) {
        if (info.getId().equals(id)) {
          current.remove(info);
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean remove(int days) throws Exception {
    Long time = System.currentTimeMillis() - days * 86400000L;// ms
    for (String userId : tempStorage.keySet()) {
      List<NotificationInfo> current = tempStorage.get(userId);
      if (current != null) {
        List<NotificationInfo> tmp = new ArrayList<NotificationInfo>(current);
        for (NotificationInfo info : tmp) {
          if(info.getLastModifiedDate().getTimeInMillis() < time) {
            current.remove(info);
          }
        }
      }
    }
    return false;
  }

}

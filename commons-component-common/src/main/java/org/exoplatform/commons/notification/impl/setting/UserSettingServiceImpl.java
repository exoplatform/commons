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
package org.exoplatform.commons.notification.impl.setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.NotificationCompletionService;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.notification.NotificationConfiguration;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.impl.NotificationSessionManager;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.UserImpl;
import org.picocontainer.Startable;

public class UserSettingServiceImpl extends AbstractService implements UserSettingService, Startable {
  private static final Log        LOG                = ExoLogger.getLogger(UserSettingServiceImpl.class);

  /** Setting Scope on Common Setting **/
  private static final Scope      NOTIFICATION_SCOPE = Scope.GLOBAL;
  
  private SettingService            settingService;

  private String                    workspace;

  private NotificationConfiguration configuration;

  private NotificationCompletionService completeService;
  
  protected static final int MAX_LIMIT = 30;
  
  public UserSettingServiceImpl(SettingService settingService, NotificationConfiguration configuration) {
    this.settingService = settingService;
    this.configuration = configuration;
    this.workspace = configuration.getWorkspace();
    this.completeService = CommonsUtils.getService(NotificationCompletionService.class);
  }

  private Node getUserSettingHome(Session session) throws Exception {
    Node settingNode = session.getRootNode().getNode(SETTING_NODE);
    Node userHomeNode = null;
    if (settingNode.hasNode(SETTING_USER_NODE) == false) {
      userHomeNode = settingNode.addNode(SETTING_USER_NODE, STG_SUBCONTEXT);
      session.save();
    } else {
      userHomeNode = settingNode.getNode(SETTING_USER_NODE);
    }
    return userHomeNode;
  }

  @Override
  public void start() {
    Callable<Boolean> callable = new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        try {
          SessionProvider sProvider = NotificationSessionManager.createSystemProvider();
          if (hasUserToUpgrade(sProvider)) {
            processUpgrade();
          }
        } catch (Exception e) {
          return false;
        } finally {
          NotificationSessionManager.closeSessionProvider();
        }
        return true;
      }

      private boolean hasUserToUpgrade(SessionProvider sProvider) {
        try {
          Session session = getSession(sProvider, workspace);
          return session.getRootNode().getNode(SETTING_USER_PATH).getNodes().hasNext() == false;
        } catch (Exception e) {
          LOG.error("Cannot get node of users.", e);
          return false;
        }
      }
    };

    this.completeService.addTask(callable);
  }

  @Override
  public void stop() {
  }
  
  private void processUpgrade() {
    OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
    try {
      ListAccess<User> list = organizationService.getUserHandler().findAllUsers();
      int offset = 0, size = list.getSize();
      //
      SessionProvider sProvider = NotificationSessionManager.getOrCreateSessionProvider();
      while (offset < size) {
        addMixin(sProvider, list.load(offset, MAX_LIMIT));
        offset += MAX_LIMIT;
      }
    } catch (Exception e) {
      LOG.error("Upgrade old users to use notification default setting failed", e);
    }
  }


  @Override
  public void save(UserSetting model) {

    String userId = model.getUserId();
    String instantlys = NotificationUtils.listToString(model.getInstantlyProviders());
    String dailys = NotificationUtils.listToString(model.getDailyProviders());
    String weeklys = NotificationUtils.listToString(model.getWeeklyProviders());

    saveUserSetting(userId, EXO_IS_ACTIVE, String.valueOf(model.isActive()));
    saveUserSetting(userId, EXO_INSTANTLY, instantlys);
    saveUserSetting(userId, EXO_DAILY, dailys);
    saveUserSetting(userId, EXO_WEEKLY, weeklys);

    removeMixin(userId);
  }

  /**
   * Using the common setting service to store the data
   * 
   * @param userId the userId
   * @param key the Setting key
   * @param value the Setting value
   */
  private void saveUserSetting(String userId, String key, String value) {
    settingService.set(Context.USER.id(userId), NOTIFICATION_SCOPE, key, SettingValue.create(value));
  }

  @Override
  public UserSetting get(String userId) {
    UserSetting model = UserSetting.getInstance();

    List<String> instantlys = getArrayListValue(userId, EXO_INSTANTLY, null);
    if (instantlys != null) {
      model.setUserId(userId);
      model.setActive(isActive(userId));
      model.setInstantlyProviders(instantlys);
      model.setDailyProviders(getArrayListValue(userId, EXO_DAILY, Collections.<String> emptyList()));
      model.setWeeklyProviders(getArrayListValue(userId, EXO_WEEKLY, Collections.<String> emptyList()));
    } else {
      model = UserSetting.getDefaultInstance().setUserId(userId);
      addMixin(userId);
    }
    return model;
  }

  @SuppressWarnings("unchecked")
  private SettingValue<String> getSettingValue(String userId, String propertyName) {
    return (SettingValue<String>) settingService.get(Context.USER.id(userId), NOTIFICATION_SCOPE, propertyName);
  }

  private List<String> getArrayListValue(String userId, String propertyName, List<String> defaultValue) {
    SettingValue<String> values = getSettingValue(userId, propertyName);
    if (values != null) {
      String strs = values.getValue();
      return Arrays.asList(strs.split(","));
    }
    return defaultValue;
  }

  private boolean isActive(String userId) {
    SettingValue<String> values = getSettingValue(userId, EXO_IS_ACTIVE);
    if (values != null) {
      return Boolean.valueOf(values.getValue());
    }
    return false;
  }
  
  @Override
  public void addMixin(String userId) {
    addMixin(new User[] { new UserImpl(userId) });
  }

  @Override
  public void addMixin(User[] users) {
    SessionProvider sProvider = getSystemProvider();
    try {
      addMixin(sProvider, users);
    } catch (Exception e) {
      LOG.error("Failed to add mixin for default setting of users", e);
    }
  }

  private void addMixin(SessionProvider sProvider, User[] users) {
    try {
      Session session = getSession(sProvider, workspace);
      Node userHomeNode = getUserSettingHome(session);
      Node userNode;
      for (int i = 0; i < users.length; ++i) {
        User user = users[i];
        if (user == null || user.getUserName() == null) {
          continue;
        }
        if (userHomeNode.hasNode(user.getUserName())) {
          userNode = userHomeNode.getNode(user.getUserName());
        } else {
          userNode = userHomeNode.addNode(user.getUserName(), STG_SIMPLE_CONTEXT);
        }
        if (userNode.canAddMixin(MIX_DEFAULT_SETTING)) {
          userNode.addMixin(MIX_DEFAULT_SETTING);
        }
      }
      session.save();
    } catch (Exception e) {
      LOG.error("Failed to upgrade user notification setting", e);
    }
  }

  /**
   * When has any changes on the user's default setting.
   * We must remove the mix type for user setting.
   * 
   * @param userId the userId for removing
   */
  private void removeMixin(String userId) {
    SessionProvider sProvider = getSystemProvider();
    try {
      Session session = getSession(sProvider, workspace);
      Node userHomeNode = session.getRootNode().getNode(SETTING_USER_PATH);
      if (userHomeNode.hasNode(userId)) {
        Node userNode = userHomeNode.getNode(userId);
        if (userNode.isNodeType(MIX_DEFAULT_SETTING)) {
          userNode.removeMixin(MIX_DEFAULT_SETTING);
          sessionSave(userNode);
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to remove mixin for default setting of user: " + userId, e);
    }
  }
  
  private StringBuilder buildQuery() {
    StringBuilder queryBuffer = new StringBuilder();
    queryBuffer.append(EXO_IS_ACTIVE).append("='true' AND (")
               .append(EXO_DAILY).append("<>''");
    if (configuration.isSendWeekly()) {
      queryBuffer.append(" OR ").append(EXO_WEEKLY).append("<>''");
    }

    queryBuffer.append(")");

    return queryBuffer;
  }
  
  private StringBuilder buildQuery(String pluginId) {
    if (pluginId == null) {
      return buildQuery();
    }
    StringBuilder queryBuffer = new StringBuilder();
    queryBuffer.append(EXO_IS_ACTIVE).append("='true' AND (")
                //if user wants to receive this kind of notification instantly
               .append(EXO_INSTANTLY).append("='").append(pluginId).append("'")
               .append(" OR ").append(EXO_INSTANTLY).append(" LIKE '%,").append(pluginId).append(",%'")
               .append(" OR ").append(EXO_INSTANTLY).append(" LIKE '%,").append(pluginId).append("'")
               .append(" OR ").append(EXO_INSTANTLY).append(" LIKE '").append(pluginId).append(",%'")
               .append(")");

    return queryBuffer;
  }
  
  @Override
  public List<String> getUserSettingByPlugin(String pluginId) {
    SessionProvider sProvider = getSystemProvider();
    List<String> userIds = new ArrayList<String>();
    try {
      NodeIterator iter = getDailyIterator(sProvider, 0, 0, pluginId);
      while (iter != null && iter.hasNext()) {
        Node node = iter.nextNode();
        userIds.add(node.getParent().getName());
      }
    } catch (Exception e) {
      LOG.error("Failed to get all users have the " + pluginId + " in settings", e);
    }

    return userIds;
  }

  /**
   * Gets these plugins what configured the daily
   * 
   * @param sProvider
   * @param offset
   * @param limit
   * @return
   * @throws Exception
   */
  private NodeIterator getDailyIterator(SessionProvider sProvider, int offset, int limit, String pluginId) throws Exception {
    Session session = getSession(sProvider, workspace);
    if(session.getRootNode().hasNode(SETTING_USER_PATH) == false) {
      return null;
    }
    
    StringBuilder strQuery = new StringBuilder("SELECT * FROM ").append(STG_SCOPE);
    strQuery.append(" WHERE ").append(buildQuery(pluginId));
    
    QueryManager qm = session.getWorkspace().getQueryManager();
    QueryImpl query = (QueryImpl) qm.createQuery(strQuery.toString(), Query.SQL);
    if (limit > 0) {
      query.setLimit(limit);
      query.setOffset(offset);
    }
    return query.execute().getNodes();
  }
  
  @Override
  public List<UserSetting> getDaily(int offset, int limit) {
    SessionProvider sProvider = NotificationSessionManager.createSystemProvider();
    List<UserSetting> models = new ArrayList<UserSetting>();
    try {
      NodeIterator iter = getDailyIterator(sProvider, offset, limit, null);
      while (iter != null && iter.hasNext()) {
        Node node = iter.nextNode();
        models.add(fillModel(node));
      }
    } catch (Exception e) {
      LOG.error("Failed to get all daily users have notification messages", e);
    }

    return models;
  }

  /**
   * Gets plugin's ID by propertyName
   * 
   * @param node
   * @param frequency
   * @return
   * @throws Exception
   */
  private List<String> getValues(Node node, String propertyName) throws Exception {
    String values = node.getProperty(propertyName).getString();
    if (values.trim().length() == 0) {
      return new ArrayList<String>();
    }
    return Arrays.asList(values.split(","));
  }

  /**
   * Fill the model data from UserSetting node
   * 
   * @param node the given node
   * @return the UserSetting
   * @throws Exception
   */
  private UserSetting fillModel(Node node) throws Exception {
    UserSetting model = UserSetting.getInstance();
    model.setDailyProviders(getValues(node, EXO_DAILY));
    model.setWeeklyProviders(getValues(node, EXO_WEEKLY));
    model.setUserId(node.getParent().getName());
    model.setLastUpdateTime(node.getParent().getProperty(EXO_LAST_MODIFIED_DATE).getDate());
    return model;
  }
  
  @Override
  public long getNumberOfDaily() {
    SessionProvider sProvider = NotificationSessionManager.createSystemProvider();
    try {
      NodeIterator iter = getDailyIterator(sProvider, 0, 0, null);
      return (iter == null) ? 0l : iter.getSize();
    } catch (Exception e) {
      return 0l;
    }
  }
  
  private NodeIterator getDefaultDailyIterator(SessionProvider sProvider, int offset, int limit) throws Exception {
    Session session = getSession(sProvider, workspace);
    StringBuilder strQuery = new StringBuilder("SELECT * FROM ").append(MIX_DEFAULT_SETTING);
    strQuery.append(" WHERE jcr:path LIKE '/").append(SETTING_USER_PATH)
            .append("/%' AND NOT jcr:path LIKE '/").append(SETTING_USER_PATH).append("/%/%'");

    QueryManager qm = session.getWorkspace().getQueryManager();
    QueryImpl query = (QueryImpl) qm.createQuery(strQuery.toString(), Query.SQL);
    if (limit > 0) {
      query.setLimit(limit);
      query.setOffset(offset);
    }
    return query.execute().getNodes();
  }

  @Override
  public List<UserSetting> getDefaultDaily(int offset, int limit) {
    SessionProvider sProvider = NotificationSessionManager.createSystemProvider();
    List<UserSetting> users = new ArrayList<UserSetting>();
    try {
      Session session = getSession(sProvider, workspace);
      if (session.getRootNode().hasNode(SETTING_USER_PATH)) {

        NodeIterator iter = getDefaultDailyIterator(sProvider, offset, limit);
        while (iter.hasNext()) {
          Node node = iter.nextNode();
          users.add(UserSetting.getInstance()
                    .setUserId(node.getName())
                    .setLastUpdateTime(node.getProperty(EXO_LAST_MODIFIED_DATE).getDate()));
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to get default daily users have notification messages", e);
    } 

    return users;
  }

  @Override
  public long getNumberOfDefaultDaily() {
    SessionProvider sProvider = NotificationSessionManager.createSystemProvider();
    try {
      Session session = getSession(sProvider, workspace);
      if (session.getRootNode().hasNode(SETTING_USER_PATH)) {
        NodeIterator iter = getDefaultDailyIterator(sProvider, 0, 0);
        return iter.getSize();
      }
    } catch (Exception e) {
      LOG.error("Failed to get default daily users have notification messages", e);
    }
    return 0;
  }

}

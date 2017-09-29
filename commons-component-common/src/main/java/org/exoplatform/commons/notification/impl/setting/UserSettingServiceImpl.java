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

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.notification.NotificationConfiguration;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.impl.NotificationSessionManager;
import org.exoplatform.commons.notification.job.NotificationJob;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.UserImpl;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class UserSettingServiceImpl extends AbstractService implements UserSettingService {
  private static final Log LOG = ExoLogger.getLogger(UserSettingServiceImpl.class);

  /** Setting Scope on Common Setting **/
  private static final Scope    NOTIFICATION_SCOPE = Scope.GLOBAL.id(null);
  private SettingService        settingService;
  private ChannelManager        channelManager;

  private final String          workspace;

  protected static final int MAX_LIMIT = 30;

  public static final String NAME_PATTERN = "exo:{CHANNELID}Channel";
  
  transient final ReentrantLock lock = new ReentrantLock();
  
  public UserSettingServiceImpl(SettingService settingService, NotificationConfiguration configuration,
                                ChannelManager channelManager) {
    this.settingService = settingService;
    this.workspace = configuration.getWorkspace();
    this.channelManager = channelManager;
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
  public void save(UserSetting model) {

    String userId = model.getUserId();
    String dailys = NotificationUtils.listToString(model.getDailyPlugins(), VALUE_PATTERN);
    String weeklys = NotificationUtils.listToString(model.getWeeklyPlugins(), VALUE_PATTERN);
    String channelActives = NotificationUtils.listToString(model.getChannelActives(), VALUE_PATTERN);

    // Save plugins active
    List<String> channels = new ArrayList<String>(model.getAllChannelPlugins().keySet());
    for (String channelId : channels) {
      saveUserSetting(userId, getChannelProperty(channelId), NotificationUtils.listToString(model.getPlugins(channelId), VALUE_PATTERN));
    }
    //
    saveUserSetting(userId, EXO_DAILY, dailys);
    saveUserSetting(userId, EXO_WEEKLY, weeklys);
    //
    saveUserSetting(userId, EXO_IS_ACTIVE, channelActives);
    saveUserSetting(userId, EXO_IS_ENABLED, "" + model.isEnabled());
    //
    if (model.getLastReadDate() > 0) {
      saveLastReadDate(userId, model.getLastReadDate());
    }
    removeMixin(userId);
  }

  private String getChannelProperty(String channelId) {
    return NAME_PATTERN.replace("{CHANNELID}", channelId);
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

    List<String> actives = getArrayListValue(userId, EXO_IS_ACTIVE, null);
    if (actives != null) {
      model.setUserId(userId);
      model.setChannelActives(actives);
      // for all channel to set plugin
      List<AbstractChannel> channels = channelManager.getChannels();
      for (AbstractChannel channel : channels) {
        model.setChannelPlugins(channel.getId(), getArrayListValue(userId, getChannelProperty(channel.getId()), new ArrayList<String>()));
      }
      //
      model.setDailyPlugins(getArrayListValue(userId, EXO_DAILY, new ArrayList<String>()));
      model.setWeeklyPlugins(getArrayListValue(userId, EXO_WEEKLY, new ArrayList<String>()));
      //
    } else {
      model = UserSetting.getDefaultInstance().setUserId(userId);
      initDefaultSettings(userId);
    }
    SettingValue<?> value = getSettingValue(userId, EXO_LAST_READ_DATE);
    if (value != null) {
      if (value.getValue() instanceof Long) {
        model.setLastReadDate((Long) value.getValue());
      } else {
        model.setLastReadDate((Long) Long.parseLong((String)value.getValue()));
      }
    } else {
      saveLastReadDate(userId, 0l);
    }
    //
    SettingValue<String> isEnabled = getSettingValue(userId, EXO_IS_ENABLED);
    if (isEnabled != null) {
      model.setEnabled(Boolean.valueOf(isEnabled.getValue()));
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
      if ("true".equals(strs)) {
        strs = UserSetting.EMAIL_CHANNEL;
      }
      if ("false".equals(strs)) {
        return defaultValue;
      }
      return NotificationUtils.stringToList(getValues(strs));
    }
    return defaultValue;
  }

  @Override
  public void initDefaultSettings(String userId) {
    initDefaultSettings(new User[] { new UserImpl(userId) });
  }

  @Override
  public void initDefaultSettings(User[] users) {
    boolean created = NotificationSessionManager.createSystemProvider();
    SessionProvider sProvider = NotificationSessionManager.getSessionProvider();
    try {
      addMixin(sProvider, users);
    } catch (Exception e) {
      LOG.error("Failed to add mixin for default setting of users", e);
    } finally {
      NotificationSessionManager.closeSessionProvider(created);
    }
  }

  private void addMixin(SessionProvider sProvider, User[] users) {
    final ReentrantLock lock = this.lock;
    try {
      Session session = getSession(sProvider, workspace);
      Node userHomeNode = getUserSettingHome(session);
      Node userNode;
      for (int i = 0; i < users.length; ++i) {
        try {
          User user = users[i];
          if (user == null || user.getUserName() == null) {
            continue;
          }
          if (userHomeNode.hasNode(user.getUserName())) {
            userNode = userHomeNode.getNode(user.getUserName());
            if (userNode.canAddMixin(MIX_DEFAULT_SETTING)) {
              lock.lock();
              userNode.addMixin(MIX_DEFAULT_SETTING);
            }
          } else {
            lock.lock();
            userNode = userHomeNode.addNode(user.getUserName(), STG_SIMPLE_CONTEXT);
            userNode.addMixin(MIX_DEFAULT_SETTING);
          }

          if ((i + 1) % 200 == 0) {
            session.save();
          }
        } finally {
          if(lock.isHeldByCurrentThread()) {
            lock.unlock();
          }
        }
      }
      session.save();
    } catch (Exception e) {
      LOG.error("Failed to addMixin for user notification setting", e);
    }
  }

  /**
   * When has any changes on the user's default setting.
   * We must remove the mix type for user setting.
   * 
   * @param userId the userId for removing
   */
  private void removeMixin(String userId) {
    SessionProvider sProvider = CommonsUtils.getSystemSessionProvider();
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
  
  private StringBuilder buildQuery(NotificationContext context) {
    StringBuilder queryBuffer = new StringBuilder();
    
    queryBuffer.append(buildSQLLikeProperty(EXO_IS_ACTIVE, UserSetting.EMAIL_CHANNEL));
    
    Boolean isWeekly = context.value(NotificationJob.JOB_WEEKLY);
    if (isWeekly) {
      queryBuffer.append(" AND ").append(EXO_WEEKLY).append("<>''");
    } else {
      queryBuffer.append(" AND ").append(EXO_DAILY).append("<>''");
    }
    queryBuffer.append(" AND (").append(EXO_IS_ENABLED).append(" IS NULL OR ").append(EXO_IS_ENABLED).append(" = 'true')");
    return queryBuffer;
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
  private NodeIterator getDigestIterator(NotificationContext context, SessionProvider sProvider, int offset, int limit) throws Exception {
    Session session = getSession(sProvider, workspace);
    if(session.getRootNode().hasNode(SETTING_USER_PATH) == false) {
      return null;
    }
    
    StringBuilder strQuery = new StringBuilder("SELECT * FROM ").append(STG_SCOPE);
    strQuery.append(" WHERE ").append(buildQuery(context));
    
    QueryManager qm = session.getWorkspace().getQueryManager();
    QueryImpl query = (QueryImpl) qm.createQuery(strQuery.toString(), Query.SQL);
    if (limit > 0) {
      query.setLimit(limit);
      query.setOffset(offset);
    }
    return query.execute().getNodes();
  }
  
  @Override
  public List<UserSetting> getDigestSettingForAllUser(NotificationContext context, int offset, int limit) {
    boolean created = NotificationSessionManager.createSystemProvider();
    SessionProvider sProvider = NotificationSessionManager.getSessionProvider();
    List<UserSetting> models = new ArrayList<UserSetting>();
    try {
      NodeIterator iter = getDigestIterator(context, sProvider, offset, limit);
      while (iter != null && iter.hasNext()) {
        Node node = iter.nextNode();
        models.add(fillModel(node));
      }
    } catch (Exception e) {
      LOG.error("Failed to get all daily users have notification messages", e);
    } finally {
      NotificationSessionManager.closeSessionProvider(created);
    }

    return models;
  }

  /**
   * Gets plugin's ID by propertyName
   * 
   * @param node
   * @param propertyName
   * @return
   * @throws Exception
   */
  private List<String> getValues(Node node, String propertyName) throws Exception {
    try {
      String values = node.getProperty(propertyName).getString();
      return NotificationUtils.stringToList(getValues(values));
    } catch (Exception e) {
      return new ArrayList<String>();
    }
  }

  /**
   * Fill the model data from UserSetting node
   * 
   * @param node the given node
   * @return the UserSetting
   * @throws Exception
   */
  private UserSetting fillModel(Node node) throws Exception {
    Node parentNode = node.getParent();
    if(!parentNode.hasProperty(EXO_LAST_MODIFIED_DATE)) {
      if(parentNode.isNodeType(EXO_MODIFY)) {
        parentNode.setProperty(EXO_LAST_MODIFIED_DATE, Calendar.getInstance());
        parentNode.save();
      }
      else if(parentNode.canAddMixin(EXO_MODIFY)) {
        parentNode.addMixin(EXO_MODIFY);
        parentNode.setProperty(EXO_LAST_MODIFIED_DATE, Calendar.getInstance());
        parentNode.save();
      }
      else {
        LOG.warn("Cannot add mixin to node '{}'.", parentNode.getPath());
      }
    }
    UserSetting model = UserSetting.getInstance();
    model.setUserId(parentNode.getName());
    model.setDailyPlugins(getValues(node, EXO_DAILY));
    model.setWeeklyPlugins(getValues(node, EXO_WEEKLY));
    //
    model.setChannelActives(getValues(node, EXO_IS_ACTIVE));
    //
    List<AbstractChannel> channels = channelManager.getChannels();
    for (AbstractChannel channel : channels) {
      model.setChannelPlugins(channel.getId(), getValues(node, getChannelProperty(channel.getId())));
    }
    //
    if(parentNode.hasProperty(EXO_LAST_MODIFIED_DATE) ){
      model.setLastUpdateTime(parentNode.getProperty(EXO_LAST_MODIFIED_DATE).getDate());
    } else {
      model.setLastUpdateTime(Calendar.getInstance());
    }
    //
    if (node.hasProperty(EXO_IS_ENABLED)) {
      model.setEnabled(Boolean.valueOf(node.getProperty(EXO_IS_ENABLED).getString()));
    }
    return model;
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
  public List<UserSetting> getDigestDefaultSettingForAllUser(int offset, int limit) {
    boolean created = NotificationSessionManager.createSystemProvider();
    SessionProvider sProvider = NotificationSessionManager.getSessionProvider();
    List<UserSetting> users = new ArrayList<UserSetting>();
    try {
      Session session = getSession(sProvider, workspace);
      if (session.getRootNode().hasNode(SETTING_USER_PATH)) {

        NodeIterator iter = getDefaultDailyIterator(sProvider, offset, limit);
        while (iter.hasNext()) {
          Node node = iter.nextNode();
          if(!node.hasProperty(EXO_LAST_MODIFIED_DATE)) {
            if(node.isNodeType(EXO_MODIFY)) {
              node.setProperty(EXO_LAST_MODIFIED_DATE, Calendar.getInstance());
              node.save();
            }
            else if(node.canAddMixin(EXO_MODIFY)) {
              node.addMixin(EXO_MODIFY);
              node.setProperty(EXO_LAST_MODIFIED_DATE, Calendar.getInstance());
              node.save();
            }
            else {
              LOG.warn("Cannot add mixin to node '{}'.", node.getPath());
            }
          }
          users.add(UserSetting.getInstance()
                    .setUserId(node.getName())
                    .setLastUpdateTime(node.getProperty(EXO_LAST_MODIFIED_DATE).getDate()));
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to get default daily users have notification messages", e);
    } finally {
      NotificationSessionManager.closeSessionProvider(created);
    }

    return users;
  }
  
  private String buildSQLLikeProperty(String property, String value) {
    StringBuilder strQuery = new StringBuilder(" (")
            .append(property).append(" LIKE '%").append(value).append("%'")
            .append(")");
    return strQuery.toString();
  }
 
  @Override
  public void saveLastReadDate(String userId, Long time) {
    settingService.set(Context.USER.id(userId), NOTIFICATION_SCOPE, EXO_LAST_READ_DATE, SettingValue.create(time));
  }

  @Override
  public void setUserEnabled(String username, boolean enabled) {
    saveUserSetting(username, EXO_IS_ENABLED, "" + enabled);
  }
}

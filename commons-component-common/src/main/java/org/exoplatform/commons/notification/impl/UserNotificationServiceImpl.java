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
import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.exoplatform.commons.api.notification.UserNotificationSetting;
import org.exoplatform.commons.api.notification.UserNotificationSetting.FREQUENCY;
import org.exoplatform.commons.api.notification.service.UserNotificationService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.notification.AbstractService;
import org.exoplatform.commons.notification.NotificationConfiguration;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class UserNotificationServiceImpl extends AbstractService implements UserNotificationService {
  private static final Log   LOG                = ExoLogger.getLogger(UserNotificationServiceImpl.class); 
  
  private static final Scope NOTIFICATION_SCOPE = Scope.GLOBAL;

  private SettingService settingService;
  
  private String workspace;

  public UserNotificationServiceImpl(SettingService settingService, NotificationConfiguration configuration) {
    this.settingService = settingService;
    this.workspace = configuration.getWorkspace();
  }

  @Override
  public void saveUserNotificationSetting(UserNotificationSetting notificationSetting) {
    
    String userId = notificationSetting.getUserId();
    String instantlys = NotificationUtils.listToString(notificationSetting.getInstantlyProviders());
    String dailys = NotificationUtils.listToString(notificationSetting.getDailyProviders());
    String weeklys = NotificationUtils.listToString(notificationSetting.getWeeklyProviders());
    String monthlys = NotificationUtils.listToString(notificationSetting.getMonthlyProviders());
    
    saveUserSetting(userId, EXO_IS_ACTIVE, String.valueOf(notificationSetting.isActive()));
    saveUserSetting(userId, FREQUENCY.INSTANTLY.getName(), instantlys);
    saveUserSetting(userId, FREQUENCY.DAILY_KEY.getName(), dailys);
    saveUserSetting(userId, FREQUENCY.WEEKLY_KEY.getName(), weeklys);
    saveUserSetting(userId, FREQUENCY.MONTHLY_KEY.getName(), monthlys);
    
    //
    removeMixinForDefautlSetting(userId);
  }
  
  private void saveUserSetting(String userId, String key, String value) {
    settingService.set(Context.USER.id(userId), NOTIFICATION_SCOPE, 
                       key, SettingValue.create(value));
  }

  @Override
  public UserNotificationSetting getUserNotificationSetting(String userId) {
    UserNotificationSetting notificationSetting = new UserNotificationSetting();
    notificationSetting.setUserId(userId);

    //
    List<String> instantlys = getSettingValue(userId, FREQUENCY.INSTANTLY);
    if(instantlys != null) {
      notificationSetting.setActive(isActiveValue(userId));

      notificationSetting.setInstantlyProviders(instantlys);
      notificationSetting.setDailyProviders(getSettingValue(userId, FREQUENCY.DAILY_KEY));
      notificationSetting.setWeeklyProviders(getSettingValue(userId, FREQUENCY.WEEKLY_KEY));
      notificationSetting.setMonthlyProviders(getSettingValue(userId, FREQUENCY.MONTHLY_KEY));
    } else {
      notificationSetting = UserNotificationSetting.getDefaultInstance();
      //
      addMixinForDefautlSetting(userId);
    }
    return notificationSetting;
  }

  @SuppressWarnings("unchecked")
  private List<String> getSettingValue(String userId, FREQUENCY  frequency) {
    SettingValue<String> values = (SettingValue<String>) settingService.get(Context.USER.id(userId), NOTIFICATION_SCOPE, frequency.getName());
    if (values != null) {
      String strs = values.getValue();
      return Arrays.asList(strs.split(","));
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private boolean isActiveValue(String userId) {
    SettingValue<String> values = (SettingValue<String>) settingService.get(Context.USER.id(userId), NOTIFICATION_SCOPE, EXO_IS_ACTIVE);
    if (values != null) {
      return Boolean.valueOf(values.getValue());
    }
    return false;
  }
  
  private void addMixinForDefautlSetting(String userId) {
    SessionProvider sProvider = createSystemProvider();
    try {
      Session session = getSession(sProvider, workspace);
      Node settingNode = session.getRootNode().getNode(SETTING_NODE);
      Node userHomeNode, userNode = null;
      if(settingNode.hasNode(SETTING_USER_NODE)) {
        userHomeNode = settingNode.getNode(SETTING_USER_NODE);
      } else {
        userHomeNode = settingNode.addNode(SETTING_USER_NODE, STG_SUBCONTEXT);
      }
      if (userHomeNode.hasNode(userId)) {
        userNode = userHomeNode.getNode(userId);
      } else {
        userNode = userHomeNode.addNode(userId, STG_SIMPLE_CONTEXT);
      }
      sessionSave(userHomeNode);
      
      //
      if (userNode.canAddMixin(MIX_DEFAULT_SETTING)) {
        userNode.addMixin(MIX_DEFAULT_SETTING);
        sessionSave(userNode);
      }
    } catch (Exception e) {
      LOG.error("Failed to add mixin for default setting of user: " + userId, e);
    }
  }

  private void removeMixinForDefautlSetting(String userId) {
    SessionProvider sProvider = createSystemProvider();
    try {
      Session session = getSession(sProvider, workspace);
      Node userHomeNode = session.getRootNode().getNode(SETTING_USER_PATH);
      if (userHomeNode.hasNode(userId)) {
        Node userNode = userHomeNode.getNode(userId);
        if (userNode.isNodeType(MIX_DEFAULT_SETTING)) {
          userNode.removeMixin(MIX_DEFAULT_SETTING);
          //
          sessionSave(userNode);
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to remove mixin for default setting of user: " + userId, e);
    }
  }
  
  private StringBuffer buildQuery() {
    StringBuffer queryBuffer = new StringBuffer();
    queryBuffer.append("@").append(EXO_IS_ACTIVE).append("='true' and (")
               .append("@").append(FREQUENCY.DAILY_KEY.getName()).append("!=").append("''");
    //
    if(NotificationUtils.isWeekEnd(6)) {
      queryBuffer.append("or @").append(FREQUENCY.WEEKLY_KEY.getName()).append("!=").append("''");
    }
    //
    if(NotificationUtils.isMonthEnd(28)) {
      queryBuffer.append("or @").append(FREQUENCY.MONTHLY_KEY.getName()).append("!=").append("''");
    }
    //
    queryBuffer.append(")");

    return queryBuffer;
  }
  
  private NodeIterator getDailyUserNotificationSettings(SessionProvider sProvider, int offset, int limit) throws Exception {
    Session session = getSession(sProvider, workspace);
    Node userHomeNode = session.getRootNode().getNode(SETTING_USER_PATH);

    StringBuffer queryBuffer = new StringBuffer(JCR_ROOT);
    queryBuffer.append(userHomeNode.getPath()).append("//element(*,").append(STG_SCOPE).append(")");
    queryBuffer.append("[").append(buildQuery()).append("]");
    QueryManager qm = session.getWorkspace().getQueryManager();
    QueryImpl query = (QueryImpl) qm.createQuery(queryBuffer.toString(), Query.XPATH);
    if(limit > 0) {
      query.setLimit(limit);
      query.setOffset(offset);
    }
    return query.execute().getNodes();
  }
  
  private List<String> getValues(Node node, FREQUENCY frequency) throws Exception {
    return Arrays.asList(node.getProperty(frequency.getName()).getString().split(","));
  }

  private UserNotificationSetting getUserNotificationSetting(Node node) throws Exception {
    UserNotificationSetting notificationSetting = UserNotificationSetting.getInstance();
    notificationSetting.setDailyProviders(getValues(node, FREQUENCY.DAILY_KEY));
    notificationSetting.setWeeklyProviders(getValues(node, FREQUENCY.WEEKLY_KEY));
    notificationSetting.setMonthlyProviders(getValues(node, FREQUENCY.MONTHLY_KEY));
    notificationSetting.setUserId(node.getParent().getName());
    notificationSetting.setLastUpdateTime(node.getProperty(EXO_LAST_MODIFIED_DATE).getDate());
    return notificationSetting;
  }

  @Override
  public List<UserNotificationSetting> getDailyUserNotificationSettings(int offset, int limit) {
    SessionProvider sProvider = createSystemProvider();
    List<UserNotificationSetting> notificationSettings = new ArrayList<UserNotificationSetting>();
    try {
      NodeIterator iter = getDailyUserNotificationSettings(sProvider, offset, limit);
      while (iter.hasNext()) {
        Node node = iter.nextNode();
        notificationSettings.add(getUserNotificationSetting(node));
      }
    } catch (Exception e) {
      LOG.error("Failed to get all daily users have notification messages", e);
    }
    
    
    return notificationSettings;
  }
  
  @Override
  public long getSizeDailyUserNotificationSettings() {
    SessionProvider sProvider = createSystemProvider();
    try {
      NodeIterator iter = getDailyUserNotificationSettings(sProvider, 0, 0);
      return iter.getSize();
    } catch (Exception e) {
      return 0l;
    }
  }

  @Override
  public List<UserNotificationSetting> getDefaultDailyUserNotificationSettings() {
    SessionProvider sProvider = createSystemProvider();
    List<UserNotificationSetting> users = new ArrayList<UserNotificationSetting>();
    try {
      Session session = getSession(sProvider, workspace);
      if(session.getRootNode().hasNode(SETTING_USER_PATH)) {
        Node userHomeNode = session.getRootNode().getNode(SETTING_USER_PATH);
        
        StringBuffer queryBuffer = new StringBuffer(JCR_ROOT);
        queryBuffer.append(userHomeNode.getPath()).append("//element(*,").append(MIX_DEFAULT_SETTING).append(")");
        QueryManager qm = session.getWorkspace().getQueryManager();
        Query query = qm.createQuery(queryBuffer.toString(), Query.XPATH);
        NodeIterator iter = query.execute().getNodes();
        while (iter.hasNext()) {
          Node node = iter.nextNode();
          users.add(UserNotificationSetting.getDefaultInstance().clone()
                    .setUserId(node.getName())
                    .setLastUpdateTime(node.getProperty(EXO_LAST_MODIFIED_DATE).getDate()));
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to get default daily users have notification messages", e);
    }

    return users;
  }

  

}

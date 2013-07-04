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
import java.util.Calendar;
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
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;

public class UserNotificationServiceImpl extends AbstractService implements UserNotificationService {

  private SettingService settingService;

  public UserNotificationServiceImpl(SettingService settingService) {
    this.settingService = settingService;
  }

  @Override
  public void saveUserNotificationSetting(String userId, UserNotificationSetting notificationSetting) {
    
    String instantlys = NotificationUtils.listToString(notificationSetting.getInstantlyProviders());
    String dailys = NotificationUtils.listToString(notificationSetting.getDailyProviders());
    String weeklys = NotificationUtils.listToString(notificationSetting.getWeeklyProviders());
    String monthlys = NotificationUtils.listToString(notificationSetting.getMonthlyProviders());
    
    saveUserSetting(userId, FREQUENCY.INSTANTLY, instantlys);
    saveUserSetting(userId, FREQUENCY.DAILY_KEY, dailys);
    saveUserSetting(userId, FREQUENCY.WEEKLY_KEY, weeklys);
    saveUserSetting(userId, FREQUENCY.MONTHLY_KEY, monthlys);
    
    //
    removeMixinForDefautlSetting(userId);
  }
  
  private void saveUserSetting(String userId, FREQUENCY frequency, String value) {
    settingService.set(Context.USER.id(userId), Scope.PORTAL, 
                       frequency.getName(), SettingValue.create(value));
  }

  @Override
  public UserNotificationSetting getUserNotificationSetting(String userId) {
    UserNotificationSetting notificationSetting = new UserNotificationSetting();

    //
    List<String> instantlys = getSettingValue(userId, FREQUENCY.INSTANTLY);
    if(instantlys != null) {
      notificationSetting.setInstantlyProviders(instantlys);
      notificationSetting.setDailyProviders(getSettingValue(userId, FREQUENCY.DAILY_KEY));
      notificationSetting.setWeeklyProviders(getSettingValue(userId, FREQUENCY.WEEKLY_KEY));
      notificationSetting.setMonthlyProviders(getSettingValue(userId, FREQUENCY.MONTHLY_KEY));
    } else {
      notificationSetting.setDefault(true);
      //
      addMixinForDefautlSetting(userId);
    }
    return notificationSetting;
  }
  
  private void addMixinForDefautlSetting(String userId) {
    SessionProvider sProvider = createSystemProvider();
    try {
      Session session = getSession(sProvider, null);
      Node userHomeNode = session.getRootNode().getNode(SETTING_USER_PATH);
      Node userNode;
      if (userHomeNode.hasNode(userId)) {
        userNode = userHomeNode.getNode(userId);
      } else {
        userNode = userHomeNode.addNode(userId);
      }
      if (userNode.canAddMixin(MIX_DEFAULT_SETTING)) {
        userNode.addMixin(MIX_DEFAULT_SETTING);
      }
      if (userHomeNode.isNew()) {
        session.save();
      } else {
        userNode.save();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void removeMixinForDefautlSetting(String userId) {
    SessionProvider sProvider = createSystemProvider();
    try {
      Session session = getSession(sProvider, null);
      Node userHomeNode = session.getRootNode().getNode(SETTING_USER_PATH);
      if (userHomeNode.hasNode(userId)) {
        Node userNode = userHomeNode.getNode(userId);
        if (userNode.isNodeType(MIX_DEFAULT_SETTING)) {
          userNode.removeMixin(MIX_DEFAULT_SETTING);
          if (userHomeNode.isNew()) {
            session.save();
          } else {
            userNode.save();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  @SuppressWarnings("unchecked")
  private List<String> getSettingValue(String userId, FREQUENCY  frequency) {
    SettingValue<String> values = (SettingValue<String>) settingService.get(Context.USER.id(userId), Scope.PORTAL, frequency.getName());
    if (values != null) {
      String strs = values.getValue();
      return Arrays.asList(strs.split(","));
    }
    return null;
  }
  
  private StringBuffer buildQuery() {
    StringBuffer queryBuffer = new StringBuffer();
    Calendar calendar = Calendar.getInstance();
    boolean isWeekEnd = (calendar.get(Calendar.DAY_OF_WEEK) == 6);
    boolean isMonthEnd = (calendar.get(Calendar.DAY_OF_WEEK) == 28);
    
    queryBuffer.append("@").append(FREQUENCY.DAILY_KEY.getName()).append("!=").append("''");
    if(isWeekEnd) {
      queryBuffer.append("or @").append(FREQUENCY.WEEKLY_KEY.getName()).append("!=").append("''");
    }
    
    if(isMonthEnd) {
      queryBuffer.append("or @").append(FREQUENCY.MONTHLY_KEY.getName()).append("!=").append("''");
    }
    
    return queryBuffer;
  }
  
  private NodeIterator getDailyUserNotificationSettings(SessionProvider sProvider, int offset, int limit) throws Exception {
    Session session = getSession(sProvider, null);
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
    UserNotificationSetting notificationSetting = new UserNotificationSetting();
    notificationSetting.setDailyProviders(getValues(node, FREQUENCY.DAILY_KEY));
    notificationSetting.setWeeklyProviders(getValues(node, FREQUENCY.WEEKLY_KEY));
    notificationSetting.setMonthlyProviders(getValues(node, FREQUENCY.MONTHLY_KEY));
    notificationSetting.setUserId(node.getParent().getName());
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
      e.printStackTrace();
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
  public List<String> getDefaultDailyUserNotificationSettings() {
    SessionProvider sProvider = createSystemProvider();
    List<String> users = new ArrayList<String>();
    try {
      Session session = getSession(sProvider, null);
      Node userHomeNode = session.getRootNode().getNode(SETTING_USER_PATH);

      StringBuffer queryBuffer = new StringBuffer(JCR_ROOT);
      queryBuffer.append(userHomeNode.getPath()).append("//element(*,").append(MIX_DEFAULT_SETTING).append(")");
      QueryManager qm = session.getWorkspace().getQueryManager();
      Query query = qm.createQuery(queryBuffer.toString(), Query.XPATH);
      NodeIterator iter = query.execute().getNodes();
      while (iter.hasNext()) {
        Node node = iter.nextNode();
        users.add(node.getName());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return users;
  }

  

}

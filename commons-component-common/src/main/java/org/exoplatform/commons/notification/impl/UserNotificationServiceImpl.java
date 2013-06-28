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

import org.exoplatform.commons.api.notification.UserNotificationSetting;
import org.exoplatform.commons.api.notification.UserNotificationSetting.FREQUENCY;
import org.exoplatform.commons.api.notification.service.UserNotificationService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.utils.ListAccess;

public class UserNotificationServiceImpl implements UserNotificationService {

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
    
    settingService.set(Context.USER.id(userId), Scope.PORTAL, 
                       FREQUENCY.INSTANTLY.getName(), SettingValue.create(instantlys));
    settingService.set(Context.USER.id(userId), Scope.PORTAL, 
                       FREQUENCY.DAILY_KEY.getName(), SettingValue.create(dailys));
    settingService.set(Context.USER.id(userId), Scope.PORTAL, 
                       FREQUENCY.WEEKLY_KEY.getName(), SettingValue.create(weeklys));
    settingService.set(Context.USER.id(userId), Scope.PORTAL, 
                       FREQUENCY.MONTHLY_KEY.getName(), SettingValue.create(monthlys));
  }

  @Override
  public UserNotificationSetting getUserNotificationSetting(String userId) {
    UserNotificationSetting notificationSetting = new UserNotificationSetting();

    //
    notificationSetting.setInstantlyProviders(getSettingValue(userId, FREQUENCY.INSTANTLY));
    notificationSetting.setDailyProviders(getSettingValue(userId, FREQUENCY.DAILY_KEY));
    notificationSetting.setWeeklyProviders(getSettingValue(userId, FREQUENCY.WEEKLY_KEY));
    notificationSetting.setMonthlyProviders(getSettingValue(userId, FREQUENCY.MONTHLY_KEY));
    return notificationSetting;
  }
  
  @SuppressWarnings("unchecked")
  private List<String> getSettingValue(String userId, FREQUENCY  frequency) {
    SettingValue<String> values = (SettingValue<String>) settingService.get(Context.USER.id(userId), Scope.PORTAL, frequency.getName());
    if (values != null) {
      String strs = values.getValue();
      return Arrays.asList(strs.split(","));
    }
    return new ArrayList<String>();
  }

  @Override
  public ListAccess<UserNotificationSetting> getDailyUserNotificationSettings() {
    // TODO Auto-generated method stub
    return null;
  }

  
  @Override
  public int getSizeOfDailyUserNotificationSettings() {
    // TODO Auto-generated method stub
    return 0;
  }
}

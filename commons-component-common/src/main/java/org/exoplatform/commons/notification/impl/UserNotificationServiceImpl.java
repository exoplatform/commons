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

import javax.jcr.Session;

import org.exoplatform.commons.api.notification.UserNotificationSetting;
import org.exoplatform.commons.api.notification.service.UserNotificationService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

public class UserNotificationServiceImpl implements UserNotificationService {

  public UserNotificationServiceImpl() {
  }

  @Override
  public void saveUserNotificationSetting(String userId, UserNotificationSetting notificationSetting) {
    String[] providers = notificationSetting.getActiveProviders();
    StringBuffer values = new StringBuffer();
    for (int i = 0; i < providers.length; i++) {
      if(i > 0) {
        values.append(",");
      }
      values.append(providers[i]);
    }
    
    SettingService settingService = CommonsUtils.getService(SettingService.class);
    settingService.set(Context.USER.id(userId), Scope.PORTAL, 
                       UserNotificationSetting.PROVIDER_KEY, SettingValue.create(values.toString()));
    settingService.set(Context.USER.id(userId), Scope.PORTAL, 
                       UserNotificationSetting.FREQUENCY_KEY, SettingValue.create(notificationSetting.getFrequency()));
  }

  @Override
  public UserNotificationSetting getUserNotificationSetting(String userId) {
    UserNotificationSetting notificationSetting = new UserNotificationSetting();
    SettingService settingService = CommonsUtils.getService(SettingService.class);

    //
    SettingValue<String> frequency = (SettingValue<String>) settingService.get(Context.USER.id(userId), Scope.PORTAL, UserNotificationSetting.FREQUENCY_KEY);

    if(frequency == null) {
      notificationSetting.setFrequency(UserNotificationSetting.FREQUENCY_DEFAULT_VALUE);
    } else {
      notificationSetting.setFrequency(frequency.getValue());
    }

    //
    SettingValue<String> values = (SettingValue<String>) settingService.get(Context.USER.id(userId), Scope.PORTAL, UserNotificationSetting.PROVIDER_KEY);

    if(values != null) {
      notificationSetting.setActiveProviders(values.getValue().split(","));
    }

    return notificationSetting;
  }

  @Override
  public LazyPageList<UserNotificationSetting> getDaiLyUserNotificationSettings() {
    // TODO Auto-generated method stub
    return null;
  }

  
  @Override
  public int getSizeOfDaiLyUserNotificationSettings() {
    
    
    // TODO Auto-generated method stub
    return 0;
  }
  
  
  private Session getSession(SessionProvider provider) {
    RepositoryService repositoryService = CommonsUtils.getService(RepositoryService.class);
    try {
      ManageableRepository manageableRepository = repositoryService.getCurrentRepository();
      provider.getSession("portal-system", manageableRepository);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

}

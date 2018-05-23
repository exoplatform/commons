/*
 *
 *  * Copyright (C) 2003-2018 eXo Platform SAS.
 *  *
 *  * This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License
 *  as published by the Free Software Foundation; either version 3
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see<http://www.gnu.org/licenses/>.
 *
 */

package org.exoplatform.settings.jpa;

import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.future.FutureExoCache;
import org.exoplatform.services.cache.future.Loader;
import org.exoplatform.services.organization.User;

public class CacheUserSettingServiceImpl implements UserSettingService {

  public static final String                                      CACHE_NAME = "commons.UserSettingService";

  private UserSettingService                                      userSettingService;

  private ExoCache<String, UserSetting>                           userSettingCache;

  private FutureExoCache<String, UserSetting, UserSettingService> userSettingFutureCache;

  public CacheUserSettingServiceImpl(CacheService cacheService, JPAUserSettingServiceImpl userSettingService) {
    this.userSettingService = userSettingService;

    userSettingCache = cacheService.getCacheInstance(CACHE_NAME);

    Loader<String, UserSetting, UserSettingService> loader = new Loader<String, UserSetting, UserSettingService>() {
      @Override
      public UserSetting retrieve(UserSettingService service, String userId) throws Exception {
        return service.get(userId);
      }
    };
    userSettingFutureCache = new FutureExoCache<String, UserSetting, UserSettingService>(loader, userSettingCache);
  }

  @Override
  public void save(UserSetting notificationSetting) {
    if (notificationSetting == null) {
      throw new IllegalArgumentException("notificationSetting argument is null");
    }
    userSettingCache.remove(notificationSetting.getUserId());
    userSettingService.save(notificationSetting);
  }

  @Override
  public UserSetting get(String userId) {
    return userSettingFutureCache.get(userSettingService, userId);
  }

  @Override
  public List<UserSetting> getDigestSettingForAllUser(NotificationContext context, int offset, int limit) {
    return userSettingService.getDigestSettingForAllUser(context, offset, limit);
  }

  @Override
  public List<UserSetting> getDigestDefaultSettingForAllUser(int offset, int limit) {
    return userSettingService.getDigestDefaultSettingForAllUser(offset, limit);
  }

  @Override
  public void initDefaultSettings(String userId) {
    userSettingService.initDefaultSettings(userId);
  }

  @Override
  public void initDefaultSettings(User[] users) {
    userSettingService.initDefaultSettings(users);
  }

  @Override
  public void saveLastReadDate(String userId, Long time) {
    userSettingCache.remove(userId);
    userSettingService.saveLastReadDate(userId, time);
  }

  @Override
  public void setUserEnabled(String username, boolean enabled) {
    userSettingCache.remove(username);
    userSettingService.setUserEnabled(username, enabled);
  }

}

/*
 * Copyright (C) 2003-2018 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.settings.impl;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.concurrent.ConcurrentFIFOExoCache;
import org.exoplatform.settings.jpa.CacheUserSettingServiceImpl;
import org.exoplatform.settings.jpa.JPAUserSettingServiceImpl;

public class CacheUserSettingTest {

  private static final String USER_ID = "demo";
  private CacheService cacheService;
  private JPAUserSettingServiceImpl userSettingServiceImpl;
  private CacheUserSettingServiceImpl cacheUserSettingServiceImpl;
  private UserSetting userSetting = new UserSetting();

  @Before
  public void setUp() throws Exception {
    cacheService = mock(CacheService.class);
    userSettingServiceImpl = mock(JPAUserSettingServiceImpl.class);

    userSetting.setUserId(USER_ID);

    when(cacheService.getCacheInstance(CacheUserSettingServiceImpl.CACHE_NAME)).thenReturn(new ConcurrentFIFOExoCache<>());
    when(userSettingServiceImpl.get(USER_ID)).thenReturn(userSetting);

    cacheUserSettingServiceImpl = new CacheUserSettingServiceImpl(cacheService, userSettingServiceImpl);
  }

  @Test
  public void testGetUserSetting() {
    cacheUserSettingServiceImpl.get(USER_ID);
    verify(userSettingServiceImpl, times(1)).get(USER_ID);

    cacheUserSettingServiceImpl.get(USER_ID);
    verify(userSettingServiceImpl, times(1)).get(USER_ID);
  }

  @Test
  public void testGetAndRemoveUserSetting() {
    cacheUserSettingServiceImpl.get(USER_ID);
    verify(userSettingServiceImpl, times(1)).get(USER_ID);

    cacheUserSettingServiceImpl.setUserEnabled(USER_ID, true);

    cacheUserSettingServiceImpl.get(USER_ID);
    verify(userSettingServiceImpl, times(2)).get(USER_ID);
  }

  @Test
  public void testGetAndSaveUserSetting() {
    cacheUserSettingServiceImpl.get(USER_ID);
    verify(userSettingServiceImpl, times(1)).get(USER_ID);

    cacheUserSettingServiceImpl.save(userSetting);

    cacheUserSettingServiceImpl.get(USER_ID);
    verify(userSettingServiceImpl, times(2)).get(USER_ID);
  }

}

/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.services.user;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityConstants;

public class UserStateService {
  private static final Log         LOG                     = ExoLogger.getLogger(UserStateService.class.getName());

  public static final String       DEFAULT_STATUS          = "available";

  public static final String       USER_STATE_CACHE_NAME = "commons.UserStateService";

  private static final int         DEFAULT_OFFLINE_DELAY   = 60000;

  private final int                delay;

  ExoCache<String, UserStateModel> userStateCache          = null;

  public UserStateService(CacheService cacheService) {
    userStateCache = cacheService.getCacheInstance(USER_STATE_CACHE_NAME);
    String strDelay = System.getProperty("user.status.offline.delay");
    int configuredDelay = NumberUtils.toInt(strDelay, DEFAULT_OFFLINE_DELAY);
    delay = (configuredDelay > 0) ? configuredDelay : DEFAULT_OFFLINE_DELAY;
  }

  public int getDelay() {
    return delay;
  }

  // Add or update a userState
  public void save(UserStateModel model) {
    userStateCache.put(model.getUserId(), model);
  }

  // Get userState for a user
  public UserStateModel getUserState(String userId) {
    if (StringUtils.isBlank(userId)) {
      throw new IllegalArgumentException("Parameter userId is mandatory");
    }
    UserStateModel model = getUserStateFromCache(userId);
    if (model != null) {
      model = model.clone();
    } else {
      ConversationState state = ConversationState.getCurrent();
      if (state == null || state.getIdentity() == null || state.getIdentity().getUserId() == null
          || !userId.equals(state.getIdentity().getUserId())) {
        return null;
      }
      // The current query is requested by a user that is online
      // but his state is not stored in cache, so cache it
      model = ping(userId);
    }
    return model;
  }

  // Ping to update last activity
  public UserStateModel ping(String userId) {
    if (userId == null || IdentityConstants.ANONIM.equals(userId)) {
      return null;
    }
    UserStateModel model = getUserStateFromCache(userId);
    long lastActivity = Calendar.getInstance().getTimeInMillis();
    if (model == null) {
      model = new UserStateModel(userId, lastActivity, DEFAULT_STATUS);
    } else {
      model.setLastActivity(lastActivity);
    }
    save(model);
    return model;
  }

  // Get all users online
  public List<UserStateModel> online() {
    List<UserStateModel> onlineUsers = new LinkedList<>();
    try {
      @SuppressWarnings("unchecked")
      List<UserStateModel> users = (List<UserStateModel>) userStateCache.getCachedObjects();
      //
      Collections.sort(users, new LastActivityComparatorASC());
      for (UserStateModel userStateModel : users) {
        if (isOnline(userStateModel)) {
          onlineUsers.add(userStateModel);
        }
      }
    } catch (Exception e) {
      LOG.error("Exception when getting online user: {}", e);
    }
    return onlineUsers;
  }

  public boolean isOnline(String userId) {
    UserStateModel model = getUserState(userId);
    if (model != null) {
      return isOnline(model);
    }
    return false;
  }

  public UserStateModel lastLogin() {
    List<UserStateModel> online = online();
    if (!online.isEmpty()) {
      return online.get(online.size() - 1);
    }
    return null;
  }

  private UserStateModel getUserStateFromCache(String userId) {
    return userStateCache.get(userId);
  }

  private boolean isOnline(UserStateModel model) {
    if (model != null) {
      long iDate = Calendar.getInstance().getTimeInMillis();
      if (model.getLastActivity() >= (iDate - delay)) {
        return true;
      }
    }
    return false;
  }

  public static class LastActivityComparatorASC implements Comparator<UserStateModel> {
    public int compare(UserStateModel u1, UserStateModel u2) {
      Long date1 = u1.getLastActivity();
      Long date2 = u2.getLastActivity();
      return date1.compareTo(date2);
    }
  }
}

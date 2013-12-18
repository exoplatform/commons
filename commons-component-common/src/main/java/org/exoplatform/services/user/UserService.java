/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;


public class UserService {
  
  private static ExoCache<Serializable, Date> usersCache;
  private static List<String> users = new ArrayList<String>();
  
  private static UserService instance = null;
  
  /* Need the following object to synchronize a block */
  private static Object syncObject_;
  
  /* Prevent direct access to the constructor*/
  private UserService() {
    super();
  }
  
  public UserService(CacheService cacheService) {
    usersCache = cacheService.getCacheInstance(UserService.class.getName()) ;
  }
  
  public static UserService getInstance(CacheService cacheService)
  {
    if (instance == null) {
      synchronized(syncObject_) {
        if (instance == null) {
          instance = new UserService(cacheService);
        }
      }      
    }
    return instance;
  }
  
  public void updateUserTime(String userKey) {
    Date newDate = new Date(System.currentTimeMillis());
    usersCache.put(userKey, newDate);
    if(!users.contains(userKey)) users.add(userKey);
  }
  
  public boolean getUserStatus(String userKey) {
    Date userDate = usersCache.get(userKey);
    if(userDate == null) return false;
    long diffInSeconds = Math.abs(System.currentTimeMillis() - userDate.getTime()) / 1000;
    if(diffInSeconds > 10) return false;
    else return true;
  }
  
  public List<String> getUsersOnline() {
    List<String> onlineUsers = new ArrayList<String>();
    for(int i=0; i< users.size(); i++) {
      String user = users.get(i);
      Date userDate = usersCache.get(user);
      long diffInSeconds = Math.abs(System.currentTimeMillis() - userDate.getTime()) / 1000;
      if(diffInSeconds <= 10) onlineUsers.add(user);
    }
    return onlineUsers;
  }
}

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityConstants;


public class UserStateService {
  private static final Log LOG = ExoLogger.getLogger(UserStateService.class.getName());
  public static String VIDEOCALLS_BASE_PATH = "VideoCalls";
  public static String USER_STATATUS_NODETYPE = "exo:userState";
  public static String USER_ID_PROP = "exo:userId";
  public static String LAST_ACTIVITY_PROP = "exo:lastActivity";
  public static String STATUS_PROP = "exo:status";
  public static String DEFAULT_STATUS = "available";
  public int delay = 60*1000;
  public static final int _delay_update_DB = 3*60*1000; //3 mins
  public static int pingCounter = 0;
  
  private final CacheService cacheService;
   
  public UserStateService(CacheService cacheService) {
    this.cacheService = cacheService;
    if (System.getProperty("user.status.offline.delay") != null) {
      delay = Integer.parseInt(System.getProperty("user.status.offline.delay"));
    }
  }

  // Add or update a userState
  public void save(UserStateModel model) {
    String userId = model.getUserId();

    SessionProvider sessionProvider = new SessionProvider(ConversationState.getCurrent());
    NodeHierarchyCreator nodeHierarchyCreator = CommonsUtils.getService(NodeHierarchyCreator.class);

    try {
      ManageableRepository repository = CommonsUtils.getRepository();
      Session session = sessionProvider.getSession(repository.getConfiguration().getDefaultWorkspaceName(), repository);
      String repoName = repository.getConfiguration().getName();

      Node userNodeApp = nodeHierarchyCreator.getUserApplicationNode(sessionProvider, userId);
      String userKey = repoName + "_" + userId;
      Node userState;
      if (userNodeApp.hasNode(VIDEOCALLS_BASE_PATH)) {
        userState = userNodeApp.getNode(VIDEOCALLS_BASE_PATH);
      } else {
        userState = userNodeApp.addNode(VIDEOCALLS_BASE_PATH, USER_STATATUS_NODETYPE);
      }
      userState.setProperty(USER_ID_PROP, userId);
      userState.setProperty(LAST_ACTIVITY_PROP, model.getLastActivity());
      userState.setProperty(STATUS_PROP, model.getStatus());
      session.save();
      ExoCache<Serializable, UserStateModel> cache = getUserStateCache();
      if (cache == null) {
        LOG.warn("Cant save user state of {} to cache", userId);
      } else {
        cache.put(userKey, model);
      }
    } catch (Exception ex) {
      if (LOG.isErrorEnabled()) {
        LOG.error("save() failed because of ", ex);
      }
    } finally {
      sessionProvider.close();
    }
  }
  
  //Get userState for a user
  public UserStateModel getUserState(String userId) {
    UserStateModel model = null;
    String repoName = CommonsUtils.getRepository().getConfiguration().getName();
    String userKey = repoName + "_" + userId;
    ExoCache<Serializable, UserStateModel> userStateCache = getUserStateCache();
    if(userStateCache != null && userStateCache.get(userKey) != null) {
      model = userStateCache.get(userKey).clone();
    } else {
      SessionProvider sessionProvider = new SessionProvider(ConversationState.getCurrent());
      NodeHierarchyCreator nodeHierarchyCreator = CommonsUtils.getService(NodeHierarchyCreator.class);
      Node userNodeApp = null;
      try {
        userNodeApp = nodeHierarchyCreator.getUserApplicationNode(sessionProvider, userId);
      } catch (Exception e) {
        //Do nothing
      } 
      try{               
        if(userNodeApp == null || !userNodeApp.hasNode(VIDEOCALLS_BASE_PATH)) return null;        
        Node userState = userNodeApp.getNode(VIDEOCALLS_BASE_PATH);
        model = new UserStateModel();
        model.setUserId(userState.getProperty(USER_ID_PROP).getString());
        model.setLastActivity(Integer.parseInt(userState.getProperty(LAST_ACTIVITY_PROP).getString()));
        model.setStatus(userState.getProperty(STATUS_PROP).getString());        
        userStateCache.put(userKey, model);
      } catch(Exception ex) {
        if (LOG.isErrorEnabled()) {
          LOG.error("getUserState() failed because of ", ex.getMessage());
        }
      } finally {
        sessionProvider.close();
      }
    }
    return model;
  }
  
  //Ping to update last activity
  public void ping(String userId) {
    if (userId == null || IdentityConstants.ANONIM.equals(userId)) {
      return;
    }
    UserStateModel model = getUserState(userId);

    long lastActivity = Calendar.getInstance().getTimeInMillis();

    boolean isSave = (model == null || (lastActivity - model.getLastActivity()) > _delay_update_DB);

    if (model == null) {
      model = new UserStateModel(userId, lastActivity, DEFAULT_STATUS);
    } else {
      model.setLastActivity(lastActivity);
      String repoName = CommonsUtils.getRepository().getConfiguration().getName();
      String userKey = repoName + "_" + userId;
      getUserStateCache().put(userKey, model);
    }
    if (isSave) {
      save(model);
    }
  }
  
  //Get all users online
  public List<UserStateModel> online() {
    List<UserStateModel> onlineUsers = new ArrayList<UserStateModel>();
    List<UserStateModel> users = null;
    try {
      ExoCache<Serializable, UserStateModel> userStateCache = getUserStateCache();
      if (userStateCache == null){
        LOG.warn("Cant get online users list from cache. Will return an empty list.");
        return new ArrayList<UserStateModel>();
      }
      users = (List<UserStateModel>) userStateCache.getCachedObjects();     
      for (UserStateModel userStateModel : users) {
        if(isOnline(userStateModel)) {
          onlineUsers.add(userStateModel);
        }        
      }
    } catch (Exception e) {
      LOG.error("Exception when getting online user: {}",e);
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

  private boolean isOnline(UserStateModel model) {
    if (model != null) {
      long iDate = Calendar.getInstance().getTimeInMillis();
      if (model.getLastActivity() >= (iDate - delay)) {
        return true;
      }
    }
    return false;
  }
  
  private ExoCache<Serializable, UserStateModel> getUserStateCache(){
    return cacheService.getCacheInstance(UserStateService.class.getName() + CommonsUtils.getRepository().getConfiguration().getName()) ;     
  }
}

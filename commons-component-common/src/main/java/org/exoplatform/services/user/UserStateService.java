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
import java.util.Date;
import java.util.List;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;


public class UserStateService {
  private static final Log LOG = ExoLogger.getLogger(UserStateService.class.getName());
  protected static final String WORKSPACE_NAME = "collaboration";
  public static String VIDEOCALLS_BASE_PATH = "VideoCalls";
  public static String USER_STATATUS_NODETYPE = "exo:userState";
  public static String USER_ID_PROP = "exo:userId";
  public static String LAST_ACTIVITY_PROP = "exo:lastActivity";
  public static String STATUS_PROP = "exo:status";
  public static String DEFAULT_STATUS = "available";
  public static int delay = 60;
  
  private static ExoCache<Serializable, UserStateModel> userStateCache;
  
  public UserStateService(CacheService cacheService) {
    userStateCache = cacheService.getCacheInstance(UserStateService.class.getName() + CommonsUtils.getRepository().getConfiguration().getName()) ;   
    if(System.getProperty("user.status.offline.delay") != null ) {
      delay = Integer.parseInt(System.getProperty("user.status.offline.delay"));
    }
  }
  // Add or update a userState
  public void save(UserStateModel model) {
    String userId = model.getUserId();
    String status = model.getStatus();
    int lastActivity = model.getLastActivity();  
    
    SessionProvider sessionProvider = new SessionProvider(ConversationState.getCurrent());
    NodeHierarchyCreator nodeHierarchyCreator = CommonsUtils.getService(NodeHierarchyCreator.class);    
    
    try {
      RepositoryService repositoryService = (RepositoryService) PortalContainer.getInstance()
          .getComponentInstanceOfType(RepositoryService.class);
      Session session = sessionProvider.getSession(WORKSPACE_NAME, repositoryService.getCurrentRepository());
      String repoName = repositoryService.getCurrentRepository().getConfiguration().getName(); 
      
      Node userNodeApp = nodeHierarchyCreator.getUserApplicationNode(sessionProvider, userId);     
      String userKey = repoName + "_" + userId;      
      if(!userNodeApp.hasNode(VIDEOCALLS_BASE_PATH)) {
        userNodeApp.addNode(VIDEOCALLS_BASE_PATH, USER_STATATUS_NODETYPE);  
        userNodeApp.save();
      }
      Node userState = userNodeApp.getNode(VIDEOCALLS_BASE_PATH);
      userState.setProperty(USER_ID_PROP, userId);
      userState.setProperty(LAST_ACTIVITY_PROP, lastActivity);
      userState.setProperty(STATUS_PROP, status);
      session.save();
      userStateCache.put(userKey, model);
    } catch(Exception ex) {
      if (LOG.isErrorEnabled()) {
        LOG.error("save() failed because of ", ex);
      }
    }
  }
  
  //Get userState for a user
  public UserStateModel getUserState(String userId) {
    UserStateModel model = null;
    String repoName = CommonsUtils.getRepository().getConfiguration().getName();
    String userKey = repoName + "_" + userId;
    if(userStateCache != null && userStateCache.get(userKey) != null) {
      model = userStateCache.get(userKey);
    } else {
      SessionProvider sessionProvider = new SessionProvider(ConversationState.getCurrent());
      NodeHierarchyCreator nodeHierarchyCreator = CommonsUtils.getService(NodeHierarchyCreator.class);
      try{
        Node userNodeApp = nodeHierarchyCreator.getUserApplicationNode(sessionProvider, userId);        
        if(!userNodeApp.hasNode(VIDEOCALLS_BASE_PATH)) return null;        
        Node userState = userNodeApp.getNode(VIDEOCALLS_BASE_PATH);
        model = new UserStateModel();
        model.setUserId(userState.getProperty(USER_ID_PROP).getString());
        model.setLastActivity(Integer.parseInt(userState.getProperty(LAST_ACTIVITY_PROP).getString()));
        model.setStatus(userState.getProperty(STATUS_PROP).getString());        
        userStateCache.put(userKey, model);
      } catch(Exception ex) {
        if (LOG.isErrorEnabled()) {
          LOG.error("getUserState() failed because of ", ex);
        }
      }
    }
    return model;
  }
  
  //Ping to update last activity
  public void ping(String userId) {
    String status = DEFAULT_STATUS;
    String repoName = CommonsUtils.getRepository().getConfiguration().getName();
    String userKey = repoName + "_" + userId;
    if(userStateCache != null && userStateCache.get(userKey) != null) {
      status = userStateCache.get(userKey).getStatus();
    }
    int lastActivity = (int) (new Date().getTime()/1000);
    UserStateModel model = getUserState(userId);
    if(model != null) {
      model.setLastActivity(lastActivity);
    } else {
      model = new UserStateModel();
      model.setStatus(status);
      model.setUserId(userId);      
      model.setLastActivity(lastActivity);      
    }
    save(model);
    userStateCache.put(userKey, model);
  }
  
  //Get all users online
  public List<UserStateModel> online() {
    List<UserStateModel> onlineUsers = new ArrayList<UserStateModel>();
    SessionProvider sessionProvider = new SessionProvider(ConversationState.getCurrent());
    RepositoryService repositoryService = (RepositoryService) PortalContainer.getInstance()
        .getComponentInstanceOfType(RepositoryService.class);
    try {
      Session session = sessionProvider.getSession(WORKSPACE_NAME, repositoryService.getCurrentRepository());
      int iDate = (int) (new Date().getTime()/1000);
      QueryManager queryManager = session.getWorkspace().getQueryManager();
      //String queryStatement = "SELECT * FROM exo:userState WHERE jcr:path like '/Users/' AND exo:lastActivity > " + (iDate-60) + 
      //    " order by exo:userId";
      String queryStatement = "SELECT * FROM exo:userState WHERE jcr:path like '/Users/%' AND exo:lastActivity > " + (iDate-delay) + 
          " order by exo:userId";
      System.out.println(" TRUY VAN == " + queryStatement);
      Query query = queryManager.createQuery(queryStatement, Query.SQL);
      QueryResult results = query.execute();
      NodeIterator iter = results.getNodes();
      while (iter.hasNext()) {
        Node node = iter.nextNode();
        UserStateModel model = new UserStateModel();
        model.setUserId(node.getProperty(USER_ID_PROP).getString());
        model.setStatus(node.getProperty(STATUS_PROP).getString());
        model.setLastActivity(Integer.parseInt(node.getProperty(LAST_ACTIVITY_PROP).getString()));
        onlineUsers.add(model);
      }
      
    } catch (LoginException e) {
      e.printStackTrace();
    } catch (NoSuchWorkspaceException e) {
      e.printStackTrace();
    } catch (RepositoryException e) {
      e.printStackTrace();
    }
    
    return onlineUsers;
  } 
}

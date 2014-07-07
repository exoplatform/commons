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

import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

import javax.jcr.Node;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 22, 2014  
 */
public class UserStateServiceTest extends BaseCommonsTestCase {
  
  public static String VIDEOCALLS_BASE_PATH = "VideoCalls";
  public static String USER_STATATUS_NODETYPE = "exo:userState";
  
  public static String USER_ID_PROP = "exo:userId";
  public static String LAST_ACTIVITY_PROP = "exo:lastActivity";
  public static String STATUS_PROP = "exo:status";
  
  private UserStateService userStateService;
  private NodeHierarchyCreator nodeHierarchyCreator;
  
  private String defaultWorkspace;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    ConversationState c = new ConversationState(new Identity(session.getUserID()));
    ConversationState.setCurrent(c);

    userStateService = getService(UserStateService.class);
    nodeHierarchyCreator = getService(NodeHierarchyCreator.class);
    
    ManageableRepository repo  = repositoryService.getRepository(REPO_NAME);
    session = repo.getSystemSession(WORKSPACE_NAME);
    root = session.getRootNode();
    
    defaultWorkspace = repo.getConfiguration().getDefaultWorkspaceName();
    repo.getConfiguration().setDefaultWorkspaceName("collaboration");
  }
  
  protected void tearDown() throws Exception {
    ManageableRepository repo  = repositoryService.getRepository(REPO_NAME);
    repo.getConfiguration().setDefaultWorkspaceName(defaultWorkspace);
  }
  
  public void testSave() throws Exception {
    UserStateModel userModel = 
        new UserStateModel(session.getUserID(),
                           new Date().getTime(),
                           UserStateService.DEFAULT_STATUS);
    userStateService.save(userModel);
    
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    try {
      Node userNodeApp = nodeHierarchyCreator.getUserApplicationNode(sessionProvider, session.getUserID());
      assertTrue(userNodeApp.hasNode(VIDEOCALLS_BASE_PATH));
      
      Node videoCallNode = userNodeApp.getNode(VIDEOCALLS_BASE_PATH);
      assertTrue(videoCallNode.isNodeType(USER_STATATUS_NODETYPE));
      
      assertTrue(videoCallNode.hasProperty(USER_ID_PROP));
      assertEquals(userModel.getUserId(), videoCallNode.getProperty(USER_ID_PROP).getString());
      
      assertTrue(videoCallNode.hasProperty(LAST_ACTIVITY_PROP));
      assertEquals(userModel.getLastActivity(), videoCallNode.getProperty(LAST_ACTIVITY_PROP).getLong());
  
      assertTrue(videoCallNode.hasProperty(STATUS_PROP));
      assertEquals(userModel.getStatus(), videoCallNode.getProperty(STATUS_PROP).getString());
    } catch (Exception e) {      
      sessionProvider.close();
      throw e;
    }
  }
  
  public void testGetUserState() throws Exception {
    UserStateModel userModel = 
        new UserStateModel(session.getUserID(),
                           new Date().getTime(),
                           UserStateService.DEFAULT_STATUS);

    userStateService.save(userModel);
    UserStateModel model1 = userStateService.getUserState(session.getUserID() + "temp");
    assertNull(model1);
    
    UserStateModel model2 = userStateService.getUserState(session.getUserID());
    assertNotNull(model2);
    
    assertEquals(session.getUserID(), model2.getUserId());
    assertEquals(userModel.getLastActivity(), model2.getLastActivity());
    assertEquals(UserStateService.DEFAULT_STATUS, model2.getStatus());
  }
  
  public void testPing() throws Exception {
    UserStateModel userModel = 
        new UserStateModel(session.getUserID(),
                           new Date().getTime(),
                           UserStateService.DEFAULT_STATUS);
    userStateService.save(userModel);
    userStateService.ping(userModel.getUserId());
    
    assertTrue(userModel.getLastActivity() != userStateService.getUserState(session.getUserID()).getLastActivity());
    
    Calendar currentTime = new GregorianCalendar();
    Calendar time = (Calendar)currentTime.clone();
    time.add(Calendar.MINUTE, -10);
    userModel.setLastActivity(time.getTime().getTime());
    
    userStateService.save(userModel);
    userStateService.ping(userModel.getUserId());

    assertTrue(userModel.getLastActivity() != userStateService.getUserState(session.getUserID()).getLastActivity());
  }
  
  public void testOnline() throws Exception {
    long date = new Date().getTime();
    UserStateModel userModel = 
        new UserStateModel(session.getUserID(),
                           date,
                           UserStateService.DEFAULT_STATUS);
    userStateService.save(userModel);
    userStateService.ping(userModel.getUserId());

    List<UserStateModel> onlines = userStateService.online();
    assertEquals(1, onlines.size());
    assertEquals(session.getUserID(), onlines.get(0).getUserId());
    assertTrue(date != onlines.get(0).getLastActivity());
    assertEquals(UserStateService.DEFAULT_STATUS, onlines.get(0).getStatus());
  }

  public void testIsOnline() throws Exception {
    long date = new Date().getTime();
    UserStateModel userModel = 
        new UserStateModel(session.getUserID(),
                           date,
                           UserStateService.DEFAULT_STATUS);
    userStateService.save(userModel);
    userStateService.ping(userModel.getUserId());
    assertTrue(userStateService.isOnline(session.getUserID()));
    //
    assertFalse(userStateService.isOnline("demo"));
  }

}

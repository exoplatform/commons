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
import java.util.Date;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.junit.Ignore;

import org.exoplatform.commons.testing.BaseResourceTestCase;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.wadl.research.HTTPMethods;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Apr
 * 23, 2014
 */
@Ignore
public class RESTUserServiceTest extends BaseResourceTestCase {

  private static final String USERNAME = "root";

  private UserStateService    userStateService;

  public void setUp() throws Exception {
    super.setUp();

    UserStateServiceREST restUserService = getService(UserStateServiceREST.class);
    this.resourceBinder.addResource(restUserService, null);

    userStateService = getService(UserStateService.class);

    ConversationState c = new ConversationState(new Identity(USERNAME));
    ConversationState.setCurrent(c);
  }

  public void tearDown() throws Exception {
    //
    ExoCache<Serializable, UserStateModel> cache =
                                                 getService(CacheService.class).getCacheInstance(UserStateService.USER_STATE_CACHE_NAME);
    //
    cache.clearCache();
  }

  public void testUpdateState() throws Exception {
    String restPath = "/state/ping";

    UserStateModel userModel =
                             new UserStateModel(USERNAME,
                                                new Date().getTime(),
                                                UserStateService.DEFAULT_STATUS);
    userStateService.save(userModel);

    ContainerResponse response = service(HTTPMethods.GET.toString(), restPath, StringUtils.EMPTY, null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    assertTrue(userModel.getLastActivity() != userStateService.getUserState(USERNAME).getLastActivity());
  }

  public void testOnline() throws Exception {
    String restPath = "/state/status";

    UserStateModel userModel =
                             new UserStateModel(USERNAME,
                                                new Date().getTime(),
                                                UserStateService.DEFAULT_STATUS);
    userStateService.save(userModel);
    userStateService.ping(userModel.getUserId());

    ContainerResponse response = service(HTTPMethods.GET.toString(), restPath, StringUtils.EMPTY, null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    assertTrue(response.getEntity().toString().contains("\"activity\":\"online\""));
    assertTrue(response.getEntity().toString().contains("\"status\":\"" + userModel.getStatus() + "\""));
    assertTrue(response.getEntity().toString().contains("\"userId\":\"" + userModel.getUserId() + "\""));

    assertFalse(response.getEntity().toString().contains("\"userId\":\"" + userModel.getUserId() + "temp\""));
  }

  public void testGetStatus() throws Exception {
    String restPath = "/state/status/" + USERNAME;

    UserStateModel userModel =
                             new UserStateModel(USERNAME,
                                                new Date().getTime(),
                                                "offline");
    userStateService.save(userModel);
    userStateService.ping(userModel.getUserId());

    ContainerResponse response = service(HTTPMethods.GET.toString(), restPath, StringUtils.EMPTY, null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    assertTrue(response.getEntity().toString().contains("\"activity\":\"online\""));
    assertTrue(response.getEntity().toString().contains("\"status\":\"" + userModel.getStatus() + "\""));
    assertTrue(response.getEntity().toString().contains("\"userId\":\"" + userModel.getUserId() + "\""));

    assertFalse(response.getEntity().toString().contains("\"userId\":\"" + userModel.getUserId() + "temp\""));
  }

  public void testSetStatus() throws Exception {
    String restPath = "/state/status/" + USERNAME + "?status=offline";

    UserStateModel userModel =
                             new UserStateModel(USERNAME,
                                                new Date().getTime(),
                                                UserStateService.DEFAULT_STATUS);
    userStateService.save(userModel);
    userStateService.ping(userModel.getUserId());
    assertTrue(userStateService.getUserState(USERNAME).getStatus().equals(UserStateService.DEFAULT_STATUS));

    ContainerResponse response = service(HTTPMethods.PUT.toString(), restPath, StringUtils.EMPTY, null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertTrue(userStateService.getUserState(userModel.getUserId()).getStatus().equals("offline"));
  }
}

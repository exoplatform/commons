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

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


@Path("/state/")
public class RESTUserService implements ResourceContainer{
  private static final Log LOG = ExoLogger.getLogger(RESTUserService.class);
  private final UserService userService;
  
  public RESTUserService(UserService userService) {
    this.userService = userService;
  }
  
    
  @GET
  @Path("/ping/{userId}/")
  @RolesAllowed("users")
  public Response updateState(@PathParam("userId") String userId) {
    userService.updateUserTime(userId);
    return Response.ok().build();
  }
  
  @GET
  @Path("/online/")
  @RolesAllowed("users")
  public Response online() throws ParserConfigurationException {
    List<String> usersOnline = userService.getUsersOnline();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.newDocument();
    
    Element rootElement = document.createElement("Users");
    document.appendChild(rootElement);
    
    for(int i=0; i< usersOnline.size(); i++) {
      String user = usersOnline.get(i);
      Element newChild = document.createElement("User");
      newChild.setAttribute("userName", user);
      rootElement.appendChild(newChild);
    }
    return Response.ok(new DOMSource(document), MediaType.TEXT_XML).build();
  }
  
  @GET
  @Path("/online/{userId}/")
  @RolesAllowed("users")
  public Response online(@PathParam("userId") String userId) {
    String status = "offline";
    Boolean b = userService.getUserStatus(userId);
    if(b) status = "available";
    return Response.ok(status.toString()).build();
  }
  
  
}

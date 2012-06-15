/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.bench;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * Created by The eXo Platform SAS
 * @Author : <a href="mailto:quanglt@exoplatform.com">Le Thanh Quang</a>
 * Jul 20, 2011  
 */
@Path("/bench")
public class DataInjectorService implements ResourceContainer {
  
  private static final Log         LOG             = ExoLogger.getLogger(DataInjectorService.class);
  
  private enum Actions {
    INJECT, REJECT, EXECUTE
  }

  private static CacheControl cc = new CacheControl();
  static {
    cc.setNoCache(true);
    cc.setNoStore(true);
  }
  
  private List<DataInjector> listOfInjectors = new LinkedList<DataInjector>();
  
  private HashMap<String, String> convertToHashMap(MultivaluedMap<String, String> paramsMap) {
    HashMap<String, String> parameters = new HashMap<String, String>();
    Iterator<Entry<String, List<String>>> iterator = paramsMap.entrySet().iterator();
    while (iterator.hasNext()) {
      Entry<String, List<String>> entry = iterator.next();
      String key = entry.getKey();
      List<String> values = entry.getValue();
      String value = values.size() > 0 ? values.get(0) : null;
      parameters.put(key, value);
    }
    
    return parameters;
  }
  
  private DataInjector getDataInjector(String injectorId) {
    for (DataInjector di : listOfInjectors) {
      if (injectorId.equals(di.getName())) {
        return di;
      }
    }
    return null;
  }
  
  /**
   * this function is used to add DataInjector plugins.
   * @param dataInjector
   */
  public void addInjector(DataInjector dataInjector) {
    listOfInjectors.add(dataInjector);
  }
  
  @GET
  @Path("/execute/{injectorId}")
  @RolesAllowed("administrators")
  public Response execute(@PathParam("injectorId") String type, @Context UriInfo info) {
    DataInjector injector = getDataInjector(type);
    if (injector == null) {
      return Response.status(Status.BAD_REQUEST).entity("Injector id is incorrect!").cacheControl(cc).build();
    }
    HashMap<String, String> params = convertToHashMap(info.getQueryParameters());
    try {
      beginPrintInfo(params, Actions.EXECUTE);
      Object response = injector.execute(params);
      return Response.ok(response, MediaType.APPLICATION_JSON).cacheControl(cc).build();
    } catch (Exception e) {   
      //injector.execute() throws Exception(). It's a public abstract method, we shouldn't modify it
      //So we have to catch Exception
      errorPrintInfo(injector, Actions.EXECUTE, e);
      return Response.serverError()
                     .entity(String.format("%1$s executed failed due to %2$s",
                                           injector.getName(),
                                           e.getMessage()))
                     .build();
    }
  }
  
  @GET
  @Path("/inject/{injectorId}")
  @RolesAllowed("administrators")
  public Response inject(@PathParam("injectorId") String type, @Context UriInfo info) {
    DataInjector injector = getDataInjector(type);
    if (injector == null) {
      return Response.status(Status.BAD_REQUEST).entity("Injector id is incorrect!").cacheControl(cc).build();
    }
    HashMap<String, String> params = convertToHashMap(info.getQueryParameters());
    try {
      beginPrintInfo(params, Actions.INJECT);
      injector.inject(params);
    } catch (Exception e) {   
      //injector.inject() throws Exception(). It's a public abstract method, we shouldn't modify it
      //So we have to catch Exception
      errorPrintInfo(injector, Actions.INJECT, e);
      return Response.serverError()
                     .entity(String.format("%1$s injected failed due to %2$s",
                                           injector.getName(),
                                           e.getMessage()))
                     .build();
    }
    endPrintInfo(Actions.INJECT);
    return Response.ok(String.format("%s injected successfully!!!", injector.getName()),
                       MediaType.TEXT_PLAIN)
                   .cacheControl(cc)
                   .build();
  }
  
  @GET
  @Path("/reject/{injectorId}")
  @RolesAllowed("administrators")
  public Response reject(@PathParam("injectorId") String type, @Context UriInfo info) {
    DataInjector injector = getDataInjector(type);
    if (injector == null) {
      return Response.status(Status.BAD_REQUEST).entity("Injector id is incorrect").cacheControl(cc).build();
    }
    HashMap<String, String> params = convertToHashMap(info.getQueryParameters());
    try {
      beginPrintInfo(params, Actions.REJECT);
      injector.reject(params);
    } catch (Exception e) {   
      //injector.inject() throws Exception(). It's a public abstract method, we shouldn't modify it
      //So we have to catch Exception
      errorPrintInfo(injector, Actions.REJECT, e);
      return Response.serverError()
                     .entity(String.format("%1$s rejected failed due to %2$s",
                                           injector.getName(),
                                           e.getMessage()))
                     .build();
    }
    endPrintInfo(Actions.REJECT);
    return Response.ok(String.format("%s rejected successfully!!!", injector.getName()),
                       MediaType.TEXT_PLAIN)
                   .cacheControl(cc)
                   .build();
  }

  private void beginPrintInfo(HashMap<String, String> params, Actions action) {
    LOG.info(String.format("Start to %s............... ", action.toString().toLowerCase()));
    StringBuilder sb = new StringBuilder();
    sb.append("PARAMS: \n");
    Iterator<String> keys = params.keySet().iterator();
    while (keys.hasNext()) {
      String key = keys.next();
      sb.append(String.format("%1$10s    :    %2$10s \n", key, params.get(key)));
    }
    LOG.info(sb.toString());
  }

  private void endPrintInfo(Actions action) {
    LOG.info(String.format("%sing data has been done successfully!", action.toString().toLowerCase()));
  }

  private void errorPrintInfo(DataInjector injector, Actions action, Exception ex) {
    if (LOG.isWarnEnabled())
      LOG.warn(String.format("%s %sed failed", injector.getName(), action.toString().toLowerCase()), ex);
  }

}

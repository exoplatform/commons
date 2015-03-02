/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.commons.juzu.ajax;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import juzu.Response;
import juzu.impl.plugin.ServiceContext;
import juzu.impl.plugin.ServiceDescriptor;
import juzu.impl.plugin.application.ApplicationService;
import juzu.impl.plugin.controller.ControllerService;
import juzu.impl.request.ControllerHandler;
import juzu.impl.request.Request;
import juzu.impl.request.RequestFilter;
import juzu.impl.request.Stage;
import juzu.io.Chunk;
import juzu.io.Stream;
import juzu.io.Streamable;
import juzu.request.Phase;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AjaxService extends ApplicationService implements RequestFilter<Stage.Unmarshalling> {

  /** . */
  Map<String, ControllerHandler> table;

  @Inject
  ControllerService controllerService;

  public AjaxService() {
    super("plf4-ajax");
  }

  @Override
  public ServiceDescriptor init(ServiceContext context) throws Exception {
    return context.getConfig() != null ? new ServiceDescriptor() : null;
  }

  @PostConstruct
  public void start() throws Exception {
    //
    Map<String, ControllerHandler> table = new HashMap<String, ControllerHandler>();
    for (ControllerHandler cm : controllerService.getDescriptor().getHandlers()) {
      Ajax ajax = cm.getMethod().getAnnotation(Ajax.class);
      if (ajax != null) {
        table.put(cm.getName(), cm);
      }
    }

    //
    this.table = table;
  }

  @Override
  public Class<Stage.Unmarshalling> getStageType() {
    return Stage.Unmarshalling.class;
  }

  @Override
  public Response handle(Stage.Unmarshalling argument) {
    final Request request = argument.getRequest();
    Response result = argument.invoke();

    //
    if (request.getPhase() == Phase.VIEW) {
      if (result instanceof Response.Content) {
        Response.Status status = (Response.Status)result;
        final Streamable wrapped = status.streamable();
        Streamable wrapper = new Streamable() {
          public void send(final Stream stream) throws IllegalStateException {
            Stream our = new Stream() {
              boolean done = false;
              public void provide(Chunk chunk) {
                if (chunk instanceof Chunk.Data && !done) {
                  done = true;
                  // FOR NOW WE DO WITH THE METHOD NAME
                  // BUT THAT SHOULD BE REVISED TO USE THE ID INSTEAD
                  StringBuilder sb = new StringBuilder();
                  sb.append("<div class=\"jz\">\n");
                  for (Map.Entry<String, ControllerHandler> entry : table.entrySet()) {
                    String baseURL = request.createDispatch(entry.getValue()).toString();
                    sb.append("<div data-method-id=\"");
                    sb.append(entry.getValue().getId());
                    sb.append("\" data-url=\"");
                    sb.append(baseURL);
                    sb.append("\"/>");
                    sb.append("</div>");
                  }
                  stream.provide(Chunk.create(sb));
                }
                stream.provide(chunk);
              }
              public void close(Thread.UncaughtExceptionHandler errorHandler) {
                stream.provide(Chunk.create("</div>"));
                stream.close(errorHandler);
              }
            };
            wrapped.send(our);
          }
        };
        result = new Response.Content(status.getCode(), wrapper);
      }
    }

    //
    return result;
  }
}

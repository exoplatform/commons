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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


import javax.annotation.PostConstruct;
import javax.inject.Inject;
import juzu.PropertyMap;
import juzu.Response;
import juzu.impl.metadata.Descriptor;
import juzu.impl.plugin.PluginContext;
import juzu.impl.plugin.application.ApplicationPlugin;
import juzu.impl.plugin.controller.ControllerPlugin;
import juzu.impl.request.Method;
import juzu.impl.request.Request;
import juzu.impl.request.RequestFilter;
import juzu.io.Stream;
import juzu.io.Streamable;
import juzu.request.RenderContext;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AjaxPlugin extends ApplicationPlugin implements RequestFilter {

  /** . */
  Map<String, Method> table;

  @Inject
  ControllerPlugin controllerPlugin;

  public AjaxPlugin() {
    super("plf4-ajax");
  }

  @Override
  public Descriptor init(PluginContext context) throws Exception {
    return context.getConfig() != null ? new Descriptor() : null;
  }

  @PostConstruct
  public void start() throws Exception {
    //
    Map<String, Method> table = new HashMap<String, Method>();
    for (Method cm : controllerPlugin.getDescriptor().getMethods()) {
      Ajax ajax = cm.getMethod().getAnnotation(Ajax.class);
      if (ajax != null) {
        table.put(cm.getName(), cm);
      }
    }

    //
    this.table = table;
  }

  public void invoke(final Request request) {
    request.invoke();

    //
    if (request.getContext() instanceof RenderContext) {
      Response response = request.getResponse();
      if (response instanceof Response.Render) {
        Response.Render render = (Response.Render)response;

        //
        PropertyMap properties = new PropertyMap(response.getProperties());

        //
        final Streamable<Stream.Char> decorated = render.getStreamable();
        Streamable<Stream.Char> decorator = new Streamable<Stream.Char>() {
          public void send(Stream.Char stream) throws IOException {
            // FOR NOW WE DO WITH THE METHOD NAME
            // BUT THAT SHOULD BE REVISED TO USE THE ID INSTEAD

            //
            stream.append("<div class=\"jz\">\n");

            //
            for (Map.Entry<String, Method> entry : table.entrySet()) {
              String baseURL = request.getContext().createDispatch(entry.getValue()).toString();
              stream.append("<div data-method-id=\"");
              stream.append(entry.getValue().getId());
              stream.append("\" data-url=\"");
              stream.append(baseURL);
              stream.append("\"/>");
              stream.append("</div>");
            }

            // The page
            decorated.send(stream);

            //
            stream.append("</div>");
          }
        };

        //
        request.setResponse(new Response.Render(properties, decorator));
      }
    }
  }
}

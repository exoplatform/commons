/**
 * Copyright (C) 2015 eXo Platform SAS.
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

package org.exoplatform.commons.api.websocket;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ExtensibleWSFilter {
  private volatile List<WSFilterDefinition> filters = Collections.unmodifiableList(new ArrayList<WSFilterDefinition>());

  public void addFilterDefinitions(FilterDefinitionPlugin plugin) {
    addFilterDefinitions(plugin.getFilterDefinitions());
  }

  void addFilterDefinitions(List<WSFilterDefinition> pluginFilters) {
    if (pluginFilters == null || pluginFilters.isEmpty()) {
      // No filter to add
      return;
    }
    synchronized (this) {
      List<WSFilterDefinition> result = new ArrayList<WSFilterDefinition>(filters);
      result.addAll(pluginFilters);
      this.filters = Collections.unmodifiableList(result);
    }
  }

  public void onOpen(Session wsSession, EndpointConfig config, AbstractEndpoint delegate) {
    ExtensibleFilterChain efChain = new ExtensibleFilterChain(filters, delegate);
    efChain.onOpen(wsSession, config);
  }

  public void onClose(Session wsSession, CloseReason closeReason, AbstractEndpoint delegate) {
    ExtensibleFilterChain efChain = new ExtensibleFilterChain(filters, delegate);
    efChain.onClose(wsSession, closeReason);
  }

  public void onError(Session wsSession, Throwable failure, AbstractEndpoint delegate) {
    ExtensibleFilterChain efChain = new ExtensibleFilterChain(filters, delegate);
    efChain.onError(wsSession, failure);
  }

  public void onMessage(String message, AbstractEndpoint delegate) {
    ExtensibleFilterChain efChain = new ExtensibleFilterChain(filters, delegate);
    efChain.onMessage(delegate._session, message);
  }

  public void onMessage(String message, boolean arg1, AbstractEndpoint delegate) {
    ExtensibleFilterChain efChain = new ExtensibleFilterChain(filters, delegate);
    efChain.onMessage(delegate._session, message, arg1);
  }

  public class ExtensibleFilterChain {
    private final Iterator<WSFilterDefinition> filters;

    private final AbstractEndpoint             delegate;

    public ExtensibleFilterChain(List<WSFilterDefinition> filters, AbstractEndpoint delegate) {
      this.filters = filters.iterator();
      this.delegate = delegate;
    }

    public void onMessage(Session wsSession, String message, boolean arg1) {
      while (filters.hasNext()) {
        WSFilterDefinition filterDef = filters.next();
        if (filterDef.match(wsSession.getRequestURI().getPath())) {
          filterDef.getFilter().onMessage(wsSession, message, arg1, this);
          return;
        }
      }
      delegate.doMessage(wsSession, message, arg1);
    }

    public void onMessage(Session wsSession, String message) {
      while (filters.hasNext()) {
        WSFilterDefinition filterDef = filters.next();
        if (filterDef.match(wsSession.getRequestURI().getPath())) {
          filterDef.getFilter().onMessage(wsSession, message, this);
          return;
        }
      }
      delegate.doMessage(wsSession, message);
    }

    public void onError(Session wsSession, Throwable failure) {
      while (filters.hasNext()) {
        WSFilterDefinition filterDef = filters.next();
        if (filterDef.match(wsSession.getRequestURI().getPath())) {
          filterDef.getFilter().onError(wsSession, failure, this);
          return;
        }
      }
      delegate.doError(wsSession, failure);
    }

    public void onClose(Session wsSession, CloseReason closeReason) {
      while (filters.hasNext()) {
        WSFilterDefinition filterDef = filters.next();
        if (filterDef.match(wsSession.getRequestURI().getPath())) {
          filterDef.getFilter().onClose(wsSession, closeReason, this);
          return;
        }
      }
      delegate.doClose(wsSession, closeReason);
    }

    public void onOpen(Session wsSession, EndpointConfig config) {
      while (filters.hasNext()) {
        WSFilterDefinition filterDef = filters.next();
        if (filterDef.match(wsSession.getRequestURI().getPath())) {
          filterDef.getFilter().onOpen(wsSession, config, this);
          return;
        }
      }
      delegate.doOpen(wsSession, config);
    }
  }
}

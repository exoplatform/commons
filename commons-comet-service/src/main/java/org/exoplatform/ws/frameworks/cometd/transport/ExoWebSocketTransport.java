/**
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ws.frameworks.cometd.transport;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.EndpointConfig;
import javax.websocket.Extension;
import javax.websocket.HandshakeResponse;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executor;

import org.cometd.bayeux.server.BayeuxContext;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.server.AbstractServerTransport;
import org.cometd.server.BayeuxServerImpl;
import org.cometd.websocket.server.AbstractWebSocketTransport;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.component.LifeCycle;
import org.exoplatform.commons.api.websocket.AbstractConfigurator;
import org.exoplatform.commons.api.websocket.AbstractEndpoint;

public class ExoWebSocketTransport extends AbstractWebSocketTransport<Session> {
  public ExoWebSocketTransport(BayeuxServerImpl bayeux) {
    super(bayeux);
  }

  @Override
  public void init() {
    super.init();

    final ServletContext context = (ServletContext) getOption(ServletContext.class.getName());
    if (context == null)
      throw new IllegalArgumentException("Missing ServletContext");

    String cometdURLMapping = (String) getOption(COMETD_URL_MAPPING);
    if (cometdURLMapping == null)
      throw new IllegalArgumentException("Missing '" + COMETD_URL_MAPPING + "' parameter");

    ServerContainer container = (ServerContainer) context.getAttribute(ServerContainer.class.getName());
    if (container == null)
      throw new IllegalArgumentException("Missing WebSocket ServerContainer");

    // JSR 356 does not support a input buffer size option
    int maxMessageSize = getOption(MAX_MESSAGE_SIZE_OPTION,
                                   container.getDefaultMaxTextMessageBufferSize());
    container.setDefaultMaxTextMessageBufferSize(maxMessageSize);
    long idleTimeout = getOption(IDLE_TIMEOUT_OPTION, container.getDefaultMaxSessionIdleTimeout());
    container.setDefaultMaxSessionIdleTimeout(idleTimeout);

    String protocol = getProtocol();
    List<String> protocols = protocol == null ? null : Collections.singletonList(protocol);

    for (String mapping : normalizeURLMapping(cometdURLMapping)) {
      ServerEndpointConfig config = ServerEndpointConfig.Builder.create(WebSocketScheduler.class,
                                                                        mapping)
                                                                .subprotocols(protocols)
                                                                .configurator(new Configurator(context))
                                                                .build();
      try {
        container.addEndpoint(config);
      } catch (DeploymentException x) {
        throw new RuntimeException(x);
      }
    }
  }

  @Override
  public void destroy() {
    Executor threadPool = getExecutor();
    if (threadPool instanceof LifeCycle) {
      try {
        ((LifeCycle) threadPool).stop();
      } catch (Exception x) {
        _logger.trace("", x);
      }
    }
    super.destroy();
  }

  protected boolean checkOrigin(String origin) {
    return true;
  }

  protected void modifyHandshake(HandshakeRequest request, HandshakeResponse response) {
  }

  protected void send(final Session wsSession,
                      final ServerSession session,
                      String data,
                      final Callback callback) {
    if (_logger.isDebugEnabled())
      _logger.debug("Sending {}", data);

    // Async write.
    wsSession.getAsyncRemote().sendText(data, new SendHandler() {
      @Override
      public void onResult(SendResult result) {
        Throwable failure = result.getException();
        if (failure == null) {
          callback.succeeded();
        } else {
          handleException(wsSession, session, failure);
          callback.failed(failure);
        }
      }
    });
  }

  private class WebSocketScheduler extends AbstractEndpoint implements AbstractServerTransport.Scheduler {
    private WSSchedulerDelegate delegate;

    private WebSocketScheduler(WebSocketContext context) {
      delegate = new WSSchedulerDelegate(context);
    }

    @Override
    public void cancel() {
      delegate.cancel();
    }

    @Override
    public void schedule() {
      delegate.schedule();
    }
    
    @Override
    protected void doOpen(Session wsSession, EndpointConfig config) {
      delegate.setSession(wsSession);
    }

    @Override
    protected void doClose(Session wsSession, CloseReason closeReason) {
      delegate.onClose(closeReason.getCloseCode().getCode(), closeReason.getReasonPhrase());
    }

    @Override
    protected void doError(Session wsSession, Throwable failure) {
      delegate.onError(failure);
    }

    @Override
    protected void doMessage(Session wsSession, String message) {
      if (_logger.isDebugEnabled())
        _logger.debug("WebSocket Text message on {}/{}",
                      ExoWebSocketTransport.this.hashCode(),
                      hashCode());
      delegate.onMessage(wsSession, message);
    }

    @Override
    protected void doMessage(Session wsSession, String message, boolean arg1) {   
      if (_logger.isDebugEnabled())
        _logger.debug("WebSocket Text message on {}/{}",
                      ExoWebSocketTransport.this.hashCode(),
                      hashCode());
      delegate.onMessage(wsSession, message);
    }
  }
  
  private class WSSchedulerDelegate extends AbstractWebSocketScheduler {
    private Session session;
    
    public WSSchedulerDelegate(WebSocketContext context) {
      super(context);
    }
    
    @Override
    protected void onClose(int code, String reason) {
      // workaround to expand method visbility
      super.onClose(code, reason);
    }

    @Override
    protected void onError(Throwable failure) {
      // workaround to expand method visbility
      super.onError(failure);
    }

    @Override
    protected void onMessage(Session wsSession, String data) {
      // workaround to expand method visbility
      super.onMessage(wsSession, data);
    }

    @Override
    protected void close(final int code, String reason) {
      try {
        session.close(new CloseReason(CloseReason.CloseCodes.getCloseCode(code), reason));
      } catch (IOException x) {
        _logger.trace("Could not close WebSocket session " + session, x);
      }
    }

    @Override
    protected void schedule(boolean timeout, ServerMessage.Mutable expiredConnectReply) {
      schedule(session, timeout, expiredConnectReply);
    }
    
    public void setSession(Session session) {
      this.session = session;
    }
  }

  private class WebSocketContext implements BayeuxContext {
    private final ServletContext            context;

    private final String                    url;

    private final Principal                 principal;

    private final Map<String, List<String>> headers;

    private final Map<String, List<String>> parameters;

    private final HttpSession               session;

    private final InetSocketAddress         localAddress;

    private final InetSocketAddress         remoteAddress;

    private WebSocketContext(ServletContext context,
                             HandshakeRequest request,
                             Map<String, Object> userProperties) {
      this.context = context;
      // Must copy everything from the request, it may be gone afterwards.
      String uri = request.getRequestURI().toString();
      String query = request.getQueryString();
      if (query != null)
        uri += "?" + query;
      this.url = uri;
      this.principal = request.getUserPrincipal();
      this.headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      this.headers.putAll(request.getHeaders());
      this.parameters = request.getParameterMap();
      // Assume the HttpSession does not go away immediately after the upgrade.
      this.session = (HttpSession) request.getHttpSession();
      // Hopefully this will become a standard, for now it's Jetty specific.
      this.localAddress = (InetSocketAddress) userProperties.get("javax.websocket.endpoint.localAddress");
      this.remoteAddress = (InetSocketAddress) userProperties.get("javax.websocket.endpoint.remoteAddress");
    }

    @Override
    public Principal getUserPrincipal() {
      return principal;
    }

    @Override
    public boolean isUserInRole(String role) {
      return false;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
      return remoteAddress;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
      return localAddress;
    }

    @Override
    public String getHeader(String name) {
      List<String> values = headers.get(name);
      return values != null && values.size() > 0 ? values.get(0) : null;
    }

    @Override
    public List<String> getHeaderValues(String name) {
      return headers.get(name);
    }

    public String getParameter(String name) {
      List<String> values = parameters.get(name);
      return values != null && values.size() > 0 ? values.get(0) : null;
    }

    @Override
    public List<String> getParameterValues(String name) {
      return parameters.get(name);
    }

    @Override
    public String getCookie(String name) {
      List<String> values = headers.get("Cookie");
      if (values != null) {
        for (String value : values) {
          for (HttpCookie cookie : HttpCookie.parse(value)) {
            if (cookie.getName().equals(name))
              return cookie.getValue();
          }
        }
      }
      return null;
    }

    @Override
    public String getHttpSessionId() {
      return session == null ? null : session.getId();
    }

    @Override
    public Object getHttpSessionAttribute(String name) {
      return session == null ? null : session.getAttribute(name);
    }

    @Override
    public void setHttpSessionAttribute(String name, Object value) {
      if (session != null)
        session.setAttribute(name, value);
    }

    @Override
    public void invalidateHttpSession() {
      if (session != null)
        session.invalidate();
    }

    @Override
    public Object getRequestAttribute(String name) {
      // Not available in JSR 356
      return null;
    }

    @Override
    public Object getContextAttribute(String name) {
      return context.getAttribute(name);
    }

    @Override
    public String getContextInitParameter(String name) {
      return context.getInitParameter(name);
    }

    @Override
    public String getURL() {
      return url;
    }
  }

  private class Configurator extends AbstractConfigurator {
    private final ServletContext servletContext;

    private WebSocketContext     bayeuxContext;

    private boolean              protocolMatches;

    private Configurator(ServletContext servletContext) {
      this.servletContext = servletContext;
      // Use a sensible default in case getNegotiatedSubprotocol() is not
      // invoked.
      this.protocolMatches = true;
    }

    @Override
    public void doModifyHandshake(ServerEndpointConfig sec,
                                HandshakeRequest request,
                                HandshakeResponse response) {
      this.bayeuxContext = new WebSocketContext(servletContext, request, sec.getUserProperties());
      ExoWebSocketTransport.this.modifyHandshake(request, response);
    }

    @Override
    public boolean checkOrigin(String originHeaderValue) {
      return ExoWebSocketTransport.this.checkOrigin(originHeaderValue);
    }

    @Override
    public String getNegotiatedSubprotocol(List<String> supported, List<String> requested) {
      if (protocolMatches = checkProtocol(supported, requested))
        return super.getNegotiatedSubprotocol(supported, requested);
      _logger.warn("Could not negotiate WebSocket SubProtocols: server{} != client{}",
                   supported,
                   requested);
      return null;
    }

    @Override
    public List<Extension> getNegotiatedExtensions(List<Extension> installed,
                                                   List<Extension> requested) {
      return super.getNegotiatedExtensions(installed, requested);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
      if (!getBayeux().getAllowedTransports().contains(getName()))
        throw new InstantiationException("Transport not allowed");
      if (!protocolMatches)
        throw new InstantiationException("Could not negotiate WebSocket SubProtocols");
      return (T) new WebSocketScheduler(bayeuxContext);
    }
  }
}

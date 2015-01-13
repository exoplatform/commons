package org.mortbay.cometd.continuation;

/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.client.transport.LongPollingTransport;
import org.cometd.oort.Oort;
import org.cometd.oort.OortConfigServlet;
import org.cometd.oort.OortMulticastConfigServlet;
import org.cometd.oort.OortStaticConfigServlet;
import org.cometd.oort.Seti;
import org.cometd.oort.SetiServlet;
import org.cometd.server.CometDServlet;
import org.cometd.websocket.client.WebSocketTransport;
import org.eclipse.jetty.client.HttpClient;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer.PortalContainerPostInitTask;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */

public class EXoContinuationCometdServlet extends CometDServlet {
  /**
    * 
    */
  private static final long     serialVersionUID   = 9204910608302112814L;
  /**
   * Logger.
   */
  private static final Log      LOG                = ExoLogger.getLogger(CometDServlet.class);

  /**
   * The portal container
   */
  private ExoContainer          container;

  private OortConfigServlet     oConfig;

  private SetiServlet           setiServlet;

  private boolean clusterEnabled = false;
  
  public static final String    PREFIX             = "exo.cometd.";

  protected static final String CLOUD_ID_SEPARATOR = "cloudIDSeparator";
  
  public static String OORT_CONFIG_TYPE = PREFIX + "oort.configType";
  public static String OORT_MULTICAST = "multicast";
  public static String OORT_STATIC = "static";

  public static String[]        configs            = { "transports", "allowedTransports", "jsonContext", "validateMessageFields", "broadcastToPublisher", "timeout", "interval",
      "maxInterval", "maxLazyTimeout", "metaConnectDeliverOnly", "maxQueue", "maxSessionsPerBrowser", "allowMultiSessionsNoBrowser", "multiSessionInterval", "browserCookieName",
      "browserCookieDomain", "browserCookiePath", "ws.cometdURLMapping", "ws.messagesPerFrame", "ws.bufferSize", "ws.maxMessageSize", "ws.idleTimeout" };

  /**
   * {@inheritDoc}
   */
  public void init(final ServletConfig config) throws ServletException {
    final PortalContainerPostInitTask task = new PortalContainerPostInitTask() {

      public void execute(ServletContext context, PortalContainer portalContainer) {
        EXoContinuationCometdServlet.this.container = portalContainer;
        try {
          EXoContinuationCometdServlet.super.init(config);

          String profiles = PropertyManager.getProperty("exo.profiles");
          if (profiles != null) {
            clusterEnabled = profiles.contains("cluster");
          }
          
          String configType = getInitParameter(OORT_CONFIG_TYPE);
          if (OORT_STATIC.equals(configType)) {
            oConfig = new OortStaticConfig();
          } else {
            oConfig = new OortMulticastConfig();
          }
          oConfig.init(new ServletConfig() {

            @Override
            public String getServletName() {
              return config.getServletName();
            }

            @Override
            public ServletContext getServletContext() {
              return config.getServletContext();
            }

            @Override
            public Enumeration getInitParameterNames() {
              return config.getInitParameterNames();
            }

            @Override
            public String getInitParameter(String name) {
              return EXoContinuationCometdServlet.this.getInitParameter(name);
            }
          });

          setiServlet = new SetiServlet();
          setiServlet.init(config);

          ServletContext cometdContext = config.getServletContext();
          Seti seti = (Seti) cometdContext.getAttribute(Seti.SETI_ATTRIBUTE);
          Oort oort = (Oort) cometdContext.getAttribute(Oort.OORT_ATTRIBUTE);

          EXoContinuationBayeux bayeux = (EXoContinuationBayeux) getBayeux();
          bayeux.setSeti(seti);
          bayeux.setOort(oort);

          String separator = getInitParameter(CLOUD_ID_SEPARATOR);
          if (separator != null) {
            bayeux.setCloudIDSeparator(separator);
          }
        } catch (ServletException e) {
          LOG.error("Cannot initialize Bayeux", e);
        }
      }
    };
    PortalContainer.addInitTask(config.getServletContext(), task);
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
                                                                                  IOException {
    if (container == null || container.getComponentInstanceOfType(BayeuxServer.class) == null) {
      final AsyncContext ac = request.startAsync(request, response);      
      ac.start(new Runnable() {
        
        @Override
        public void run() {
          while (container == null || container.getComponentInstanceOfType(BayeuxServer.class) == null) {
            try {
              Thread.sleep(5000);
            } catch (InterruptedException e) {
              LOG.error(e);
            }
          }
          
          try {
            EXoContinuationCometdServlet.this.service(ac.getRequest(), ac.getResponse());
          } catch (Exception e) {
            LOG.error(e);
          }
        }
      });
    } else {
      super.service(request, response);      
    }
  }

  /**
   * {@inheritDoc}
   */
  protected EXoContinuationBayeux newBayeuxServer() {
    try {
      if (LOG.isDebugEnabled())
        LOG.debug("EXoContinuationCometdServlet - Current Container-ExoContainer: " + container);
      EXoContinuationBayeux bayeux = (EXoContinuationBayeux) container.getComponentInstanceOfType(BayeuxServer.class);
      bayeux.setTimeout(Long.parseLong(getInitParameter("timeout")));
      if (LOG.isDebugEnabled())
        LOG.debug("EXoContinuationCometdServlet - -->AbstractBayeux=" + bayeux);
      return bayeux;
    } catch (NumberFormatException e) {
      LOG.error("Error new Bayeux creation ", e);
      return null;
    }
  }

  @Override
  public String getInitParameter(String name) {
    String value = PropertyManager.getProperty(PREFIX + name);
    if (value == null) {
      value = super.getInitParameter(name);
    }
    return value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Enumeration getInitParameterNames() {
    Set<String> names = new HashSet<String>();
    names.addAll(Collections.list(super.getInitParameterNames()));
    names.addAll(Arrays.asList(configs));

    return Collections.enumeration(names);
  }

  @Override
  public void destroy() {
    setiServlet.destroy();
    oConfig.destroy();
    super.destroy();
  }
  
  private Oort configTransports(Oort oort) {
    ServletConfig config = getServletConfig();
    String transport = config.getInitParameter("transports");
    if (transport == null || !transport.contains(WebSocketTransport.class.getName())) {
      oort.getClientTransportFactories().add(new LongPollingTransport.Factory(new HttpClient()));
    }
    return oort;
  }

  /**
   * This class help to workaround issue with eap 6.2 that has not support
   * Websocket transport yet
   */
  public class OortStaticConfig extends OortStaticConfigServlet {
    private static final long serialVersionUID = 1054209695244836363L;

    @Override
    protected void configureCloud(ServletConfig config, Oort oort) throws Exception {
      if (clusterEnabled) {
        super.configureCloud(config, oort);        
      }
    }

    @Override
    protected Oort newOort(BayeuxServer bayeux, String url) {
      Oort oort = super.newOort(bayeux, url);
      return configTransports(oort);
    }
  }
  
  public class OortMulticastConfig extends OortMulticastConfigServlet {
    private static final long serialVersionUID = 6836833932474627776L;

    @Override
    protected void configureCloud(ServletConfig config, Oort oort) throws Exception {
      if (clusterEnabled) {
        super.configureCloud(config, oort);        
      }
    }
    
    @Override
    protected Oort newOort(BayeuxServer bayeux, String url) {
      Oort oort = super.newOort(bayeux, url);
      return configTransports(oort);
    }
  }
}

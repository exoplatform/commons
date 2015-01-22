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
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

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
import org.exoplatform.commons.utils.PrivilegedSystemHelper;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer.PortalContainerPostInitTask;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.ws.frameworks.cometd.ServletContextWrapper;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */

public class EXoContinuationCometdServlet extends CometDServlet {

  private static final long     serialVersionUID   = 9204910608302112814L;
  
  private static final Log      LOG                = ExoLogger.getLogger(CometDServlet.class);

  private OortConfigServlet     oConfig;

  private SetiServlet           setiConfig;
  
  private ExoContainer container;
  
  private boolean initialized;

  private boolean clusterEnabled = false;  
  
  private static EXoContinuationCometdServlet instance;
  
  private ServletConfig originConfig;

  public static final String    PREFIX             = "exo.cometd.";

  protected static final String CLOUD_ID_SEPARATOR = PREFIX + "cloudIDSeparator";
  
  public static String OORT_CONFIG_TYPE = PREFIX + "oort.configType";
  public static String OORT_MULTICAST = "multicast";
  public static String OORT_STATIC = "static";

  public static final Pattern URL_REGEX;
  static {
    String ip_regex = "(((((25[0-5])|(2[0-4][0-9])|([01]?[0-9]?[0-9]))\\.){3}((25[0-4])|(2[0-4][0-9])|((1?[1-9]?[1-9])|([1-9]0))))|(0\\.){3}0)";
    URL_REGEX = Pattern.compile("^((ht|f)tp(s?)://)" // protocol
                                + "(\\w+(:\\w+)?@)?" // username:password@
                                + "(" + ip_regex // ip
                                + "|([0-9a-z_!~*'()-]+\\.)*([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\.[a-z]{2,6}" // domain like www.exoplatform.org
                                + "|([a-zA-Z][-a-zA-Z0-9]+))" // domain like localhost
                                + "(:[0-9]{1,5})?" // port number :8080
                                + "((/?)|(/[0-9a-zA-Z_!~*'().;?:@&=+$,%#-]+)+/?)$"); // uri    
  }

  public void init(ServletConfig config) throws ServletException {
    originConfig = config;

    PortalContainerPostInitTask task = new PortalContainerPostInitTask() {
      public void execute(ServletContext context, PortalContainer portalContainer) {
          setContainer(portalContainer);
          init();
          initialized = true;
      }
    };
    PortalContainer.addInitTask(config.getServletContext(), task);
    instance = this;
  }
  
  public void reInit() {
    if (initialized) {
      init();
    }
  }

  public void init() {
    ServletConfig servletConfig = getServletConfig();

    try {
      super.init();

      String profiles = PropertyManager.getProperty("exo.profiles");
      if (profiles != null) {
        clusterEnabled = profiles.contains("cluster");
        if (clusterEnabled) {
          warnInvalidUrl(getInitParameter(OortConfigServlet.OORT_URL_PARAM));
        }
      }

      String configType = getInitParameter(OORT_CONFIG_TYPE);
      if (OORT_STATIC.equals(configType)) {
        oConfig = new OortStaticConfig();
      } else {
        oConfig = new OortMulticastConfig();
      }
      oConfig.init(servletConfig);

      setiConfig = new SetiServlet();
      setiConfig.init(servletConfig);

      ServletContext cometdContext = servletConfig.getServletContext();
      Seti seti = (Seti) cometdContext.getAttribute(Seti.SETI_ATTRIBUTE);
      Oort oort = (Oort) cometdContext.getAttribute(Oort.OORT_ATTRIBUTE);

      EXoContinuationBayeux bayeux = (EXoContinuationBayeux) getBayeux();
      bayeux.setSeti(seti);
      bayeux.setOort(oort);

      String separator = getInitParameter(CLOUD_ID_SEPARATOR);
      if (separator != null) {
        bayeux.setCloudIDSeparator(separator);
      }
    } catch (Exception e) {
      LOG.error("Cannot initialize Bayeux", e);
    }
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
                                                                                  IOException {
    if (getContainer() == null || getBayeux() == null) {
      final AsyncContext ac = request.startAsync(request, response);
      ac.start(new Runnable() {
        
        @Override
        public void run() {
          while (getContainer() == null || getBayeux() == null) {
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

  @Override
  public ServletConfig getServletConfig() {
    EXoContinuationBayeux bayeux = getBayeux();
    ServletConfig config = bayeux.getServletConfig();
    if (config == null) {
      config = new ServletConfigWrapper(originConfig);
      bayeux.setServletConfig(config);
    }
    return config;
  }

  public void setContainer(ExoContainer container) {
    this.container = container;
  }

  private ExoContainer getContainer() {
    return container;
  }
  
  /**
   * {@inheritDoc}
   */
  protected EXoContinuationBayeux newBayeuxServer() {
    return getBayeux();
  }

  public EXoContinuationBayeux getBayeux() {
    try {
      if (LOG.isDebugEnabled())
        LOG.debug("EXoContinuationCometdServlet - Current Container-ExoContainer: " + getContainer());
      EXoContinuationBayeux bayeux = (EXoContinuationBayeux) getContainer().getComponentInstanceOfType(BayeuxServer.class);
      if (LOG.isDebugEnabled())
        LOG.debug("EXoContinuationCometdServlet - -->AbstractBayeux=" + bayeux);
      return bayeux;
    } catch (Exception e) {
      LOG.error("Error new Bayeux creation ", e);
      return null;
    }
  }
  
  public static EXoContinuationCometdServlet getInstance() {
    return instance;
  }

  @Override
  public void destroy() {
    //destroy procedure should be done in BayeuxServer eXo kernel disposable lifecycle method
  }
  
  private Oort configTransports(Oort oort) {
    ServletConfig config = getServletConfig();
    String transport = config.getInitParameter("transports");
    if (transport == null || !transport.contains(WebSocketTransport.class.getName())) {
      oort.getClientTransportFactories().add(new LongPollingTransport.Factory(new HttpClient()));
    }
    return oort;
  }
  
  private void warnInvalidUrl(String url) {
    if (url == null || url.isEmpty()) {
      LOG.warn("You didn’t set exo.cometd.oort.url, cometd cannot work in cluster mode without this property, please set it.");
    } else if (!URL_REGEX.matcher(url).matches()) {
      LOG.warn("exo.cometd.oort.url is invalid {}, cometd cannot work in cluster mode without this property, please set it.", url);
    }
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
  
  private class ServletConfigWrapper implements ServletConfig {
    private ServletConfig delegate;
    private ServletContext contextWrapper;
    private String[] configs;

    public ServletConfigWrapper(ServletConfig config) {
      this.delegate = config;
      contextWrapper = new ServletContextWrapper(delegate.getServletContext());
    }

    @Override
    public String getInitParameter(String name) {
      String value = PropertyManager.getProperty(PREFIX + name);
      if (value == null) {
        value = delegate.getInitParameter(name);
      }
      return value;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
      if (configs == null) {
        List<String> keys = new LinkedList<String>();
        Properties props = PrivilegedSystemHelper.getProperties();
        int len = PREFIX.length();

        for (Object key : props.keySet()) {
          String k = key.toString().trim();
          if (k.startsWith(PREFIX) && k.length() > len) {
            keys.add(k.substring(len));
          }
        }

        configs = keys.toArray(new String[keys.size()]);
      }
      Set<String> names = new HashSet<String>();
      names.addAll(Collections.list(delegate.getInitParameterNames()));
      names.addAll(Arrays.asList(configs));

      return Collections.enumeration(names);
    }

    @Override
    public ServletContext getServletContext() {
      return contextWrapper;
    }

    @Override
    public String getServletName() {
      return delegate.getServletName();
    }    
  } 
}

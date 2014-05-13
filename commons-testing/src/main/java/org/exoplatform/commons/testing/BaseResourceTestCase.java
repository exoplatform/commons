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
package org.exoplatform.commons.testing;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.ContainerResponseWriter;
import org.exoplatform.services.rest.impl.ApplicationContextImpl;
import org.exoplatform.services.rest.impl.ContainerRequest;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.EnvironmentContext;
import org.exoplatform.services.rest.impl.InputHeadersMap;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.rest.impl.ProviderBinder;
import org.exoplatform.services.rest.impl.RequestHandlerImpl;
import org.exoplatform.services.rest.impl.ResourceBinder;
import org.exoplatform.services.rest.tools.DummyContainerResponseWriter;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
//import org.exoplatform.services.test.mock.MockHttpServletRequest;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 23, 2014  
 */
//@ConfiguredBy({
//  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/test-configuration.xml")
//  })
public class BaseResourceTestCase extends BaseExoTestCase {
  protected static Log log = ExoLogger.getLogger(BaseResourceTestCase.class.getName());
  protected SessionProvider sessionProvider;
  protected ProviderBinder providerBinder;
  protected ResourceBinder resourceBinder;
  protected RequestHandlerImpl requestHandler;
  
  protected final String         REPO_NAME      = "repository";
  protected final String         WORKSPACE_NAME = "portal-test";
  protected PortalContainer      container;
  protected RepositoryService    repositoryService;
  protected ConfigurationManager configurationManager;
  protected Session              session;
  protected Node                 root;
  
  public void setUp() throws Exception {
    super.setUp();
    container = PortalContainer.getInstance();
    repositoryService = getService(RepositoryService.class);
    configurationManager = getService(ConfigurationManager.class);

    session = repositoryService.getRepository(REPO_NAME).getSystemSession(WORKSPACE_NAME);
    root = session.getRootNode();
    
    resourceBinder = getService(ResourceBinder.class);
    requestHandler = getService(RequestHandlerImpl.class);
    // Reset providers to be sure it is clean
    ProviderBinder.setInstance(new ProviderBinder());
    providerBinder = ProviderBinder.getInstance();
    ApplicationContextImpl.setCurrent(new ApplicationContextImpl(null, null, providerBinder));
    resourceBinder.clear();
    begin();    
  }
  
  public void tearDown() throws Exception {
    end();
    super.tearDown();
  }
  
  
  /**
   * Get response with provided writer
   * @param method
   * @param requestURI
   * @param baseURI
   * @param headers
   * @param data
   * @param writer
   * @return
   * @throws Exception
   */
  public ContainerResponse service(String method,
                                   String requestURI,
                                   String baseURI,
                                   Map<String, List<String>> headers,
                                   byte[] data,
                                   ContainerResponseWriter writer) throws Exception{

    if (headers == null) {
      headers = new MultivaluedMapImpl();
    }

    ByteArrayInputStream in = null;
    if (data != null) {
      in = new ByteArrayInputStream(data);
    }

    EnvironmentContext envctx = new EnvironmentContext();
//  HttpServletRequest httpRequest = new MockHttpServletRequest("", in, in != null ? in.available() : 0, method, headers);
//    envctx.put(HttpServletRequest.class, httpRequest);
    EnvironmentContext.setCurrent(envctx);
    ContainerRequest request = new ContainerRequest(method,
                                                    new URI(requestURI),
                                                    new URI(baseURI),
                                                    in,
                                                    new InputHeadersMap(headers));
    ContainerResponse response = new ContainerResponse(writer);
    requestHandler.handleRequest(request, response);
    return response;
  }

  /**
   * Get response without provided writer
   * @param method
   * @param requestURI
   * @param baseURI
   * @param headers
   * @param data
   * @return
   * @throws Exception
   */
  public ContainerResponse service(String method,
                                   String requestURI,
                                   String baseURI,
                                   MultivaluedMap<String, String> headers,
                                   byte[] data) throws Exception {
    return service(method, requestURI, baseURI, headers, data, new DummyContainerResponseWriter());
  }
  
  /**
   * Register supplied class as per-request root resource if it has valid
   * JAX-RS annotations and no one resource with the same UriPattern already
   * registered.
   * 
   * @param resourceClass class of candidate to be root resource
   * @param properties optional resource properties. It may contains additional
   *          info about resource, e.g. description of resource, its
   *          responsibility, etc. This info can be retrieved
   *          {@link org.exoplatform.services.rest.ObjectModel#getProperties()}.
   *          This parameter may be <code>null</code>
   */
  public void addResource(final Class<?> resourceClass, MultivaluedMap<String, String> properties) {
    this.resourceBinder.addResource(resourceClass, properties);
  }

  /**
   * Register supplied Object as singleton root resource if it has valid JAX-RS
   * annotations and no one resource with the same UriPattern already
   * registered.
   * 
   * @param resource candidate to be root resource
   * @param properties optional resource properties. It may contains additional
   *          info about resource, e.g. description of resource, its
   *          responsibility, etc. This info can be retrieved
   *          {@link org.exoplatform.services.rest.ObjectModel#getProperties()}.
   *          This parameter may be <code>null</code>
   */
  public void addResource(final Object resource, MultivaluedMap<String, String> properties) {
    this.resourceBinder.addResource(resource, properties);
  }

  /**
   * Remove the resource instance of provided class from root resource
   * container.
   * 
   * @param clazz the class of resource
   */
  public void removeResource(Class clazz) {
    this.resourceBinder.removeResource(clazz);
  }
  
  protected <T> T getService(Class<T> clazz) {
    return clazz.cast(container.getComponentInstanceOfType(clazz));
  }

}

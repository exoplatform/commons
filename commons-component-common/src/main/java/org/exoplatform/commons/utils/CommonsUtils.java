package org.exoplatform.commons.utils;

import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.definition.PortalContainerConfig;
import org.exoplatform.container.xml.PortalContainerInfo;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class CommonsUtils {
	
	private static final Log LOG = ExoLogger.getLogger(CommonsUtils.class.getName());

	public static final String CONFIGURED_TENANT_MASTER_HOST_KEY = "tenant.masterhost";
	public static final String CONFIGURED_DOMAIN_URL_KEY = "gatein.email.domain.url";

    /**
     * Gets the system session provider.
     *
     * @return the system session provider
     */
    public static SessionProvider getSystemSessionProvider() {
      SessionProviderService sessionProviderService = getService(SessionProviderService.class);
      SessionProvider sessionProvider = sessionProviderService.getSystemSessionProvider(null);
      return sessionProvider;
    }

    /**
     * Gets the session provider.
     *
     * @return the session provider
     */
    public static SessionProvider getUserSessionProvider() {
      SessionProviderService sessionProviderService = getService(SessionProviderService.class);
      SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
      return sessionProvider;
    }
    
    /**
     * Gets the service.
     *
     * @param clazz the clazz
     *
     * @return the service
     */
    public static <T> T getService(Class<T> clazz) {
      return getService(clazz, null);
    }
    
    /**
     * Gets the service.
     *
     * @param clazz the class
     * @param containerName the container's name
     *
     * @return the service
     */
    public static <T> T getService(Class<T> clazz, String containerName) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      if (containerName != null) {
        container = RootContainer.getInstance().getPortalContainer(containerName);
      }
      if (container.getComponentInstanceOfType(clazz)==null) {
        containerName = PortalContainer.getCurrentPortalContainerName();
        container = RootContainer.getInstance().getPortalContainer(containerName);
      }
      return clazz.cast(container.getComponentInstanceOfType(clazz));
    }
    
    /**
     * Get the current repository
     *
     * @return the current manageable repository
     */
    public static ManageableRepository getRepository() {
      try {
        RepositoryService repositoryService = getService(RepositoryService.class);
        return repositoryService.getCurrentRepository();
      } catch (Exception e) {
        if (LOG.isErrorEnabled()) {
          LOG.error("getRepository() failed because of ", e);
        }
      }
      return null;
    }   
    
    public static String getRestContextName() {
        ExoContainer container = ExoContainerContext.getCurrentContainer();
        PortalContainerConfig portalContainerConfig = (PortalContainerConfig) container.
            getComponentInstance(PortalContainerConfig.class);
        PortalContainerInfo containerInfo =
          (PortalContainerInfo)container.getComponentInstanceOfType(PortalContainerInfo.class) ;
        return portalContainerConfig.getRestContextName(containerInfo.getContainerName());
      }    

    public static boolean isFeatureActive(String featureName) {
      ExoFeatureService featureService = getService(ExoFeatureService.class);
      if (featureService == null) {
        return false;
      }
      return featureService.isActiveFeature(featureName);
    }

    /**
     * Get the current domain name by configuration
     * 
     * @return the current domain name.
     */
    public static String getCurrentDomain() {
      String sysDomain = System.getProperty(CONFIGURED_DOMAIN_URL_KEY);
      if (sysDomain == null || sysDomain.length() == 0) {
        throw new NullPointerException("Get the domain is unsuccessfully. Please, add configuration domain on configuration.properties file with key: " +
                                         CONFIGURED_DOMAIN_URL_KEY);
      }
      // multiple tenant
      String masterHost = System.getProperty(CONFIGURED_TENANT_MASTER_HOST_KEY);
      if (masterHost != null && masterHost.length() > 0) {
        String currentTenant = getRepository().getConfiguration().getName();
        return sysDomain.replace(masterHost, currentTenant + "." + masterHost);
      }
      //
      return sysDomain;
    }
    
    public static void startRequest(Object service)
    {
      if(service instanceof ComponentRequestLifecycle) {
        ((ComponentRequestLifecycle) service).startRequest(ExoContainerContext.getCurrentContainer());
      }
    }

    public static void endRequest(Object service) 
    {
      if(service instanceof ComponentRequestLifecycle) {
        ((ComponentRequestLifecycle) service).endRequest(ExoContainerContext.getCurrentContainer());
      }
    }
    
}

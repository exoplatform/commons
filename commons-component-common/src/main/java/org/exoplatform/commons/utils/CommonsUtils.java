package org.exoplatform.commons.utils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.definition.PortalContainerConfig;
import org.exoplatform.container.xml.PortalContainerInfo;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.services.security.StateKey;

public class CommonsUtils {
	
	private static final Log LOG = ExoLogger.getLogger(CommonsUtils.class.getName());

	public static final String CONFIGURED_TENANT_MASTER_HOST_KEY = "tenant.masterhost";
	public static final String CONFIGURED_DOMAIN_URL_KEY = "gatein.email.domain.url";

  public static OrganizationService getOrganizationService(){
    return (OrganizationService)ExoContainerContext.getCurrentContainer().getComponentInstance(OrganizationService.class) ;
  }

  public static ConversationRegistry getConversationRegistry() {
    return (ConversationRegistry) ExoContainerContext.getCurrentContainer().getComponentInstance(ConversationRegistry.class);
  }

  /**
   * Gets the user state enabled/disabled from ConversationRegistry, if not found,
   * get it from OrgnizationService
   * 
   * @param userId username
   * @return true if enabled, else false
   * @throws Exception if an error occured during requesting the user from OrganizationService
   */
  public static boolean isUserEnabled(String userId) throws Exception {
    if(IdentityConstants.ANONIM.equals(userId) || IdentityConstants.SYSTEM.equals(userId)) {
      return true;
    }
    ConversationState conversationState = getConversationState(userId);
    User user = conversationState == null ? null : (User) conversationState.getAttribute("UserProfile");
    if(user != null) {
      return user.isEnabled();
    }
    return getOrganizationService().getUserHandler().findUserByName(userId, UserStatus.ANY).isEnabled();
  }

  /**
   * Get the last added ConversationState of a given user.
   * 
   * @param userId username
   * @return ConversationState entity of user
   */
  public static ConversationState getConversationState(String userId) {
    ConversationRegistry conversationRegistry = getConversationRegistry();
    if(conversationRegistry == null) {
      return null;
    }
    List<StateKey> stateKeys = conversationRegistry.getStateKeys(userId);
    ConversationState conversationState = null;
    if(stateKeys != null && !stateKeys.isEmpty()) {
      // get last conversation state of connected user
      StateKey stateKey = stateKeys.get(stateKeys.size() - 1);
      conversationState = conversationRegistry.getState(stateKey);
    }
    return conversationState;
  }

  /**
   * Gets groups of user from ConversationRegistry, if not found,
   * get it from OrgnizationService
   * 
   * @param userId username
   * @return a collection of group Id of type String
   * @throws Exception if an error occured during requesting the user from OrganizationService
   */
  public static Collection<String> getGroupsOfUser(String userId) throws Exception {
    Collection<String> groupIDs = null;

    ConversationState conversationState = CommonsUtils.getConversationState(userId);
    Identity identity = conversationState == null ? null : conversationState.getIdentity();
    if(identity == null) {
      Collection<Group> groups = getOrganizationService().getGroupHandler().findGroupsOfUser(userId);
      groupIDs = groups.stream().map(Group::getId).collect(Collectors.toSet());
    } else {
      groupIDs = identity.getGroups();
    }
    return groupIDs;
  }

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

  /**
   * Get {@link SiteKey} of current site
   * @return currentSite if available or default site in otherwise
   */
  public static SiteKey getCurrentSite() {
      PortalRequestContext pContext = null;
      try {
        pContext = Util.getPortalRequestContext();
      } catch (NullPointerException e) {
        pContext = null;
      }
      if (pContext != null) {
        return pContext.getSiteKey();
      } else {
        UserPortalConfigService portalConfig = getService(UserPortalConfigService.class);
        return SiteKey.portal(portalConfig.getDefaultPortal());
      }
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

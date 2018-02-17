package org.exoplatform.commons.api.notification.plugin;

import junit.framework.TestCase;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.services.organization.impl.UserProfileImpl;
import org.exoplatform.services.resources.LocalePolicy;


/**
 * Created by eXo Platform SAS.
 *
 * @author Ali Hamdi <ahamdi@exoplatform.com>
 * @since 15/02/18 10:23
 */

public class NotificationPluginUtilsTest extends TestCase {

  public void testGetLanguage() throws Exception {
    OrganizationService organizationService = PortalContainer.getInstance().getComponentInstanceOfType(OrganizationService.class);
    String userName = "testUser";
    String langFR = "fr";
    String langFR_FR = "fr_FR";
    UserProfileHandler profileHandler = organizationService.getUserProfileHandler();
    UserProfile profile = new UserProfileImpl(userName);
    profile.setAttribute("user.language",langFR);
    profileHandler.saveUserProfile(profile,true);
    
    String language = NotificationPluginUtils.getLanguage(userName);
    assertEquals(langFR, language);

    profile.setAttribute("user.language",langFR_FR);
    profileHandler.saveUserProfile(profile,true);

    language = NotificationPluginUtils.getLanguage(userName);
    assertEquals(langFR_FR, language);
  }
}

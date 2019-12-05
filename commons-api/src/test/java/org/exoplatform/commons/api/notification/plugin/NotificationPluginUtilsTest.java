package org.exoplatform.commons.api.notification.plugin;

import junit.framework.TestCase;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.services.organization.impl.UserProfileImpl;
import org.mockito.Mockito;

import static org.exoplatform.commons.api.notification.plugin.NotificationPluginUtils.BRANDING_COMPANY_NAME_SETTING_KEY;


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

  public void testGetBrandingPortalName() throws Exception {
    SettingService settingService = Mockito.mock(SettingService.class);
    PortalContainer container = PortalContainer.getInstance();
    container.unregisterComponent(SettingService.class);
    container.registerComponentInstance(SettingService.class, settingService);

    String companyName = "ACME";
    SettingValue value = new SettingValue(companyName);
    Mockito.when(settingService.get(Context.GLOBAL, Scope.GLOBAL, BRANDING_COMPANY_NAME_SETTING_KEY)).thenReturn(value);
    
    // Make sure that method getBrandingPortalName returns the changed company name.
    assertEquals(companyName, NotificationPluginUtils.getBrandingPortalName());
  }
}

/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.api.notification.plugin;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.utils.MailUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.portal.localization.LocaleContextInfoUtils;
import org.exoplatform.services.resources.LocaleContextInfo;
import org.exoplatform.services.resources.LocalePolicy;

public class NotificationPluginUtils {

  public static final String BRANDING_PORTAL_NAME = "exo:brandingPortalName";
  
  public static final String BRANDING_COMPANY_NAME_SETTING_KEY = "exo.branding.company.name";

  public static String getPortalName() {
    return getExoContainerContext().getPortalContainerName();
  }

  public static String getFirstName(String userName) {
    startRequest(getOrganizationService());
    User user = null;
    try {
      UserHandler userHandler = getOrganizationService().getUserHandler();
      user = userHandler.findUserByName(userName);
      return user.getFirstName();
    } catch (Exception e) {
      return null;
    } finally {
      endRequest(getOrganizationService());
    }
  }
  
  private static ExoContainerContext getExoContainerContext() {
    return PortalContainer.getInstance().getComponentInstanceOfType(ExoContainerContext.class);
  }

  public static String getFullName(String userId) {
    startRequest(getOrganizationService());    
    try {
      User user = getOrganizationService().getUserHandler().findUserByName(userId);
      return getFullName(user);
    } catch (Exception e) {
      return null;
    } finally {
      endRequest(getOrganizationService());
    }
  }

  private static String getFullName(User user) {
    StringBuilder userInfor = new StringBuilder();
    String displayName = user.getDisplayName();
    if (displayName == null || displayName.length() == 0) {
      userInfor.append(user.getFirstName()).append(" ").append(user.getLastName());
    } else {
      userInfor.append(displayName);
    }
    return userInfor.toString();
  }

  private static String getEmailFormat(String userId) {
    startRequest(getOrganizationService());
    try {
      User user = getOrganizationService().getUserHandler().findUserByName(userId);
      StringBuilder userInfor = new StringBuilder(getFullName(user));
      userInfor.append("<").append(user.getEmail()).append(">");
      return userInfor.toString();
    } catch (Exception e) {
      return null;
    } finally {
      endRequest(getOrganizationService());
    }
  }

  public static String getFrom(String from) {
    if (from != null && from.length() > 0 && from.indexOf("@") > 0) {
      return from;
    }

    return new StringBuffer(MailUtils.getSenderName()).append("<").append(MailUtils.getSenderEmail()).append(">").toString();
  }

  /**
   * Get branding Portal Name 
   * @return
   */
  public static String getBrandingPortalName() {
    SettingValue<?> name = getSettingService().get(Context.GLOBAL, Scope.GLOBAL.id(null), BRANDING_PORTAL_NAME);
    if (name == null) {
      name = getSettingService().get(Context.GLOBAL, Scope.GLOBAL, BRANDING_COMPANY_NAME_SETTING_KEY);
    }
    return name != null && StringUtils.isNotBlank((CharSequence) name.getValue()) ? (String) name.getValue() : "eXo";
  }
  
  public static String getTo(String to) {
    if (to.indexOf("@") < 0) {
      return getEmailFormat(to);
    }
    return to;
  }

  /**
   * @param userId
   * @return
   */
  public static String getLanguage(String userId) {
    LocaleContextInfo localeCtx = LocaleContextInfoUtils.buildLocaleContextInfo(userId);
    LocalePolicy localePolicy = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(LocalePolicy.class);
    String lang = null;
    if(localePolicy != null) {
      Locale locale = localePolicy.determineLocale(localeCtx);
      lang = locale.toString();
    }
    return lang;
  }

  public static OrganizationService getOrganizationService() {
      return ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
  }
  
  public static SettingService getSettingService() {
    return ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SettingService.class);
  }

  private static void startRequest(Object service) {
    if (service instanceof ComponentRequestLifecycle) {
      RequestLifeCycle.begin((ComponentRequestLifecycle) service);
    }
  }

  private static void endRequest(Object service) {
    if (service instanceof ComponentRequestLifecycle) {
      RequestLifeCycle.end();
    }
  }
  
}

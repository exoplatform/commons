/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserProfile;

public class NotificationPluginUtils {
  public static final String DEFAULT_LANGUAGE = Locale.ENGLISH.getLanguage();

  private static OrganizationService organizationService;
  
  private static SettingService settingService;
  
  public static final String NOTIFICATION_SENDER_NAME  = "exo:notificationSenderName";
  
  public static final String NOTIFICATION_SENDER_EMAIL = "exo:notificationSenderEmail";
  
  public static final String BRANDING_PORTAL_NAME = "exo:brandingPortalName";

  public static String getPortalName() {
    return getExoContainerContext().getPortalContainerName();
  }

  public static String getDomain() {
    return System.getProperty("gatein.email.domain.url", "http://localhost:8080");
  }

  public static String getFirstName(String userName) {
    User user = null;
    try {
      UserHandler userHandler = getOrganizationService().getUserHandler();
      user = userHandler.findUserByName(userName);
      return user.getFirstName();
    } catch (Exception e) {
      return null;
    }
  }
  
  private static ExoContainerContext getExoContainerContext() {
    return (ExoContainerContext) PortalContainer.getInstance().getComponentInstanceOfType(ExoContainerContext.class);
  }

  public static String getProfileUrl(String userId) {
    StringBuffer footerLink = new StringBuffer(getDomain());
    return footerLink.append("/").append(getExoContainerContext().getRestContextName())
            .append("/").append("social/notifications/redirectUrl/notification_settings")
            .append("/").append(userId).toString();
  }

  public static String getFullName(String userId) {
    try {
      User user = getOrganizationService().getUserHandler().findUserByName(userId);
      return getFullName(user);
    } catch (Exception e) {
      return null;
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
    try {
      User user = getOrganizationService().getUserHandler().findUserByName(userId);
      StringBuilder userInfor = new StringBuilder(getFullName(user));
      userInfor.append("<").append(user.getEmail()).append(">");
      return userInfor.toString();
    } catch (Exception e) {
      return null;
    }
  }

  public static String getFrom(String from) {
    if (from != null && from.length() > 0 && from.indexOf("@") > 0) {
      return from;
    }

    return new StringBuffer(getSenderName()).append("<").append(getEmailFrom()).append(">").toString();
  }

  public static String getEmailFrom() {
    SettingValue<?> mail = getSettingService().get(Context.GLOBAL, Scope.GLOBAL, NOTIFICATION_SENDER_EMAIL);
    return mail != null ? (String) mail.getValue() : System.getProperty("gatein.email.smtp.from", "noreply@exoplatform.com");
  }

  public static String getSenderName() {
    SettingValue<?> name = getSettingService().get(Context.GLOBAL, Scope.GLOBAL, NOTIFICATION_SENDER_NAME);
    return name != null ? (String) name.getValue() : System.getProperty("exo.notifications.portalname", "eXo");
  }
  
  /**
   * Get branding Portal Name 
   * @return
   */
  public static String getBrandingPortalName() {
    SettingValue<?> name = getSettingService().get(Context.GLOBAL, Scope.GLOBAL, BRANDING_PORTAL_NAME);
    return name != null ? (String) name.getValue() : "eXo";
  }
  
  public static String getPortalHome(String portalName) {
    StringBuffer portalLink = new StringBuffer(getDomain());
    portalLink.append("/")
              .append(getExoContainerContext().getRestContextName())
              .append("/")
              .append("social/notifications/redirectUrl/portal_home")
              .append("/")
              .append(portalName);
    
    return "<a target=\"_blank\" style=\"text-decoration: none; font-weight: bold; color: #2F5E92; \" href=\"" + portalLink.toString() + "\">" + portalName + "</a>";
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
    try {
      UserProfile profile = getOrganizationService().getUserProfileHandler().findUserProfileByName(userId);
      String lang = profile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[8]);
      return (lang != null && lang.trim().length() > 0) ? lang : DEFAULT_LANGUAGE;
    } catch (Exception e) {
      return DEFAULT_LANGUAGE;
    }
  }

  public static OrganizationService getOrganizationService() {
    if (organizationService == null) {
      organizationService = (OrganizationService) PortalContainer.getInstance()
            .getComponentInstanceOfType(OrganizationService.class);
    }
    return organizationService;
  }
  
  public static SettingService getSettingService() {
    if (settingService == null) {
      settingService = (SettingService) PortalContainer.getInstance()
            .getComponentInstanceOfType(SettingService.class);
    }
    return settingService;
  }
  
}

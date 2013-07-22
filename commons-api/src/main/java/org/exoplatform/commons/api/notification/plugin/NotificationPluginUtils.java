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

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserProfile;

public class NotificationPluginUtils {
  public static final String         DEFAULT_LANGUAGE = Locale.ENGLISH.getLanguage();

  private static OrganizationService organizationService;

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
            .append("/").append("social/notifications/redirectUrl/settings")
            .append("/").append(userId).toString();
  }

  private static String getEmailFormat(String userId) {
    try {
      User user = getOrganizationService().getUserHandler().findUserByName(userId);
      StringBuilder userInfor = new StringBuilder();
      String displayName = user.getDisplayName();
      if (displayName == null || displayName.length() == 0) {
        userInfor.append(user.getFirstName()).append(" ").append(user.getLastName());
      } else {
        userInfor.append(displayName);
      }
      userInfor.append("<").append(user.getEmail()).append(">");
      return userInfor.toString();
    } catch (Exception e) {
      return null;
    }
  }

  public static String getFrom(String from) {
    if (from != null && from.length() > 0 && from.indexOf("@") < 0) {
      from = getEmailFormat(from);
    }

    if (from == null || from.length() <= 0) {
      from = System.getProperty("gatein.email.smtp.from", "noreply@exoplatform.com");
      String senderName = System.getProperty("exo.notifications.portalname", "eXo");
      from = new StringBuffer(senderName).append("<").append(from).append(">").toString();
    }
    return from;
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
      return (profile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[8]) != null) ? profile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[8]) : DEFAULT_LANGUAGE;
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
}

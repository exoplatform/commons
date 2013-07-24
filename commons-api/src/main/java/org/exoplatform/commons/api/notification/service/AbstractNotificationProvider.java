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
package org.exoplatform.commons.api.notification.service;

import java.util.List;
import java.util.Locale;

import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationMessage;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserProfile;

public abstract class AbstractNotificationProvider extends BaseComponentPlugin {
  protected OrganizationService organizationService;
  protected TemplateGenerator templateGenerator;
  
  public static final String DEFAULT_LANGUAGE = Locale.ENGLISH.getLanguage();
  
  protected OrganizationService getOrganizationService() {
    if (organizationService == null) {
      organizationService = (OrganizationService) PortalContainer.getInstance().getComponentInstanceOfType(OrganizationService.class);
    }
    return organizationService;
  }

  protected String getPortalName() {
    ExoContainerContext eXoContext = (ExoContainerContext) PortalContainer.getInstance()
                                                              .getComponentInstanceOfType(ExoContainerContext.class);
    return eXoContext.getPortalContainerName();
  }
  
  protected String getDomain() {
    return System.getProperty("gatein.email.domain.url", "http://localhost:8080");
  }
  
  protected String getFirstName(String userName) {
    User user = null;
    try {
      UserHandler userHandler = getOrganizationService().getUserHandler();
      user = userHandler.findUserByName(userName);
      return user.getFirstName();
    } catch (Exception e) {
      return null;
    }
  }

  protected String getProfileUrl(String userId) {
    StringBuffer footerLink = new StringBuffer(getDomain());
    ExoContainerContext context = (ExoContainerContext) PortalContainer.getInstance()
                                                           .getComponentInstanceOfType(ExoContainerContext.class);
    return footerLink.append("/").append(context.getRestContextName())
                     .append("/").append("social/notifications/redirectUrl/settings")
                     .append("/").append(userId).toString();
  }
  
  private String getEmailFormat(String userId) {
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
  
  protected String getFrom(NotificationMessage message) {
    String from = message.getFrom();
    if (from != null && from.length() > 0 && from.indexOf("@") < 0) {
      from = getEmailFormat(from);
    }
    
    if(from == null || from.length() <= 0) {
      from = System.getProperty("gatein.email.smtp.from", "noreply@exoplatform.com");
      String senderName = System.getProperty("exo.notifications.portalname", "eXo");
      from = new StringBuffer(senderName).append("<").append(from).append(">").toString();
    }
    return from;
  }

  protected String getTo(NotificationMessage message) {
    String to = message.getTo();
    if (to.indexOf("@") < 0) {
      return getEmailFormat(to);
    }
    return to;
  }

  protected String getLanguage(NotificationMessage message) {
    String to = message.getTo();
    try {
      UserProfile profile = getOrganizationService().getUserProfileHandler().findUserProfileByName(to);
      return profile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[8]);
    } catch (Exception e) {
      return DEFAULT_LANGUAGE;
    }
  }
  
  public abstract List<String> getSupportType();
  public abstract MessageInfo buildMessageInfo(NotificationMessage message);
  public abstract String buildDigestMessageInfo(List<NotificationMessage> messages);
}

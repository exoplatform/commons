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
import java.util.Map;

import org.exoplatform.commons.api.notification.MessageInfo;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.ProviderData;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;

public abstract class AbstractNotificationProvider extends BaseComponentPlugin {
  protected OrganizationService organizationService;
  
  private static final String DEFAULT_LANGUAGE = "English";
  
  protected OrganizationService getOrganizationService() {
    if (organizationService == null) {
      organizationService = (OrganizationService) PortalContainer.getInstance().getComponentInstanceOfType(OrganizationService.class);
    }
    return organizationService;
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
    
    if(from == null || from.length() < 0) {
      from = System.getProperty("gatein.email.smtp.from");
    }
    return from;
  }

  protected String getTo(NotificationMessage message) {
    String to = message.getSendToUserIds().get(0);
    if (to.indexOf("@") < 0) {
      return getEmailFormat(to);
    }
    return to;
  }

  protected String getLanguage(NotificationMessage message) {
    String to = message.getSendToUserIds().get(0);
    try {
      UserProfile profile = getOrganizationService().getUserProfileHandler().findUserProfileByName(to);
      return profile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[8]);
    } catch (Exception e) {
      return DEFAULT_LANGUAGE;
    }
  }
  
  private String getValue(Map<String, String> maps, String language) {
    String value = "";
    if (language != null) {
      value = maps.get(language);
    }
    if ((value == null || value.length() == 0) && maps.size() > 0) {
      value = maps.values().iterator().next();
    }
    return value;
  }

  protected String getTemplate(ProviderData provider, String language) {
    return getValue(provider.getTemplates(), language);
  }

  protected String getSubject(ProviderData provider, String language) {
    return getValue(provider.getSubjects(), language);
  }
  
  public abstract List<String> getSupportType();
  public abstract MessageInfo buildMessageInfo(NotificationMessage message);
  public abstract String buildDigestMessageInfo(List<NotificationMessage> messages);
}

/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.commons.api.notification.plugin;

import java.io.Writer;
import java.util.Locale;

import org.exoplatform.commons.api.notification.MessageInfo;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;

public abstract class AbstractNotificationPlugin {
  
  public static final String DEFAULT_LANGUAGE = Locale.ENGLISH.getLanguage();
  
  private OrganizationService organizationService;
  
  /**
   * Start the plug in
   * @param context
   * @return
   */
  public void start(NotificationContext ctx) {
    
  }
  
  /**
   * End the plug in
   * @param context
   * @return
   */
  public void end(NotificationContext ctx) {
    
  }
  
  /**
   * Gets Notification Plug in key
   * @return
   */
  public abstract String getId();
  
  /**
   * Makes MessageInfo from given information what keep inside NotificationContext
   * @param context
   * @return
   */
  public abstract NotificationMessage makeNotification(NotificationContext ctx);
  
  /**
   * Makes the MessageInfor from given NotificationMessage what keep inside NotificationContext
   * @param context
   * @return
   */
  public abstract MessageInfo makeMessage(NotificationContext ctx);
  
  /**
   * Makes the Digest message from given NotificationMessage what keep inside NotificationContext
   * @param ctx
   * @param wtiter
   * @return
   */
  public abstract boolean makeDigest(NotificationContext ctx, Writer writer);
  
  /**
   * Makes notification
   * @param ctx
   * @return
   */
  protected NotificationMessage buildNotification(NotificationContext ctx) {
    return makeNotification(ctx);
  }
  
  /**
   * Makes massage
   * @param ctx
   * @return
   */
  protected MessageInfo buildMessage(NotificationContext ctx) {
    return makeMessage(ctx);
  }
  
  /**
   * Makes digest message
   * @param ctx
   * @param writer
   * @return
   */
  protected boolean buildDigest(NotificationContext ctx, Writer writer) {
    return makeDigest(ctx, writer);
  }
  
  /**
   * Creates the key for NotificationPlugin
   * @return
   */
  public NotificationKey getKey() {
    return NotificationKey.key(this);
  }
  
  /**
   * 
   * @param message
   * @return
   */
  protected String getLanguage(NotificationMessage message) {
    String to = message.getTo();
    try {
      UserProfile profile = getOrganizationService().getUserProfileHandler().findUserProfileByName(to);
      return profile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[8]);
    } catch (Exception e) {
      return DEFAULT_LANGUAGE;
    }
  }
  
  protected OrganizationService getOrganizationService() {
    if (organizationService == null) {
      organizationService = (OrganizationService) PortalContainer.getInstance().getComponentInstanceOfType(OrganizationService.class);
    }
    return organizationService;
  }

}

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
package org.exoplatform.commons.api.notification.service.template;

import java.util.HashMap;
import java.util.Locale;

import org.exoplatform.commons.api.notification.model.UserSetting;

public class TemplateContext extends HashMap<String, Object> {
  
  private static final long serialVersionUID = 1L;

  private String            pluginId;

  private String            channelId        = UserSetting.EMAIL_CHANNEL;

  private String            language;

  private int               digestSize       = 0;
  
  private Throwable         error = null;

  public TemplateContext() {
    
  }

  public TemplateContext(String pluginId, String language) {
    this.channelId = UserSetting.EMAIL_CHANNEL;
    this.pluginId = pluginId;
    this.language = language;
  }

  public static TemplateContext newChannelInstance(String channelId, String pluginId, String language) {
    TemplateContext context = new TemplateContext(pluginId, language);
    context.channelId = channelId;
    return context;
  }
  
  /**
   * Holds the exception if have any on transform processing.
   * @param exception
   */
  public void setException(Throwable exception) {
    this.error = exception;
  }
  
  public Throwable getException() {
    return this.error;
  }

  public TemplateContext digestType(int digestSize) {
    this.digestSize = digestSize;
    return this;
  }

  public TemplateContext channelId(String channelId) {
    this.channelId = channelId;
    return this;
  }

  public TemplateContext pluginId(String pluginId) {
    this.pluginId = pluginId;
    return this;
  }

  public TemplateContext language(String language) {
    this.language = language;
    return this;
  }

  /**
   * @return the TemplateContext
   */
  public TemplateContext end() {
    return this;
  }

  /**
   * @return the pluginId
   */
  public String getPluginId() {
    return pluginId;
  }

  /**
   * @return the channelId
   */
  public String getChannelId() {
    return channelId;
  }

  /**
   * @return the language
   */
  public String getLanguage() {
    if (this.language == null) {
      return Locale.ENGLISH.getLanguage();
    }
    
    return language;
  }

  /**
   * @return the digestSize
   */
  public int getDigestSize() {
    return this.digestSize;
  }
    
}
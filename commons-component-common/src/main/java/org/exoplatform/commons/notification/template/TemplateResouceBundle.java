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
package org.exoplatform.commons.notification.template;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.notification.NotificationUtils;

public class TemplateResouceBundle {
  private String language;
  private String resouceLocal;

  public TemplateResouceBundle(String language, String resouceLocal) {
    this.language = language;
    this.resouceLocal = resouceLocal;
  }

  /**
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * @param language the language to set
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * @return the resouceLocal
   */
  public String getResouceLocal() {
    return resouceLocal;
  }

  /**
   * @param resouceLocal the resouceLocal to set
   */
  public void setResouceLocal(String resouceLocal) {
    this.resouceLocal = resouceLocal;
  }
  
  public String appRes(String key){
    Locale locale = Locale.ENGLISH;
    if(language != null && language.length() > 0) {
      locale = new Locale(language);
    }
    return NotificationUtils.getResourceBundle(key,  locale,  resouceLocal) ;
  }

  public String appRes(String key, String... strs) {
    String value = appRes(key);
    if (strs != null && strs.length > 0) {
      for (int i = 0; i < strs.length; ++i) {
        value = StringUtils.replace(value, "{" + i + "}", strs[i]);
      }
    }
    return value;
  }

}

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
package org.exoplatform.commons.api.notification;

import java.util.HashMap;

public class TemplateContext extends HashMap<String, Object> {
  private static final long serialVersionUID = 1L;

  private String            providerId;

  private String            language;

  private int               digestSize       = 0;

  public TemplateContext() {
  }

  public TemplateContext(String providerId, String language) {
    this.providerId = providerId;
    this.language = language;
  }

  public TemplateContext digestType(int digestSize) {
    this.digestSize = digestSize;
    return this;
  }

  public TemplateContext provider(String providerId) {
    this.providerId = providerId;
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
   * @return the providerId
   */
  public String getProviderId() {
    return providerId;
  }

  /**
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * @return the digestSize
   */
  public int getDigestSize() {
    return digestSize;
  }
}

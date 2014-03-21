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
package org.exoplatform.commons.notification.cache;

public class ElementCacheKey extends ScopeCacheKey {
  private static final long serialVersionUID = 1L;

  private final String      pluinId;

  private final String      language;

  public ElementCacheKey(String key) {
    this(null, key);
  }

  public ElementCacheKey(String pluginId, String language) {
    this.pluinId = pluginId;
    this.language = language;
  }
  
  public String getLanguage() {
    return language;
  }
  
  public String getPlugId() {
    return pluinId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof ElementCacheKey))
      return false;
    if (!super.equals(o))
      return false;

    ElementCacheKey that = (ElementCacheKey) o;

    if (pluinId != null ? !pluinId.equals(that.pluinId) : that.pluinId != null)
      return false;
    if (language != null ? !language.equals(that.language) : that.language != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (language != null ? language.hashCode() : 0);
    result = 31 * result + (pluinId != null ? pluinId.hashCode() : 0);
    return result;
  }

}

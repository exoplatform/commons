/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.commons.api.indexing.data;

/**
 * The class defines standard entry
 * @LevelAPI Experimental  
 */
public class StandardEntry extends SimpleEntry{
  private static final String CREATION_DATE = "creationDate";
  private static final String CREATION_AUTHOR = "creationAuthor";
  private static final String LAST_UPDATE_DATE = "lastUpdateDate";
  private static final String LAST_UPDATE_AUTHOR = "lastUpdateAuthor";
  
  /**
   * Get creation date
   * @return Long
   * @LevelAPI Experimental
   */
  public long getCreationDate() {
    return (Long)content.get(CREATION_DATE);
  }

  /**
   * Set creation Date
   * @param creationDate
   * @LevelAPI Experimental
   */
  public void setCreationDate(long creationDate) {
    content.put(CREATION_DATE, creationDate);
  }

  /**
   * Get creation author
   * @return String
   * @LevelAPI Experimental
   */
  public String getCreationAuthor() {
    return (String)content.get(CREATION_AUTHOR);
  }

  /**
   * Set creation author
   * @param creationAuthor
   * @LevelAPI Experimental
   */
  public void setCreationAuthor(String creationAuthor) {
    content.put(CREATION_AUTHOR, creationAuthor);
  }

  /**
   * Get last update date
   * @return long
   * @LevelAPI Experimental
   */
  public long getLastUpdateDate() {
    return (Long)content.get(LAST_UPDATE_DATE);
  }

  /**
   * Set last update date
   * @param lastUpdateDate
   * @LevelAPI Experimental
   */
  public void setLastUpdateDate(long lastUpdateDate) {
    content.put(LAST_UPDATE_DATE, lastUpdateDate);
  }

  /**
   * Get last update author
   * @return String
   * @LevelAPI Experimental
   */
  public String getLastUpdateAuthor() {
    return (String)content.get(LAST_UPDATE_AUTHOR);
  }

  /**
   * Set last update author
   * @param lastUpdateAuthor
   * @LevelAPI Experimental
   */
  public void setLastUpdateAuthor(String lastUpdateAuthor) {
    content.put(LAST_UPDATE_AUTHOR, lastUpdateAuthor);
  }
  
}

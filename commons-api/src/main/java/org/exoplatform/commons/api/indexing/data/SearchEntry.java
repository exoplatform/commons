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

import java.util.HashMap;
import java.util.Map;


/**
 * The class defines data structure of entry that is as input for indexing
 * @LevelAPI Experimental  
 */
public class SearchEntry {
  protected SearchEntryId id;
  protected Map<String, Object> content;

  /**
   * Default constructor 
   * @LevelAPI Experimental
   */
  public SearchEntry(){
    this.content = new HashMap<String, Object>();
  }
  /**
   * Constructor creates a entry with specified parameters
   * @param collection
   * @param type
   * @param name
   * @param content
   * @LevelAPI Experimental
   */
  public SearchEntry(String collection, String type, String name, Map<String, Object> content){
    this.id = new SearchEntryId(collection, type, name);
    this.content = content;
  }
  /**
   * Get entry id
   * @return SearchEntryId
   * @LevelAPI Experimental
   */
  public SearchEntryId getId() {
    return id;
  }

  /**
   * Set entry id
   * @param id
   * @LevelAPI Experimental
   */
  public void setId(SearchEntryId id) {
    this.id = id;
  }

  /**
   * Get content
   * @return map
   * @LevelAPI Experimental
   */
  public Map<String, Object> getContent() {
    return content;
  }

  /**
   * Set content
   * @param content
   * @LevelAPI Experimental
   */
  public void setContent(Map<String, Object> content) {
    this.content = content;
  }

  @Override
  public String toString() {
    return id + ": " + content;
  }

}

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
package org.exoplatform.commons.search;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Nov 21, 2012  
 */
public class SearchEntry {
  protected SearchEntryId id;
  protected Map<String, Object> content;

  public SearchEntry(){
    this.content = new HashMap<String, Object>();
  }
  
  public SearchEntry(String collection, String type, String name, Map<String, Object> content){
    this.id = new SearchEntryId(collection, type, name);
    this.content = content;
  }
  
  public SearchEntryId getId() {
    return id;
  }

  public void setId(SearchEntryId id) {
    this.id = id;
  }

  public Map<String, Object> getContent() {
    return content;
  }

  public void setContent(Map<String, Object> content) {
    this.content = content;
  }

  @Override
  public String toString() {
    return id + ": " + content;
  }

}

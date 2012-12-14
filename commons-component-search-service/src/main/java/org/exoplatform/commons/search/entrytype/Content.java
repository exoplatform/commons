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
package org.exoplatform.commons.search.entrytype;

import org.exoplatform.commons.search.SearchEntry;
import org.exoplatform.commons.search.SimpleEntry;

/**
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Nov 21, 2012  
 */
public class Content extends SimpleEntry {
  //private static final Collection<String> JCR_TYPES = Arrays.asList("exo:webContent", "nt:file", "nt:resource");
//  private static final Map<String, Map<String, String>> JCR_TYPES = new HashMap<String, Map<String, String>>(){{
//    put("exo:webContent", new HashMap<String, String>());
//    put("nt:file", null);
//  }};
  
  public Content(SearchEntry entry) {
    this.setId(entry.getId());
    this.setContent(entry.getContent());
  }
    
}

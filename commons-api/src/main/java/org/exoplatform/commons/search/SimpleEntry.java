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


/**
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Nov 21, 2012  
 */
public class SimpleEntry extends SearchEntry{
  private static final String TITLE = "title";
  private static final String EXCERPT = "excerpt";
  private static final String URL = "url";
  
  public String getTitle() {
    return (String)content.get(TITLE);
  }

  public void setTitle(String title) {
    content.put(TITLE, title);
  }

  public String getExcerpt() {
    return (String)content.get(EXCERPT);
  }

  public void setExcerpt(String excerpt) {
    content.put(EXCERPT, excerpt);
  }
  
  public String getUrl() {
    return (String)content.get(URL);
  }

  public void setUrl(String url) {
    content.put(URL, url);
  }
}

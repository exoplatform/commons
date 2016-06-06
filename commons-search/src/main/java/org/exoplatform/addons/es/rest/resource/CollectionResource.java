/* 
* Copyright (C) 2003-2015 eXo Platform SAS.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see http://www.gnu.org/licenses/ .
*/
package org.exoplatform.addons.es.rest.resource;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 12/4/15
 */
public class CollectionResource<T> implements Serializable {

  public final static int QUERY_LIMIT = 100;

  private int offset = 0;
  private int limit = QUERY_LIMIT;

  private Collection<T> resources;

  public CollectionResource(Collection<T> resources) {
    this.resources = resources;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public Collection<T> getResources() {
    return resources;
  }

  public void setResources(Collection<T> resources) {
    this.resources = resources;
  }
}


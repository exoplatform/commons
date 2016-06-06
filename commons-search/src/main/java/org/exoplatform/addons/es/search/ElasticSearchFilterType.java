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
package org.exoplatform.addons.es.search;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 11/27/15
 */
public enum  ElasticSearchFilterType {
  FILTER_BY_TERM("term"),
  FILTER_EXIST("exist"),
  FILTER_NOT_EXIST("notExist");

  private final String filterType;

  ElasticSearchFilterType(String filterType) {
    this.filterType = filterType;
  }

  @Override
  public String toString() {
    return filterType;
  }

}


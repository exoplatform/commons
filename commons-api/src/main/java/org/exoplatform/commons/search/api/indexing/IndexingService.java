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
package org.exoplatform.commons.search.api.indexing;

import java.util.Map;

import org.exoplatform.commons.search.api.indexing.data.SearchEntry;
import org.exoplatform.commons.search.api.indexing.data.SearchEntryId;

/**
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Nov 21, 2012  
 */
public abstract class IndexingService {
  protected static final String DATE_INDEXED = "se_dateIndexed";
  protected static final String LAST_UPDATE = "se_lastUpdate";

  public abstract void add(SearchEntry searchEntry);
  public abstract void update(SearchEntryId id, Map<String, Object> changes);
  public abstract void delete(SearchEntryId id);
}
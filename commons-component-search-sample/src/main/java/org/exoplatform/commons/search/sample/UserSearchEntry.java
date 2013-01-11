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
package org.exoplatform.commons.search.sample;

import org.exoplatform.commons.api.indexing.data.SearchEntryId;
import org.exoplatform.commons.api.indexing.data.SimpleEntry;
import org.exoplatform.services.organization.User;

/**
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Nov 21, 2012  
 */
public class UserSearchEntry extends SimpleEntry {
  private static final String ENTRY_COLLECTION = "organization";
  private static final String ENTRY_TYPE = "user";

  public UserSearchEntry(User user) {
    this.setId(getEntryId(user.getUserName()));
    this.setTitle(user.getFullName());
    this.setExcerpt(user.getUserName()+ " (" + user.getEmail() + ")");
  }

  public static SearchEntryId getEntryId(String userName) {
    return new SearchEntryId(ENTRY_COLLECTION, ENTRY_TYPE, userName);
  }

  @Override
  public String toString() {
    return this.getTitle() + " [" + this.getExcerpt() + "]";
  }

}

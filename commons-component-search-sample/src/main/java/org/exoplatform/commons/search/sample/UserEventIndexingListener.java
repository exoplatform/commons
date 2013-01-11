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

import org.exoplatform.commons.api.indexing.IndexingService;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Nov 21, 2012  
 */
public class UserEventIndexingListener extends UserEventListener {
  @Override
  public void postSave(User newUser, boolean isNew) throws Exception {
    super.postSave(newUser, isNew);
    IndexingService indexingService = (IndexingService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IndexingService.class);
    indexingService.add(new UserSearchEntry(newUser));
  }

  @Override
  public void postDelete(User user) throws Exception {
    super.postDelete(user);
    IndexingService indexingService = (IndexingService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IndexingService.class);
    indexingService.delete(UserSearchEntry.getEntryId(user.getUserName()));
  }
}

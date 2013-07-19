/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.commons.notification.impl.spi;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.command.NotificationCommand;
import org.exoplatform.commons.api.notification.command.NotificationExecutor;
import org.exoplatform.commons.api.notification.service.storage.NotificationDataStorage;
import org.exoplatform.commons.utils.CommonsUtils;

public class NotificationExecutorImpl implements NotificationExecutor {

  private final List<NotificationCommand> commands;
  
  public NotificationExecutorImpl() {
    commands = new ArrayList<NotificationCommand>();
  }
  
  private boolean process(NotificationContext ctx, NotificationCommand command) {
    NotificationDataStorage storage = CommonsUtils.getService(NotificationDataStorage.class);
    storage.add(create(ctx, command));
    return true;
  }

  private NotificationMessage create(NotificationContext ctx, NotificationCommand command) {
    return null;
  }

  @Override
  public boolean execute(NotificationContext ctx) {
    boolean result = true;
    //
    for(NotificationCommand command : commands) {
      result &= process(ctx, command);
    }
    
    //empty commands
    this.commands.clear();
    return result;
    
  }

  @Override
  public NotificationExecutor with(NotificationCommand command) {
    this.commands.add(command);
    return this;
  }

  @Override
  public NotificationExecutor with(List<NotificationCommand> commands) {
    this.commands.addAll(commands);
    return this;
  }
  
}

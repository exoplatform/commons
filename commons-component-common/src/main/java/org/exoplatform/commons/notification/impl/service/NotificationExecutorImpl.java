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
package org.exoplatform.commons.notification.impl.service;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.command.NotificationCommand;
import org.exoplatform.commons.api.notification.command.NotificationExecutor;
import org.exoplatform.commons.api.notification.model.NotificationMessage;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.utils.CommonsUtils;

public class NotificationExecutorImpl implements NotificationExecutor {

  private static NotificationExecutor executor;

  private final Queue<NotificationCommand>  commands;
  
  private NotificationExecutorImpl() {
    commands = new ConcurrentLinkedQueue<NotificationCommand>();
  }
  
  public static NotificationExecutor getInstance() {
    if (executor == null) {
      executor = new NotificationExecutorImpl();
    }
    return executor;
  }
  
  private boolean process(NotificationContext ctx, NotificationCommand command) {
    try {
      NotificationService service = CommonsUtils.getService(NotificationService.class);
      service.process(create(ctx, command));
      return true;
    } catch (Exception e) {
      ctx.setException(e);
      return false;
    }
  }

  private NotificationMessage create(NotificationContext ctx, NotificationCommand command) {
    return command.processNotification(ctx);
  }

  @Override
  public boolean execute(NotificationContext ctx) {
    boolean result = true;
    //
    while (commands.isEmpty() == false) {
      result &= process(ctx, commands.poll());
    }

    return result;
  }

  @Override
  public NotificationExecutor with(NotificationCommand command) {
    if (command != null) {
      this.commands.add(command);
    }
    return this;
  }

  @Override
  public NotificationExecutor with(List<NotificationCommand> commands) {
    this.commands.addAll(commands);
    return this;
  }
  
}

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
package org.exoplatform.commons.notification.impl.command;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.command.NotificationCommand;
import org.exoplatform.commons.api.notification.command.NotificationExecutor;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.service.NotificationCompletionService;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class NotificationExecutorImpl implements NotificationExecutor {
  
  private static final Log LOG = ExoLogger.getLogger(NotificationExecutorImpl.class);

  private static NotificationExecutor executor;

  private final List<NotificationCommand>  commands;
  
  //The executor to execute multiple threads
  private NotificationCompletionService completionService; 
  
  private NotificationService notificationService;
  
  public NotificationExecutorImpl() {
    commands = new CopyOnWriteArrayList<NotificationCommand>();
    notificationService = CommonsUtils.getService(NotificationService.class);
    completionService = CommonsUtils.getService(NotificationCompletionService.class);
  }
  
  public static NotificationExecutor getInstance() {
    if (executor == null) {
      executor = new NotificationExecutorImpl();
    }
    return executor;
  }
  
  private boolean process(final NotificationContext ctx, final NotificationCommand command) {
    try {
      if (command.getPlugin().isValid(ctx) == false) {
        return false;
      }
      Callable<Boolean> task = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
          try {
            notificationService.process(create(ctx, command));
          } catch (Exception e) {
            LOG.warn("Process NotificationInfo is failed: " + e.getMessage());
            LOG.debug(e.getMessage(), e);
            return false;
          }
          //
          return true;
        }
      };
      completionService.addTask(task);

      return true;
    } catch (Exception e) {
      ctx.setException(e);
      return false;
    }
  }

  private NotificationInfo create(NotificationContext ctx, NotificationCommand command) {
    return command.processNotification(ctx);
  }

  @Override
  public boolean execute(NotificationContext ctx) {
    boolean result = true;
    
    // Notification will not be executed when the feature is off
    if (CommonsUtils.isFeatureActive(NotificationUtils.FEATURE_NAME) == false) {
      commands.clear();
      return result;
    }

    //
    for(NotificationCommand command : commands) {
      result &= process(ctx, command);
      printLog(ctx);
    }
    
    commands.clear();

    return result;
  }
  
  private void printLog(NotificationContext ctx) {
    if (ctx.isFailed()) {
      LOG.error("Failed to process the notification.", ctx.getException());
    }
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

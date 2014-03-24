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
package org.exoplatform.commons.notification.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.command.NotificationCommand;
import org.exoplatform.commons.api.notification.command.NotificationExecutor;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.NotificationKey;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.api.notification.service.setting.PluginContainer;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.notification.impl.command.NotificationCommandImpl;
import org.exoplatform.commons.notification.impl.command.NotificationExecutorImpl;
import org.exoplatform.commons.notification.impl.setting.NotificationPluginContainer;
import org.exoplatform.commons.utils.CommonsUtils;

public final class NotificationContextImpl implements NotificationContext {
  
  private static final NotificationContext DEFAULT = new NotificationContextImpl();
  
  private Map<String, Object> arguments = new ConcurrentHashMap<String, Object>();
  
  private NotificationInfo notification;
  
  private List<NotificationInfo> notifications;
  
  private Exception exception;
  
  private final NotificationExecutor executor;
  
  private final NotificationPluginContainer pluginService;
  
  private final PluginSettingService settingService;

  private NotificationContextImpl() {
    //TODO apply static method for Notification
    //Create the pluginConttext for operation-per-session such as transaction 
    executor = new NotificationExecutorImpl();
    pluginService = CommonsUtils.getService(NotificationPluginContainer.class);
    settingService = CommonsUtils.getService(PluginSettingService.class);
  }

  public static NotificationContext cloneInstance() {
    return DEFAULT.clone();
  }

  @Override
  public NotificationExecutor getNotificationExecutor() {
    return this.executor;
  }
  
  @Override
  public PluginContainer getPluginContainer() {
    return this.pluginService;
  }
  
  public PluginSettingService getPluginSettingService() {
    return this.settingService;
  }
  
  @Override
  public <T> NotificationContext append(ArgumentLiteral<T> argument, Object value) {
    arguments.put(argument.getKey(), value);

    return this;
  }

  @Override
  public <T> NotificationContext remove(ArgumentLiteral<T> argument) {
    arguments.remove(argument);
    return this;
  }

  @Override
  public void clear() {
    arguments.clear();
  }

  @Override
  public <T> T value(ArgumentLiteral<T> argument) {
    Object value = arguments.get(argument.getKey());
    T got = null;
    
    try {
      got = argument.getType().cast(value);
    } catch (Exception e) {
      return null;
    }
    
    return got;
  }

  @Override
  public NotificationInfo getNotificationInfo() {
    return this.notification;
  }

  @Override
  public NotificationContext setNotificationInfo(NotificationInfo notification) {
    this.notification = notification;
    return this;
  }
  
  public Exception getException() {
    return exception;
  }

  public <T> T getException(Class<T> type) {
    if (type == exception.getClass()) {
      return type.cast(exception);
    }
    return null;
  }

  public void setException(Throwable t) {
    if (t == null) {
      this.exception = null;
    } else if (t instanceof Exception) {
      this.exception = (Exception) t;
    } else {
      this.exception = new Exception("Exception occurred during execution" ,t);
    }
  }

  @Override
  public boolean isFailed() {
    return getException() != null;
  }

  @Override
  public void setNotificationInfos(List<NotificationInfo> notifications) {
    this.notifications = notifications;
  }

  @Override
  public List<NotificationInfo> getNotificationInfos() {
    return this.notifications;
  }
  
  @Override
  public NotificationCommand makeCommand(NotificationKey key) {
    AbstractNotificationPlugin plugin = this.pluginService.getPlugin(key);
    return (plugin != null) ? new NotificationCommandImpl(plugin) : null;
  }

  @Override
  public NotificationContext clone() {
    return new NotificationContextImpl();
  }

}

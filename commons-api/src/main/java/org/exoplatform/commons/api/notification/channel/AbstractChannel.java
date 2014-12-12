/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.commons.api.notification.channel;

import org.exoplatform.commons.api.notification.lifecycle.AbstractNotificationLifecycle;
import org.exoplatform.container.component.BaseComponentPlugin;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 12, 2014  
 */
public abstract class AbstractChannel extends BaseComponentPlugin {
  /** Defines the lifecycle what will handle the notification each channel*/
  private final AbstractNotificationLifecycle lifecycle;
  
  public AbstractChannel(AbstractNotificationLifecycle lifecycle) {
    this.lifecycle = lifecycle;
  }
  
  /**
   * Gets ChannelId
   * @return
   */
  public abstract String getId();
  
  /**
   * Gets the lifecycle what assigned to the channel
   * @return
   */
  public AbstractNotificationLifecycle getLifecycle() {
    return this.lifecycle;
  }

  
}
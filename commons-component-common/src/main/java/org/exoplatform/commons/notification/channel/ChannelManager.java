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
package org.exoplatform.commons.notification.channel;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.lifecycle.AbstractNotificationLifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 12, 2014  
 */
public class ChannelManager {
  
  /** Defines the channels: key = channelId and Channel*/
  private final Map<String, AbstractChannel> channels;
  
  public ChannelManager() {
    channels = new HashMap<String, AbstractChannel>();
  }
  
  /**
   * Register new channel
   * @param channel
   */
  public void register(AbstractChannel channel) {
    channels.put(channel.getId(), channel);
  }
  
  /**
   * Unregister the specified channel
   * @param channel
   */
  public void unregister(AbstractChannel channel) {
    channels.remove(channel.getId());
  }
  
  public AbstractChannel getChannel(String channelId) {
    return channels.get(channelId);
  }
  
  public AbstractNotificationLifecycle getLifecycle(String channelId) {
    return getChannel(channelId).getLifecycle();
  }
  
  /**
   * Gets size of channels has been registered
   * @return
   */
  public int sizeChannels() {
    return channels.size();
  }

}

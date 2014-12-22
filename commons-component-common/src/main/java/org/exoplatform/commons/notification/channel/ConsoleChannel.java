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

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.notification.lifecycle.SimpleLifecycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 12, 2014  
 */
public class ConsoleChannel extends AbstractChannel {
  /** logger */
  private static final Log LOG = ExoLogger.getLogger(ConsoleChannel.class);
  /** */
  private final static String ID = "CONSOLE_CHANNEL";
  /** */
  private final ChannelKey key = ChannelKey.key(ID);
  
  public ConsoleChannel() {
    super(new SimpleLifecycle());
  }
  
  @Override
  public String getId() {
    return ID;
  }
  
  @Override
  public ChannelKey getKey() {
    return key;
  }
  
  @Override
  public void dispatch(NotificationContext ctx, String userId) {
    LOG.info(String.format("CONSOLE:: %s will be received the message from pluginId: %s",
                           userId,
                           ctx.getNotificationInfo().getKey().getId()));
  }
  
  @Override
  public void registerTemplateProvider(TemplateProvider provider) {}
}

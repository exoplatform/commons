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

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.annotation.ChannelConfig;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.lifecycle.MailLifecycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 12, 2014  
 */
@ChannelConfig (
  lifecycle = MailLifecycle.class
)
public final class MailChannel extends AbstractChannel {
  /** logger */
  private static final Log LOG = ExoLogger.getLogger(MailChannel.class);
  /** */
  public final static String ID = "MAIL_CHANNEL";
  /** */
  private final ChannelKey key = ChannelKey.key(ID);
  /** */
  private final Map<PluginKey, String> templateFilePaths = new HashMap<PluginKey, String>();
  /** */
  private final Map<PluginKey, AbstractTemplateBuilder> templateBuilders = new HashMap<PluginKey, AbstractTemplateBuilder>();

  public MailChannel() {
    super(new MailLifecycle());
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
  public void registerTemplateProvider(TemplateProvider provider) {
    this.templateFilePaths.putAll(provider.getTemplateFilePathConfigs());
    this.templateBuilders.putAll(provider.getTemplateBuilder());
  }
  
  @Override
  public void dispatch(NotificationContext ctx, String userId) {
    // TODO call MailSendService to send mail to receipts
    String pluginId = ctx.getNotificationInfo().getKey().getId();
    String templateFilePath = this.templateFilePaths.get(pluginId);
    LOG.info("Mail::{ userId:" + userId + ", pluginId: " + pluginId + ", templateFilePath: "+ templateFilePath + "}");
  }
  
  @Override
  public String getTemplateFilePath(PluginKey key) {
    return this.templateFilePaths.get(key);
  }
  
  @Override
  public boolean hasTemplateBuilder(PluginKey key) {
    AbstractTemplateBuilder builder = this.templateBuilders.get(key);
    return builder != null;
  }
  
  @Override
  public AbstractTemplateBuilder getTemplateBuilder(PluginKey key) {
    return this.templateBuilders.get(key);
  }
  
}

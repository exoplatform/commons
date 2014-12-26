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

import groovy.text.GStringTemplateEngine;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.lifecycle.AbstractNotificationLifecycle;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 12, 2014  
 */
public class ChannelManagerImpl implements ChannelManager, Startable {
  /** logger */
  private static final Log LOG = ExoLogger.getLogger(ChannelManagerImpl.class);
  /** Defines the channels: key = channelId and Channel*/
  private final Map<ChannelKey, AbstractChannel> channels;
  private List<TemplateProvider> providers;
  private GStringTemplateEngine gTemplateEngine;
  public ChannelManagerImpl() {
    channels = new HashMap<ChannelKey, AbstractChannel>();
    providers = new LinkedList<TemplateProvider>();
    gTemplateEngine = new GStringTemplateEngine();
  }
  
  /**
   * Register new channel
   * @param channel
   */
  @Override
  public void register(AbstractChannel channel) {
    channels.put(ChannelKey.key(channel.getId()), channel);
  }

  /**
   * Unregister the specified channel
   * 
   * @param channel
   */
  @Override
  public void unregister(AbstractChannel channel) {
    channels.remove(channel.getId());
  }

  /**
   * Register the template provider
   * 
   * @param provider
   */
  @Override
  public void registerTemplateProvider(TemplateProvider provider) {
    providers.add(provider);
  }
  
  /**
   * Register and override the template provider
   * 
   * @param provider
   */
  @Override
  public void registerOverrideTemplateProvider(TemplateProvider provider) {
    providers.add(provider);
    AbstractChannel channel = channels.get(provider.getChannelKey());
    if (channel != null) {
      channel.registerTemplateProvider(addTemplateEngine(provider));
    } else {
      LOG.warn("Register the new TemplateProvider is unsucessful");
    }
  }

  @Override
  public AbstractChannel getChannel(ChannelKey key) {
    return channels.get(key);
  }
  
  @Override
  public AbstractNotificationLifecycle getLifecycle(ChannelKey key) {
    return getChannel(key).getLifecycle();
  }

  /**
   * Gets size of channels has been registered
   * 
   * @return
   */
  @Override
  public int sizeChannels() {
    return channels.size();
  }

  @Override
  public List<AbstractChannel> getChannels() {
    List<AbstractChannel> channels = new LinkedList<AbstractChannel>();
    AbstractChannel emailChannel = getChannel(ChannelKey.key(MailChannel.ID));
    if (emailChannel != null) {
      channels.add(emailChannel);
    }
    for (AbstractChannel channel : this.channels.values()) {
      if (MailChannel.ID.equals(channel.getId())) {
        continue;
      }
      channels.add(channel);
    }
    return Collections.unmodifiableList(channels);
  }

  @Override
  public void start() {
    for (TemplateProvider provider : providers) {
      AbstractChannel channel = channels.get(provider.getChannelKey());
      if (channel != null) {
        channel.registerTemplateProvider(addTemplateEngine(provider));
      } else {
        LOG.warn("Register the new TemplateProvider is unsucessful");
      }
    }
  }

  @Override
  public void stop() {
  }

  /**
   * Makes the template file to Groovy template.
   * It's ready to generate the message.
   * 
   * Why needs to do this way?
   * - TemplateBuilder and TemplateProvider can be extensible or override. So only final list to make the template.
   * - Don't waste time to build the template for useless template. 
   
   * @param provider
   * @return
   */
  private TemplateProvider addTemplateEngine(TemplateProvider provider) {
    Map<PluginKey, AbstractTemplateBuilder> builders = provider.getTemplateBuilder();
    Map<PluginKey, String> configs = provider.getTemplateFilePathConfigs();
    for (PluginKey plugin : configs.keySet()) {
      String templatePath = configs.get(plugin);
      if (templatePath != null && templatePath.length() > 0) {
        try {
          AbstractTemplateBuilder builder = builders.get(plugin);
          if (builder != null) {
            String template = TemplateUtils.loadGroovyTemplate(templatePath);
            builder.setTemplateEngine(gTemplateEngine.createTemplate(template));
          }
        } catch (Exception e) {
          LOG.warn("Failed to build groovy template engine for: " + plugin.getId() + " templatePath: " + templatePath, e);
        }
      }
    }
    return provider;
  }
}

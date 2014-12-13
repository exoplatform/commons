package org.exoplatform.commons.api.notification.channel;

import java.util.List;

import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.lifecycle.AbstractNotificationLifecycle;

public interface ChannelManager {

  /**
   * Register new channel
   * 
   * @param channel
   */
  void register(AbstractChannel channel);

  /**
   * Unregister the specified channel
   * 
   * @param channel
   */
  void unregister(AbstractChannel channel);

  /**
   * Register the new channel
   * 
   * @param provider
   */
  void registerTemplateProvider(TemplateProvider provider);

  /**
   * @param channelId
   * @return
   */
  AbstractChannel getChannel(String channelId);

  /**
   * @return
   */
  List<AbstractChannel> getChannels();

  /**
   * @param channelId
   * @return
   */
  AbstractNotificationLifecycle getLifecycle(String channelId);

  /**
   * Gets size of channels has been registered
   * 
   * @return
   */
  int sizeChannels();
}

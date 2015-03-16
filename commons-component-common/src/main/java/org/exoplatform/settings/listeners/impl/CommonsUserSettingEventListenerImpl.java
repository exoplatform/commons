package org.exoplatform.settings.listeners.impl;

import java.util.List;

import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;

/**
 * This listener is called when an user was enabled/disabled from the system
 * and update his setting as active/inactive
 *
 */
public class CommonsUserSettingEventListenerImpl extends UserEventListener {

  private static Log LOG = ExoLogger.getLogger(CommonsUserSettingEventListenerImpl.class);
  
  @Override
  public void postSetEnabled(User user) throws Exception {
    UserSettingService userSettingService = CommonsUtils.getService(UserSettingService.class);
    try {
      UserSetting userSetting = userSettingService.get(user.getUserName());
      if (!user.isEnabled()) {
        List<AbstractChannel> chanels = CommonsUtils.getService(ChannelManager.class).getChannels();
        for (AbstractChannel channel : chanels) {
          userSetting.removeChannelActive(channel.getId());
        }
        userSettingService.save(userSetting);
      }
    } catch (Exception e) {
      LOG.warn("Failed to update user's setting : ", e);
    }
  }
}

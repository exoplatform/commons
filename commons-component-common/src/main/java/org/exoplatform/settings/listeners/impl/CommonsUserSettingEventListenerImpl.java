package org.exoplatform.settings.listeners.impl;

import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.data.Context;
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
      userSettingService.setUserEnabled(user.getUserName(), user.isEnabled());
    } catch (Exception e) {
      LOG.warn("Failed to update user's 'enable' setting : ", e);
    }
  }

  @Override
  public void postDelete(User user) throws Exception {
    SettingService settingService = CommonsUtils.getService(SettingService.class);
    try {
      settingService.remove(Context.USER.id(user.getUserName()));
    } catch (Exception e) {
      LOG.warn("Failed to delete settings of user : " + user.getUserName(), e);
    }
  }
}

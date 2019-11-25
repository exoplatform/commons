/*
 *
 *  * Copyright (C) 2003-2017 eXo Platform SAS.
 *  *
 *  * This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License
 *  as published by the Free Software Foundation; either version 3
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see<http://www.gnu.org/licenses/>.
 *
 */

package org.exoplatform.settings.jpa;

import static org.exoplatform.commons.api.settings.data.Context.USER;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.model.PluginInfo;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.persistence.DataInitializer;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.job.NotificationJob;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;

public class JPAUserSettingServiceImpl extends AbstractService implements UserSettingService {
  private static final Log      LOG                = ExoLogger.getLogger(JPAUserSettingServiceImpl.class);

  /** Setting Scope on Common Setting **/
  public static final Scope     NOTIFICATION_SCOPE = Scope.APPLICATION.id("NOTIFICATION");

  public static final String   NAME_PATTERN       = "exo:{CHANNELID}Channel";

  private SettingService        settingService;

  private ChannelManager        channelManager;

  private PluginSettingService  pluginSettingService;

  private UserSetting           defaultSetting;

  /**
   * JPAUserSettingServiceImpl must depend on DataInitializer to make sure data
   * structure is created before initializing it
   */
  public JPAUserSettingServiceImpl(SettingService settingService,
                                   ChannelManager channelManager,
                                   PluginSettingService pluginSettingService,
                                   DataInitializer dataInitializer) {
    this.settingService = settingService;
    this.channelManager = channelManager;
    this.pluginSettingService = pluginSettingService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void save(UserSetting model) {
    String userId = model.getUserId();
    String dailys = NotificationUtils.listToString(model.getDailyPlugins(), VALUE_PATTERN);
    String weeklys = NotificationUtils.listToString(model.getWeeklyPlugins(), VALUE_PATTERN);
    String channelActives = NotificationUtils.listToString(model.getChannelActives(), VALUE_PATTERN);

    // Notification scope

    // Save plugins active
    Set<String> channels = model.getAllChannelPlugins().keySet();
    for (String channelId : channels) {
      saveUserSetting(userId,
                      NOTIFICATION_SCOPE,
                      getChannelProperty(channelId),
                      NotificationUtils.listToString(model.getPlugins(channelId), VALUE_PATTERN));
    }
    saveUserSetting(userId, NOTIFICATION_SCOPE, EXO_DAILY, dailys);
    saveUserSetting(userId, NOTIFICATION_SCOPE, EXO_WEEKLY, weeklys);
    saveUserSetting(userId, NOTIFICATION_SCOPE, EXO_IS_ACTIVE, channelActives);
    if (model.getLastReadDate() > 0) {
      saveLastReadDate(userId, model.getLastReadDate());
    }

    // Global scope
    saveUserSetting(userId, Scope.GLOBAL, EXO_IS_ENABLED, "" + model.isEnabled());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setUserEnabled(String username, boolean enabled) {
    saveUserSetting(username, Scope.GLOBAL, EXO_IS_ENABLED, "" + enabled);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public UserSetting get(String userId) {
    UserSetting model = getDefaultSettings();
    model.setUserId(userId);

    Map<Scope, Map<String, SettingValue<String>>> userNotificationSettings = settingService.getSettingsByContext(USER.id(userId));
    if (userNotificationSettings == null || userNotificationSettings.isEmpty()) {
      return model;
    }
    List<AbstractChannel> channels = channelManager.getChannels();
    Map<String, AbstractChannel> channelsByPropertyName = channels.stream()
                                                                  .collect(Collectors.toMap(channel -> getChannelProperty(channel.getId()),
                                                                                            Function.identity()));

    // Global Settings
    if (userNotificationSettings.containsKey(Scope.GLOBAL)
        && userNotificationSettings.get(Scope.GLOBAL).containsKey(EXO_IS_ENABLED)) {
      SettingValue<String> enabledSetting = userNotificationSettings.get(Scope.GLOBAL).get(EXO_IS_ENABLED);
      model.setEnabled(enabledSetting == null ? true : Boolean.valueOf(enabledSetting.getValue()));
    } else {
      model.setEnabled(true);
    }
    if (userNotificationSettings.containsKey(NOTIFICATION_SCOPE)) {
      boolean cleared = false;
      // Notification settings
      Map<String, SettingValue<String>> notificationSettings = userNotificationSettings.get(NOTIFICATION_SCOPE);
      for (Map.Entry<String, SettingValue<String>> setting : notificationSettings.entrySet()) {
        String key = setting.getKey();
        String value = setting.getValue().getValue();
        if (StringUtils.isBlank(value)) {
          continue;
        }
        if (EXO_IS_ACTIVE.equals(key)) {
          cleared = clearDefaultValue(model, cleared);
          model.setChannelActives(getArrayListValue(value, model.getChannelActives()));
        } else if (EXO_LAST_READ_DATE.equals(key)) {
          model.setLastReadDate((Long) Long.parseLong((String) value));
        } else if (EXO_DAILY.equals(key)) {
          cleared = clearDefaultValue(model, cleared);
          model.setDailyPlugins(getArrayListValue(value, model.getDailyPlugins()));
        } else if (EXO_WEEKLY.equals(key)) {
          cleared = clearDefaultValue(model, cleared);
          model.setWeeklyPlugins(getArrayListValue(value, model.getWeeklyPlugins()));
        } else if (channelsByPropertyName.containsKey(key)) {
          cleared = clearDefaultValue(model, cleared);
          AbstractChannel channel = channelsByPropertyName.get(key);
          model.setChannelPlugins(channel.getId(), getArrayListValue(value, new ArrayList<>()));
        } else if (PropertyManager.isDevelopping()) {
          LOG.warn("A setting was found for user {}, but not considered", userId);
        } else {
          LOG.debug("A setting was found for user {}, but not considered", userId);
        }
      }
    }
    return model;
  }

  private boolean clearDefaultValue(UserSetting model, boolean cleared) {
    if (!cleared) {
      model.getAllChannelPlugins().clear();
      model.getChannelActives().clear();
      model.getDailyPlugins().clear();
      model.getWeeklyPlugins().clear();
      cleared = true;
    }
    return cleared;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void initDefaultSettings(String userName) {
    try {
      fillDefaultSettingsOfUser(userName);
    } catch (Exception e) {
      LOG.error("Failed to init default settings for user " + userName, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void initDefaultSettings(User[] users) {
    for (User user : users) {
      String userName = user.getUserName();
      try {
        fillDefaultSettingsOfUser(userName);
      } catch (Exception e) {
        LOG.error("Failed to init default settings for user " + userName, e);
      }
    }
  }

  @Override
  public UserSetting getDefaultSettings() {
    if (defaultSetting == null) {
      defaultSetting = UserSetting.getInstance();
      List<String> activeChannels = getDefaultSettingActiveChannels();
      if (activeChannels.size() > 0) {
        defaultSetting.getChannelActives().addAll(activeChannels);
      } else {
        for (AbstractChannel channel : channelManager.getChannels()) {
          defaultSetting.setChannelActive(channel.getId());
        }
      }
      //
      List<PluginInfo> plugins = pluginSettingService.getAllPlugins();
      for (PluginInfo pluginInfo : plugins) {
        for (String defaultConf : pluginInfo.getDefaultConfig()) {
          for (String channelId : pluginInfo.getAllChannelActive()) {
            if (UserSetting.FREQUENCY.getFrequecy(defaultConf) == UserSetting.FREQUENCY.INSTANTLY) {
              defaultSetting.addChannelPlugin(channelId, pluginInfo.getType());
            } else {
              defaultSetting.addPlugin(pluginInfo.getType(), UserSetting.FREQUENCY.getFrequecy(defaultConf));
            }
          }
        }
      }
    }
    return defaultSetting.clone();
  }

  private List<String> getDefaultSettingActiveChannels() {
    String activeChannels = System.getProperty("exo.notification.channels", "");
    return activeChannels.isEmpty() ? new ArrayList<String>() : Arrays.asList(activeChannels.split(","));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<UserSetting> getDigestSettingForAllUser(NotificationContext notificationContext, int offset, int limit) {
    List<UserSetting> models = new ArrayList<UserSetting>();
    Boolean isWeekly = notificationContext.value(NotificationJob.JOB_WEEKLY);
    String frequency = EXO_DAILY;
    if (isWeekly) {
      frequency = EXO_WEEKLY;
    }

    try {
      boolean continueSearching = true;
      while (models.size() < limit && continueSearching) {
        List<Context> contexts = settingService.getContextsByTypeAndScopeAndSettingName(Context.USER.getName(),
                                                                                        NOTIFICATION_SCOPE.getName(),
                                                                                        NOTIFICATION_SCOPE.getId(),
                                                                                        frequency,
                                                                                        offset,
                                                                                        limit);
        continueSearching = contexts.size() == limit;
        for (Context context : contexts) {
          String username = context.getId();
          UserSetting userSetting = get(username);
          if (userSetting.isEnabled()) {
            models.add(userSetting);
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to get all " + frequency + " users have notification messages", e);
    }
    return models;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<UserSetting> getDigestDefaultSettingForAllUser(int offset, int limit) {
    List<UserSetting> users = new ArrayList<UserSetting>();
    try {
      // Get all users not having EXO_DAILY setting stored in DB.
      // Not having this setting assumes that users uses default settings
      // and haven't changed their notification settings.
      Set<String> userNames = settingService.getEmptyContextsByTypeAndScopeAndSettingName(Context.USER.getName(),
                                                                                   NOTIFICATION_SCOPE.getName(),
                                                                                   NOTIFICATION_SCOPE.getId(),
                                                                                   EXO_DAILY,
                                                                                   offset,
                                                                                   limit);
      for (String userName : userNames) {
        users.add(new UserSetting().setUserId(userName).setLastUpdateTime(Calendar.getInstance()));
      }
    } catch (Exception e) {
      LOG.error("Failed to get default daily users have notification messages", e);
    }
    return users;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void saveLastReadDate(String userId, Long time) {
    settingService.set(USER.id(userId), NOTIFICATION_SCOPE, EXO_LAST_READ_DATE, SettingValue.create(time));
  }

  private String getChannelProperty(String channelId) {
    return NAME_PATTERN.replace("{CHANNELID}", channelId);
  }

  private void saveUserSetting(String userId, Scope scope, String key, String value) {
    settingService.set(USER.id(userId), scope, key, SettingValue.create(value));
  }

  private List<String> getArrayListValue(String value, List<String> defaultValue) {
    if (StringUtils.isNotBlank(value) && !"false".equals(value)) {
      if ("true".equals(value)) {
        value = UserSetting.EMAIL_CHANNEL;
      }
      return NotificationUtils.stringToList(getValues(value));
    }
    return defaultValue;
  }

  private void fillDefaultSettingsOfUser(String username) throws Exception {
    settingService.save(Context.USER.id(username));
  }

}

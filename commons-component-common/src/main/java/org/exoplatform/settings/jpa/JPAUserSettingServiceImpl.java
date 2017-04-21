package org.exoplatform.settings.jpa;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.persistence.DataInitializer;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.notification.NotificationConfiguration;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.job.NotificationJob;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.UserImpl;
import org.exoplatform.settings.jpa.dao.SettingContextDAO;
import org.exoplatform.settings.jpa.dao.SettingScopeDAO;
import org.exoplatform.settings.jpa.dao.SettingsDAO;
import org.exoplatform.settings.jpa.entity.ContextEntity;
import org.exoplatform.settings.jpa.entity.SettingsEntity;

import java.util.*;

import static org.exoplatform.commons.api.settings.data.Context.USER;

/**
 * Created by exo on 4/6/17.
 */
public class JPAUserSettingServiceImpl extends AbstractService implements UserSettingService {
  private static final Log LOG = ExoLogger.getLogger(JPAUserSettingServiceImpl.class);

  /** Setting Scope on Common Setting **/
  private static final Scope NOTIFICATION_SCOPE = Scope.GLOBAL.id(null);


  public static final String NAME_PATTERN = "exo:{CHANNELID}Channel";
  private SettingsDAO settingsDAO;
  private SettingContextDAO settingContextDAO;
  private SettingScopeDAO settingScopeDAO;
  private SettingService        settingService;
  private ChannelManager        channelManager;

  /**
   * JPAUserSettingServiceImpl must depend on DataInitializer to make sure data structure is created before initializing it
   */
  public JPAUserSettingServiceImpl(SettingsDAO settingsDAO, SettingContextDAO settingContextDAO, SettingScopeDAO settingScopeDAO,
                                   SettingService settingService, NotificationConfiguration configuration,
                                   ChannelManager channelManager, DataInitializer dataInitializer) {

    this.settingService = settingService;
    this.channelManager = channelManager;
    this.settingsDAO = settingsDAO;
    this.settingContextDAO = settingContextDAO;
    this.settingScopeDAO = settingScopeDAO;
  }

  @Override
  public void save(UserSetting model) {

    String userId = model.getUserId();
    String dailys = NotificationUtils.listToString(model.getDailyPlugins(), VALUE_PATTERN);
    String weeklys = NotificationUtils.listToString(model.getWeeklyPlugins(), VALUE_PATTERN);
    String channelActives = NotificationUtils.listToString(model.getChannelActives(), VALUE_PATTERN);

    // Save plugins active
    List<String> channels = new ArrayList<String>(model.getAllChannelPlugins().keySet());
    for (String channelId : channels) {
      saveUserSetting(userId, getChannelProperty(channelId), NotificationUtils.listToString(model.getPlugins(channelId), VALUE_PATTERN));
    }
    //
    saveUserSetting(userId, EXO_DAILY, dailys);
    saveUserSetting(userId, EXO_WEEKLY, weeklys);
    //
    saveUserSetting(userId, EXO_IS_ACTIVE, channelActives);
    saveUserSetting(userId, EXO_IS_ENABLED, "" + model.isEnabled());
    //
    if (model.getLastReadDate() > 0) {
      saveLastReadDate(userId, model.getLastReadDate());
    }
    removeMixin(userId);
  }

  private String getChannelProperty(String channelId) {
    return NAME_PATTERN.replace("{CHANNELID}", channelId);
  }

  /**
   * Using the common setting service to store the data
   *
   * @param userId the userId
   * @param key the Setting key
   * @param value the Setting value
   */
  private void saveUserSetting(String userId, String key, String value) {
    settingService.set(USER.id(userId), NOTIFICATION_SCOPE, key, SettingValue.create(value));
  }

  @Override
  public UserSetting get(String userId) {
    UserSetting model = UserSetting.getInstance();

    List<String> actives = getArrayListValue(userId, EXO_IS_ACTIVE, null);
    if (actives != null) {
      model.setUserId(userId);
      model.setChannelActives(actives);
      // for all channel to set plugin
      List<AbstractChannel> channels = channelManager.getChannels();
      for (AbstractChannel channel : channels) {
        model.setChannelPlugins(channel.getId(), getArrayListValue(userId, getChannelProperty(channel.getId()), new ArrayList<String>()));
      }
      //
      model.setDailyPlugins(getArrayListValue(userId, EXO_DAILY, new ArrayList<String>()));
      model.setWeeklyPlugins(getArrayListValue(userId, EXO_WEEKLY, new ArrayList<String>()));
      //
    } else {
      model = UserSetting.getDefaultInstance().setUserId(userId);
      addMixin(userId);
    }
    SettingValue<?> value = getSettingValue(userId, EXO_LAST_READ_DATE);
    if (value != null) {
      if (value.getValue() instanceof Long) {
        model.setLastReadDate((Long) value.getValue());
      } else {
        model.setLastReadDate((Long) Long.parseLong((String)value.getValue()));
      }
    } else {
      saveLastReadDate(userId, 0l);
    }
    //
    SettingValue<String> isEnabled = getSettingValue(userId, EXO_IS_ENABLED);
    if (isEnabled != null) {
      model.setEnabled(Boolean.valueOf(isEnabled.getValue()));
    }
    return model;
  }

  @SuppressWarnings("unchecked")
  private SettingValue<String> getSettingValue(String userId, String propertyName) {
    return (SettingValue<String>) settingService.get(USER.id(userId), NOTIFICATION_SCOPE, propertyName);
  }

  private List<String> getArrayListValue(String userId, String propertyName, List<String> defaultValue) {
    SettingValue<String> values = getSettingValue(userId, propertyName);
    if (values != null) {
      String strs = values.getValue();
      if ("true".equals(strs)) {
        strs = UserSetting.EMAIL_CHANNEL;
      }
      if ("false".equals(strs)) {
        return defaultValue;
      }
      return NotificationUtils.stringToList(getValues(strs));
    }
    return defaultValue;
  }

  @Override
  public void addMixin(String userId) {
    addMixin(new User[] { new UserImpl(userId) });
  }

  @Override
  @ExoTransactional
  public void addMixin(User[] users) {
    try {
      fillDefaultSettingsOfUser(users);
    } catch (Exception e) {
      LOG.error("Failed to add mixin for default setting of users", e);
    }
  }


  private void removeMixin(String userId) {}

  @Override
  @ExoTransactional
  public List<UserSetting> getUserSettingWithDeactivate() {
    List<UserSetting> models = new ArrayList<UserSetting>();
    try {
      for (ContextEntity contextEntity : settingContextDAO.getContextsofType(USER.toString())) {
        models.add(fillModel(contextEntity.getName(), settingsDAO.getUserSettingsWithDeactivate(contextEntity.getName(),
            EXO_IS_ACTIVE, EXO_IS_ENABLED)));

      }
    } catch (Exception e) {
      LOG.warn("Can not get the user setting with deactivated");
    }
    return models;
  }

  @Override
  public List<String> getUserSettingByPlugin(String pluginId) {// only use for email channel
    return getUserHasSettingPlugin(UserSetting.EMAIL_CHANNEL, pluginId);
  }

  @Override
  @ExoTransactional
  public List<String> getUserHasSettingPlugin(String channelId, String pluginId) {
    List<String> userIds = new ArrayList<String>();
    try {
      String isActive="", plugins="", daily="", weekly="", endabled="";
      for (ContextEntity contextEntity : settingContextDAO.getContextsofType(USER.toString())) {
        for (SettingsEntity settingsEntity : settingsDAO.getSettingsByUser(contextEntity.getName())) {
          if (settingsEntity.getName().equals(EXO_IS_ACTIVE)) {
            isActive = settingsEntity.getValue();
          } else if (settingsEntity.getName().contains(getChannelProperty(channelId))) {
            plugins = settingsEntity.getValue();
          } else if (settingsEntity.getName().equals(EXO_DAILY)) {
            daily = settingsEntity.getValue();
          } else if (settingsEntity.getName().equals(EXO_WEEKLY)) {
            weekly = settingsEntity.getValue();
          } else if (settingsEntity.getName().equals(EXO_IS_ENABLED)) {
            endabled = settingsEntity.getValue();
          }
        }
        if (isActive.contains(channelId)) {
          if (UserSetting.EMAIL_CHANNEL.equals(channelId)
              && (plugins.contains(pluginId) || daily.contains(pluginId) || weekly.contains(pluginId))
              && (endabled.isEmpty() || endabled.equals("true"))) {
            userIds.add(contextEntity.getName());
          } else if (plugins.contains(pluginId) && (endabled.isEmpty() || endabled.equals("true"))) {
            userIds.add(contextEntity.getName());
          }
        }
        isActive=""; plugins=""; daily=""; weekly=""; endabled="";
      }
    } catch (Exception e) {
      LOG.error("Failed to get all users have the " + pluginId + " in settings", e);
    }
    return userIds;
  }

  /**
   * Gets these plugins what configured the daily
   *
   * @param context
   * @param offset
   * @param limit
   * @return
   * @throws Exception
   */
  private Map<String, List<SettingsEntity>> getDigestIterator(NotificationContext context, int offset, int limit) throws Exception {
    Map<String, List<SettingsEntity>> map = new HashMap<String, List<SettingsEntity>>();
    Boolean isWeekly = context.value(NotificationJob.JOB_WEEKLY);
    String frequency = EXO_DAILY;
    if (isWeekly) {
      frequency = EXO_WEEKLY;
    }
    boolean isEnabled, isActive, isFrequency;
    for (ContextEntity contextEntity : settingContextDAO.getContextsofType(USER.toString(), offset, limit)) {
      String username = contextEntity.getName();
      isActive=false; isEnabled=false; isFrequency=false;
      for (SettingsEntity settingsEntity : settingsDAO.getSettingsByUser(username)) {
        if (settingsEntity.getName().equals(EXO_IS_ENABLED) && (settingsEntity.getValue()==null
        || settingsEntity.getValue().equals("true"))) {
          isEnabled=true;
        } else if (settingsEntity.getName().equals(EXO_IS_ACTIVE) && settingsEntity.getValue().contains(UserSetting.EMAIL_CHANNEL)) {
          isActive=true;
        } else if (settingsEntity.getName().equals(frequency) && settingsEntity.getValue() != null) {
          isFrequency=true;
        }
        if (isActive && isEnabled && isFrequency) {
          map.put(username, settingsDAO.getSettingsByUser(username));
        }
      }
    }
    return map;
  }

  @Override
  @ExoTransactional
  public List<UserSetting> getDigestSettingForAllUser(NotificationContext context, int offset, int limit) {
    List<UserSetting> models = new ArrayList<UserSetting>();
    try {
      for (String username : getDigestIterator(context, offset, limit).keySet()) {
        models.add(fillModel(username, getDigestIterator(context, offset, limit).get(username)));
      }
    } catch (Exception e) {
      LOG.error("Failed to get all daily users have notification messages", e);
    }
    return models;
  }

  /**
   * Gets plugin's ID by propertyName
   *
   * @param settings
   * @return
   * @throws Exception
   */
  private List<String> getValues(List<SettingsEntity> settings, String propertyName) throws Exception {
    String token1="\\{";
    String token2="}";
    try {
      for(SettingsEntity settingsEntity : settings) {
        if (settingsEntity.getName().equals(propertyName)) {
          String values = settingsEntity.getValue().replaceAll(token1,"").replaceAll(token2,"");
          return NotificationUtils.stringToList(values);
        }
      }
    } catch (Exception e) {
      return new ArrayList<String>();
    }
    return new ArrayList<String>();
  }

  /**
   * Fill the model data from UserSetting entity
   *
   * @param username the given user name
   * @param settings the given settings of the user
   * @return the UserSetting
   * @throws Exception
   */
  private UserSetting fillModel(String username, List<SettingsEntity> settings) throws Exception {
//    if(!parentNode.hasProperty(EXO_LAST_MODIFIED_DATE)) {
//      if(parentNode.isNodeType(EXO_MODIFY)) {
//        parentNode.setProperty(EXO_LAST_MODIFIED_DATE, Calendar.getInstance());
//        parentNode.save();
//      }
//      else if(parentNode.canAddMixin(EXO_MODIFY)) {
//        parentNode.addMixin(EXO_MODIFY);
//        parentNode.setProperty(EXO_LAST_MODIFIED_DATE, Calendar.getInstance());
//        parentNode.save();
//      }
//      else {
//        LOG.warn("Cannot add mixin to node '{}'.", parentNode.getPath());
//      }
//    }
    UserSetting model = UserSetting.getInstance();
    model.setUserId(username);
    model.setDailyPlugins(getValues(settings, EXO_DAILY));
    model.setWeeklyPlugins(getValues(settings, EXO_WEEKLY));
    //
    model.setChannelActives(getValues(settings, EXO_IS_ACTIVE));
    //
    List<AbstractChannel> channels = channelManager.getChannels();
    for (AbstractChannel channel : channels) {
      model.setChannelPlugins(channel.getId(), getValues(settings, getChannelProperty(channel.getId())));
    }
    //
//    if(parentNode.hasProperty(EXO_LAST_MODIFIED_DATE) ){
//      model.setLastUpdateTime(parentNode.getProperty(EXO_LAST_MODIFIED_DATE).getDate());
//    } else {
//      model.setLastUpdateTime(Calendar.getInstance());
//    }
    //
    model.setLastUpdateTime(Calendar.getInstance());
    for (SettingsEntity settingsEntity : settings) {
      if (settingsEntity.getName().equals(EXO_IS_ENABLED)) {
        model.setEnabled(Boolean.valueOf(settingsEntity.getValue()));
      }
    }
    return model;
  }

  @ExoTransactional
  private void fillDefaultSettingsOfUser(User[] users) throws Exception {
    for (int i = 0; i < users.length; ++i) {
      String userId = users[i].getUserName();
      ContextEntity contextEntity = new ContextEntity();
      contextEntity.setType(USER.toString());
      contextEntity.setName(userId);
      if (settingContextDAO.getContext(contextEntity) == null) {
        settingContextDAO.create(contextEntity);
      }
    }
  }

  @ExoTransactional
  private List<String> getDefaultDailyIterator(int offset, int limit) throws Exception {
    List<String> list = new ArrayList<String>();
    for (ContextEntity contextEntity : settingContextDAO.getContextsofType(USER.toString(), offset, limit)) {
      if (!(settingsDAO.getSettingsByUser(contextEntity.getName()).size() > 1)) {
        list.add(contextEntity.getName());
      }
    }
    return list;
  }

  @Override
  public List<UserSetting> getDigestDefaultSettingForAllUser(int offset, int limit) {
    List<UserSetting> users = new ArrayList<UserSetting>();
    try {
      for(String user : getDefaultDailyIterator(offset, limit)) {
        users.add(UserSetting.getInstance()
            .setUserId(user)
            .setLastUpdateTime(Calendar.getInstance()));
      }

    } catch (Exception e) {
      LOG.error("Failed to get default daily users have notification messages", e);
    }
    return users;
  }

  @Override
  public void saveLastReadDate(String userId, Long time) {
    settingService.set(USER.id(userId), NOTIFICATION_SCOPE, EXO_LAST_READ_DATE, SettingValue.create(time));
  }
}

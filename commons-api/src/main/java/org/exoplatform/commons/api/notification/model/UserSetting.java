/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.api.notification.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.container.PortalContainer;

/**
 * User setting notification
 */

/**
 * @author hanhvq
 *
 */
public class UserSetting {
  private static UserSetting defaultSetting = null;

  public static String EMAIL_CHANNEL = "email";

  public static String INTRANET_CHANNEL = "intranet";
  
  public enum FREQUENCY {
    INSTANTLY, DAILY, WEEKLY;

    public static FREQUENCY getFrequecy(String name) {
      for (int i = 0; i < values().length; ++i) {
        if (values()[i].name().equalsIgnoreCase(name)) {
          return values()[i];
        }
      }
      return null;
    }
  }

  private List<String> channelActives;

  private Calendar     lastUpdateTime;

  private String       userId;

  private Map<String, List<String>> channelPlugins;

  private List<String> instantlyPlugins;

  private List<String> dailyPlugins;

  private List<String> weeklyPlugins;

  public UserSetting() {
    this.channelActives = new ArrayList<String>();
    this.channelPlugins = new HashMap<String, List<String>>();
    //
    this.instantlyPlugins = new ArrayList<String>();
    this.dailyPlugins = new ArrayList<String>();
    this.weeklyPlugins = new ArrayList<String>();
    this.lastUpdateTime = Calendar.getInstance();
  }
  
  public static UserSetting getInstance() {
    return new UserSetting();
  }

  /**
   * @return
   */
  public List<String> getChannelActives() {
    return channelActives;
  }
  
  /**
   * @return
   */
  public boolean isChannelActive(String channelId) {
    return channelActives.contains(channelId);
  }

  /**
   * @param channelId
   */
  public void setChannelActive(String channelId) {
    if(!isChannelActive(channelId)) {
      channelActives.add(channelId);
    }
  }

  /**
   * @param channelId
   */
  public void removeChannelActive(String channelId) {
    if(isChannelActive(channelId)) {
      channelActives.remove(channelId);
    }
  }
  
  /**
   * @param channelActives
   */
  public void setChannelActives(List<String> channelActives) {
    this.channelActives = channelActives;
  }

  /**
   * @return the userId
   */
  public String getUserId() {
    return userId;
  }

  /**
   * @param userId the userId to set
   */
  public UserSetting setUserId(String userId) {
    this.userId = userId;
    return this;
  }

  /**
   * @return the lastUpdateTime
   */
  public Calendar getLastUpdateTime() {
    return lastUpdateTime;
  }

  /**
   * @param lastUpdateTime the lastUpdateTime to set
   */
  public UserSetting setLastUpdateTime(Calendar lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
    return this;
  }

  /**
   * @return the all channelPlugins
   */
  public void setAllChannelPlugins(Map<String, List<String>> channelPlugins) {
    this.channelPlugins = channelPlugins;
  }
  
  /**
   * @return the all channelPlugins
   */
  public Map<String, List<String>> getAllChannelPlugins() {
    return channelPlugins;
  }

  /**
   * @return the channelPlugins
   */
  public List<String> getPlugins(String channelId) {
    List<String> channelPlugins = this.channelPlugins.get(channelId);
    if (channelPlugins == null) {
      channelPlugins = new ArrayList<String>();
      this.channelPlugins.put(channelId, channelPlugins);
    }
    return channelPlugins;
  }

  /**
   * @param channelId
   * @param pluginIds
   */
  public void setChannelPlugins(String channelId, List<String> pluginIds) {
    this.channelPlugins.put(channelId, pluginIds);
  }

  /**
   * Add the pluginId by channel
   * @param channelId
   * @param pluginId
   */
  public void addChannelPlugin(String channelId, String pluginId) {
    getPlugins(channelId).add(pluginId);
  }

  /**
   * @return the instantlyPlugins
   */
  public List<String> getInstantlyPlugins() {
    return instantlyPlugins;
  }

  /**
   * @param instantlyPlugins the instantlyPlugins to set
   */
  public void setInstantlyPlugins(List<String> instantlyPlugins) {
    this.instantlyPlugins = instantlyPlugins;
  }

  /**
   * @return the dailyPlugins
   */
  public List<String> getDailyPlugins() {
    return dailyPlugins;
  }

  /**
   * @param dailyPlugins the dailyPlugins to set
   */
  public void setDailyPlugins(List<String> dailyPlugins) {
    this.dailyPlugins = dailyPlugins;
  }

  /**
   * @return the weeklyPlugins
   */
  public List<String> getWeeklyPlugins() {
    return weeklyPlugins;
  }

  /**
   * @param weeklyPlugins the weeklyPlugins to set
   */
  public void setWeeklyPlugins(List<String> weeklyPlugins) {
    this.weeklyPlugins = weeklyPlugins;
  }


  /**
   * @param pluginId the provider's id to add
   */
  public void addPlugin(String pluginId, FREQUENCY frequencyType) {
    if (frequencyType.equals(FREQUENCY.DAILY)) {
      addProperty(dailyPlugins, pluginId);
    } else if (frequencyType.equals(FREQUENCY.WEEKLY)) {
      addProperty(weeklyPlugins, pluginId);
    } else if (frequencyType.equals(FREQUENCY.INSTANTLY)) {
      addProperty(instantlyPlugins, pluginId);
    }
  }

  /**
   * @param pluginId
   * @return
   */
  public boolean isInInstantly(String pluginId) {
    return (instantlyPlugins.contains(pluginId)) ? true : false;
  }

  /**
   * @param pluginId
   * @return
   */
  public boolean isInChannel(String channelId, String pluginId) {
    return (getPlugins(channelId).contains(pluginId));
  }
  
  /**
   * @param pluginId
   * @return
   */
  public boolean isInDaily(String pluginId) {
    return (dailyPlugins.contains(pluginId)) ? true : false;
  }

  /**
   * @param pluginId
   * @return
   */
  public boolean isInWeekly(String pluginId) {
    return (weeklyPlugins.contains(pluginId)) ? true : false;
  }


  public boolean isActiveWithoutInstantly(String pluginId) {
    return isInDaily(pluginId) || isInWeekly(pluginId);
  }

  private void addProperty(List<String> providers, String pluginId) {
    if (providers.contains(pluginId) == false) {
      providers.add(pluginId);
    }
  }
  
  @Override
  public UserSetting clone() {
    UserSetting setting = getInstance();
    setting.setChannelActives(channelActives);
    setting.setDailyPlugins(dailyPlugins);
    setting.setWeeklyPlugins(weeklyPlugins);
    setting.setInstantlyPlugins(instantlyPlugins);
    //
    setting.setAllChannelPlugins(channelPlugins);
    setting.setUserId(userId);
    return setting;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj)) {
      return true;
    }
    if (!(obj instanceof UserSetting)) {
      return false;
    }
    UserSetting that = (UserSetting) obj;
    if (userId != null && userId.equals(that.userId)) {
      return true;
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    if (userId != null) {
      return userId.hashCode();
    }
    return super.hashCode();
  }
  
  @Override
  public String toString() {
    return "UserSetting : {userId : " + userId + "}";
  }
  
  public static final UserSetting getDefaultInstance() {
    if (defaultSetting == null) {
      PluginSettingService settingService = (PluginSettingService) PortalContainer.getInstance()
                                              .getComponentInstanceOfType(PluginSettingService.class);
      List<PluginInfo> plugins = settingService.getAllPlugins();
      
      defaultSetting = getInstance();
      //
      for (PluginInfo pluginInfo : plugins) {
        for (String defaultConf : pluginInfo.getDefaultConfig()) {
          for (String channelId : pluginInfo.getAllChannelActive()) {
            if (FREQUENCY.getFrequecy(defaultConf) == FREQUENCY.INSTANTLY && !EMAIL_CHANNEL.equals(channelId)) {
              defaultSetting.addChannelPlugin(channelId, pluginInfo.getType());
            } else {
              defaultSetting.addPlugin(pluginInfo.getType(), FREQUENCY.getFrequecy(defaultConf));
            }
          }
        }
      }
    }

    return defaultSetting.clone();
  }

}

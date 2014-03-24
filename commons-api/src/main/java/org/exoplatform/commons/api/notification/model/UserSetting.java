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
import java.util.List;

import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.container.PortalContainer;

/**
 * User setting notification
 */

public class UserSetting {
  private static UserSetting defaultSetting = null;
  
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

  private boolean     isActive = true;

  private Calendar     lastUpdateTime;

  private String       userId;

  private List<String> instantlyProviders;

  private List<String> dailyProviders;

  private List<String> weeklyProviders;

  public UserSetting() {
    this.instantlyProviders = new ArrayList<String>();
    this.dailyProviders = new ArrayList<String>();
    this.weeklyProviders = new ArrayList<String>();
    this.lastUpdateTime = Calendar.getInstance();
  }
  
  public static UserSetting getInstance() {
    return new UserSetting();
  }

  /**
   * @return the isActive
   */
  public boolean isActive() {
    return isActive;
  }

  /**
   * @param isActive the isActive to set
   */
  public void setActive(boolean isActive) {
    this.isActive = isActive;
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
   * @return the instantlyProviders
   */
  public List<String> getInstantlyProviders() {
    return instantlyProviders;
  }

  /**
   * @param instantlyProviders the instantlyProviders to set
   */
  public void setInstantlyProviders(List<String> instantlyProviders) {
    this.instantlyProviders = instantlyProviders;
  }

  /**
   * @return the dailyProviders
   */
  public List<String> getDailyProviders() {
    return dailyProviders;
  }

  /**
   * @param dailyProviders the dailyProviders to set
   */
  public void setDailyProviders(List<String> dailyProviders) {
    this.dailyProviders = dailyProviders;
  }

  /**
   * @return the weeklyProviders
   */
  public List<String> getWeeklyProviders() {
    return weeklyProviders;
  }

  /**
   * @param weeklyProviders the weeklyProviders to set
   */
  public void setWeeklyProviders(List<String> weeklyProviders) {
    this.weeklyProviders = weeklyProviders;
  }


  /**
   * @param providerId the provider's id to add
   */
  public void addProvider(String providerId, FREQUENCY frequencyType) {
    if (frequencyType.equals(FREQUENCY.DAILY)) {
      addProperty(dailyProviders, providerId);
    } else if (frequencyType.equals(FREQUENCY.WEEKLY)) {
      addProperty(weeklyProviders, providerId);
    } else if (frequencyType.equals(FREQUENCY.INSTANTLY)) {
      addProperty(instantlyProviders, providerId);
    }
  }

  /**
   * @param providerId
   * @return
   */
  public boolean isInInstantly(String providerId) {
    return (instantlyProviders.contains(providerId)) ? true : false;
  }

  /**
   * @param providerId
   * @return
   */
  public boolean isInDaily(String providerId) {
    return (dailyProviders.contains(providerId)) ? true : false;
  }

  /**
   * @param providerId
   * @return
   */
  public boolean isInWeekly(String providerId) {
    return (weeklyProviders.contains(providerId)) ? true : false;
  }


  public boolean isActiveWithoutInstantly(String pluginId) {
    return isInDaily(pluginId) || isInWeekly(pluginId);
  }

  private void addProperty(List<String> providers, String providerId) {
    if (providers.contains(providerId) == false) {
      providers.add(providerId);
    }
  }
  
  @Override
  public UserSetting clone() {
    UserSetting setting = getInstance();
    setting.setActive(isActive);
    setting.setDailyProviders(dailyProviders);
    setting.setWeeklyProviders(weeklyProviders);
    setting.setInstantlyProviders(instantlyProviders);
    setting.setUserId(userId);
    return setting;
  }
  
  public static final UserSetting getDefaultInstance() {
    if (defaultSetting == null) {
      PluginSettingService settingService = (PluginSettingService) PortalContainer.getInstance()
                                              .getComponentInstanceOfType(PluginSettingService.class);
      List<PluginInfo> providerDatas = settingService.getActivePlugins();
      
      if (providerDatas == null || providerDatas.size()==0) {
        return new UserSetting();
      }
      
      defaultSetting = getInstance();
      defaultSetting.setActive(true);
      for (PluginInfo providerData : providerDatas) {
        for (String defaultConf : providerData.getDefaultConfig()) {
          defaultSetting.addProvider(providerData.getType(), FREQUENCY.getFrequecy(defaultConf));
        }
      }
    }

    return defaultSetting.clone();
  }

}

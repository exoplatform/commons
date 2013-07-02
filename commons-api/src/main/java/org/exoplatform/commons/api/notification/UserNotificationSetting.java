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
package org.exoplatform.commons.api.notification;

import java.util.ArrayList;
import java.util.List;

/**
 * User setting notification
 */

public class UserNotificationSetting {
  public enum FREQUENCY {
    INSTANTLY("Instantly"), DAILY_KEY("daily"),
    WEEKLY_KEY("weekly"), MONTHLY_KEY("monthly");
    private final String name;

    FREQUENCY(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return name;
    }

  }
  
  private String userId;

  private List<String> instantlyProviders;

  private List<String> dailyProviders;

  private List<String> weeklyProviders;

  private List<String> monthlyProviders;

  public UserNotificationSetting() {
    this.instantlyProviders = new ArrayList<String>();
    this.dailyProviders = new ArrayList<String>();
    this.weeklyProviders = new ArrayList<String>();
    this.monthlyProviders = new ArrayList<String>();
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
  public void setUserId(String userId) {
    this.userId = userId;
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
   * @return the monthlyProviders
   */
  public List<String> getMonthlyProviders() {
    return monthlyProviders;
  }

  /**
   * @param monthlyProviders the monthlyProviders to set
   */
  public void setMonthlyProviders(List<String> monthlyProviders) {
    this.monthlyProviders = monthlyProviders;
  }

  /**
   * @param providerId the provider's id to add
   */
  public void addProvider(String providerId, FREQUENCY frequencyType) {
    if (frequencyType.equals(FREQUENCY.DAILY_KEY)) {
      addProperty(dailyProviders, providerId);
    } else if (frequencyType.equals(FREQUENCY.WEEKLY_KEY)) {
      addProperty(weeklyProviders, providerId);
    } else if (frequencyType.equals(FREQUENCY.MONTHLY_KEY)) {
      addProperty(monthlyProviders, providerId);
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

  /**
   * @param providerId
   * @return
   */
  public boolean isInMonthly(String providerId) {
    return (monthlyProviders.contains(providerId)) ? true : false;
  }

  public boolean isActiveWithoutInstantly(String providerId) {
    return isInDaily(providerId) || isInWeekly(providerId) || isInMonthly(providerId);
  }

  private void addProperty(List<String> providers, String providerId) {
    if (providers.contains(providerId) == false) {
      providers.add(providerId);
    }
  }

}

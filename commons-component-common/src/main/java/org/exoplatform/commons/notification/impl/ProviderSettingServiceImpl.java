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
package org.exoplatform.commons.notification.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.exoplatform.commons.api.notification.plugin.ActiveProviderPlugin;
import org.exoplatform.commons.api.notification.plugin.GroupProviderModel;
import org.exoplatform.commons.api.notification.plugin.GroupProviderPlugin;
import org.exoplatform.commons.api.notification.service.ProviderSettingService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

public class ProviderSettingServiceImpl implements ProviderSettingService, Startable {
  private static final Log LOG = ExoLogger.getLogger(ProviderSettingServiceImpl.class);

  private List<ActiveProviderPlugin> activeProviderPlugins = new ArrayList<ActiveProviderPlugin>();

  private List<GroupProviderPlugin>  groupProviderPlugins  = new ArrayList<GroupProviderPlugin>();
  
  private List<String> activeProviderIds = new ArrayList<String>();

  private static final String NAME_SPACES = "exo:";

  private SettingService settingService;

  public ProviderSettingServiceImpl(SettingService settingService) {
    this.settingService = settingService;
  }

  @Override
  public void start() {
    try {
      long startTime = System.currentTimeMillis();
      LOG.info("initializing active Provider...");
      initActiveProviders();
      LOG.info("end initialize Provider... " + (System.currentTimeMillis() - startTime) + " ms");
    } catch (Exception e) {
      LOG.error("Error while active Provider: ", e);
    }
  }

  @Override
  public void stop() {
  }

  @Override
  public void registerActiveProviderPlugin(ActiveProviderPlugin activeProviderPlugin) {
    activeProviderPlugins.add(activeProviderPlugin);    
  }

  @Override
  public void registerGroupProviderPlugin(GroupProviderPlugin groupProviderPlugin) {
    groupProviderPlugins.add(groupProviderPlugin);    
  }
  
  private void initActiveProviders() {
    //
    for (ActiveProviderPlugin pp : activeProviderPlugins) {
      //
      saveSetting(pp.getActiveProviderForUsers(), Scope.GLOBAL);
      //
      saveSetting(pp.getActiveProviderForAdmins(), Scope.PORTAL);
    }
  }
  
  private void saveSetting(List<String> providerIds, Scope scope) {
    for (String str : providerIds) {
      if (str != null && str.length() > 0) {
        saveSetting(scope, str, true);
      }
    }
    //
    activeProviderIds.clear();
  }

  private void saveSetting(Scope scope, String property, boolean value) {
    settingService.set(Context.GLOBAL, scope, (NAME_SPACES + property), SettingValue.create(value));
  }

  private void removeSetting(Scope scope, String property) {
    settingService.remove(Context.GLOBAL, scope, (NAME_SPACES + property));
  }

  private boolean getValueSetting(Scope scope, String property) {
    SettingValue value =settingService.get(Context.GLOBAL, scope, (NAME_SPACES + property));
    if (value != null) {
      return ((Boolean) value.getValue()) ? true : false;
    }
    return false;
  }
  
  @Override
  public List<String> getActiveProviderIds(boolean isAdmin) {
    if (activeProviderIds.size() == 0) {
      for (ActiveProviderPlugin pp : activeProviderPlugins) {
        for (String str : pp.getActiveProviderForUsers()) {
          if (isActive(str, Scope.GLOBAL) == true) {
            activeProviderIds.add(str);
          }
        }

        if (isAdmin == false) {
          continue;
        }

        for (String str : pp.getActiveProviderForAdmins()) {
          if (isActive(str, Scope.PORTAL) == true) {
            activeProviderIds.add(str);
          }
        }
      }
    }
    return activeProviderIds;
  }
  
  private boolean isActive(String providerId, Scope scope) {
    if (providerId == null || providerId.length() == 0) {
      return false;
    }
    return getValueSetting(scope, providerId);
  }

  @Override
  public Map<String, Boolean> getActiveProviderIdForSetting() {
    Map<String, Boolean> mapProviderId = new HashMap<String, Boolean>();

    for (String str : getAllKeyOfProviderSetting()) {
      // for administrators
      boolean value = getValueSetting(Scope.PORTAL, str);
      if (value == true) {
        mapProviderId.put(str, true);
        continue;
      }
      // for users
      value = getValueSetting(Scope.GLOBAL, str);
      if (value == true) {
        mapProviderId.put(str, false);
      }
    }
    return mapProviderId;
  }

  @Override
  public void setActiveProviders(Map<String, Boolean> mapProviderId) {
    Set<String> key = mapProviderId.keySet();
    for (String str : getAllKeyOfProviderSetting()) {
      
      if (key.contains(str) == true) {
        saveSetting((mapProviderId.get(str) == true) ? Scope.PORTAL : Scope.GLOBAL, str, true);

      } else {
        removeSetting(Scope.GLOBAL, str);
        removeSetting(Scope.PORTAL, str);
      }
    }
    activeProviderIds.clear();
  }
  
  private List<String> getAllKeyOfProviderSetting() {
    List<String> providerIds = new ArrayList<String>();
    for (ActiveProviderPlugin app : activeProviderPlugins) {
      providerIds.addAll(app.getActiveProviderForUsers());
      //
      providerIds.addAll(app.getActiveProviderForAdmins());
    }
    return providerIds;
  }

  @Override
  public List<GroupProviderModel> getGroupProviders() {
    List<GroupProviderModel> groupProviders = new ArrayList<GroupProviderModel>();
    for (GroupProviderPlugin gpp : groupProviderPlugins) {
      GroupProviderPlugin.addGroupProviderData(groupProviders, gpp.getGroupProviders());
    }

    return groupProviders;
  }
}

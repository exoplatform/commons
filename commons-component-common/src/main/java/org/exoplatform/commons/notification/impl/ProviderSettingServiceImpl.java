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

  private SettingService settingService;

  public ProviderSettingServiceImpl(SettingService settingService) {
    this.settingService = settingService;
  }

  @Override
  public void start() {
    try {
      LOG.info("initializing active Provider...");
      initActiveProviders();
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
  
  private void initActiveProviders() {
    List<String> providerIds;
    //
    for (ActiveProviderPlugin pp : activeProviderPlugins) {
      
      providerIds = pp.getActiveProviderForUsers();
      for (String str : providerIds) {
        if (str != null && str.length() > 0) {
          settingService.set(Context.GLOBAL, Scope.GLOBAL, str, SettingValue.create(true));
        }
      }
      
      providerIds = pp.getActiveProviderForAdmins();
      for (String str : providerIds) {
        if (str != null && str.length() > 0) {
          settingService.set(Context.GLOBAL, Scope.PORTAL, str, SettingValue.create(true));
        }
      }
    }
  }

  @Override
  public List<String> getActiveProviderIds(boolean isAdmin) {
    List<String> providerIds = new ArrayList<String>();

    for (ActiveProviderPlugin pp : activeProviderPlugins) {
      for (String str : pp.getActiveProviderForUsers()) {
        if (isActive(str, Scope.GLOBAL) == true) {
          providerIds.add(str);
        }
      }

      if (isAdmin == false) {
        continue;
      }

      for (String str : pp.getActiveProviderForAdmins()) {
        if (isActive(str, Scope.PORTAL) == true) {
          providerIds.add(str);
        }
      }
    }
    return providerIds;
  }
  
  private boolean isActive(String providerId, Scope scope) {
    if (providerId == null || providerId.length() == 0) {
      return false;
    }
    SettingValue value = settingService.get(Context.GLOBAL, scope, providerId);
    if (value != null) {
      return ((Boolean) value.getValue()) ? true : false;
    }
    return false;
  }

  @Override
  public Map<String, Boolean> getActiveProviderIdForSetting() {
    Map<String, Boolean> mapProviderId = new HashMap<String, Boolean>();

    for (String str : getAllKeyOfProviderSetting()) {

      SettingValue value = settingService.get(Context.GLOBAL, Scope.PORTAL, str);
      if (value != null) {
        mapProviderId.put((String) value.getValue(), true);
        continue;
      }

      value = settingService.get(Context.GLOBAL, Scope.GLOBAL, str);
      if (value != null) {
        mapProviderId.put((String) value.getValue(), false);
      }
    }
    return mapProviderId;
  }

  @Override
  public void setActiveProviders(Map<String, Boolean> mapProviderId) {
    Set<String> key = mapProviderId.keySet();
    for (String str : getAllKeyOfProviderSetting()) {
      
      if (key.contains(str) == true) {
        settingService.set(Context.GLOBAL, (mapProviderId.get(str) == true) ? Scope.PORTAL : Scope.GLOBAL,
                                          str, SettingValue.create(mapProviderId.get(str)));
      } else {
        settingService.remove(Context.GLOBAL, Scope.GLOBAL, str);
        settingService.remove(Context.GLOBAL, Scope.PORTAL, str);
      }
    }
  }
  
  private List<String> getAllKeyOfProviderSetting() {
    List<String> providerIds = new ArrayList<String>();
    for (ActiveProviderPlugin pp : activeProviderPlugins) {
      providerIds.addAll(pp.getActiveProviderForUsers());
      //
      providerIds.addAll(pp.getActiveProviderForAdmins());
    }
    return providerIds;
  }
}

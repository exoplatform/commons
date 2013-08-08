/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.settings.impl;

import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.container.PortalContainer;

public class ExoFeatureServiceImpl implements ExoFeatureService {
  
  private static final String NAME_SPACES = "exo:";

  private SettingService      settingService;
  
  public ExoFeatureServiceImpl() {
    if (settingService == null) {
      settingService = (SettingService) PortalContainer.getInstance().getComponentInstanceOfType(SettingService.class);
    }
  }

  @Override
  public boolean isActiveFeature(String featureName) {
    SettingValue<?> sValue = settingService.get(Context.GLOBAL, Scope.GLOBAL, (NAME_SPACES + featureName));
    return (sValue != null) ? (Boolean) sValue.getValue() : true;
  }

  @Override
  public void saveActiveFeature(String featureName, boolean isActive) {
    settingService.set(Context.GLOBAL, Scope.GLOBAL, (NAME_SPACES + featureName), SettingValue.create(isActive));
  }


}

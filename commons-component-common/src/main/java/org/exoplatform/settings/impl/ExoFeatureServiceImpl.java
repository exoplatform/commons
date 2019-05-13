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
import org.exoplatform.management.annotations.*;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.management.rest.annotations.RESTEndpoint;

@Managed
@ManagedDescription("eXo Feature Service")
@NameTemplate({
    @Property(key = "service", value = "feature"),
    @Property(key = "view", value = "ExoFeatureService")
})
@RESTEndpoint(path = "featureservice")
public class ExoFeatureServiceImpl implements ExoFeatureService {
  
  private static final String NAME_SPACES = "exo:";

  private SettingService      settingService;
  
  public ExoFeatureServiceImpl(SettingService      settingService) {
    this.settingService = settingService;
  }

  @Managed
  @ManagedDescription("Determine if the feature is active")
  @Impact(ImpactType.READ)
  @Override
  public boolean isActiveFeature(@ManagedDescription("Feature name") @ManagedName("featureName") String featureName) {
    SettingValue<?> sValue = settingService.get(Context.GLOBAL, Scope.GLOBAL.id(null), (NAME_SPACES + featureName));
    return (sValue == null) ? true : Boolean.valueOf(sValue.getValue().toString());
  }

  @Override
  public void saveActiveFeature(String featureName, boolean isActive) {
    settingService.set(Context.GLOBAL, Scope.GLOBAL.id(null), (NAME_SPACES + featureName), SettingValue.create(isActive));
  }

  @Managed
  @ManagedDescription("Activate/Deactivate feature")
  @Impact(ImpactType.WRITE)
  public void changeFeatureActivation(@ManagedDescription("Feature name") @ManagedName("featureName") String featureName,
                                @ManagedDescription("Is active") @ManagedName("isActive") String isActive) {
    boolean isActiveBool = Boolean.parseBoolean(isActive);
    saveActiveFeature(featureName, isActiveBool);
  }


}

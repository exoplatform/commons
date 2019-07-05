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

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.commons.api.settings.*;
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

  private Map<String, Boolean> featuresProperties = new HashMap<>();

  private Map<String, FeaturePlugin> plugins     = new HashMap<>();

  public ExoFeatureServiceImpl(SettingService      settingService) {
    this.settingService = settingService;
  }

  @Managed
  @ManagedDescription("Determine if the feature is active")
  @Impact(ImpactType.READ)
  @Override
  public boolean isActiveFeature(@ManagedDescription("Feature name") @ManagedName("featureName") String featureName) {
    Boolean active;
    SettingValue<?> sValue = settingService.get(Context.GLOBAL, Scope.GLOBAL.id(null), (NAME_SPACES + featureName));
    if(sValue != null) {
      active = Boolean.valueOf(sValue.getValue().toString());
    } else {
      active = getFeaturePropertyValue(featureName);
    }
    return active == null ? true : active;
  }

  private Boolean getFeaturePropertyValue(String featureName) {
    String propertyName = "exo.feature." + featureName + ".enabled";
    if(featuresProperties.containsKey(propertyName)) {
      return featuresProperties.get(propertyName);
    } else {
      String propertyValue = System.getProperty(propertyName);
      Boolean active = propertyValue != null ? Boolean.valueOf(propertyValue) : null;
      featuresProperties.put(propertyName, active);
      return active;
    }
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

  @Override
  public void addFeaturePlugin(FeaturePlugin featurePlugin) {
    plugins.put(featurePlugin.getName(), featurePlugin);
  }

  @Override
  public boolean isFeatureActiveForUser(String featureName, String username) {
    if (!isActiveFeature(featureName)) {
      return false;
    }
    FeaturePlugin featurePlugin = plugins.get(featureName);
    return featurePlugin != null && featurePlugin.isFeatureActiveForUser(featureName, username);
  }

}

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
package org.exoplatform.settings.impl;

import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.testing.BaseCommonsTestCase;

public class FeatureServiceTest extends BaseCommonsTestCase  {
  
  private ExoFeatureService featureService;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    featureService = getService(ExoFeatureService.class);
  }
  
  public void testShouldFeatureBeActiveWhenSettingIsTrue() {
    // Given
    featureService.saveActiveFeature("feature1", true);

    // When
    boolean activeFeature = featureService.isActiveFeature("feature1");

    // Then
    assertTrue(activeFeature);
  }

  public void testShouldFeatureNotBeActiveWhenSettingIsFalse() {
    // Given
    featureService.saveActiveFeature("feature2", false);

    // When
    boolean activeFeature = featureService.isActiveFeature("feature2");

    // Then
    assertFalse(activeFeature);
  }

  public void testShouldFeatureBeActiveWhenNoSettingAndNoProperty() {
    // Given

    // When
    boolean activeFeature = featureService.isActiveFeature("feature3");

    // Then
    assertTrue(activeFeature);
  }

  public void testShouldFeatureBeActiveWhenNoSettingAndPropertyIsTrue() {
    // Given
    System.setProperty("exo.feature.feature3.enabled", "true");

    // When
    boolean activeFeature = featureService.isActiveFeature("feature3");

    // Then
    assertTrue(activeFeature);
  }

  public void testShouldFeatureNotBeActiveWhenNoSettingAndPropertyIsFalse() {
    // Given
    System.setProperty("exo.feature.feature4.enabled", "false");

    // When
    boolean activeFeature = featureService.isActiveFeature("feature4");

    // Then
    assertFalse(activeFeature);
  }

  public void testShouldFeatureBeActiveWhenSettingIsTrueAndPropertyIsFalse() {
    // Given
    featureService.saveActiveFeature("feature5", true);
    System.setProperty("exo.feature.feature5.enabled", "false");

    // When
    boolean activeFeature = featureService.isActiveFeature("feature5");

    // Then
    assertTrue(activeFeature);
  }

  public void testShouldFeatureNotBeActiveWhenSettingIsFalseAndPropertyIsTrue() {
    // Given
    featureService.saveActiveFeature("feature6", false);
    System.setProperty("exo.feature.feature6.enabled", "true");

    // When
    boolean activeFeature = featureService.isActiveFeature("feature6");

    // Then
    assertFalse(activeFeature);
  }
}

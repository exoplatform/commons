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
package org.exoplatform.commons.api.settings;


public interface ExoFeatureService {
  
  /**
   * Check the status of a feature
   * 
   * @param featureName
   * @return true if the featureName is on, false if it's off
   */
  public boolean isActiveFeature(String featureName);

  /**
   * Switch feature featureName on or off
   * 
   * @param featureName
   * @param isActive new status of feature, true = on and false = off
   */
  public void saveActiveFeature(String featureName, boolean isActive);

}

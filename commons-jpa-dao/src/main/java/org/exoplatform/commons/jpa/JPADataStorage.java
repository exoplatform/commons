/*
 *
 *  * Copyright (C) 2003-2015 eXo Platform SAS.
 *  *
 *  * This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License
 *  as published by the Free Software Foundation; either version 3
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see<http://www.gnu.org/licenses/>.
 *
 */

package org.exoplatform.commons.jpa;

import org.exoplatform.commons.api.persistence.DataInitializer;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.jpa.dao.SettingsDAO;
import org.exoplatform.commons.jpa.entity.SettingsEntity;

import static org.exoplatform.commons.jpa.EntityConverter.*;


/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 * 9/8/15
 */
public class JPADataStorage implements SettingService {

  private SettingsDAO settingsDAO;

  /**
   * JPADataStorage must depends on DataInitializer to make sure data structure is created before initializing it
   */
  public JPADataStorage(SettingsDAO settingsDAO, DataInitializer dataInitializer) {
    this.settingsDAO = settingsDAO;
  }


  @Override
  public void set(Context context, Scope scope, String key, SettingValue<?> value) {
    SettingsEntity settingsEntity = settingsDAO.create(convertSettingsToSettingsEntity(context, scope, key, value));

  }

  @Override
  public void remove(Context context, Scope scope, String key) {

  }

  @Override
  public void remove(Context context, Scope scope) {

  }

  @Override
  public void remove(Context context) {

  }

  @Override
  public SettingValue<?> get(Context context, Scope scope, String key) {
    return null;
  }
}

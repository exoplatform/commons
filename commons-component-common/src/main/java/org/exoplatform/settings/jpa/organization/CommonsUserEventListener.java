package org.exoplatform.settings.jpa.organization;

/*
* Copyright (C) 2003-2017 eXo Platform SAS.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see http://www.gnu.org/licenses/ .
*/


import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.settings.jpa.dao.SettingContextDAO;
import org.exoplatform.settings.jpa.dao.SettingScopeDAO;
import org.exoplatform.settings.jpa.dao.SettingsDAO;
import org.exoplatform.settings.jpa.entity.SettingsEntity;

import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Walid Khessairi
 * wkhessairi@exoplatform.com
 * 4/12/17
 */
public class CommonsUserEventListener extends UserEventListener {

  private static final Log LOG = ExoLogger.getLogger(CommonsUserEventListener.class);

  private SettingsDAO settingsDAO;

  public CommonsUserEventListener(SettingsDAO settingsDAO, SettingContextDAO settingContextDAO, SettingScopeDAO settingScopeDAO) {
    this.settingsDAO = settingsDAO;
  }

  /**
   * Deletes all settings data of the deleted user.
   * @param user Deleted user
   * @throws Exception
   */
  @Override
  public void postDelete(User user) throws Exception {

    LOG.info("Removing all settings data of the user "+user.getUserName());

    List<SettingsEntity> settings = settingsDAO.getSettingsByUser(user.getUserName());
    if (settings != null) {
      settingsDAO.deleteAll(settings);
    }
  }
}

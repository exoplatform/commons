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

package org.exoplatform.settings.jpa;

import org.exoplatform.commons.api.persistence.DataInitializer;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.*;
import org.exoplatform.commons.event.impl.EventManagerImpl;
import org.exoplatform.services.listener.Event;
import org.exoplatform.settings.jpa.dao.SettingContextDAO;
import org.exoplatform.settings.jpa.dao.SettingScopeDAO;
import org.exoplatform.settings.jpa.dao.SettingsDAO;
import org.exoplatform.settings.jpa.entity.ContextEntity;
import org.exoplatform.settings.jpa.entity.ScopeEntity;
import org.exoplatform.settings.jpa.entity.SettingsEntity;

import java.util.List;

import static org.exoplatform.settings.jpa.EntityConverter.convertContextToContextEntity;
import static org.exoplatform.settings.jpa.EntityConverter.convertScopeToScopeEntity;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 07, 2017
 */
public class JPASettingServiceImpl implements SettingService {

  private SettingsDAO settingsDAO;
  private SettingContextDAO settingContextDAO;
  private SettingScopeDAO settingScopeDAO;
  private final EventManagerImpl<JPASettingServiceImpl, SettingData> eventManager;

  /**
   * JPASettingServiceImpl must depend on DataInitializer to make sure data structure is created before initializing it
   */
  public JPASettingServiceImpl(SettingsDAO settingsDAO, SettingContextDAO settingContextDAO, SettingScopeDAO settingScopeDAO, EventManagerImpl<JPASettingServiceImpl, SettingData> eventManager, DataInitializer dataInitializer) {
    this.settingsDAO = settingsDAO;
    this.settingContextDAO = settingContextDAO;
    this.settingScopeDAO = settingScopeDAO;
    this.eventManager = eventManager;
  }


  @Override
  @ExoTransactional
  public void set(Context context, Scope scope, String key, SettingValue<?> value) {
    ContextEntity contextEntity = settingContextDAO.getContext(convertContextToContextEntity(context));
    if (contextEntity == null) {
      contextEntity = settingContextDAO.create(convertContextToContextEntity(context));
    }
    ScopeEntity scopeEntity = settingScopeDAO.getScope(convertScopeToScopeEntity(scope));
    if (scopeEntity == null) {
      scopeEntity = settingScopeDAO.create(convertScopeToScopeEntity(scope));
    }
    SettingsEntity settingsEntity = settingsDAO.getSetting(contextEntity, scopeEntity, key);
    if (settingsEntity != null) {
      settingsEntity.setName(key);
      settingsEntity.setContext(contextEntity);
      settingsEntity.setScope(scopeEntity);
      settingsEntity.setValue(value.getValue().toString());
    } else {
      settingsDAO.create(new SettingsEntity().setScope(scopeEntity).setContext(contextEntity).setName(key).setValue(value.getValue().toString()));
    }
    SettingData data = new SettingData (EventType.SETTING_SET,new SettingKey(context, scope, key),value);
    eventManager.broadcastEvent(new Event<JPASettingServiceImpl,SettingData>(data.getEventType().toString(),this,data));
  }

  @Override
  @ExoTransactional
  public void remove(Context context, Scope scope, String key) {
    SettingsEntity setting = settingsDAO.getSetting(convertContextToContextEntity(context),
        convertScopeToScopeEntity(scope), key);
    if (setting != null) {
      settingsDAO.delete(setting);
      SettingData data = new SettingData(EventType.SETTING_REMOVE_KEY,  new SettingKey(context, scope, key));
      eventManager.broadcastEvent(new Event<JPASettingServiceImpl,SettingData>(data.getEventType().toString(),this,data));
    }
  }

  @Override
  @ExoTransactional
  public void remove(Context context, Scope scope) {
    List<SettingsEntity> settings = settingsDAO.getSettingsByContextAndScope(context, scope);
      settingsDAO.deleteAll(settings);
      SettingData data = new SettingData(EventType.SETTING_REMOVE_SCOPE,  new SettingScope(context, scope));
      eventManager.broadcastEvent(new Event<JPASettingServiceImpl,SettingData>(data.getEventType().toString(),this,data));
  }

  @Override
  @ExoTransactional
  public void remove(Context context) {
    List<SettingsEntity> settings = settingsDAO.getSettingsByContext(context);
      settingsDAO.deleteAll(settings);
      SettingData data = new SettingData(EventType.SETTING_REMOVE_CONTEXT,  new SettingContext(context));
      eventManager.broadcastEvent(new Event<JPASettingServiceImpl,SettingData>(data.getEventType().toString(),this,data));
  }

  @Override
  @ExoTransactional
  public SettingValue<?> get(Context context, Scope scope, String key) {
    SettingsEntity setting = settingsDAO.getSetting(convertContextToContextEntity(context),
        convertScopeToScopeEntity(scope), key);
    if (setting == null) {
      return null; // Property doesn't exist
    } else {
      return SettingValue.create((String) setting.getValue());
    }
  }
}

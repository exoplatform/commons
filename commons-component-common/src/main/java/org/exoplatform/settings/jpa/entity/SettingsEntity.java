/*
 * Copyright (C) 2003-2017 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.settings.jpa.entity;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 07, 2017
 */
@Entity(name = "SettingsEntity")
@ExoEntity
@Table(name = "STG_SETTINGS")
@NamedQueries({
    @NamedQuery(name = "SettingsEntity.getSettingsByContextAndScope", query = "SELECT s FROM SettingsEntity s " +
        "WHERE s.context.type= :contextType " +
        "AND s.context.name= :contextName " +
        "AND s.scope.name= :scopeName " +
        "AND s.scope.type= :scopeType "),
    @NamedQuery(name = "SettingsEntity.getSettingsByContextAndScopeWithNullName", query = "SELECT s FROM SettingsEntity s " +
        "WHERE s.context.type= :contextType " +
        "AND s.context.name= :contextName " +
        "AND s.scope.name IS NULL " +
        "AND s.scope.type= :scopeType "),
    @NamedQuery(name = "SettingsEntity.getSettingByContextAndScopeAndKey", query = "SELECT s FROM SettingsEntity s " +
        "WHERE s.name = :settingName " +
        "AND s.context.type= :contextType " +
        "AND s.context.name= :contextName " +
        "AND s.scope.name= :scopeName " +
        "AND s.scope.type= :scopeType "),
    @NamedQuery(name = "SettingsEntity.getSettingByContextAndScopeWithNullNameAndKey", query = "SELECT s FROM SettingsEntity s " +
        "WHERE s.name = :settingName " +
        "AND s.context.type= :contextType " +
        "AND s.context.name= :contextName " +
        "AND s.scope.name IS NULL " +
        "AND s.scope.type= :scopeType "),
    @NamedQuery(name = "SettingsEntity.countSettingsByNameAndValueAndScope", query = "SELECT count(s.id) FROM SettingsEntity s " +
        "WHERE s.name = :settingName " +
        "AND s.value LIKE :settingValue " +
        "AND s.scope.name= :scopeName " +
        "AND s.scope.type= :scopeType "),
    @NamedQuery(name = "SettingsEntity.countSettingsByNameAndValueAndScopeWithNullName", query = "SELECT count(s.id) FROM SettingsEntity s " +
        "WHERE s.name = :settingName " +
        "AND s.value LIKE :settingValue " +
        "AND s.scope.name IS NULL " +
        "AND s.scope.type= :scopeType "),
    @NamedQuery(name = "SettingsEntity.getSettingsByContextByTypeAndName", query = "SELECT s FROM SettingsEntity s " +
        "WHERE s.context.type= :contextType " +
        "AND s.context.name= :contextName ")
})
public class SettingsEntity {
  @Id
  @Column(name = "SETTING_ID")
  @SequenceGenerator(name="SEQ_STG_SETTINGS_COMMON_ID", sequenceName="SEQ_STG_SETTINGS_COMMON_ID")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_STG_SETTINGS_COMMON_ID")
  private long id;

  @Column(name = "NAME")
  private String name;

  @Column(name = "VALUE", columnDefinition = "CLOB NOT NULL")
  @Lob
  private String value;

  @ManyToOne
  @JoinColumn(name = "CONTEXT_ID")
  private ContextEntity context;

  @ManyToOne
  @JoinColumn(name = "SCOPE_ID")
  private ScopeEntity scope;


  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public SettingsEntity setName(String name) {
    this.name = name;
    return this;
  }

  public String getValue() {
    return (value == null) ? "" : value;
  }

  public SettingsEntity setValue(String value) {
    this.value = value;
    return this;
  }

  public ContextEntity getContext() {
    return context;
  }

  public SettingsEntity setContext(ContextEntity context) {
    this.context = context;
    return this;
  }

  public ScopeEntity getScope() {
    return scope;
  }

  public SettingsEntity setScope(ScopeEntity scope) {
    this.scope = scope;
    return this;
  }
}

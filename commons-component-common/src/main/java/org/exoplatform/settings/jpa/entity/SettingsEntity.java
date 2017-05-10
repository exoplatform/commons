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
    @NamedQuery(name = "commons.getSettingsByUser", query = "SELECT s FROM SettingsEntity s " +
        "WHERE s.context.name= :user "),
    @NamedQuery(name = "commons.getSettingsByScope", query = "SELECT s FROM SettingsEntity s " +
        "WHERE s.scope.name= :scopeName " +
        "AND s.scope.type= :scopeType "),
    @NamedQuery(name = "commons.getAllSettingsIds", query = "SELECT s.id FROM SettingsEntity s ORDER BY s.id"),
    @NamedQuery(name = "commons.getSettingsByContextAndScope", query = "SELECT s FROM SettingsEntity s " +
        "WHERE s.context.type= :contextType " +
        "AND s.context.name= :contextName " +
        "AND s.scope.name= :scopeName " +
        "AND s.scope.type= :scopeType "),
    @NamedQuery(name = "commons.getSetting", query = "SELECT s FROM SettingsEntity s " +
        "WHERE s.name = :name " +
        "AND s.context.type= :contextType " +
        "AND s.context.name= :contextName " +
        "AND s.scope.name= :scopeName " +
        "AND s.scope.type= :scopeType "),
    @NamedQuery(name = "commons.getSettingsByContext", query = "SELECT s FROM SettingsEntity s " +
        "WHERE s.context.type= :contextType " +
        "AND s.context.name= :contextName "),
    @NamedQuery(name = "commons.getUserSettingsWithDeactivate", query = "SELECT s FROM SettingsEntity s " +
        "WHERE (s.context.name= :user) " +
        "AND (s.name= :isActive AND s.value='') " +
        "OR (s.name= :isEnabled AND s.value IS NOT NULL AND s.value='false') ")
})
public class SettingsEntity {
  @Id
  @Column(name = "SETTING_ID")
  @SequenceGenerator(name="SEQ_STG_SETTINGS_COMMON_ID", sequenceName="SEQ_STG_SETTINGS_COMMON_ID")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_STG_SETTINGS_COMMON_ID")
  private long id;

  @Column(name = "NAME")
  private String name;

  @Column(name = "VALUE")
  private String value;

  @ManyToOne(cascade = CascadeType.PERSIST)
  @JoinColumn(name = "CONTEXT_ID")
  private ContextEntity context;

  @ManyToOne(cascade = CascadeType.PERSIST)
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
    return value;
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

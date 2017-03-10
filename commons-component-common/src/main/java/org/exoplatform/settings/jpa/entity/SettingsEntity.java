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
        "OR (s.name= :isEnabled AND s.value IS NOT NULL AND s.value='false') "),
//    @NamedQuery(name = "commons.getUserSettingsForDigest", query = "SELECT s1 FROM SettingsEntity s1 " +
////        "WHERE s1.id IN " +
////        "(SELECT s2.id FROM SettingsEntity s2 WHERE s2.name= :exoIsActive AND s2.value LIKE :emailChannel " +
////        "AND s2.id IN " +
////        "(SELECT s3.id FROM SettingsEntity s3 WHERE s3.name= :exoIsEnabled AND (s3.value='true' or s3.value IS NULL)" +
////        "AND s3.context.type= :contextType))" +
////        "AND s1.name= :frequency AND s1.value IS NOT NULL ")SELECT s1 FROM SettingsEntity s1 " +
//        "JOIN  s1 s2 " +
//        "JOIN  s2 s3 " +
//        "WHERE s3.name= :exoIsEnabled AND (s3.value='true' or s3.value IS NULL) " +
//        "AND s3.context.type= :contextType " +
//        "AND s2.name= :exoIsActive AND s2.value LIKE :emailChannel " +
//        "AND s1.name= :frequency AND s1.value IS NOT NULL ")
////        "JOIN (SELECT s2 FROM SettingsEntity s2 WHERE s2.name= :exoIsActive AND s2.value LIKE :emailChannel) AS T1 " +
////        "JOIN (SELECT s3 FROM SettingsEntity s3 WHERE s3.name= :exoIsEnabled AND (s3.STG_VALUE='true' or s3.STG_VALUE IS NULL)) AS T2 " +
////        "WHERE s1.name= :frequency AND s1.value IS NOT NULL ")
})
public class SettingsEntity {
  @Id
  @Column(name = "STG_ID")
  @SequenceGenerator(name="SEQ_STG_SETTINGS_COMMON_ID", sequenceName="SEQ_STG_SETTINGS_COMMON_ID")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_STG_SETTINGS_COMMON_ID")
  private long id;

  @Column(name = "STG_NAME")
  private String name;

  @Column(name = "STG_VALUE")
  private String value;

  @ManyToOne(cascade = CascadeType.PERSIST)
  @JoinColumn(name = "STG_CONTEXT")
  private ContextEntity context;

  @ManyToOne(cascade = CascadeType.PERSIST)
  @JoinColumn(name = "STG_SCOPE")
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

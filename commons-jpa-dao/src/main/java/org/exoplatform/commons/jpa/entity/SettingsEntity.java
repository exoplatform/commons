//package org.exoplatform.settings.jpa;
//
//import org.chromattic.api.annotations.OneToMany;
//import org.chromattic.api.annotations.PrimaryType;
//import org.exoplatform.settings.chromattic.ContextEntity;
//
//import java.util.Map;
//
///**
// * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
// */
//@PrimaryType(name = "stg:settings")
//public abstract class SettingsRoot {
//
//  @OneToMany
//  protected abstract Map<String, ContextEntity> getContexts();
//
//  public ContextEntity getContext(String contextName) {
//    return getContexts().get(contextName);
//  }
//
//}

/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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
package org.exoplatform.commons.jpa.entity;

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
    @NamedQuery(name = "commons.getSettingsByUser", query = "SELECT w.id FROM SettingsEntity w"), //+
        //"JOIN STG_CONTEXT ON STG_SETTINGS.STG_CONTEXT_ID=STG_CONTEXT.STG_CONTEXT_ID "+
        //"WHERE STG_CONTEXT.STG_CONTEXT_TYPE='USER' AND STG_CONTEXT.STG_CONTEXT_NAME= :user"),
    @NamedQuery(name = "commons.getSettingsByApplication", query = "SELECT w.id FROM SettingsEntity w"), //+
//        "JOIN STG_CONTEXT ON STG_SETTINGS.STG_CONTEXT_ID=STG_CONTEXT.STG_CONTEXT_ID " +
//        "JOIN STG_SCOPE ON STG_SETTINGS.STG_SCOPE_ID=STG_SCOPE.STG_SCOPE_ID " +
//        "WHERE STG_CONTEXT.STG_CONTEXT_TYPE='USER' " +
//        "AND STG_CONTEXT.STG_CONTEXT_NAME= :user " +
//        "AND STG_SCOPE.STG_SCOPE_TYPE='APPLICATION' " +
//        "AND STG_SCOPE.STG_SCOPE_NAME= :application"),
    @NamedQuery(name = "commons.getSettingOfUser", query = "SELECT w.id FROM SettingsEntity w") //+
//        "JOIN STG_CONTEXT ON STG_SETTINGS.STG_CONTEXT_ID=STG_CONTEXT.STG_CONTEXT_ID " +
//        "JOIN STG_SCOPE ON STG_SETTINGS.STG_SCOPE_ID=STG_SCOPE.STG_SCOPE_ID " +
//        "WHERE STG_CONTEXT.STG_CONTEXT_TYPE='USER' " +
//        "AND STG_CONTEXT.STG_CONTEXT_NAME= :user " +
//        "AND STG_SCOPE.STG_SCOPE_TYPE='GLOBAL' " +
//        "AND STG_SETTINGS.STG_NAME= :setting")
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

  @OneToOne
  @JoinColumn(name = "STG_CONTEXT")
  private ContextEntity context;

  @OneToOne
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

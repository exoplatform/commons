package org.exoplatform.settings.jpa.entity;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 07, 2017
 */
@Entity(name = "SettingsScopeEntity")
@ExoEntity
@Table(name = "STG_SCOPES")
@NamedQueries({
    @NamedQuery(name = "commons.getScope", query = "SELECT s FROM SettingsScopeEntity s " +
        "WHERE s.name = :name " +
        "AND s.type= :scopeType ")
})
public class ScopeEntity {
  @Id
  @Column(name = "SCOPE_ID")
  @SequenceGenerator(name="SEQ_STG_SCOPE_COMMON_ID", sequenceName="SEQ_STG_SCOPE_COMMON_ID")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_STG_SCOPE_COMMON_ID")
  private long id;

  @Column(name = "NAME")
  private String name;

  @Column(name = "TYPE")
  private String type;

  @OneToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "scope")
  private Set<SettingsEntity> settings;

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public ScopeEntity setName(String name) {
    this.name = name;
    return this;
  }

  public String getType() {
    return type;
  }

  public ScopeEntity setType(String type) {
    this.type = type;
    return this;
  }

  public Set<SettingsEntity> getSettings() {
    return settings;
  }

  public void setSettings(Set<SettingsEntity> settings) {
    this.settings = settings;
  }
}

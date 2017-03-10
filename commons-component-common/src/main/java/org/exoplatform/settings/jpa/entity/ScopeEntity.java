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
@Entity(name = "ScopeEntity")
@ExoEntity
@Table(name = "STG_SCOPE")
@NamedQueries({
    @NamedQuery(name = "commons.getScope", query = "SELECT s FROM ScopeEntity s " +
        "WHERE s.name = :name " +
        "AND s.type= :scopeType "),
    @NamedQuery(name = "commons.getScopeOfType", query = "SELECT s FROM ScopeEntity s " +
        "WHERE s.type= :scopeType " +
        "AND s.name IS NULL ")
})
public class ScopeEntity {
  @Id
  @Column(name = "STG_SCOPE_ID")
  @SequenceGenerator(name="SEQ_STG_SCOPE_COMMON_ID", sequenceName="SEQ_STG_SCOPE_COMMON_ID")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_STG_SCOPE_COMMON_ID")
  private long id;

  @Column(name = "STG_SCOPE_NAME")
  private String name;

  @Column(name = "STG_SCOPE_TYPE")
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

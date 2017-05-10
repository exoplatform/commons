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
@Entity(name = "ContextEntity")
@ExoEntity
@Table(name = "STG_CONTEXTS")
@NamedQueries({
    @NamedQuery(name = "commons.getContext", query = "SELECT c FROM ContextEntity c " +
        "WHERE c.name = :name " +
        "AND c.type= :contextType "),
    @NamedQuery(name = "commons.getContextofType", query = "SELECT c FROM ContextEntity c " +
        "WHERE c.type= :contextType ")
})
public class ContextEntity {
  @Id
  @Column(name = "CONTEXT_ID")
  @SequenceGenerator(name="SEQ_STG_CONTEXT_COMMON_ID", sequenceName="SEQ_STG_CONTEXT_COMMON_ID")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_STG_CONTEXT_COMMON_ID")
  private long id;

  @Column(name = "NAME")
  private String name;

  @Column(name = "TYPE")
  private String type;

  @OneToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "context")
  private Set<SettingsEntity> settings;

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public ContextEntity setName(String name) {
    this.name = name;
    return this;
  }

  public String getType() {
    return type;
  }

  public ContextEntity setType(String type) {
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


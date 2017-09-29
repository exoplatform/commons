package org.exoplatform.settings.jpa.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.exoplatform.commons.api.persistence.ExoEntity;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 07, 2017
 */
@Entity(name = "SettingsContextEntity")
@ExoEntity
@Table(name = "STG_CONTEXTS")
@NamedQueries({
    @NamedQuery(name = "SettingsContextEntity.getContextByTypeAndName", query = "SELECT c FROM SettingsContextEntity c " +
        "WHERE c.name = :contextName " +
        "AND c.type= :contextType "),
    @NamedQuery(name = "SettingsContextEntity.getContextByTypeWithNullName", query = "SELECT c FROM SettingsContextEntity c " +
        "WHERE c.name IS NULL " +
        "AND c.type= :contextType "),
    @NamedQuery(name = "SettingsContextEntity.getEmptyContextsByScopeAndContextType", query = "SELECT distinct(c) FROM SettingsContextEntity c " +
        "WHERE  c.type = :contextType " +
        "AND NOT EXISTS( " +
        " SELECT s FROM SettingsEntity s " +
        " JOIN s.context c2 " +
        " JOIN s.scope sc " +
        " WHERE c2.id = c.id " +
        " AND sc.type = :scopeType " +
        " AND sc.name = :scopeName " +
        ")"),
    @NamedQuery(name = "SettingsContextEntity.getEmptyContextsByScopeWithNullNameAndContextType", query = "SELECT distinct(c) FROM SettingsContextEntity c " +
        "WHERE c.type = :contextType " +
        "AND NOT EXISTS( " +
        " SELECT s FROM SettingsEntity s " +
        " JOIN s.context c2 " +
        " JOIN s.scope sc " +
        " WHERE c2.id = c.id " +
        " AND sc.type = :scopeType " +
        " AND sc.name IS NULL " +
        ")"),
    @NamedQuery(name = "SettingsContextEntity.getContextsByTypeAndScopeAndSettingName", query = "SELECT distinct(s.context) FROM SettingsEntity s " +
        "JOIN s.context c " +
        "JOIN s.scope sc " +
        "WHERE sc.name = :scopeName " +
        "AND sc.type = :scopeType " +
        "AND c.type = :contextType " +
        "AND s.name = :settingName "),
    @NamedQuery(name = "SettingsContextEntity.getContextsByTypeAndScopeWithNullNameAndSettingName", query = "SELECT distinct(s.context) FROM SettingsEntity s " +
        "JOIN s.context c " +
        "JOIN s.scope sc " +
        "WHERE sc.name = :scopeName " +
        "AND sc.type = :scopeType " +
        "AND c.type = :contextType " +
        "AND s.name = :settingName "),
    @NamedQuery(name = "SettingsContextEntity.countContextsByType", query = "SELECT count(c) FROM SettingsContextEntity c " +
        "WHERE c.type = :contextType "),
    @NamedQuery(name = "SettingsContextEntity.getContextNamesByType", query = "SELECT c.name FROM SettingsContextEntity c " +
        "WHERE c.type = :contextType ")
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

  @OneToMany(fetch=FetchType.LAZY, mappedBy = "context")
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


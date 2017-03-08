package org.exoplatform.commons.jpa.entity;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 07, 2017
 */
@Entity(name = "ContextEntity")
@ExoEntity
@Table(name = "STG_CONTEXT")
//@NamedQueries({
//    @NamedQuery(name = "commons.getAllIds", query = "SELECT w.id FROM WikiWikiEntity w ORDER BY w.id"),
//    @NamedQuery(name = "commons.getWikisByType", query = "SELECT w FROM WikiWikiEntity w WHERE w.type = :type"),
//    @NamedQuery(name = "commons.getWikiByTypeAndOwner", query = "SELECT w FROM WikiWikiEntity w WHERE w.type = :type AND w.owner = :owner")
//})
public class ContextEntity {
  @Id
  @Column(name = "STG_CONTEXT_ID")
  @SequenceGenerator(name="SEQ_STG_CONTEXT_COMMON_ID", sequenceName="SEQ_STG_CONTEXT_COMMON_ID")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_STG_CONTEXT_COMMON_ID")
  private long id;

  @Column(name = "STG_CONTEXT_NAME")
  private String name;

  @Column(name = "STG_CONTEXT_TYPE")
  private String type;

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
}


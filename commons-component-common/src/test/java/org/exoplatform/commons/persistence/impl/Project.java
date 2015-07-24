package org.exoplatform.commons.persistence.impl;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.exoplatform.commons.api.persistence.ExoEntity;

@Entity
@ExoEntity
public class Project {
  @Id
  @GeneratedValue
  private Long id;

  public Long getId() {
    return id;
  }
}

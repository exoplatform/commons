package org.exoplatform.commons.persistence.impl;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@ExoEntity
public class Task {
  @Id
  @GeneratedValue
  private Long id;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
}

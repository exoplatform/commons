package org.exoplatform.commons.persistence.impl;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Task {
  @Id
  @GeneratedValue
  private Long id;

  public Long getId() {
    return id;
  }
}

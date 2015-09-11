package org.exoplatform.commons.persistence.impl;

import org.exoplatform.commons.api.persistence.ExoTransactional;

public class TaskService {
  private TaskDao dao = new TaskDao();

  @ExoTransactional
  public Task create(Task task) {
    return dao.create(task);
  }

  public Task find(long id) {
    return dao.find(id);
  }

  public void delete(Task task) {
    dao.delete(task);
  }
}

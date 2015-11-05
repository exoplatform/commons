package org.exoplatform.commons.persistence.impl;

import javax.persistence.Query;

import org.exoplatform.commons.api.persistence.ExoTransactional;

import java.util.List;

public class TaskDao extends GenericDAOJPAImpl<Task, Long> {

  @ExoTransactional
  public void createWithRollback(Task task) {
    getEntityManager().persist(task);
    getEntityManager().getTransaction().rollback();
  }

  public void nonTransactionalDeleteAll() {
    Query query = getEntityManager().createQuery("Delete from Task");
    query.executeUpdate();
  }

  @ExoTransactional
  public void createWithCommit(Task task) {
    getEntityManager().persist(task);
    getEntityManager().getTransaction().commit();
  }

  @Override
  @ExoTransactional
  //We invoke this method within an @ExoTransactional context because
  //our TU are not run within a portal lifecycle => there is no EM in the threadLocal
  public List<Task> findAll() {
    return super.findAll();
  }
}

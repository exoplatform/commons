package org.exoplatform.commons.persistence.impl;

import org.exoplatform.container.PortalContainer;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.TransactionRequiredException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class ExoTransactionalAnnotationTest {

  @Test
  public void testCreateDaoMethodIsTransactional() {
    // Given
    TaskDao dao = new TaskDao();
    // When
    long id = dao.create(new Task()).getId();
    // Then
    assertNotNull(dao.find(id));
  }

  @Test
  public void testCreateServiceMethodIsTransactional() {
    // Given
    TaskService service = new TaskService();
    // When
    long id = service.create(new Task()).getId();
    // Then
    assertNotNull(service.find(id));
  }

  @Test
  public void testDeleteDaoMethodIsTransactional() {
    // Given
    TaskService service = new TaskService();
    Task task = service.create(new Task());
    // When
    service.delete(task);
    // Then
    assertNull(service.find(task.getId()));
  }

  @Test(expected = TransactionRequiredException.class)
  public void testNonTransactionalMethodDoesNotCommit() {
    // Given
    TaskService service = new TaskService();
    TaskDao dao = new TaskDao();
    service.create(new Task());
    // When
    dao.nonTransactionalDeleteAll();
    // Then
    fail("deleteAll must throw an exception");
  }

  @Test
  public void testManualRollback() {
    // Given
    TaskDao dao = new TaskDao();
    // When
    dao.createWithRollback(new Task());
    // Then
    assertThat(dao.findAll().size(), is(0));
  }

  @Test
  public void testManualCommit() {
    // Given
    TaskDao dao = new TaskDao();
    // When
    dao.createWithCommit(new Task());
    // Then
    assertThat(dao.findAll().size(), is(1));
  }

  @Test
  public void testCommitFailed() {
    EntityManagerService service = PortalContainer.getInstance().getComponentInstanceOfType(EntityManagerService.class);
    assertNull(service.getEntityManager());

    TaskDao dao = new TaskDao();
    Task task = new Task();
    task.setId((long) 1); // Invalid set to cause commit failed.
    try {
      dao.create(task);
    } catch (RuntimeException e) {
      // Expected.
    }

    assertNull(service.getEntityManager());
  }

  @Test
  public void testMultiThreading() throws ExecutionException, InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(2);

    List<Future<Boolean>> list = new ArrayList<>();
    Callable<Boolean> callable = new MyCallable();
    for (int i = 0; i < 10; i++) {
      Future<Boolean> future = executor.submit(callable);
      list.add(future);
    }

    for (Future<Boolean> fut : list) {
      assertTrue(fut.get());
    }
    // shut down the executor service now
    executor.shutdown();
  }

  private class MyCallable implements Callable<Boolean> {
    @Override
    public Boolean call() throws Exception {
      // Given
      TaskDao dao = new TaskDao();
      // When
      long id = dao.create(new Task()).getId();
      // Then
      return (dao.find(id) != null);
    }
  }

  @Test
  public void testRequestLifeCycle() {
    // Given
    PortalContainer container = PortalContainer.getInstance();
    EntityManagerService service = container.getComponentInstanceOfType(EntityManagerService.class);
    service.startRequest(container);
    TaskDao dao = new TaskDao();
    dao.create(new Task());
    // When
    service.endRequest(container);
    // Then
    assertThat(dao.findAll().size(), is(1));
  }

  @Before
  public void deleteAllTask() {
    new TaskDao().deleteAll();
  }

  @Test
  public void test_ifRollbackOnlyTrue_transactionRollbackWithNoError() {
    // Given
    TaskDao dao = new TaskDao();
    // When
    dao.createWithSetRollbackOnly(new Task());
    // Then
    assertThat(dao.findAll().size(), is(0));
  }

  @Test
  public void test_ifRollbackPreviousTransaction_noErrorToCommitNextTransaction() {
    // Given
    PortalContainer container = PortalContainer.getInstance();
    EntityManagerService service = container.getComponentInstanceOfType(EntityManagerService.class);
    service.startRequest(container);
    TaskDao dao = new TaskDao();
    dao.createWithSetRollbackOnly(new Task());
    // When
    dao.create(new Task());
    // Then
    service.endRequest(container);
    assertThat(dao.findAll().size(), is(1));
  }
}

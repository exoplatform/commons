package org.exoplatform.commons.persistence.impl;

import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.jpa.BaseTest;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.TransactionRequiredException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


public class ExoTransactionalAnnotationTest extends BaseTest {
  @Override
  public void setUp() {
    getContainer();
  }

  @Override
  public void tearDown() {
    // don't call tearDown on super to not clean DB
    // super.tearDown();
  }

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

  @Test
  public void testNonTransactionalMethodDoesNotCommit() {
    // Given
    TaskService service = new TaskService();
    TaskDao dao = new TaskDao();
    service.create(new Task());
    // When
    try {
      dao.nonTransactionalDeleteAll();
      fail("deleteAll must throw an exception");
    } catch (TransactionRequiredException e) {
      // Then
    }
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
    } catch (Exception e) {
      // Expected.
    }

    assertNull(service.getEntityManager());
  }

  @Test
  public void testMultipleTransactions() {
    PortalContainer container = getContainer();
    EntityManagerService entityManagerService = getContainer().getComponentInstanceOfType(EntityManagerService.class);
    entityManagerService.startRequest(container);

    // Given
    TaskDao dao = new TaskDao();
    int initialSize = dao.findAll().size();
    // When
    try {
      // create first task
      Task task1 = new Task();
      task1.setName("task");
      dao.create(task1);
    } catch(Exception e) {
      fail(e.getMessage());
    }
    try {
      // create second task with the same name -> fails because of unicity constraints on task name
      Task task2 = new Task();
      task2.setName("task");
      dao.create(task2);
    } catch(Exception e) {
      // expected exception
    }
    try {
      // create thrid task -> should work since it is in another transaction
      Task task3 = new Task();
      task3.setName("other task");
      dao.create(task3);
    } catch(Exception e) {
      fail(e.getMessage());
    }

    // Then
    assertThat(dao.findAll().size(), is(initialSize + 2));

    entityManagerService.endRequest(container);
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
      // Make sure the container has been created
      PortalContainer.getInstance();

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
    PortalContainer container = getContainer();
    // Given
    EntityManagerService service = getContainer().getComponentInstanceOfType(EntityManagerService.class);
    service.startRequest(container);
    TaskDao dao = new TaskDao();
    int initialSize = dao.findAll().size();
    dao.create(new Task());
    // When
    service.endRequest(container);
    // Then
    assertThat(dao.findAll().size(), is(initialSize + 1));
  }

  @Before
  public void deleteAllTask() {
    begin();
    new TaskDao().deleteAll();
    end();
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
    PortalContainer container = getContainer();
    EntityManagerService service = container.getComponentInstanceOfType(EntityManagerService.class);
    service.startRequest(container);

    TaskDao dao = new TaskDao();
    int initialSize = dao.findAll().size();

    dao.createWithSetRollbackOnly(new Task());
    // When
    dao.create(new Task());
    // Then
    service.endRequest(container);
    assertThat(dao.findAll().size(), is(initialSize + 1));
  }
}

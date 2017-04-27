package org.exoplatform.commons.persistence.impl;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.jpa.CommonsDAOJPAImplTest;

import javax.persistence.EntityManager;

public class TestEntityManagerService extends CommonsDAOJPAImplTest {

  private EntityManagerService service;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    service = PortalContainer.getInstance().getComponentInstanceOfType(EntityManagerService.class);
  }

  public void testServiceInitialize() {
    assertNotNull(service);

    EntityManager em1 = service.getEntityManager();
    assertNotNull(em1);
  }

  public void testStartLifecycleTwice() {
    EntityManager em1 = service.getEntityManager();
    assertNotNull(em1);

    //
    RequestLifeCycle.begin(service);
    EntityManager em2 = service.getEntityManager();
    assertSame(em2, em1);

    RequestLifeCycle.end();
    EntityManager em3 = service.getEntityManager();
    assertNotNull(em3);
    assertSame(em3, em1);
  }

  public void testEntityManagerOutsideDefaultLifecycle() {
    EntityManager em1 = service.getEntityManager();
    assertNotNull(em1);

    //
    A a = new A(em1);
    a.run();
    assertTrue(a.succeed);
    assertTrue(a.sameEntity);

    // Try to run in a different thread (out of default lifecycle)
    a = new A(em1);
    Thread t = new Thread(a);
    t.start();
    try {
      t.join();
    } catch (InterruptedException e) {
      fail("Thread is interrupted");
    }
    assertTrue(a.succeed);
    assertFalse(a.sameEntity);
  }

  class A implements Runnable {

    private EntityManager em1;

    private boolean       succeed;

    private boolean       sameEntity;

    public A(EntityManager em) {
      this.em1 = em;
    }

    @Override
    public void run() {
      RequestLifeCycle.begin(service);

      EntityManager em2 = service.getEntityManager();
      if (em2 != null && em2.isOpen()) {
        succeed = true;
      }
      sameEntity = em2 == em1;

      RequestLifeCycle.end();
    }
  }
}

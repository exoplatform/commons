package org.exoplatform.commons.notification.storage;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.jcr.Node;

import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.notification.BaseNotificationTestCase;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.idm.UserImpl;


public class WebStorageMultiThreadTest extends BaseNotificationTestCase {
  private static final Log LOG = ExoLogger.getLogger(WebStorageMultiThreadTest.class);
  private OrganizationService organizationService;
  private ExecutorService executor;
  
  private int NUMBER_THREAD = 5;
  private int NUMBER_USER = 10;
  
  private int HCR_save = 0;
  private int HCT_get_time = 0;

  private long HCT_time = 0l;
  
  @Override
  public void setUp() throws Exception {
    initCollaborationWorkspace();
    super.setUp();
    //
    organizationService = getService(OrganizationService.class);
    create(NUMBER_USER, false);
    //
    create(NUMBER_USER, true);
    
    //
    ThreadFactory threadFactory = new ThreadFactory() {
      public Thread newThread(Runnable arg0) {
        return new Thread(arg0, "User thread");
      }
    };
    int threads = (NUMBER_THREAD > NUMBER_USER * 2) ? NUMBER_THREAD : NUMBER_USER * 2;
    executor = Executors.newFixedThreadPool(threads+5, threadFactory);
    //
    HCR_save = 0;
  }

  @Override
  protected void tearDown() throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    for (String userId : userIds) {
      Node userNodeApp = nodeHierarchyCreator.getUserApplicationNode(sessionProvider, userId);
      if (userNodeApp.hasNode(NOTIFICATION)) {
        userNodeApp.getNode(NOTIFICATION).remove();
        userNodeApp.save();
      }
    }
    for (String string : userIds) {
      organizationService.getUserHandler().removeUser(string, false);
    }
    //
    executor.shutdownNow();
    //
    super.tearDown();
  }

  private void create(int number, boolean isSameFirst) throws Exception {
    for (int i = 0; i < number; i++) {
      String first = (isSameFirst) ? "" : String.valueOf(new Random().nextInt(1000));
      String userId = first + "user" + i + "_"+ String.valueOf(IdGenerator.generate()).hashCode();
      User user = new UserImpl(userId);
      organizationService.getUserHandler().createUser(user, true);
      //
      userIds.add(userId);
    }
    LOG.info("Done to create " + number + " users");
  }
  
  public void testWebDatastorage() throws Exception {
    //
    LOG.info("\nThread: " + NUMBER_THREAD + 
             "\nUsers: " + userIds.size());
    //
    CountDownLatch latch = new CountDownLatch(NUMBER_THREAD * NUMBER_USER * 2);
    //
    for (final String userId : userIds) {
      for (int i = 0; i < NUMBER_THREAD; i++) {
        executor.execute(new Processor(latch) {
          @Override
          public void process() {
            long t = System.currentTimeMillis();
            try {
              storage.save(makeWebNotificationInfo(userId));
              ++HCR_save;
            } catch (Exception e) {
              assertFalse(true);
              LOG.error(e);
            } finally {
              HCT_time += (System.currentTimeMillis() - t);
            }
          }
        });
      }
    }
    waitCompletionFinished(latch);
    //
    LOG.info("\nTotal number of notifications saved: " + HCR_save + " total time: " + HCT_time + " ms");
    //
    latch = new CountDownLatch(NUMBER_USER * 2);
    for (final String userId : userIds) {
      executor.execute(new Processor(latch) {
        @Override
        public void process() {
          long t = System.currentTimeMillis();
          try {
            WebNotificationFilter filter = new WebNotificationFilter(userId);
            //
            storage.get(filter, 0, 20);
          } catch (Exception e) {
            assertFalse(true);
            LOG.error(e);
          } finally {
            HCT_get_time += (System.currentTimeMillis() - t);
          }
        }
      });
    }
    //
    waitCompletionFinished(latch);
    Thread.sleep(1000);
    LOG.info("\nTotal time get notifications: " + HCT_get_time + " ms");
  }
  
  public void waitCompletionFinished(CountDownLatch latch) throws InterruptedException {
    try {
      while (latch.getCount() > 0) {
        Thread.sleep(1000);
      }; // wait untill latch counted down to 0
    } catch (Exception e) {
      LOG.warn(e);
    }
  }

  public abstract class Processor implements Runnable {
    protected CountDownLatch latch;

    public Processor(CountDownLatch latch) {
      this.latch = latch;
    }
    public void run() {
      process();
      latch.countDown();
    }
    protected abstract void process();
  }
}

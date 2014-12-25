package org.exoplatform.commons.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.jcr.Node;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.model.WebFilter;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.plugin.PluginTest;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.idm.UserImpl;

public class WebStorageMultiThreadTestCase extends BaseNotificationTestCase {
  private static final Log LOG = ExoLogger.getLogger(WebStorageMultiThreadTestCase.class);
  private NodeHierarchyCreator nodeHierarchyCreator;
  private OrganizationService organizationService;
  private WebNotificationStorage   webStorage;
  private ExecutorService executor;
  
  private int NUMBER_THREAD = 10;
  private int NUMBER_USER = 20;
  
  private int HCR_save = 0;
  private int HCT_get_time = 0;

  private long HCT_time = 0l;
  
  private final String WORKSPACE_COLLABORATION = "collaboration";
  private List<String> userIds;
  public WebStorageMultiThreadTestCase() {
  }
  @Override
  public void setUp() throws Exception {
    super.setUp();
    //
    organizationService = getService(OrganizationService.class);
    nodeHierarchyCreator = getService(NodeHierarchyCreator.class);
    webStorage = getService(WebNotificationStorage.class);
    //
    ManageableRepository repo = repositoryService.getRepository(REPO_NAME);
    repo.getConfiguration().setDefaultWorkspaceName(WORKSPACE_COLLABORATION);
    session = repo.getSystemSession(WORKSPACE_COLLABORATION);
    root = session.getRootNode();
    //
    addNodeEventListener();
    //
    userIds = new ArrayList<String>();
    //
    craeteUsers(NUMBER_USER, false);
    //
    craeteUsers(NUMBER_USER, true);
    
    //
    ThreadFactory threadFactory = new ThreadFactory() {
      public Thread newThread(Runnable arg0) {
        return new Thread(arg0, "User thread");
      }
    };
    int threads = (NUMBER_THREAD > NUMBER_USER) ? NUMBER_THREAD : NUMBER_USER ;
    executor = Executors.newFixedThreadPool(threads+5, threadFactory);
    //
    HCR_save = 0;
  }
  
  @Override
  protected void tearDown() throws Exception {
    for (String userId : userIds) {
      Node userNodeApp = nodeHierarchyCreator.getUserApplicationNode(SessionProvider.createSystemProvider(), userId);
      if(userNodeApp.hasNode("notification")) {
        userNodeApp.getNode("notification").remove();
      }
    }
    session.save();
    for (String string : userIds) {
      organizationService.getUserHandler().removeUser(string, false);
    }
    super.tearDown();
  }

  private void craeteUsers(int number, boolean isSameFirst) throws Exception {
    for (int i = 0; i < number; i++) {
      String first = (isSameFirst) ? "" : String.valueOf(new Random().nextInt(1000));
      String userId = first + "user" + i + "_"+ String.valueOf(IdGenerator.generate()).hashCode();
      User user = new UserImpl(userId);
      organizationService.getUserHandler().createUser(user, true);
      //
      userIds.add(userId);
    }
    LOG.info("Done to cureate " + number + " users");
  }

  private NotificationInfo makeWebNotificationInfo(String userId) {
    NotificationInfo info = NotificationInfo.instance();
    info.key(new PluginKey(PluginTest.ID));
    info.setTitle("The title of test notification");
    info.setFrom("abc_user");
    info.setTo(userId);
    info.with(AbstractService.NTF_SHOW_POPOVER, "true")
        .with(AbstractService.NTF_READ, "false")
        .with("sender", "abc_user")
        .with("activityId", "TheActivityId")
        .with("accessLink", "http://fsdfsdf.com/fsdfsf");
    return info;
  }
  
  public void testWebDatastorage() throws Exception {
    //
    LOG.info("\nThread: " + NUMBER_THREAD + 
             "\nUsers: " + userIds.size());
    //
    CountDownLatch latch = new CountDownLatch(NUMBER_THREAD * NUMBER_USER);
    //
    for (final String userId : userIds) {
      for (int i = 0; i < NUMBER_THREAD; i++) {
        executor.execute(new Processor(latch) {
          @Override
          public void process() {
            long t = System.currentTimeMillis();
            RequestLifeCycle.begin(container);
            try {
              webStorage.save(makeWebNotificationInfo(userId));
              ++HCR_save;
            } catch (Exception e) {
              assertFalse(true);
              LOG.error(e);
            } finally {
              RequestLifeCycle.end();
              latch.countDown();
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
    latch = new CountDownLatch(NUMBER_USER);
    for (final String userId : userIds) {
      executor.execute(new Processor(latch) {
        @Override
        public void process() {
          long t = System.currentTimeMillis();
          try {
            WebFilter filter = new WebFilter(userId, 0, 20);
            //
            webStorage.get(filter);
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
      latch.await(); // wait untill latch counted down to 0
    } catch (InterruptedException e) {
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

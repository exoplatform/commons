package org.exoplatform.commons.notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

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
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.idm.UserImpl;

public class UserNodeCreatorTestCase extends BaseNotificationTestCase {
  private static final Log LOG = ExoLogger.getLogger(UserNodeCreatorTestCase.class);
  private NodeHierarchyCreator nodeHierarchyCreator;
  private OrganizationService organizationService;
  private WebNotificationStorage   webStorage;
  private ExecutorService executor;
  
  private int NUMBER_THREAD = 10;
  private int NUMBER_USER = 20;
  
  private int HCR_save = 0;
  private int HCT_get_time = 0;

  private long HCT_time = 0l;
  
  private long index = 0l;
  
  private final String WORKSPACE_COLLABORATION = "collaboration";
  private List<String> userIds;
  public UserNodeCreatorTestCase() {
  }
  @Override
  public void setUp() throws Exception {
    super.setUp();
    //
    organizationService = getService(OrganizationService.class);
    nodeHierarchyCreator = getService(NodeHierarchyCreator.class);
    webStorage = getService(WebNotificationStorage.class);
    //
    Node nftNode = null;
    if(!session.getRootNode().hasNode("eXoNotification")) {
      nftNode = session.getRootNode().addNode(AbstractService.NOTIFICATION_HOME_NODE, AbstractService.NTF_NOTIFICATION);
      nftNode.addNode(AbstractService.WEB_CHANNEL, AbstractService.NTF_CHANNEL);
    } else if(!session.getRootNode().hasNode("eXoNotification/web")) {
      session.getRootNode().getNode(AbstractService.NOTIFICATION_HOME_NODE)
             .addNode(AbstractService.WEB_CHANNEL, AbstractService.NTF_CHANNEL);
    }
    session.save();
    //
    ManageableRepository repo = repositoryService.getRepository(REPO_NAME);
    repo.getConfiguration().setDefaultWorkspaceName(WORKSPACE_COLLABORATION);
    session = repo.getSystemSession(WORKSPACE_COLLABORATION);
    root = session.getRootNode();
    //
    userIds = new ArrayList<String>();
    //
    craeteUsers(NUMBER_USER, false);
    
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
  
  public void testHierarchyCreator() throws Exception {
    //
    LOG.info("\nThread: " + NUMBER_THREAD + 
             "\nUsers: " + userIds.size());
    for (final String userId : userIds) {
      Node userNodeApp = nodeHierarchyCreator.getUserApplicationNode(SessionProvider.createSystemProvider(), userId);
      //
      getOrCreateWebCurrentUserNode(userNodeApp);
    }
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
            SessionProvider sessionProvider = SessionProvider.createSystemProvider();
            try {
              Node userNodeApp = nodeHierarchyCreator.getUserApplicationNode(sessionProvider, userId);
              //
              save(userNodeApp, makeWebNotificationInfo(userId));
              latch.countDown();
            } catch (Exception e) {
              e.printStackTrace();
              assertFalse(true);
            } finally {
              sessionProvider.close();
              RequestLifeCycle.end();
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
    index = 0;
    for (final String userId : userIds) {
      executor.execute(new Processor(latch) {
        @Override
        public void process() {
          long t = System.currentTimeMillis();
          SessionProvider sessionProvider = SessionProvider.createSystemProvider();
          try {
            List<NotificationInfo> notificationInfos = new ArrayList<NotificationInfo>();
            WebFilter filter = new WebFilter(userId, 0, 20);
            Node userNodeApp = nodeHierarchyCreator.getUserApplicationNode(sessionProvider, userId);
            filter.setJcrPath(getOrCreateWebCurrentUserNode(userNodeApp).getPath());
            NodeIterator it = gets(session, filter);
            while (it.hasNext()) {
              Node node = it.nextNode();
              notificationInfos.add(fillModel(node)) ;
            }
            System.out.print(" " + (++index));
          } catch (Exception e) {
            e.printStackTrace();
            assertFalse(true);
          } finally {
            latch.countDown();
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
  
  
  public void testWebDatastorage() throws Exception {
    //
    LOG.info("\nThread: " + NUMBER_THREAD + 
             "\nUsers: " + userIds.size());
    for (final String userId : userIds) {
      Node userNodeApp = nodeHierarchyCreator.getUserApplicationNode(SessionProvider.createSystemProvider(), userId);
      //
      getOrCreateWebCurrentUserNode(userNodeApp);
    }
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
              e.printStackTrace();
              assertFalse(true);
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
    index = 0;
    for (final String userId : userIds) {
      executor.execute(new Processor(latch) {
        @Override
        public void process() {
          long t = System.currentTimeMillis();
          try {
            WebFilter filter = new WebFilter(userId, 0, 20);
            //
            webStorage.get(filter);
            System.out.print(" " + (++index));
          } catch (Exception e) {
            e.printStackTrace();
            assertFalse(true);
          } finally {
            latch.countDown();
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
      e.printStackTrace();
    }
  }

  protected Node getOrCreateWebDateNode(Node userNodeApp, String dateNodeName) throws Exception {
    Node parentNode = null;
    if (userNodeApp.hasNode("notification")) {
      parentNode = userNodeApp.getNode("notification");
    } else {
      parentNode = userNodeApp.addNode("notification", "nt:unstructured");
    }
    if (parentNode.hasNode(AbstractService.WEB_CHANNEL)) {
      parentNode = parentNode.getNode(AbstractService.WEB_CHANNEL);
    } else {
      parentNode = parentNode.addNode(AbstractService.WEB_CHANNEL, AbstractService.NTF_CHANNEL);
    }
    if (parentNode.hasNode(dateNodeName)) {
      return parentNode.getNode(dateNodeName);
    }
    //
    return parentNode.addNode(dateNodeName, AbstractService.NTF_NOTIF_DATE);
  }

  protected Node getOrCreateWebCurrentUserNode(Node userNodeApp) throws Exception {
    Node currentNode = getOrCreateWebDateNode(userNodeApp, getDateName(Calendar.getInstance()));
    if (currentNode.isNew()) {
      currentNode.getSession().save();
    }
    return currentNode;
  }

  protected String getDateName(Calendar cal) {
    return new SimpleDateFormat(AbstractService.DATE_NODE_PATTERN).format(cal.getTime());
  }

  private NodeIterator gets(Session session, WebFilter filter) throws Exception {
    StringBuilder strQuery = new StringBuilder("SELECT * FROM ");
    strQuery.append(AbstractService.NTF_NOTIF_INFO).append(" WHERE ");
    strQuery.append("jcr:path LIKE '").append(filter.getJcrPath()).append("/%' ");
    if (filter.getUserId() != null && !filter.getUserId().isEmpty()) {
      strQuery.append("AND ").append(AbstractService.NTF_OWNER).append("='").append(filter.getUserId()).append("' ");
    }
    if (filter.isOnPopover()) {
      strQuery.append("AND ").append(AbstractService.NTF_SHOW_POPOVER).append("='true' ");
    }
    if (filter.getPluginKey() != null) {
      strQuery.append("AND ").append(AbstractService.NTF_PLUGIN_ID).append("='").append(filter.getPluginKey().getId()).append("' ");
    }
    if (filter.isRead() != null) {
      strQuery.append("AND ").append(AbstractService.NTF_READ).append("='").append(filter.isRead()).append("' ");
    }
    if (filter.getLimitDay() > 0) {
      long time = System.currentTimeMillis() - filter.getLimitDay() * 86400000;
      strQuery.append("AND ").append(AbstractService.NTF_LAST_MODIFIED_DATE).append(">='").append(time).append("' ");
    }
    if (filter.isOrder()) {
      strQuery.append("ORDER BY ").append(AbstractService.NTF_LAST_MODIFIED_DATE).append(" DESC");
    }
    //
    LOG.debug(" The query get web notification:\n" + strQuery);
    QueryManager qm = session.getWorkspace().getQueryManager();
    QueryImpl query = (QueryImpl) qm.createQuery(strQuery.toString(), Query.SQL);
    if (filter.getLimit() > 0) {
      query.setLimit(filter.getLimit());
      query.setOffset(filter.getOffset());
    }
    return query.execute().getNodes();
  }
  
  private void save(Node userNodeApp, NotificationInfo notification) throws Exception {
    String owner = notification.getTo();
    Node userNode = getOrCreateWebCurrentUserNode(userNodeApp);
    Node notifyNode = null;
    if (userNode.hasNode(notification.getId())) {
      notifyNode = userNode.getNode(notification.getId());
    } else {
      notifyNode = userNode.addNode(notification.getId(), AbstractService.NTF_NOTIF_INFO);
    }
    notifyNode.setProperty(AbstractService.NTF_PLUGIN_ID, notification.getKey().getId());
    notifyNode.setProperty(AbstractService.NTF_TEXT, notification.getTitle());
    notifyNode.setProperty(AbstractService.NTF_SENDER, notification.getFrom());
    notifyNode.setProperty(AbstractService.NTF_OWNER, owner);
    notifyNode.setProperty(AbstractService.NTF_LAST_MODIFIED_DATE, notification.getLastModifiedDate());
    // NTF_NAME_SPACE
    Map<String, String> ownerParameter = notification.getOwnerParameter();
    if (ownerParameter != null && !ownerParameter.isEmpty()) {
      for (String key : ownerParameter.keySet()) {
        String propertyName = (key.indexOf(AbstractService.NTF_NAME_SPACE) != 0) ? AbstractService.NTF_NAME_SPACE + key : key;
        notifyNode.setProperty(propertyName, ownerParameter.get(key));
      }
    }
    notifyNode.getSession().save();
    //
    notification.with("UUID", notifyNode.getUUID());
    ++HCR_save;
  }
  
  private synchronized NotificationInfo fillModel(Node node) throws Exception {
    if(node == null) return null;
    NotificationInfo notifiInfo = NotificationInfo.instance()
      .setTo(node.getParent().getName()) // owner of notification NTF_OWNER
      .setFrom(node.getProperty(AbstractService.NTF_SENDER).getString()) // user make event of notification
      .key(node.getProperty(AbstractService.NTF_PLUGIN_ID).getString())//pluginId
      .setTitle(node.getProperty(AbstractService.NTF_TEXT).getString())
      //
      .setLastModifiedDate(node.getProperty(AbstractService.NTF_LAST_MODIFIED_DATE).getLong())
      .with("UUID", node.getUUID())
      .setId(node.getName())
      .end();
    List<String> ignoreProperties = Arrays.asList(AbstractService.NTF_PLUGIN_ID, AbstractService.NTF_TEXT,
                                                  AbstractService.NTF_OWNER, AbstractService.NTF_LAST_MODIFIED_DATE);
    PropertyIterator iterator = node.getProperties();
    while (iterator.hasNext()) {
      Property p = iterator.nextProperty();
      if (p.getName().indexOf(AbstractService.NTF_NAME_SPACE) == 0) {
        if (ignoreProperties.contains(p.getName())) {
          continue;
        }
        try {
          notifiInfo.with(p.getName(), p.getString());
          notifiInfo.with(p.getName().replace(AbstractService.NTF_NAME_SPACE, ""), p.getString());
        } catch (Exception e) {}
      }
    }
    //
    return notifiInfo;
  }
  
  public abstract class Processor implements Runnable {
    protected CountDownLatch latch;

    public Processor(CountDownLatch latch) {
      this.latch = latch;
    }
    public void run() {
      process();
    }
    protected abstract void process();
  }
}

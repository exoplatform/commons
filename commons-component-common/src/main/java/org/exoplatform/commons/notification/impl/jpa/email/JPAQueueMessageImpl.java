package org.exoplatform.commons.notification.impl.jpa.email;

import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.service.QueueMessage;
import org.exoplatform.commons.api.persistence.DataInitializer;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.notification.impl.jpa.email.dao.MailQueueDAO;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailQueueEntity;
import org.exoplatform.commons.notification.impl.jpa.service.JPASendEmailService;
import org.exoplatform.settings.jpa.dao.SettingsDAO;
import org.exoplatform.settings.jpa.entity.SettingsEntity;
import org.exoplatform.commons.notification.NotificationContextFactory;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.job.SendEmailNotificationJob;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.management.annotations.ManagedBy;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.PeriodInfo;
import org.picocontainer.Startable;
import org.quartz.JobDataMap;

import java.util.*;

import static org.exoplatform.commons.notification.impl.jpa.EntityConverter.convertQueueEntityToMessageInfo;

/**
 * Created by exo on 3/27/17.
 */
@ManagedBy(JPASendEmailService.class)
public class JPAQueueMessageImpl implements QueueMessage, Startable {
  private static final Log LOG                   = ExoLogger.getExoLogger(JPAQueueMessageImpl.class);

  private static final String            MAX_TO_SEND_SYS_KEY   = "conf.notification.service.QueueMessage.numberOfMailPerBatch";
  private static final String            MAX_TO_SEND_KEY       = "numberOfMailPerBatch";
  private static final String            DELAY_TIME_SYS_KEY    = "conf.notification.service.QueueMessage.period";
  private static final String            DELAY_TIME_KEY        = "period";
  private static final String            CACHE_REPO_NAME       = "repositoryName";
  private static int                     LIMIT                 = 20;

  private int                            MAX_TO_SEND;
  private long                           DELAY_TIME;

  private JPASendEmailService sendEmailService;
  private MailService mailService;

  /** using the set to keep the messages. */
  private Set<MessageInfo> messages = Collections.synchronizedSet(new HashSet<MessageInfo>());
  /** .. */
  private ThreadLocal<Set<String>> idsRemovingLocal = new ThreadLocal<Set<String>>();

  private MailQueueDAO mailQueueDAO;

  private SettingsDAO settingsDAO;


  public JPAQueueMessageImpl(InitParams params, DataInitializer dataInitializer) {
    this.mailService = CommonsUtils.getService(MailService.class);
    this.mailQueueDAO = PortalContainer.getInstance().getComponentInstanceOfType(MailQueueDAO.class);
    this.settingsDAO = PortalContainer.getInstance().getComponentInstanceOfType(SettingsDAO.class);

    MAX_TO_SEND = NotificationUtils.getSystemValue(params, MAX_TO_SEND_SYS_KEY, MAX_TO_SEND_KEY, 20);
    DELAY_TIME = NotificationUtils.getSystemValue(params, DELAY_TIME_SYS_KEY, DELAY_TIME_KEY, 120) * 1000;
  }

  @Override
  public boolean put(MessageInfo message) {
    if (message == null || message.getTo() == null || message.getTo().length() == 0) {
      return false;
    }
    //
    if (NotificationUtils.isValidEmailAddresses(message.getTo()) == false) {
      LOG.warn(String.format("The email %s is not valid for sending notification", message.getTo()));
      return false;
    }
    //

    saveMessageInfo(message);
    //
    sendEmailService.addCurrentCapacity();
    return true;
  }

  @ExoTransactional
  private void saveMessageInfo(MessageInfo message) {
    try {
      MailQueueEntity mailQueueEntity = new MailQueueEntity();
      mailQueueEntity.setType(message.getPluginId());
      mailQueueEntity.setFrom(message.getFrom());
      mailQueueEntity.setTo(message.getTo());
      mailQueueEntity.setSubject(message.getSubject());
      mailQueueEntity.setBody(message.getBody());
      mailQueueEntity.setFooter(message.getFooter());
      mailQueueEntity.setCreationDate(message.getCreatedTime());

      mailQueueDAO.create(mailQueueEntity);

    } catch (Exception e) {
      LOG.error("Failed to save message.", e.getMessage() + message.toJSON(), e);
    }
  }

  @Override
  @ExoTransactional
  public void send() {
    final boolean stats = NotificationContextFactory.getInstance().getStatistics().isStatisticsEnabled();
    try {
      //
      load();
      if (idsRemovingLocal.get() == null) {
        idsRemovingLocal.set(new HashSet<String>());
      }
      //
      if (messages.size() > 0) {
        LOG.info(messages.size() + " message(s) will be sent.");
      }

      for (MessageInfo messageInfo : messages) {
        if (messageInfo != null && !idsRemovingLocal.get().contains(messageInfo.getId())
            && sendMessage(messageInfo.makeEmailNotification())) {

          LOG.debug("Message sent to user: " + messageInfo.getTo());
          //
          idsRemovingLocal.get().add(messageInfo.getId());
          if (stats) {
            NotificationContextFactory.getInstance().getStatisticsCollector().pollQueue(messageInfo.getPluginId());
          }
        }
      }
    } catch (Exception e) {
      LOG.warn("Failed to send message.");
      LOG.debug(e.getMessage(), e);
    }
    removeMessageInfo();
  }

  private void load() {
    try {
      for (MailQueueEntity mailQueueEntity : mailQueueDAO.findAll(0, LIMIT)) {
        messages.add(convertQueueEntityToMessageInfo(mailQueueEntity));
      }
    } catch (Exception e) {
      LOG.error("Failed to load message.", e.getMessage(), e);
    }
  }

  @Override
  public boolean sendMessage(Message message) {
    if (sendEmailService.isOn() == false) {
      try {
        //ensure the message is valid
        if (message.getFrom() == null) {
          return false;
        }
        mailService.sendMessage(message);
        return true;
      } catch (Exception e) {
        LOG.error("Error while sending a message - Cause : " + e.getMessage(), e);
        return false;
      }
    } else {
      sendEmailService.counter();
    }
    // if service is off, removed message.
    return true;
  }

  @ExoTransactional
  private void removeMessageInfo() {
    List<String> ids = new ArrayList<String>(idsRemovingLocal.get()) ;
    try {
      for (String messageId : ids) {
        mailQueueDAO.delete(mailQueueDAO.find(Long.valueOf(messageId)));
        //
        sendEmailService.removeCurrentCapacity();
        LOG.debug("Removing messageId: " + messageId);
      }
    } catch (Exception e) {
      LOG.warn("Failed to remove message.");
      LOG.debug(e.getMessage(), e);
    } finally {
      messages.clear();
      idsRemovingLocal.get().removeAll(ids);
    }
  }

  @Override
  public void start() {
    resetDefaultConfigJob();
    //
    sendEmailService.registerManager(this);
  }

  public void resetDefaultConfigJob() {
    makeJob(MAX_TO_SEND, DELAY_TIME);
  }

  public void makeJob(int limit, long interval) {
    if (interval > 0) {
      LIMIT = limit;
      //
      JobSchedulerService schedulerService = CommonsUtils.getService(JobSchedulerService.class);
      Calendar cal = new GregorianCalendar();
      //
      try {
        PeriodInfo periodInfo = new PeriodInfo(cal.getTime(), null, -1, interval);
        JobInfo info = new JobInfo("SendEmailNotificationJob", "Notification", SendEmailNotificationJob.class);
        info.setDescription("Send email notification job.");
        //
        schedulerService.removeJob(info);

        JobDataMap jdatamap = new JobDataMap();
        jdatamap.put(CACHE_REPO_NAME, CommonsUtils.getRepository().getConfiguration().getName());
        //
        schedulerService.addPeriodJob(info, periodInfo, jdatamap);
        LOG.debug("Job executes interval: " + interval);
      } catch (Exception e) {
        LOG.warn("Failed at makeJob().");
        LOG.debug(e.getMessage(), e);
      }
    }
  }

  public void setManagementView(JPASendEmailService managementView) {
    this.sendEmailService = managementView;
  }

  public String removeAll() {
    //TODO
//    int t = 0, j = 0;
//    String pli="";
//    try {
//      //
//      LOG.trace("Removing messages: ");
//      mailNotifDAO.deleteAll();
//      LOG.trace("Done to removed messages! ");
//      //
//      LOG.trace("Removing notification info... ");
//      NodeIterator it = root.getNode("eXoNotification/messageHome").getNodes();
//      List<String> pluginPaths = new ArrayList<String>();
//      while (it.hasNext()) {
//        pluginPaths.add(it.nextNode().getPath());
//      }
//      session.logout();
//      for (String string : pluginPaths) {
//        pli = string;
//        LOG.trace("Remove notification info on plugin: " + pli);
//        //
//        session = getSession(sProvider, configuration.getWorkspace());
//        it = ((Node) session.getItem(string)).getNodes();
//        while (it.hasNext()) {
//          NodeIterator hIter = it.nextNode().getNodes();
//          j = removeNodes(session, hIter);
//          t += j;
//        }
//        LOG.trace("Removed " + j + " nodes info on plugin: " + pli);
//        session.logout();
//      }
//
//      return "Done to removed " + t + " nodes!";
//    } catch (Exception e) {
//      LOG.trace("Removed " + j + " nodes info on plugin: " + pli);
//      LOG.trace("Removed all " + t + " nodes.");
//      LOG.debug("Failed to remove all data of feature notification." + e.getMessage());
//    } finally {
//      sProvider.close();
//    }
    return "Failed to remove all. Please, try again !";
  }

  @ExoTransactional
  public String removeUsersSetting() {
    List<SettingsEntity> settingsEntities = settingsDAO.getSettingsByContext(Context.USER);
    int t = settingsEntities.size();
    try {
      LOG.trace("Removing all user settings: ");
      settingsDAO.deleteAll(settingsEntities);
      return "Done to removed " + t + " users!";
    } catch (Exception e) {
      LOG.trace("Removed all " + t + " nodes.");
      LOG.debug("Failed to remove all data of feature notification." + e.getMessage());
    }
    return "Failed to remove all. Please, try again !";
  }

  @Override
  public void stop() {

  }
}

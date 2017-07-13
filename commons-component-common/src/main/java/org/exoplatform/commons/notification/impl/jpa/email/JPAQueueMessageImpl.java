package org.exoplatform.commons.notification.impl.jpa.email;

import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.service.QueueMessage;
import org.exoplatform.commons.api.persistence.DataInitializer;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.notification.impl.jpa.email.dao.MailNotifDAO;
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

  private static final String            MAX_TO_SEND_SYS_KEY   = "exo.notification.service.QueueMessage.numberOfMailPerBatch";
  private static final String            MAX_TO_SEND_KEY       = "numberOfMailPerBatch";
  private static final String            DELAY_TIME_SYS_KEY    = "exo.notification.service.QueueMessage.period";
  private static final String            DELAY_TIME_KEY        = "period";
  private static final String            CACHE_REPO_NAME       = "repositoryName";
  private static int                     limit                 = 20;

  private int                            max_to_send;
  private long                           delay_time;

  private JPASendEmailService sendEmailMockService;
  private MailService mailService;

  /** using the set to keep the messages. */
  private Set<MessageInfo> messages = Collections.synchronizedSet(new HashSet<MessageInfo>());
  /** .. */
  private ThreadLocal<Set<String>> idsRemovingLocal = new ThreadLocal<Set<String>>();

  private MailQueueDAO mailQueueDAO;

  private SettingsDAO settingsDAO;

  private MailNotifDAO mailNotifDAO;


  public JPAQueueMessageImpl(InitParams params, DataInitializer dataInitializer) {
    this.mailService = CommonsUtils.getService(MailService.class);
    this.mailQueueDAO = PortalContainer.getInstance().getComponentInstanceOfType(MailQueueDAO.class);
    this.settingsDAO = PortalContainer.getInstance().getComponentInstanceOfType(SettingsDAO.class);
    this.mailNotifDAO = PortalContainer.getInstance().getComponentInstanceOfType(MailNotifDAO.class);

    max_to_send = NotificationUtils.getSystemValue(params, MAX_TO_SEND_SYS_KEY, MAX_TO_SEND_KEY, 20);
    delay_time = NotificationUtils.getSystemValue(params, DELAY_TIME_SYS_KEY, DELAY_TIME_KEY, 120) * 1000;
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
    sendEmailMockService.addCurrentCapacity();
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
      mailQueueEntity.setCreationDate(new Date());

      mailQueueDAO.create(mailQueueEntity);

    } catch (Exception e) {
      LOG.error("Failed to save message.", e.getMessage() + message.toJSON(), e);
    }
  }

  @Override
  @ExoTransactional
  public void send() {
    final boolean statsEnabled = NotificationContextFactory.getInstance().getStatistics().isStatisticsEnabled();
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
        boolean isSent = false;
        try {
          isSent = sendMessage(messageInfo.makeEmailNotification());
        } catch (Exception e) {
          //error in sending message
        }
        if (messageInfo != null && !idsRemovingLocal.get().contains(messageInfo.getId()) && isSent) {

          LOG.debug("Message sent to user: {}", messageInfo.getTo());
          //
          idsRemovingLocal.get().add(messageInfo.getId());
          if (statsEnabled) {
            NotificationContextFactory.getInstance().getStatisticsCollector().pollQueue(messageInfo.getPluginId());
          }
        }
      }
    } catch (Exception e) {
      LOG.warn("Failed to send mail messages", e);
    }
    removeMessageInfo();
  }

  private void load() {
    try {
      for (MailQueueEntity mailQueueEntity : mailQueueDAO.findAll(0, limit)) {
        messages.add(convertQueueEntityToMessageInfo(mailQueueEntity));
      }
    } catch (Exception e) {
      LOG.error("Failed to load message.", e.getMessage(), e);
    }
  }

  @Override
  public boolean sendMessage(Message message) throws Exception {
    if (sendEmailMockService.isOn()) {
      //the sendEmailMockService is a managed service for monitoring and test purposes
      //if it is on, no effective mails will be sent until it is turned off
      sendEmailMockService.counter();
    } else {
      try {
        //ensure the message is valid
        if (message.getFrom() == null) {
          return false;
        }
        mailService.sendMessage(message);
        return true;
      } catch (Exception e) {
        LOG.error("Error while sending a message - Cause : " + e.getMessage(), e);
        throw e;
      }
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
        LOG.debug("Removing messageId: " + messageId);
        sendEmailMockService.removeCurrentCapacity();
      }
    } catch (Exception e) {
      LOG.warn("Failed to remove message: " + e.getMessage(), e);
    } finally {
      messages.clear();
      idsRemovingLocal.get().removeAll(ids);
    }
  }

  @Override
  public void start() {
    resetDefaultConfigJob();
    //
    sendEmailMockService.registerManager(this);
  }

  public void resetDefaultConfigJob() {
    makeJob(max_to_send, delay_time);
  }

  public void makeJob(int limit, long interval) {
    if (interval > 0) {
      this.limit = limit;
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
        LOG.debug("Send email notification job execution repeat interval: " + interval);
      } catch (Exception e) {
        LOG.warn("Failed at makeJob().");
        LOG.debug(e.getMessage(), e);
      }
    }
  }

  public void setManagementView(JPASendEmailService managementView) {
    this.sendEmailMockService = managementView;
  }

  public String removeAll() {
    try {
      //
      LOG.trace("Removing messages: ");
      mailNotifDAO.deleteAll();
      LOG.trace("Done to removed messages! ");

      return "Done to removed all stored messages!";
    } catch (Exception e) {
      LOG.debug("Failed to remove all data of feature notification." + e.getMessage());
    }
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

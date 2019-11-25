/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.notification.impl.service;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.picocontainer.Startable;
import org.quartz.JobDataMap;
import org.quartz.Trigger;

import org.exoplatform.commons.api.notification.service.QueueMessage;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.notification.impl.jpa.email.JPAQueueMessageImpl;
import org.exoplatform.commons.notification.job.SendEmailNotificationJob;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.management.ManagementAware;
import org.exoplatform.management.ManagementContext;
import org.exoplatform.management.annotations.Impact;
import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.PeriodInfo;

@Managed
@ManagedDescription("Mail Queue Massage service manager")
@NameTemplate({ @Property(key = "service", value = "notification"), @Property(key = "view", value = "mailqueue") })
public class MailQueueMessageManager implements ManagementAware, Startable {
  private static final String SEND_EMAIL_NOTIFICATION_JOB_GROUP = "Notification";

  private static final String SEND_EMAIL_NOTIFICATION_JOB       = "SendEmailNotificationJob";

  private static final Log    LOG                               = ExoLogger.getExoLogger(MailQueueMessageManager.class);

  private boolean             isOn                              = false;

  private long                sentCounter                       = 0;

  private long                currentCapacity                   = 0;

  private int                 emailPerSend                      = 0;

  private int                 interval                          = 120;

  private QueueMessage        queueMessage;

  private SettingService      settingService;

  private ListenerService     listenerService;

  private Trigger             defaultTrigger;

  private JobSchedulerService schedulerService;

  public MailQueueMessageManager(JPAQueueMessageImpl queueMessage) {
    this.queueMessage = queueMessage;
  }

  @Override
  public void setContext(ManagementContext context) {
  }

  public void counter() {
    ++sentCounter;
  }

  public void addCurrentCapacity() {
    ++currentCapacity;
  }

  public void removeCurrentCapacity() {
    if (currentCapacity > 0) {
      --currentCapacity;
    }
  }

  @Managed
  @ManagedDescription("Current mail service capacity should be available.")
  @Impact(ImpactType.READ)
  public long getCurrentCapacity() {
    return currentCapacity;
  }

  @Managed
  @ManagedDescription("Turn on the mail service.")
  @Impact(ImpactType.READ)
  public void on() {
    queueMessage.enable(true);
    resetCounter();
    isOn = true;
    makeJob(interval);
  }

  @Managed
  @ManagedDescription("Status of mail service. (true/false)")
  @Impact(ImpactType.READ)
  public boolean isOn() {
    return isOn;
  }

  @Managed
  @ManagedDescription("Turn off the mail service.")
  @Impact(ImpactType.READ)
  public String off() {
    queueMessage.enable(false);
    resetCounter();
    isOn = false;
    return resetDefaultConfigJob();
  }

  @Managed
  @ManagedDescription("Number emails sent")
  @Impact(ImpactType.READ)
  public long getSentCounter() {
    return sentCounter;
  }

  @Managed
  @ManagedDescription("Reset email countet.")
  @Impact(ImpactType.READ)
  public void resetCounter() {
    sentCounter = 0;
  }

  @Managed
  @ManagedDescription("Set number emails send per one time.")
  @Impact(ImpactType.READ)
  public void setNumberEmailPerSend(int emailPerSend) {
    this.emailPerSend = emailPerSend;
    makeJob(interval);
  }

  @Managed
  @ManagedDescription("Number emails send per one time.")
  @Impact(ImpactType.READ)
  public int getNumberEmailPerSend() {
    return this.emailPerSend;
  }

  @Managed
  @ManagedDescription("Set period of time (in seconds) for each sending notification execution.")
  @Impact(ImpactType.READ)
  public void setInterval() {
    makeJob(interval);
  }

  @Managed
  @ManagedDescription("Get period of time (in seconds) for each sending notification execution.")
  @Impact(ImpactType.READ)
  public int getInterval() {
    return interval;
  }

  @Managed
  @ManagedDescription("Removes all notification data that stored in database.")
  @Impact(ImpactType.READ)
  public String resetTestMail() {
    currentCapacity = 0;
    resetCounter();
    isOn = true;
    try {
      queueMessage.removeAll();
      return "Done";
    } catch (Exception e) {
      LOG.error("An error occurred while removing all mail messages from queue", e);
      return "An error occurred while removing all mail messages from queue, cause : " + e.getMessage();
    }
  }

  @Managed
  @ManagedDescription("Removes all users setting that stored in database.")
  @Impact(ImpactType.READ)
  public String removeUsersSetting() {
    settingService.remove(Context.USER);
    return "Done";
  }

  @Override
  public void start() {
    // Get services that couldn't be loaded from constructor (No dependency
    // injection)
    settingService = CommonsUtils.getService(SettingService.class);
    schedulerService = CommonsUtils.getService(JobSchedulerService.class);
    listenerService = CommonsUtils.getService(ListenerService.class);

    computeDefaultJobTrigger();
    addDefaultListeners();
  }

  @Override
  public void stop() {
  }

  private String makeJob(int interval) {
    if (isOn) {
      if (interval > 0) {
        //
        Calendar cal = new GregorianCalendar();
        //
        try {
          PeriodInfo periodInfo = new PeriodInfo(cal.getTime(), null, -1, interval);
          JobInfo info = new JobInfo(SEND_EMAIL_NOTIFICATION_JOB,
                                     SEND_EMAIL_NOTIFICATION_JOB_GROUP,
                                     SendEmailNotificationJob.class);
          info.setDescription("Send email notification job.");
          //
          schedulerService.removeJob(info);

          JobDataMap jdatamap = new JobDataMap();
          //
          schedulerService.addPeriodJob(info, periodInfo, jdatamap);
          LOG.debug("Job executes interval: " + interval);
          return "done";
        } catch (Exception e) {
          LOG.error("Error while building new Email Queue processing Job information", e);
          resetDefaultConfigJob();
          return "An error occurred while building new Email Queue processing Job information";
        }
      } else {
        return "";
      }
    } else {
      return "done";
    }
  }

  private void addDefaultListeners() {
    listenerService.addListener(QueueMessage.MESSAGE_ADDED_IN_QUEUE, new Listener<QueueMessage, String>() {
      @Override
      public void onEvent(Event<QueueMessage, String> event) throws Exception {
        addCurrentCapacity();
      }
    });
    listenerService.addListener(QueueMessage.MESSAGE_DELETED_FROM_QUEUE, new Listener<QueueMessage, String>() {
      @Override
      public void onEvent(Event<QueueMessage, String> event) throws Exception {
        removeCurrentCapacity();
      }
    });
    listenerService.addListener(QueueMessage.MESSAGE_SENT_FROM_QUEUE, new Listener<QueueMessage, String>() {
      @Override
      public void onEvent(Event<QueueMessage, String> event) throws Exception {
        if (isOn()) {
          counter();
        }
      }
    });
  }

  private void computeDefaultJobTrigger() {
    try {
      Trigger[] triggersOfJob = schedulerService.getTriggersOfJob(SEND_EMAIL_NOTIFICATION_JOB, SEND_EMAIL_NOTIFICATION_JOB_GROUP);
      if (triggersOfJob != null && triggersOfJob.length > 0) {
        defaultTrigger = triggersOfJob[0];
      }
    } catch (Exception e) {
      LOG.warn("Error while getting default job '" + SEND_EMAIL_NOTIFICATION_JOB
          + "'  trigger details. Can't reset to default if job details modified", e);
    }
  }

  private String resetDefaultConfigJob() {
    if (isOn) {
      if (defaultTrigger != null) {
        try {
          schedulerService.rescheduleJob(SEND_EMAIL_NOTIFICATION_JOB, SEND_EMAIL_NOTIFICATION_JOB_GROUP, defaultTrigger);
        } catch (Exception e) {
          LOG.warn("Failed to reset default job '" + SEND_EMAIL_NOTIFICATION_JOB + "' trigger.", e);
        }
        return "done";
      } else {
        return "Can't reset to default. Default trigger information not found.";
      }
    } else {
      return "done";
    }
  }

}

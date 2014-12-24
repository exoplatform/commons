package org.exoplatform.commons.notification.job;

import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

public class WebNotificationJob extends NotificationJob {

  protected static final Log LOG = ExoLogger.getLogger(NotificationJob.class);
  @Override
  protected void processSendNotification(JobExecutionContext context) throws Exception {
    WebNotificationStorage dataStorage = CommonsUtils.getService(WebNotificationStorage.class);
    OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
    //
    JobDataMap jdatamap = context.getJobDetail().getJobDataMap();
    int liveDays = Integer.valueOf(jdatamap.getString(WebCronJob.LIVE_DAYS_KEY));
    //
    CommonsUtils.startRequest(organizationService);
    ListAccess<User> allUsers = null;
    try {
      allUsers = organizationService.getUserHandler().findAllUsers();
    } finally {
      CommonsUtils.endRequest(organizationService);
    }
    int size = allUsers.getSize(), limit = 200;
    int index = 0, length = Math.min(limit, size);
    long startTime = System.currentTimeMillis();
    while (index < size && length > 0) {
      //
      LOG.info(String.format("Load from %s to %s, length %s", index, (index + length), length));
      User[] users = allUsers.load(index, length);
      if (users.length == 0) {
        break;
      }
      for (int i = 0; i < users.length; i++) {
        //
        dataStorage.remove(users[i].getUserName(), liveDays);
      }
      //
      index += length;
      length = Math.min(limit, size - index);
    }
    //
    LOG.info("Done clear web notifications for all users, time: " + (System.currentTimeMillis() - startTime) + "ms.");
  }
}

package org.exoplatform.commons.notification.job;

import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

public class WebNotificationJob extends NotificationJob {

  protected static final Log LOG = ExoLogger.getLogger(NotificationJob.class);
  @Override
  protected void processSendNotification(JobExecutionContext context) throws Exception {
    WebNotificationStorage dataStorage = CommonsUtils.getService(WebNotificationStorage.class);
    //
    long startTime = System.currentTimeMillis();
    long liveDays = getLiveTime(context.getJobDetail().getJobDataMap());
    //
    dataStorage.remove(liveDays);
    //
    LOG.info("Done clear web notifications for all users, time: " + (System.currentTimeMillis() - startTime) + "ms.");
  }

  private long getLiveTime(JobDataMap jdatamap) {
    long liveDays = 30;
    try {
      liveDays = Long.valueOf(jdatamap.getString(WebCronJob.LIVE_DAYS_KEY));
      if (liveDays <= 0) {
        LOG.warn("The value of the propety exo.notification.viewall cannot be 0 or negative. Using the default instead: 30.");
        liveDays = 30;
      }
    } catch (NumberFormatException e) {
      LOG.warn(String.format("The value of the propety exo.notification.viewall is incorrect:%s. Using the default instead: 30.", jdatamap.getString(WebCronJob.LIVE_DAYS_KEY)));
      liveDays = 30;
    }
    liveDays *= (24 * 60 * 60); // convert days to seconds
    //
    return liveDays;
  }
}

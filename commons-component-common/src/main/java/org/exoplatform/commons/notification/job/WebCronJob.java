package org.exoplatform.commons.notification.job;

import org.exoplatform.commons.utils.ExoProperties;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.scheduler.CronJob;
import org.quartz.JobDataMap;

public class WebCronJob extends CronJob {
  public static final String LIVE_DAYS_KEY = "liveDays";
  private JobDataMap jdatamap_;

  public WebCronJob(InitParams params) throws Exception {
    super(params);
    ExoProperties props = params.getPropertiesParam("web.info").getProperties();
    jdatamap_ = new JobDataMap();
    String days = props.getProperty(LIVE_DAYS_KEY).trim();
    jdatamap_.put(LIVE_DAYS_KEY, days);
  }

  public JobDataMap getJobDataMap() {
    return jdatamap_;
  }
}

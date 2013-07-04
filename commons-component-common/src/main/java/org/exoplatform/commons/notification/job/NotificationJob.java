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
package org.exoplatform.commons.notification.job;

import java.util.List;
import java.util.Map;

import org.exoplatform.commons.api.notification.MessageInfo;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.UserNotificationSetting;
import org.exoplatform.commons.api.notification.UserNotificationSetting.FREQUENCY;
import org.exoplatform.commons.api.notification.service.NotificationProviderService;
import org.exoplatform.commons.api.notification.service.NotificationService;
import org.exoplatform.commons.api.notification.service.ProviderSettingService;
import org.exoplatform.commons.api.notification.service.UserNotificationService;
import org.exoplatform.job.MultiTenancyJob;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

public class NotificationJob extends MultiTenancyJob {
  private static final Log LOG = ExoLogger.getLogger(NotificationJob.class);

  @Override
  public Class<? extends MultiTenancyTask> getTask() {
    return SendNotificationTask.class;
  }

  public class SendNotificationTask extends MultiTenancyTask {

    public SendNotificationTask(JobExecutionContext context, String repoName) {
      super(context, repoName);
    }
    
    @SuppressWarnings("unchecked")
    private <T> T getService(Class<T> clazz) {
      return (T) container.getComponentInstanceOfType(clazz);
    }
    
    
    private void processSendEmailNotification(NotificationService notificationService, NotificationProviderService notificationProviderService,
                                               MailService mailService, UserNotificationSetting userSetting) {
      try {
        // get all notificationMessage will send to this user.
        Map<String, List<NotificationMessage>> notificationMessageMap = notificationService.getNotificationMessagesByUser(userSetting);

        // build digest messageInfo
        MessageInfo messageInfo = notificationProviderService.buildMessageInfo(notificationMessageMap);

        if (messageInfo != null) {
          Message message_ = messageInfo.makeEmailNotification();

          mailService.sendMessage(message_);
          LOG.info("Process send daily email notification successfully for user: " + userSetting.getUserId());
        }
      } catch (Exception e) {
        LOG.error("Failed to send email for user " + userSetting.getUserId(), e);
      }
    }
    
    private UserNotificationSetting getDefaultUserNotificationSetting() {
      UserNotificationSetting notificationSetting = new UserNotificationSetting();
      ProviderSettingService settingService = getService(ProviderSettingService.class);
      List<String> activesProvider = settingService.getActiveProviderIds(false);
      for (String string : activesProvider) {
        notificationSetting.addProvider(string, FREQUENCY.WEEKLY_KEY);
      }

      return notificationSetting;
    }

    @Override
    public void run() {
      super.run();
      try {
        UserNotificationService userService = getService(UserNotificationService.class);
        NotificationService notificationService = getService(NotificationService.class);
        NotificationProviderService notificationProviderService = getService(NotificationProviderService.class);
        MailService mailService = getService(MailService.class);
        
        JobDataMap dataMap = context.getMergedJobDataMap();
        int offset = 0, limit = dataMap.getIntValue(NotificationInfoJob.MAX_EMAIL_TO_SEND_ONE_TIME);
        limit = (limit <= 0) ? 20 : limit;
        int timeSleep = dataMap.getIntValue(NotificationInfoJob.SLEEP_TIME_TO_NEXT_SEND);
        
        // case one: for user had setting

        // get size of all userSetting have daily setting
        long size = userService.getSizeDailyUserNotificationSettings();

        List<UserNotificationSetting> userSettings;
        while ((size - offset) > 0) {
          
          // get userSetting have daily setting
          userSettings = userService.getDailyUserNotificationSettings(offset, limit);
          for (UserNotificationSetting userSetting : userSettings) {
            //
            processSendEmailNotification(notificationService, notificationProviderService, mailService, userSetting);
            
          }
          
          offset += limit;
          if(timeSleep > 0) {
            Thread.sleep(timeSleep * 1000);
          }
        }
        
        
        // case two: for user used default setting.
        UserNotificationSetting userNotificationSetting = getDefaultUserNotificationSetting();
        // get all user had default setting
        List<String> usersDefaultSetting = userService.getDefaultDailyUserNotificationSettings();
        
        size = usersDefaultSetting.size();
        offset = 0;
        while ((size - offset) > 0) {
          int toIndex = offset + limit;
          if (toIndex > size)
            toIndex = (int) size;
          List<String> subList = usersDefaultSetting.subList(offset, toIndex);

          for (String userId : subList) {
            userNotificationSetting.setUserId(userId);
            //
            processSendEmailNotification(notificationService, notificationProviderService, mailService, userNotificationSetting);
          }

          offset = toIndex;
          if(timeSleep > 0) {
            Thread.sleep(timeSleep * 1000);
          }
        }
        
      } catch (Exception e) {
        LOG.error("Failed to running NotificationJob", e);
      }
      
    }
  }
}

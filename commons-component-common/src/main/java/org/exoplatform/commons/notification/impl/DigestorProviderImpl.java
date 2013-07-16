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
package org.exoplatform.commons.notification.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.commons.api.notification.MessageInfo;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.NotificationMessage.SEND_TYPE;
import org.exoplatform.commons.api.notification.ProviderData;
import org.exoplatform.commons.api.notification.UserNotificationSetting;
import org.exoplatform.commons.api.notification.service.AbstractNotificationProvider;
import org.exoplatform.commons.api.notification.service.NotificationProviderService;
import org.exoplatform.commons.api.notification.service.ProviderService;
import org.exoplatform.commons.api.notification.service.TemplateGenerator;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.utils.TimeConvertUtils;

public class DigestorProviderImpl extends AbstractNotificationProvider implements NotificationProviderService {
  
  private static final Log LOG = ExoLogger.getLogger(DigestorProviderImpl.class);

  private List<AbstractNotificationProvider> listSupportProviderImpl = new ArrayList<AbstractNotificationProvider>();

  TemplateGenerator                          templateGenerator;

  public DigestorProviderImpl(TemplateGenerator templateGenerator) {
    this.templateGenerator = templateGenerator;
  }
  
  @Override
  public void addSupportProviderImpl(AbstractNotificationProvider providerImpl) {
    listSupportProviderImpl.add(providerImpl);
  }
  
  @Override
  public AbstractNotificationProvider getSupportProviderImpl(String providerType) {
    LOG.info("\n get class support to provider:" + providerType);
    for (AbstractNotificationProvider providerImpl : listSupportProviderImpl) {
      if(providerImpl.getSupportType().contains(providerType)) {
        return providerImpl;
      }
    }
    return null;
  }

  @Override
  public MessageInfo buildMessageInfo(Map<String, List<NotificationMessage>> notificationData, UserNotificationSetting userSetting, SEND_TYPE type) {
    LOG.info("\nBuild digest MessageInfo ....");
    long startTime = System.currentTimeMillis();

    MessageInfo messageInfo = null;
    
    if (notificationData == null || notificationData.size() == 0) {
      return messageInfo;
    }

    try {
      messageInfo = new MessageInfo();
      ProviderService providerService = CommonsUtils.getService(ProviderService.class);
      
      List<ProviderData> providerDatas = providerService.getAllProviders();
      StringBuilder sb = new StringBuilder();
      for (ProviderData providerData : providerDatas) {
        String providerType = providerData.getType();
        List<NotificationMessage> messages = notificationData.get(providerData.getType());
        if (messages == null || messages.size() == 0)
          continue;
        AbstractNotificationProvider providerImpl = getSupportProviderImpl(providerType);
        sb.append(providerImpl.buildDigestMessageInfo(messages)).append("<br/>");
      }
      
      if (sb.toString().isEmpty())
        return null;
      
      NotificationMessage notificationMessage = notificationData.values().iterator().next().get(0);
      String language = getLanguage(notificationMessage);

      String fromTo = "Today";
      Calendar periodFrom = userSetting.getLastUpdateTime();
      long currentTime = System.currentTimeMillis();
      long lastTime =  currentTime - periodFrom.getTimeInMillis();
      long day = lastTime/86400000;
      
      
      String periodType = "ToDay";
      if(SEND_TYPE.WEEKLY.equals(type)) {
        periodType = "Weekly";
        if(day > 7) {
          periodFrom.setTimeInMillis(currentTime - (86400000 * 7));
        }
      } else if(SEND_TYPE.MONTHLY.equals(type)) {
        periodType = "Monthly";
        if(day > 28) {
          periodFrom.setTimeInMillis(currentTime - (86400000 * 28));
        }
      }
      
      if(SEND_TYPE.DAILY.equals(type) == false){
        Locale locale = new Locale(language);
        fromTo = TimeConvertUtils.getFormatDate(periodFrom.getTime(), "mmmm dd", locale);
        fromTo += " - ";
        fromTo += TimeConvertUtils.getFormatDate(Calendar.getInstance().getTime(), "mmmm dd, yyyy", locale);
      }
      
      Map<String, String> valueables = new HashMap<String, String>();

      valueables.put("PORTAL_NAME", System.getProperty("exo.notifications.portalname", "eXo"));
      valueables.put("PERIOD", periodType);
      valueables.put("FROM_TO", fromTo);
      String subject = templateGenerator.processSubjectIntoString("DigestProvider", valueables, language);
      
      valueables.put("FOOTER_LINK", getProfileUrl(userSetting.getUserId()));
      valueables.put("DIGEST_MESSAGES_LIST", sb.toString());
      String body = templateGenerator.processTemplateIntoString("DigestProvider", valueables, language);

      messageInfo.setBody(body).setSubject(subject).setTo(getTo(notificationMessage));
    } catch (Exception e) {
      LOG.error("Can not build template of DigestorProviderImpl ", e);
      return null;
    }
    
    LOG.info("End build template of DigestorProviderImpl ... " + (System.currentTimeMillis() - startTime) + " ms");
    
    return messageInfo;
  }

  @Override
  public MessageInfo buildMessageInfo(NotificationMessage message) {
    return null;
  }

  @Override
  public List<String> getSupportType() {
    return new ArrayList<String>();
  }

  @Override
  public String buildDigestMessageInfo(List<NotificationMessage> messages) {
   return null;
  }

}

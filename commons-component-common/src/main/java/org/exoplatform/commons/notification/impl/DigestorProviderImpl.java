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
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.api.notification.MessageInfo;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.ProviderData;
import org.exoplatform.commons.api.notification.UserNotificationSetting;
import org.exoplatform.commons.api.notification.service.AbstractNotificationProvider;
import org.exoplatform.commons.api.notification.service.NotificationProviderService;
import org.exoplatform.commons.api.notification.service.ProviderService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class DigestorProviderImpl extends AbstractNotificationProvider implements NotificationProviderService {
  
  private static final Log LOG = ExoLogger.getLogger(DigestorProviderImpl.class);

  private List<AbstractNotificationProvider> listSupportProviderImpl = new ArrayList<AbstractNotificationProvider>();

  public DigestorProviderImpl() {
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
  public MessageInfo buildMessageInfo(Map<String, List<NotificationMessage>> notificationData, UserNotificationSetting userSetting) {
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
        sb.append(providerImpl.buildDigestMessageInfo(messages));
      }
      
      if (sb.toString().isEmpty())
        return null;
      
      ProviderData digestProvider = providerService.getProvider("DigestProvider");
      NotificationMessage notificationMessage = notificationData.values().iterator().next().get(0);
      String language = getLanguage(notificationMessage);
      String body = getTemplate(digestProvider, language);
      String subject = getSubject(digestProvider, language);
      
      body = body.replace("$content", sb.toString());
      messageInfo.setBody(body).setSubject(subject).setTo(getTo(notificationMessage));
    } catch (Exception e) {
      LOG.error("Can not build template of DigestorProviderImpl ", e);
      return null;
    }
    // get digest provider ==> get subject, template
    // building body...
    // for providerids get by ProviderService
       // get support provider
       // for list messages
        // providerImpl process message
    
    
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

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
package org.exoplatform.commons.notification.impl.service.template;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationKey;
import org.exoplatform.commons.api.notification.model.NotificationMessage;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.api.notification.plugin.NotificationPluginUtils;
import org.exoplatform.commons.api.notification.service.setting.ProviderSettingService;
import org.exoplatform.commons.api.notification.service.template.DigestorService;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.api.notification.service.template.TemplateGenerator;
import org.exoplatform.commons.notification.NotificationConfiguration;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.DigestDailyPlugin;
import org.exoplatform.commons.notification.impl.DigestWeeklyPlugin;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.notification.impl.setting.NotificationPluginContainer;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.utils.TimeConvertUtils;

public class DigestorServiceImpl implements DigestorService {
  
  private static final Log LOG = ExoLogger.getLogger(DigestorServiceImpl.class);


  public DigestorServiceImpl() {
  }
  
  
  public MessageInfo buildMessage(Map<NotificationKey, List<NotificationMessage>> notificationData, UserSetting userSetting) {
    LOG.info("\nBuild digest MessageInfo ....");
    long startTime = System.currentTimeMillis();

    MessageInfo messageInfo = null;
    
    if (notificationData == null || notificationData.size() == 0) {
      return messageInfo;
    }

    try {
      messageInfo = new MessageInfo();
      ProviderSettingService providerService = CommonsUtils.getService(ProviderSettingService.class);
      NotificationPluginContainer pluginService = CommonsUtils.getService(NotificationPluginContainer.class);
      TemplateGenerator templateGenerator = CommonsUtils.getService(TemplateGenerator.class);
      NotificationConfiguration configuration= CommonsUtils.getService(NotificationConfiguration.class);
      
      List<String> activeProviders = providerService.getActiveProviderIds();
      NotificationContext nCtx = NotificationContextImpl.cloneInstance();
      Writer writer = new StringWriter();
      for (String providerId : activeProviders) {
        List<NotificationMessage> messages = notificationData.get(NotificationKey.key(providerId));
        if (messages == null || messages.size() == 0)
          continue;
        
        AbstractNotificationPlugin plugin = pluginService.getPlugin(NotificationKey.key(providerId));
        nCtx.setNotificationMessages(messages);
        plugin.buildDigest(nCtx, writer);
        writer.append("<br/>");
      }
      
      StringBuffer sb = ((StringWriter)writer).getBuffer();
      if (sb.length() == 0) {
        return null;
      }
      
      String language = NotificationPluginUtils.getLanguage(userSetting.getUserId());

      String fromTo = "Today";
      Calendar periodFrom = userSetting.getLastUpdateTime();
      long currentTime = System.currentTimeMillis();
      long lastTime =  currentTime - periodFrom.getTimeInMillis();
      long day = lastTime/86400000;
      //TODO need to make utils with 
      String pluginId = DigestDailyPlugin.ID;
      String periodType = "Daily";
      if(NotificationUtils.isWeekEnd(configuration.getDayOfWeekend()) &&
          userSetting.getWeeklyProviders().size() > 0) {
        periodType = "Weekly";
        pluginId = DigestWeeklyPlugin.ID;
        if(day > 7) {
          periodFrom.setTimeInMillis(currentTime - (86400000 * 7));
        }
      }
      
      if ("Weekly".equals(periodType) == true) {
        Locale locale = (language == null || language.length() == 0) ? Locale.ENGLISH : new Locale(language);
        fromTo = TimeConvertUtils.getFormatDate(periodFrom.getTime(), "mmmm dd", locale);
        fromTo += " - ";
        fromTo += TimeConvertUtils.getFormatDate(Calendar.getInstance().getTime(), "mmmm dd, yyyy", locale);
      }
      
      TemplateContext ctx = new TemplateContext(pluginId, language);

      ctx.put("FIRSTNAME", NotificationPluginUtils.getFirstName(userSetting.getUserId()));
      ctx.put("PORTAL_NAME", System.getProperty("exo.notifications.portalname", "eXo"));
      ctx.put("PERIOD", periodType);
      ctx.put("FROM_TO", fromTo);
      String subject = templateGenerator.processSubject(ctx);
      
      ctx.put("FOOTER_LINK", NotificationPluginUtils.getProfileUrl(userSetting.getUserId()));
      ctx.put("DIGEST_MESSAGES_LIST", sb.toString());
      String body = templateGenerator.processTemplate(ctx);

      messageInfo.body(body).subject(subject).to(NotificationPluginUtils.getTo(userSetting.getUserId()));
    } catch (Exception e) {
      LOG.error("Can not build template of DigestorProviderImpl ", e);
      return null;
    }
    
    LOG.info("End build template of DigestorProviderImpl ... " + (System.currentTimeMillis() - startTime) + " ms");
    
    return messageInfo;
  }

}

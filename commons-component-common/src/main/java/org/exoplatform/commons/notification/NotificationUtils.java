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
package org.exoplatform.commons.notification;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.api.notification.template.Element;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.notification.template.*;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.organization.OrganizationService;


public class NotificationUtils {

  public static final String EXO_IS_ACTIVE             = "exo:isActive";

  public static final String DEFAULT_SUBJECT_KEY       = "Notification.subject.{0}";

  public static final String DEFAULT_SIMPLE_DIGEST_KEY = "Notification.digest.{0}";

  public static final String DEFAULT_DIGEST_ONE_KEY    = "Notification.digest.one.{0}";

  public static final String DEFAULT_DIGEST_THREE_KEY  = "Notification.digest.three.{0}";
  
  public static final String DEFAULT_DIGEST_MORE_KEY   = "Notification.digest.more.{0}";

  public static final String FEATURE_NAME              = "notification";
  
  private static final Pattern LINK_PATTERN = Pattern.compile("<a ([^>]+)>([^<]+)</a>");

  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[_a-zA-Z0-9-+]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{2,})$");
  
  private static final String styleCSS = " style=\"color: #2f5e92; text-decoration: none;\"";
  
  /** This value must be the same with CalendarSpaceActivityPublisher.CALENDAR_APP_ID */
  public static final String CALENDAR_ACTIVITY = "cs-calendar:spaces";
  
  public static String getDefaultKey(String key, String providerId) {
    return MessageFormat.format(key, providerId);
  }
  
  /**
   * Get locale by user's language
   * 
   * @param language the language of target user
   * @return
   */
  public static Locale getLocale(String language) {
    if (language == null || language.isEmpty()) {
      return Locale.ENGLISH;
    }
    String[] infos = language.split("_");
    String lang = infos[0];
    String country = (infos.length > 1) ? infos[1] : "";
    String variant = (infos.length > 2) ? infos[2] : "";
    return new Locale(lang, country, variant);
  }
  
  /**
   * Gets the digest's resource bundle
   * 
   * @param templateConfig
   * @param pluginId
   * @param language
   * @return
   */
  public static DigestTemplate getDigest(PluginConfig templateConfig, String pluginId, String language) {
    String srcResource = templateConfig.getBundlePath();
    String digestOneKey = templateConfig.getKeyValue(PluginConfig.DIGEST_ONE_KEY, getDefaultKey(DEFAULT_DIGEST_ONE_KEY, pluginId));
    String digestThreeKey = templateConfig.getKeyValue(PluginConfig.DIGEST_THREE_KEY, getDefaultKey(DEFAULT_DIGEST_THREE_KEY, pluginId));
    String digestMoreKey = templateConfig.getKeyValue(PluginConfig.DIGEST_MORE_KEY, getDefaultKey(DEFAULT_DIGEST_MORE_KEY, pluginId));
    
    Locale locale = getLocale(language);
    
    return new DigestTemplate().digestOne(TemplateUtils.getResourceBundle(digestOneKey, locale, srcResource))
                               .digestThree(TemplateUtils.getResourceBundle(digestThreeKey, locale, srcResource))
                               .digestMore(TemplateUtils.getResourceBundle(digestMoreKey, locale, srcResource));
        
                                
  }
  
  /**
   * Gets the subject's resource bundle
   * 
   * @param templateConfig
   * @param pluginId
   * @param language
   * @return
   */
  public static Element getSubject(PluginConfig templateConfig, String pluginId, String language) {
    String bundlePath = templateConfig.getBundlePath();
    String subjectKey = templateConfig.getKeyValue(PluginConfig.SUBJECT_KEY, getDefaultKey(DEFAULT_SUBJECT_KEY, pluginId));
    
    Locale locale = getLocale(language);
    
    return new SimpleElement().language(locale.getLanguage()).template(TemplateUtils.getResourceBundle(subjectKey, locale, bundlePath));
  }
  
  
  public static String listToString(List<String> list) {
    if (list == null || list.size() == 0) {
      return "";
    }
    StringBuffer values = new StringBuffer();
    for (String str : list) {
      if (values.length() > 0) {
        values.append(",");
      }
      values.append(str);
    }
    return values.toString();
  }

  public static String listToString(List<String> list, String pattern) {
    if (list == null || list.size() == 0) {
      return "";
    }
    StringBuffer values = new StringBuffer();
    for (String str : list) {
      if (values.length() > 0) {
        values.append(",");
      }
      values.append(pattern.replace("VALUE", str));
    }
    return values.toString();
  }
  
  public static List<String> stringToList(String value) {
    List<String> result = new ArrayList<String>();
    if (value == null || value.isEmpty()) {
      return result;
    }
    StringTokenizer tokenizer = new StringTokenizer(value, ",");
    while (tokenizer.hasMoreTokens()) {
      result.add(tokenizer.nextToken());
    }
    return result;
  }

  public static String getValueParam(InitParams params, String key, String defaultValue) {
    try {
      return params.getValueParam(key).getValue();
    } catch (Exception e) {
      return defaultValue;
    }
  }

  public static int getValueParam(InitParams params, String key, int defaultValue) {
    try {
      return Integer.valueOf(params.getValueParam(key).getValue());
    } catch (Exception e) {
      return defaultValue;
    }
  }

  public static String getSystemValue(InitParams params, String systemKey, String paramKey, String defaultValue) {
    try {
      String vl = System.getProperty(systemKey);
      if (vl == null || vl.length() == 0) {
        vl = getValueParam(params, paramKey, defaultValue);
      }
      return vl.trim();
    } catch (Exception e) {
      return defaultValue;
    }
  }
  
  public static int getSystemValue(InitParams params, String systemKey, String paramKey, int defaultValue) {
    return Integer.valueOf(getSystemValue(params, systemKey, paramKey, String.valueOf(defaultValue)));
  }
  
  public static boolean isValidEmailAddresses(String addressList){
    if (addressList == null || addressList.length() < 0)
      return false;
    addressList = StringUtils.replace(addressList, ";", ",");
    try {
      InternetAddress[] iAdds = InternetAddress.parse(addressList, true);
      for (int i = 0; i < iAdds.length; i++) {
        Matcher matcher = EMAIL_PATTERN.matcher(iAdds[i].getAddress().trim());
        if (! matcher.find())
          return false;
      }
    } catch (AddressException e) {
      return false;
    }
    return true;
  }
  
  public static boolean isDeletedMember(String userName) {
    try {
      CommonsUtils.startRequest(CommonsUtils.getService(OrganizationService.class));
      return CommonsUtils.getService(OrganizationService.class).getUserHandler().findUserByName(userName) == null;
    } catch (Exception e) {
      return false;
    } finally {
      CommonsUtils.endRequest(CommonsUtils.getService(OrganizationService.class));
    }
  }
  
  public static boolean isActiveSetting(String userId) {
    try {
      SettingService settingService = CommonsUtils.getService(SettingService.class);
      SettingValue<String> value = (SettingValue<String>) settingService.get(Context.USER.id(userId), Scope.GLOBAL, EXO_IS_ACTIVE);
      return (value == null || value.getValue() == null) ? false : true;
    } catch (Exception e) {
      return false;
    }
  }
  
  /**
   * Add the style css for a link in the activity title to display a link without underline
   * 
   * @param title activity title
   * @return activity title after process all link
   */
  public static String processLinkTitle(String title) {
    Matcher matcher = LINK_PATTERN.matcher(title);
    while (matcher.find()) {
      String result = matcher.group(1);
      title = title.replace(result, result + styleCSS);
    }
    return title;
  }
  
  /**
   * 
   * @param title
   * @param activityType
   * @return
   */
  public static String getNotificationActivityTitle(String title, String activityType) {
    String displayTitle = removeLinkTitle(title);
    
    //Work-around for SOC-4730 : for calendar activity only, the title is not stored by raw data but 
    //it's escaped before storing it. We need to unescape the title when build the notification
    if (CALENDAR_ACTIVITY.equals(activityType)) {
      displayTitle = StringEscapeUtils.unescapeHtml(displayTitle);
    }
    //
    return displayTitle;
  }
  
  /**
   * Remove all link in activity title and add user-name class
   *  
   * @param title
   * @return
   */
  public static String removeLinkTitle(String title) {
    Matcher mat = LINK_PATTERN.matcher(title);
    return mat.replaceAll("<span class=\"user-name text-bold\">$2</span>");
  }
  
  public static String getProfileUrl(String userId) {
    StringBuffer footerLink = new StringBuffer(CommonsUtils.getCurrentDomain());
    return footerLink.append("/").append(CommonsUtils.getRestContextName())
            .append("/").append("social/notifications/redirectUrl/notification_settings")
            .append("/").append(userId).toString();
  }
  
  public static String getPortalHome(String portalName) {
    StringBuffer portalLink = new StringBuffer(CommonsUtils.getCurrentDomain());
    portalLink.append("/")
              .append(CommonsUtils.getRestContextName())
              .append("/")
              .append("social/notifications/redirectUrl/portal_home")
              .append("/")
              .append(portalName);
    
    return "<a target=\"_blank\" style=\"text-decoration: none; font-weight: bold; color: #2F5E92; \" href=\"" + portalLink.toString() + "\">" + portalName + "</a>";
  }
}

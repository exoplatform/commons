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
package org.exoplatform.commons.notification.template;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.ResourceBundleData;
import org.exoplatform.services.resources.ResourceBundleService;

public class TemplateResourceBundle {
  private static final Log LOG = ExoLogger.getLogger(TemplateResourceBundle.class);

  private static final String CONF_LOCATION = "war:/classes/";

  private String           language;

  private String           bundlePath;

  public TemplateResourceBundle(String language, String bundlePath) {
    this.language = language;
    this.bundlePath = bundlePath;
  }

  /**
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * @param language the language to set
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * @return the bundlePath
   */
  public String getBundlePath() {
    return bundlePath;
  }

  /**
   * @param bundlePath the bundlePath to set
   */
  public void setBundlePath(String bundlePath) {
    this.bundlePath = bundlePath;
  }

  public String appRes(String key) {
    Locale locale = Locale.ENGLISH;
    if (language != null && language.length() > 0) {
      locale = new Locale(language);
    }
    return getResourceBundle(key, locale, bundlePath);
  }

  public String appRes(String key, String... strs) {
    String value = appRes(key);
    if (strs != null && strs.length > 0) {
      for (int i = 0; i < strs.length; ++i) {
        value = StringUtils.replace(value, "{" + i + "}", strs[i]);
      }
    }
    return value;
  }

  private static ResourceBundle addResourceBundle(ResourceBundleService bundleService, String resourceLocale, Locale locale) {
    String id = new StringBuffer(CONF_LOCATION).append(resourceLocale.replace(".", "/"))
                      .append("_").append(locale.getLanguage()).append(".properties").toString();
    try {
      ConfigurationManager configurationManager = CommonsUtils.getService(ConfigurationManager.class);
      InputStream inputStream = configurationManager.getInputStream(id);
      if (inputStream != null) {
        String data = getContent(inputStream);
        ResourceBundleData bundleData = new ResourceBundleData(data);
        bundleData.setLanguage(locale.getLanguage());
        bundleData.setName(resourceLocale);
        bundleData.setCountry("");
        bundleData.setVariant("");
        //
        bundleService.saveResourceBundle(bundleData);
        return bundleService.getResourceBundle(resourceLocale, locale);
      }
    } catch (Exception e) {
      LOG.warn("Can not add resource bundle of locale " + resourceLocale + "\n" + e.getMessage());
    }
    return null;
  }

  public static String getResourceBundle(String key, Locale locale, String resourceLocale) {
    if (key == null || key.trim().length() == 0) {
      return "";
    }

    if (locale == null || locale.getLanguage().isEmpty()) {
      locale = Locale.ENGLISH;
    }

    ResourceBundle res = null;
    ResourceBundleService bundleService = CommonsUtils.getService(ResourceBundleService.class);
    if (bundleService != null) {
      res = bundleService.getResourceBundle(resourceLocale, locale);
      // if null, try another way
      if (res == null) {
        //
        res = addResourceBundle(bundleService, resourceLocale, locale);
      }
    }
    // still null
    if (res == null || res.containsKey(key) == false) {
      if (key.indexOf(".digest.") < 0) {
        LOG.warn("Can not get resource bundle by key: " + key);
      }
      return key;
    }

    return res.getString(key);
  }

  static private String getContent(InputStream input) throws IOException {
    StringBuilder content = new StringBuilder();
    Scanner scanner = new Scanner(input, "UTF-8");
    try {
      while (scanner.hasNextLine()) {
        if (content.length() > 0) {
          content.append("\n");
        }
        String s = scanner.nextLine();
        content.append(s);
      }
    } finally {
      scanner.close();
      input.close();
    }
    return content.toString();
  }
}

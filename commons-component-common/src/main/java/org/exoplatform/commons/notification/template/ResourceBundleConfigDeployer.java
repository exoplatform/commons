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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.ResourceBundleData;
import org.exoplatform.services.resources.ResourceBundleService;
import org.gatein.wci.WebApp;
import org.gatein.wci.WebAppEvent;
import org.gatein.wci.WebAppLifeCycleEvent;
import org.gatein.wci.WebAppListener;

public class ResourceBundleConfigDeployer implements WebAppListener {
  private static final Log          LOG                       = ExoLogger.getLogger(ResourceBundleConfigDeployer.class);

  ResourceBundleService               bundleService;

  private static final String       CONF_LOCATION             = "/WEB-INF/classes/";

  private static final String       FILE_EXTENSION_PROPERTIES = ".properties";

  private Map<String, String>        dataResourceBundle         = new ConcurrentHashMap<String, String>();

  private final Map<String, WebApp> contexts;

  public ResourceBundleConfigDeployer() {
    bundleService = CommonsUtils.getService(ResourceBundleService.class);
    this.contexts = new HashMap<String, WebApp>();
  }

  @Override
  public void onEvent(WebAppEvent event) {
    if (event instanceof WebAppLifeCycleEvent) {
      WebAppLifeCycleEvent lifeCycleEvent = (WebAppLifeCycleEvent) event;
      switch (lifeCycleEvent.getType()) {
      case WebAppLifeCycleEvent.ADDED:
        add(event.getWebApp());
        break;
      case WebAppLifeCycleEvent.REMOVED:
        remove(event.getWebApp());
        break;
      }
    }

  }

  private void remove(WebApp webApp) {
    contexts.remove(webApp.getContextPath());
  }

  private void add(WebApp webApp) {
    contexts.put(webApp.getContextPath(), webApp);
  }

  public void initBundlePath(Collection<String> list) {
    try {
      for (String path : list) {
        for (WebApp app : contexts.values()) {
          initBundle(app.getServletContext(), path);
        }
      }
    } catch (Exception e) {
      LOG.debug("Error when initializing resource bundle of Notification.", e);
    } finally {
      addAllResourcebundle();
    }
  }

  private void initBundle(ServletContext servletCtx, String path) {
    Set<String> paths = servletCtx.getResourcePaths(getPathFile(path));
    if (paths != null) {
      for (String rsLocation : paths) {
        if (rsLocation == null || rsLocation.indexOf(path.replace(".", "/")) < 0) {
          continue;
        }
        try {
          InputStream is = servletCtx.getResourceAsStream(rsLocation);
          String lang = getLang(rsLocation.replace(FILE_EXTENSION_PROPERTIES, ""));
          //
          addResourceBundle(is, lang, path);
        } catch (IOException e) {
          LOG.debug("Error when initializing resource bundle: " + path, e);
        }
      }
    }
  }

  private String getPathFile(String path) {
    return new StringBuffer(CONF_LOCATION).append(path.substring(0, path.lastIndexOf(".")).replace(".", "/")).toString();
  }

  private void addResourceBundle(InputStream is, String lang, String resourceLocale) throws IOException {
    try {
      if (is != null) {
        String key = getKey(lang, resourceLocale);
        StringBuilder value = getContent(is);
        if (dataResourceBundle.containsKey(key)) {
          value.append("\n").append(dataResourceBundle.get(key));
        }
        dataResourceBundle.put(key, value.toString());
      }
    } catch (Exception e) {
      LOG.debug("Error when initializing resource bundle: " + resourceLocale, e);
    } finally {
      if (is != null) {
        is.close();
      }
    }
  }

  private static StringBuilder getContent(InputStream input) throws IOException {
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
    return content;
  }

  private void addAllResourcebundle() {

    for (String key : dataResourceBundle.keySet()) {
      ResourceBundleData bundleData = new ResourceBundleData(dataResourceBundle.get(key));
      bundleData.setLanguage(getLang(key));
      bundleData.setName(getResourceLocale(key));
      bundleData.setCountry("");
      bundleData.setVariant("");
      //
      bundleService.saveResourceBundle(bundleData);
    }
    dataResourceBundle.clear();
  }

  private String getKey(String lang, String resourceLocale) {
    return new StringBuffer(resourceLocale).append("_").append(lang).toString();
  }

  private String getLang(String key) {
    String[] strs = key.split("_");
    return strs[strs.length - 1];
  }

  private String getResourceLocale(String key) {
    return key.replace("_" + getLang(key), "");
  }

}

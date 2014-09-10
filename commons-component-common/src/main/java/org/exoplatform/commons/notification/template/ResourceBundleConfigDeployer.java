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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.ResourceBundleData;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.services.resources.impl.BaseResourceBundlePlugin;
import org.exoplatform.services.resources.impl.SimpleResourceBundleService;
import org.gatein.wci.WebApp;
import org.gatein.wci.WebAppEvent;
import org.gatein.wci.WebAppLifeCycleEvent;
import org.gatein.wci.WebAppListener;

public class ResourceBundleConfigDeployer implements WebAppListener {
  private static final Log LOG = ExoLogger.getLogger(ResourceBundleConfigDeployer.class);
  private final ResourceBundleService bundleService;
  private static final String CONF_LOCATION = "/WEB-INF/classes/";
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

  /**
   * Loading the notification resource bundle.
   * 
   * Notice: Don't accept the resource bundle file the same name for both jar and war file.
   * If the case the same happens, we just support to load from webapp context.
   * 
   * When gets the Resource Bundle, 1 priority is resource bundle in jar file. it returns NULL.
   * 
   * 
   * @param list the provided the path of resource bundle file
   */
  public void initBundlePath(Collection<String> list) {
    try {
      //step 1: build resource bundle list base on the supported language from platform.
      LocaleConfigService localeService_ = CommonsUtils.getService(LocaleConfigService.class);
      Collection<LocaleConfig> locales = localeService_.getLocalConfigs();
      Set<String> includedWebApp = new HashSet<String>();
      
      Set<ResourceBundleFile> filePaths = new HashSet<ResourceBundleFile>();
      for (String path : list) {
        String baseName = getRealPathFile(path);
        for (LocaleConfig config : locales) {
          filePaths.addAll(buildFilePath(path, baseName, config.getLocale()));
        }
      }
      
      //step 2: Loading resource bundle file from webapps
      for (WebApp app : contexts.values()) {
        for(ResourceBundleFile bundleFile : filePaths) {
          String content = this.getResourceBundleContent(bundleFile.filePath, app.getServletContext());
          if (content != null) {
            // save the content
            ResourceBundleData data = new ResourceBundleData(content);
            data.setName(bundleFile.resourceName);
            data.setLanguage(bundleFile.locale.getLanguage());
            data.setCountry(bundleFile.locale.getCountry());
            data.setVariant(bundleFile.locale.getVariant());
            this.bundleService.saveResourceBundle(data);
            includedWebApp.add(bundleFile.resourceName);
            LOG.debug("Loading file path = " + bundleFile.filePath + " ResourceBundleData's ID = " + data.getId());
          }
        }
      }
      
      //step 3 Loading the resource bundle from jar file what keep in integration notification. 
      for (String path : list) {
        if (!includedWebApp.contains(path)) {
        //add the bundle from jar file
          addResourceBundleByPlugin(path);
        }
      }
      
    } catch (Exception e) {
      LOG.debug("Error when initializing resource bundle of Notification.", e);
    }
  }
  
  private String getResourceBundleContent(String filePath, ServletContext servletContext) throws Exception {
    String fileName = null;
    try {
      URL url = servletContext.getResource(filePath);
      if (url != null) {
        InputStream is = url.openStream();
        try {
          byte[] buf = IOUtil.getStreamContentAsBytes(is);
          return new String(buf, "UTF-8");
        } finally {
          try {
            is.close();
          } catch (IOException e) {
            // Do nothing
          }
        }
      }
    } catch (Exception ex) {
      throw new Exception("Error while reading the file: " + fileName, ex);
    }
    return null;
  }

  private List<ResourceBundleFile> buildFilePath(String resourceName, String baseName, Locale locale) {
    List<ResourceBundleFile> candidateFiles = new ArrayList<ResourceBundleFile>();

    String language = locale.getLanguage();
    String country = locale.getCountry().toUpperCase();
    String variant = locale.getVariant();

    if (variant != null && variant.length() > 0) {
      candidateFiles.add(new ResourceBundleFile(resourceName, baseName + "_" + language + "_" + country + "_" + variant + ".properties", locale));
    }

    if (country != null && country.length() > 0) {
      candidateFiles.add(new ResourceBundleFile(resourceName, baseName + "_" + language + "_" + country + ".properties", locale));
    }

    if (language != null && language.length() > 0) {
      candidateFiles.add(new ResourceBundleFile(resourceName, baseName + "_" + language + ".properties", locale));
    }
    return candidateFiles;
  }

  private void addResourceBundleByPlugin(String path) {
    InitParams params = new InitParams();

    ValuesParam classPathParam = new ValuesParam();
    classPathParam.setName("classpath.resources");
    classPathParam.setValues(new ArrayList<String>(Arrays.asList(path)));
    params.addParameter(classPathParam);

    ValuesParam portalParam = new ValuesParam();
    portalParam.setName("portal.resource.names");
    portalParam.setValues(new ArrayList<String>(Arrays.asList(path)));
    params.addParameter(portalParam);

    BaseResourceBundlePlugin bundlePlugin = new BaseResourceBundlePlugin(params);
    ((SimpleResourceBundleService) bundleService).addResourceBundle(bundlePlugin);
  }
  
  private String getRealPathFile(String path) {
    return new StringBuffer(CONF_LOCATION).append(path.replace(".", "/")).toString();
  }
  
  /**
   * The class what wrapped the filePath and locale
   * @author thanhvc
   *
   */
  private class ResourceBundleFile {
    final String resourceName;
    final String filePath;
    final Locale locale;
    public ResourceBundleFile(String resourceName, String filePath, Locale locale) {
      this.resourceName = resourceName;
      this.filePath = filePath;
      this.locale = locale;
    }
    
    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ResourceBundleFile)) {
        return false;
      }
      
      ResourceBundleFile that = (ResourceBundleFile) o;

      if (filePath != null ? !filePath.equals(that.filePath) : that.filePath != null) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + (filePath != null ? filePath.hashCode() : 0);
      return result;
    }
    
    @Override
    public String toString() {
      return new StringBuilder().append("filePath: ")
                                .append(this.filePath)
                                .append(", resourceName: ")
                                .append(this.resourceName)
                                .toString();
    }
  }

}

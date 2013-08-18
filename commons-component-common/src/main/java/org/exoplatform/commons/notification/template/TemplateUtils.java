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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.api.notification.plugin.config.TemplateConfig;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.api.notification.template.Element;
import org.exoplatform.commons.api.notification.template.ElementVisitor;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.cache.ElementCacheKey;
import org.exoplatform.commons.notification.cache.TemplateCaching;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.groovyscript.GroovyTemplate;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.ResourceBundleService;
import org.gatein.common.io.IOTools;

public class TemplateUtils {
  
  private static final Log LOG = ExoLogger.getLogger(TemplateUtils.class);
  private static Map<String, Element> cacheTemplate = new ConcurrentHashMap<String, Element>();
  
  /**
   * Process the Groovy template ascossiate with Template context to generate
   * It will be use for digest mail
   * @param ctx
   * @return
   */
  public static String processGroovy(TemplateContext ctx) {
    ElementCacheKey cacheKey = new ElementCacheKey(ctx.getPluginId(), ctx.getLanguage());
    Element groovyElement = CommonsUtils.getService(TemplateCaching.class).getTemplateElement(cacheKey);
    
    ElementVisitor visitor = new GroovyElementVisitor();
    String content = visitor.with(ctx).visit(groovyElement).out();
    return content;
  }
  
  /**
   * Generate the Groovy Template
   * @param context
   * @param template
   * @return
   */
  public static void loadGroovy(TemplateContext context, Element element, Writer out) {
    
    try {
      String groovyTemplate = element.getTemplate();
      if (groovyTemplate == null) {
        groovyTemplate = loadGroovyTemplate(element.getTemplateConfig().getTemplatePath());
        element.template(groovyTemplate);
      }
      GroovyTemplate gTemplate = new GroovyTemplate(groovyTemplate);
      //
      gTemplate.render(out, context);
    } catch (Exception e) {
    }
  }
  
  /**
   * Gets InputStream for groovy template
   * 
   * @param templatePath
   * @return
   * @throws Exception
   */
  private static InputStream getTemplateInputStream(String templatePath) throws Exception {
    try {
      
      ConfigurationManager configurationManager =  CommonsUtils.getService(ConfigurationManager.class);
      
      String uri = templatePath;
      if (templatePath.indexOf("war") < 0 && templatePath.indexOf("jar") < 0) {
        URL url = null;
        if (templatePath.indexOf("/") == 0) {
          templatePath = templatePath.substring(1);
        }
        uri = "war:/" + templatePath;
        url = configurationManager.getURL(uri);
        if (url == null) {
          uri = "jar:/" + templatePath;
          url = configurationManager.getURL(uri);
        }
      }
      return configurationManager.getInputStream(uri);
    } catch (Exception e) {
      throw new RuntimeException("Error to get notification template " + templatePath, e);
    }
  }

  /**
   * Loads the Groovy template file
   * 
   * @param templatePath
   * @return
   * @throws Exception
   */
  public static String loadGroovyTemplate(String templatePath) throws Exception {
    StringWriter templateText = new StringWriter();
    Reader reader = null;
    try {
      reader = new InputStreamReader(getTemplateInputStream(templatePath));
      IOTools.copy(reader, templateText);
    } catch (Exception e) {
      LOG.debug("Failed to reader template file: " + templatePath, e);
    } finally {
      if (reader != null) {
        reader.close();
      }
    }

    return templateText.toString();
  }

  /**
   * Load the groovy template element.
   * 
   * @param key
   * @param language
   * @return the groovy element
   */
  public static Element loadGroovyElement(String pluginId, String language) {
    TemplateConfig templateConfig = getTemplateConfig(pluginId);
    return new GroovyElement().language(language).config(templateConfig);
  }
  
  /**
   * Render for Subject template
   * @param ctx
   * @return
   */
  public static String processSubject(TemplateContext ctx) {
    Element subject = null;
    String key = new StringBuffer(ctx.getPluginId()).append(ctx.getLanguage()).toString();
    if (cacheTemplate.containsKey(key)) {
      subject = cacheTemplate.get(key);
    } else {
      TemplateConfig templateConfig = getTemplateConfig(ctx.getPluginId());
      subject = NotificationUtils.getSubject(templateConfig, ctx.getPluginId(), ctx.getLanguage());
      cacheTemplate.put(key, subject);
    }
    return subject.accept(SimpleElementVistior.instance().with(ctx)).out();
  }

  /**
   * Render for digest template
   * @param ctx
   * @return
   */
  public static String processDigest(TemplateContext ctx) {
    DigestTemplate digest = null;
    String key = new StringBuffer(ctx.getPluginId()).append(ctx.getLanguage()).toString();
    if (cacheTemplate.containsKey(key)) {
      digest = (DigestTemplate) cacheTemplate.get(key);
    } else {
      TemplateConfig templateConfig = getTemplateConfig(ctx.getPluginId());
      digest = NotificationUtils.getDigest(templateConfig, ctx.getPluginId(), ctx.getLanguage());
      cacheTemplate.put(key, digest);
    }
    
    return digest.accept(SimpleElementVistior.instance().with(ctx)).out();
  }


  /**
   * Gets Plugin configuration for specified PluginId
   * @param pluginId
   * @return
   */
  private static TemplateConfig getTemplateConfig(String pluginId) {
    PluginConfig pluginConfig = NotificationContextImpl.cloneInstance().getPluginSettingService().getPluginConfig(pluginId);
    
    if(pluginConfig == null) {
      throw new IllegalStateException("PluginConfig is NULL with plugId = " + pluginId);
    }
    
    return pluginConfig.getTemplateConfig();
  }
  
  /**
   * Gets Resource Bundle value
   * @param key
   * @param locale
   * @param resourcePath
   * @return
   */
  public static String getResourceBundle(String key, Locale locale, String resourcePath) {
    if (key == null || key.trim().length() == 0) {
      return "";
    }
    if (locale == null || locale.getLanguage().isEmpty()) {
      locale = Locale.ENGLISH;
    }

    ResourceBundleService bundleService = CommonsUtils.getService(ResourceBundleService.class);
    ResourceBundle res = bundleService.getResourceBundle(resourcePath, locale);
    
    if (res == null || res.containsKey(key) == false) {
      LOG.warn("Resource Bundle key not found. " + key);
      return key;
    }

    return res.getString(key);
  }
  
  

}

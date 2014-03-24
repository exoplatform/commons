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
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
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
  private static final String DIGEST_TEMPLATE_KEY = "Digest.{0}.{1}";
  private static final String SIMPLE_TEMPLATE_KEY = "Simple.{0}.{1}";
  private static final Pattern SCRIPT_REMOVE_PATTERN = Pattern.compile("<(script|style)[^>]*>[^<]*</(script|style)>", Pattern.CASE_INSENSITIVE);
  private static final Pattern TAGS_REMOVE_PATTERN = Pattern.compile("<[^>]*>", Pattern.CASE_INSENSITIVE);

  private static Map<String, Element> cacheTemplate = new ConcurrentHashMap<String, Element>();
  
  private static final int MAX_SUBJECT_LENGTH = 50;
  
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
      if (templatePath.indexOf("war") < 0 && templatePath.indexOf("jar") < 0 && templatePath.indexOf("classpath") < 0) {
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
    Element subjectElement = null;
    String key = makeTemplateKey(SIMPLE_TEMPLATE_KEY, ctx.getPluginId(), ctx.getLanguage());
    if (cacheTemplate.containsKey(key)) {
      subjectElement = cacheTemplate.get(key);
    } else {
      TemplateConfig templateConfig = getTemplateConfig(ctx.getPluginId());
      subjectElement = NotificationUtils.getSubject(templateConfig, ctx.getPluginId(), ctx.getLanguage()).addNewLine(false);
      cacheTemplate.put(key, subjectElement);
    }
    
    //The title of activity is escaped on social, then we need to unescape it to process the send email
    String value = (String) ctx.get("ACTIVITY");
    if (value != null) {
      ctx.put("ACTIVITY", StringEscapeUtils.unescapeHtml(value));
    }

    String subject = (String) ctx.get("SUBJECT");
    if (subject != null && subject.length() > 0) {
      ctx.put("SUBJECT", getExcerptSubject(subject));
      return subjectElement.accept(SimpleElementVistior.instance().with(ctx)).out();
    }
    //
    subject = subjectElement.accept(SimpleElementVistior.instance().with(ctx)).out();
    return getExcerptSubject(subject);
  }
  
  /**
   * Get the excerpt subject of notification mail from origin string
   *  - Just contains plain text
   *  - Limit number of characters
   * @param subject the origin string
   * @return the excerpt of subject
   * @since 4.1.x
   */
  public static String getExcerptSubject(String subject) {
    String newSubject = StringEscapeUtils.unescapeHtml(cleanHtmlTags(subject));
    if (newSubject.length() > MAX_SUBJECT_LENGTH) {
      newSubject = newSubject.substring(0, MAX_SUBJECT_LENGTH);
      int lastSpace = newSubject.lastIndexOf(" ");
      return newSubject.substring(0, lastSpace) + "...";
    }

    return newSubject;
  }
  
  /**
   * Clean all HTML tags on string
   *  
   * @param str the origin string
   * @return The string has not contain HTML tags.
   * @since 4.1.x
   */
  public static String cleanHtmlTags(String str) {
    //
    if (str == null || str.trim().length() == 0) {
      return "";
    }
    // clean multi-lines
    String newSubject = StringUtils.replace(str, "\n", " ");
    // clean script
    newSubject = SCRIPT_REMOVE_PATTERN.matcher(newSubject).replaceAll("");
    // clean tags HTML
    newSubject = TAGS_REMOVE_PATTERN.matcher(newSubject).replaceAll(" ");
    return newSubject.replaceAll("\\s+", " ").trim();
  }

  /**
   * Render for digest template
   * @param ctx
   * @return
   */
  public static String processDigest(TemplateContext ctx) {
    DigestTemplate digest = null;
    String key = makeTemplateKey(DIGEST_TEMPLATE_KEY, ctx.getPluginId(), ctx.getLanguage());
    if (cacheTemplate.containsKey(key)) {
      digest = (DigestTemplate) cacheTemplate.get(key);
    } else {
      TemplateConfig templateConfig = getTemplateConfig(ctx.getPluginId());
      digest = NotificationUtils.getDigest(templateConfig, ctx.getPluginId(), ctx.getLanguage());
      cacheTemplate.put(key, digest);
    }
    
    return digest.accept(SimpleElementVistior.instance().with(ctx)).out();
  }
  
  private static String makeTemplateKey(String pattern, String pluginId, String language) {
    return MessageFormat.format(pattern, pluginId, language);
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

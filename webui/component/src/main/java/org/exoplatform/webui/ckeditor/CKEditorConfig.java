/*
Copyright (c) 2003-2011, CKSource - Frederico Knabben. All rights reserved.
For licensing, see http://ckeditor.com/license
 */
package org.exoplatform.webui.ckeditor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CKEditor configuration class.
 */
public class CKEditorConfig implements Cloneable {

  private Map<String, Object> config;

  /**
   * Default constructor.
   */
  public CKEditorConfig() {
    config = new HashMap<String, Object>();
  }

  /**
   * Adds a Number parameter to the configuration. <b>Usage:</b>
   * 
   * <pre>
   * config.addConfigValue(&quot;width&quot;, 100);
   * </pre>
   * 
   * <pre>
   * config.addConfigValue(&quot;dialog_backgroundCoverOpacity&quot;, 0.7);
   * </pre>
   * 
   * @param key configuration parameter key
   * @param value configuration parameter value.
   */
  public void addConfigValue(final String key, final Number value) {
    config.put(key, value);
  }

  /**
   * Adds a String parameter to the configuration. <b>Usage:</b>
   * 
   * <pre>
   * config.addConfigValue(&quot;baseHref&quot;, &quot;http://www.example.com/path/&quot;);
   * </pre>
   * 
   * <pre>
   * config.addConfigValue(&quot;toolbar&quot;, &quot;[[ 'Source', '-', 'Bold', 'Italic' ]]&quot;);
   * </pre>
   * 
   * @param key configuration parameter key
   * @param value configuration parameter value.
   */
  public void addConfigValue(final String key, final String value) {
    config.put(key, value);
  }

  /**
   * Adds a Map parameter to the configuration. <b>Usage:</b>
   * 
   * <pre>
   * Map&lt;String, Object&gt; map = new HashMap&lt;String, Object&gt;();
   * </pre>
   * 
   * <pre>
   * map.put(&quot;element&quot;, &quot;span&quot;);
   * </pre>
   * 
   * <pre>
   * map.put(&quot;styles&quot;, &quot;{'background-color' : '#(color)'}&quot;);
   * </pre>
   * 
   * <pre>
   * config.addConfigValue(&quot;colorButton_backStyle&quot;, map);
   * </pre>
   * 
   * @param key configuration parameter key
   * @param value configuration parameter value.
   */
  public void addConfigValue(final String key, final Map<String, ? extends Object> value) {
    config.put(key, value);
  }

  /**
   * Adds a List parameter to the configuration. <b>Usage:</b>
   * 
   * <pre>
   * List&lt;List&lt;String&gt;&gt; list = new ArrayList&lt;List&lt;String&gt;&gt;();
   * List&lt;String&gt; subList = new ArrayList&lt;String&gt;();
   * subList.add(&quot;Source&quot;);
   * subList.add(&quot;-&quot;);
   * subList.add(&quot;Bold&quot;);
   * subList.add(&quot;Italic&quot;);
   * list.add(subList);
   * config.addConfigValue(&quot;toolbar&quot;, list);
   * </pre>
   * 
   * @param key configuration parameter key
   * @param value configuration parameter value.
   */
  public void addConfigValue(final String key, final List<? extends Object> value) {
    config.put(key, value);
  }

  /**
   * Adds a Boolean parameter to the configuration. <b>Usage:</b>
   * 
   * <pre>
   * config.addConfigValue(&quot;autoUpdateElement&quot;, true);
   * </pre>
   * 
   * @param key configuration parameter key
   * @param value configuration parameter value.
   */
  public void addConfigValue(final String key, final Boolean value) {
    config.put(key, value);
  }

  /**
   * Gets a configuration value by key.
   * 
   * @param key configuration parameter key
   * @return configuration parameter value.
   */
  Object getConfigValue(final String key) {
    return config.get(key);
  }

  /**
   * @return all configuration values.
   */
  Map<String, Object> getConfigValues() {
    return config;
  }

  /**
   * Removes a configuration value by key. <b>Usage:</b>
   * 
   * <pre>
   * config.removeConfigValue(&quot;toolbar&quot;);
   * </pre>
   * 
   * @param key configuration parameter key.
   */
  public void removeConfigValue(final String key) {
    config.remove(key);
  }

  /**
   * Configure settings. Merge configuration and event handlers.
   * 
   * @return setting configuration.
   * @param eventHandler events
   */
  public CKEditorConfig configSettings(final EventHandler eventHandler) {
    try {
      CKEditorConfig cfg = (CKEditorConfig) this.clone();
      if (eventHandler != null) {
        for (String eventName : eventHandler.events.keySet()) {
          Set<String> set = eventHandler.events.get(eventName);
          if (set.isEmpty()) {
            continue;
          } else if (set.size() == 1) {
            Map<String, String> hm = new HashMap<String, String>();
            for (String code : set) {
              hm.put(eventName, "@@" + code);
            }
            cfg.addConfigValue("on", hm);
          } else {
            Map<String, String> hm = new HashMap<String, String>();
            StringBuilder sb = new StringBuilder("@@function (ev){");
            for (String code : set) {
              sb.append("(");
              sb.append(code);
              sb.append(")(ev);");
            }
            sb.append("}");
            hm.put(eventName, sb.toString());
            cfg.addConfigValue("on", hm);
          }
        }
      }
      return cfg;
    } catch (CloneNotSupportedException e) {
      return null;
    }

  }

  /**
   * Checks if configuration is empty.
   * 
   * @return true if the configuration is empty.
   */
  public boolean isEmpty() {
    return config.isEmpty();
  }

  /**
   * Override.
   */
  protected Object clone() throws CloneNotSupportedException {
    CKEditorConfig cfg = (CKEditorConfig) super.clone();
    cfg.config = new HashMap<String, Object>(this.config);
    return cfg;
  }

}

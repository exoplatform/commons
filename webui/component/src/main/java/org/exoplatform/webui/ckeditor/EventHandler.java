/*
Copyright (c) 2003-2011, CKSource - Frederico Knabben. All rights reserved.
For licensing, see http://ckeditor.com/license
 */
package org.exoplatform.webui.ckeditor;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * CKEditor event handler class. <b>Usage:</b>
 * 
 * <pre>
 * EventHandler eventHandler = new EventHandler();
 * eventHandler.addEventHandler("instanceReady","function (ev) {
 * alert(\"Loaded: \" + ev.editor.name); }");
 * </pre>
 */
public class EventHandler {

  protected Map<String, Set<String>> events;

  /**
   * Default constructor.
   */
  public EventHandler() {
    events = new HashMap<String, Set<String>>();
  }

  /**
   * Adds an event listener.
   * 
   * @param event Event name
   * @param jsCode JavaScript anonymous function or a function name
   */
  public void addEventHandler(final String event, final String jsCode) {
    if (events.get(event) == null) {
      events.put(event, new LinkedHashSet<String>());
    }
    events.get(event).add(jsCode);
  }

  /**
   * Clears registered event handlers.
   * 
   * @param event Event name. If null, all event handlers will be removed
   *          (optional).
   */
  public void clearEventHandlers(final String event) {
    if (event == null) {
      events = new HashMap<String, Set<String>>();
    } else {
      if (events.get(event) != null) {
        events.get(event).clear();
      }
    }
  }

  /**
   * Gets all registered events.
   * 
   * @return all registered events
   */
  public Map<String, Set<String>> getEvents() {
    return events;
  }

}

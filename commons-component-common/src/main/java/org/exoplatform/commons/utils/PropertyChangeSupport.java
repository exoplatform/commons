/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.utils;

import java.beans.PropertyChangeEvent;

/**
 * Created by The eXo Platform SAS Author : thanh_vucong
 * thanh_vucong@exoplatform.com Jan 10, 2013
 */
public class PropertyChangeSupport implements java.io.Serializable {

  /**
  * Serialization version ID
  */
 static final long serialVersionUID = 0L;
  
  PropertyChangeEvent[] changeEvents = new PropertyChangeEvent[0];

  /**
   * The object to be provided as the "source" for any generated events.
   * 
   * @serial
   */
  private Object source;
  
  /**
  * Lock object for change to listeners
  */
    private final Object lock = new Object();

  public PropertyChangeSupport(Object sourceBean) {
    if (sourceBean == null) {
      throw new NullPointerException();
    }
    source = sourceBean;
  }

  /**
   * Adds PropertyChangeEvent. No event is
   * fired if old and new are equal and non-null.
   * <p>
   * 
   * @param propertyName The programmatic name of the property that was changed.
   * @param oldValue The old value of the property.
   * @param newValue The new value of the property.
   */
  public void addPropertyChange(String propertyName, Object oldValue, Object newValue) {
    if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
      return;
    }
    addPropertyChange(new PropertyChangeEvent(source, propertyName, oldValue, newValue));
  }

  /**
   * Adds an existing PropertyChangeEvent. No event
   * is created if the given event's old and new values are equal and non-null.
   * 
   * @param evt The PropertyChangeEvent object.
   */
  public void addPropertyChange(PropertyChangeEvent evt) {
    Object oldValue = evt.getOldValue();
    Object newValue = evt.getNewValue();
    if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
      return;
    }
    synchronized(lock) {
      PropertyChangeEvent results[] = new PropertyChangeEvent[changeEvents.length + 1];
      System.arraycopy(changeEvents,0,results,0,changeEvents.length);
      //Add the PropertyChangeListener to the new position.
      results[changeEvents.length] = evt;
      changeEvents = results;
    }
  }
  
  /**
   * Removes an existing PropertyChangeEvent.
   * 
   * @param propertyName propertyName
   */
  public boolean removePropertyChange(String propertyName) {
    if (hasPropertyName(propertyName)) {
      synchronized(lock) {
        PropertyChangeEvent results[] = new PropertyChangeEvent[changeEvents.length -1];
        int j = 0;
        for (int i = 0; i < changeEvents.length; i++) {
          if (changeEvents[i].getPropertyName().equals(propertyName) == false) {
            results[j++] = changeEvents[i];
          }
        }
        changeEvents = results;
      }
      
      return true;
    }
    
    return false;
  }

  /**
   * Gets PropertyChangeEvents
   * @return
   */
  public PropertyChangeEvent[] getChangeEvents() {
    return changeEvents;
  }
  
  /**
   * Gets PropertyChangeEvent by propertyName
   * @param propertyName
   * @return
   */
  public PropertyChangeEvent getPropertyChange(String propertyName) {
    for (int i = 0; i < changeEvents.length; i++) {
      if (changeEvents[i].getPropertyName().equals(propertyName)) {
        return changeEvents[i];
      }
    }
    
    return null;
  }
  
  /**
   * Has any PropertyChangeEvent by propertyName or not
   * @param propertyName
   * @return True or False
   */
  public boolean hasPropertyName(String propertyName) {
    for (int i = 0; i < changeEvents.length; i++) {
      if (changeEvents[i].getPropertyName().equals(propertyName)) {
        return true;
      }
    }
    
    return false;
  }
}

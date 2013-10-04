/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 *
 **************************************************************************/
package org.exoplatform.commons.api.event;

import java.util.List;

import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;

/**
 * Is the central point of the event system where listeners are registered (and/or unregistered) and events are fired.
 * @param <S> This is a generic object of source. It can be a file/folder/content or something else 
 * extended from <code>BaseObject</code>.
 * @param <D> This is a generic object of data. It can be an event type, such as NODE_ADDED/PROPERTY_CHANGED/NODE_REMOVED
 * which is corresponding to the JCR events.
 * @LevelAPI Experimental
 */
public interface EventManager<S, D> {

    /**
     * Registers a listener into the event system.  
     * @param listener An instance of <code>Listener</code> object.
     * @LevelAPI Experimental
     */
    public void addEventListener(Listener<S, D> listener);
    
    /**
     * Registers a listener for a given event group in the event system.
     * @param listener An instance of <code>Listener</code> object.
     * @param eventName Name of the event group.
     * @LevelAPI Experimental
     */
    public void addEventListener(String eventName, Listener<S, D> listener);    

    /**
     * Unregisters a listener out of the event system.  
     * @param listener An instance of <code>Listener</code> object.
     * @LevelAPI Experimental
     */
    public void removeEventListener(Listener<S, D> listener);
    
    /**
     * Unregisters a listener out of a given event group in the event system.  
     * @param listener An instance of <code>Listener</code> object.
     * @param eventName Name of the event group.
     * @LevelAPI Experimental
     */
    public void removeEventListener(String eventName, Listener<S, D> listener);    

    /**
     * Broadcasts an event to a dedicated listener 
	 * when an action (such as create or update) is triggered.
     * @param event The <code>Event</code> object which keeps information to be processed in the listeners.
     * @LevelAPI Experimental
     */
    public void broadcastEvent(Event<S, D> event);

    /**
     * Gets a list of listeners which are registered into the event system based on a given event group.
     * For example: If you want to get a list of listeners which are registered for listening to all the events on an instance of 
     * <code>File</code> object, the event group name will be <b>getObjectType()</b>.
     * 
     * @param type Name of the event group.
     * @return The list of listeners which are registered.
     * @LevelAPI Experimental
     */
    public List<Listener<S, D>> getEventListeners(String type);

}

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
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 8, 2012
 * 1:47:47 PM  
 */
/**
 * This is the central point of the event system where listeners are registered (and/or unregistered) and events fired.
 * @param <S> This is a generic object of source, it can be a File/Folder/Content or something else 
 * which should be extended from <code>BaseObject</code>.
 * @param <D> This is a generic object of data. It can be an event type such as NODE_ADDED/PROPERTY_CHANGED/NODE_REMOVED
 * which corresponding with JCR events.
 */
public interface EventManager<S, D> {

    /**
     * This will be used to register a listener to the event system.  
     * @param listener An instance of <code>Listener</code> object.
     */
    public void addEventListener(Listener<S, D> listener);
    
    /**
     * This will be used to register a listener to the event system.  
     * @param listener An instance of <code>Listener</code> object.
     * @param eventName Name of event which will be used to group listeners.
     */
    public void addEventListener(String eventName, Listener<S, D> listener);    

    /**
     * This will be used to unregister a listener out of the event system.  
     * @param listener An instance of <code>Listener</code> object.
     */
    public void removeEventListener(Listener<S, D> listener);
    
    /**
     * This will be used to unregister a listener out of the event system.  
     * @param listener An instance of <code>Listener</code> object.
     * @param eventName The event name which used to group listeners.
     */
    public void removeEventListener(String eventName, Listener<S, D> listener);    

    /**
     * When an action triggered such as file created/updated then 
     * it will be broadcast to the dedicated listener to dispatch the event.
     * @param event The <code>Event</code> object which keep the information to be processed in the listeners.
     */
    public void broadcastEvent(Event<S, D> event);

    /**
     * Return a list of <code>Listener</code> which registered to the event system based on its object type.
     * For example: If we want to get a list of listeners which registered to listen all the event on an instance of 
     * <code>File</code> object then the type should be gotten from its method is getObjectType().
     * 
     * @param type Type of Object
     * @return List of listeners which registered
     */
    public List<Listener<S, D>> getEventListeners(String type);

}

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

import org.exoplatform.services.listener.Event;

/** 
 * Processes all the dedicated works which are delegated from <code>EventManager</code>.
 * To adapt to the event manager system, all listeners should be implemented into this interface.
 * @param <S> This is a generic object of source. It can be a file/folder/content or something else 
 * extended from <code>BaseObject</code>.
 * @param <D> This is a generic object of data. It can be an event type, such as NODE_ADDED/PROPERTY_CHANGED/NODE_REMOVED.
 * @LevelAPI Experimental
 */
public interface EventListener<S, D> {

    /**
     * Processes a dedicated work when the create event is triggered.
     * @param event The <code>Event</code> object.
     * @LevelAPI Experimental
     */
    public void create(Event<S, D> event);

    /**
     * Processes a dedicated work when the update event is triggered.
     * @param event The <code>Event</code> object.
     * @LevelAPI Experimental
     */
    public void update(Event<S, D> event);

    /**
     * Processes a dedicated work when the remove event is triggered.
     * @param event The <code>Event</code> object.
     * @LevelAPI Experimental
     */
    public void remove(Event<S, D> event);

}

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
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 13, 2012
 * 3:00:06 PM  
 */
/** 
 * This used to process all the dedicated work which be delegated from <code>EventManager</code>
 * To adapt with event manager system then all listeners should be implemented this interface.
* @param <S> This is a generic object of source, it can be a File/Folder/Content or something else 
 * which should be extended from <code>BaseObject</code>.
 * @param <D> This is a generic object of data. It can be an event type such as NODE_ADDED/PROPERTY_CHANGED/NODE_REMOVED
 */
public interface EventListener<S, D> {

    /**
     * Process the dedicated work when the create event has been triggered.
     * @param event The <code>Event</code> object.
     */
    public void create(Event<S, D> event);

    /**
     * Process the dedicated work when the update event has been triggered.
     * @param event The <code>Event</code> object.
     */
    public void update(Event<S, D> event);

    /**
     * Process the dedicated work when the remove event has been triggered.
     * @param event The <code>Event</code> object.
     */
    public void remove(Event<S, D> event);

}

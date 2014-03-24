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
package org.exoplatform.commons.event.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.api.event.EventManager;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.naming.InitialContextInitializer;


/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 14, 2012
 * 3:49:21 PM  
 */
public class EventManagerImpl<S, D> extends ListenerService implements EventManager<S, D> {

    private Map<String, List<Listener<S, D>>> listenerMap = new HashMap<String, List<Listener<S, D>>>();

    private static final Log LOG = ExoLogger.getLogger(EventManagerImpl.class);

    public EventManagerImpl(ExoContainerContext ctx, InitialContextInitializer initializer, InitParams params) {
        super(ctx, initializer, params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEventListener(Listener<S, D> listener) {
        addEventListener(listener.getName(), listener);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addEventListener(String eventName, Listener<S, D> listener) {
        // Check is Listener or its superclass asynchronous, if so - wrap it in AsynchronousListener.
        Class<?> listenerClass = listener.getClass();
        do {
            if (listenerClass.isAnnotationPresent(Asynchronous.class)) {
                listener = new AsynchronousListener<S, D>(listener);
                break;
            }
            listenerClass = listenerClass.getSuperclass();
        } while (listenerClass != null);
        List<Listener<S, D>> list = listenerMap.get(eventName);
        if (list == null) {
            list = new ArrayList<Listener<S, D>>();
        }
        list.add(listener);
        listenerMap.put(eventName, list);
    }    

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeEventListener(Listener<S, D> listener) {
        removeEventListener(listener.getName(), listener);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void removeEventListener(String eventName, Listener<S, D> listener) {
        List<Listener<S, D>> listeners = getEventListeners(eventName);
        listeners.remove(listener);
        if(listeners.size() == 0) listenerMap.remove(eventName);
        listenerMap.put(eventName, listeners);
    }    

    /**
     * {@inheritDoc}
     */
    @Override
    public void broadcastEvent(Event<S, D> event) {
        List<Listener<S, D>> listeners = getEventListeners(event.getEventName());
        if (listeners.size() == 0) return;
        for (Listener<S, D> listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                LOG.error("Exception on broadcasting events occures: " + e.getMessage(), e);
            }
        }    
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Listener<S, D>> getEventListeners(String type) {
    	if(!listenerMap.containsKey(type)) {
    		return new ArrayList<Listener<S, D>>();
    	}
        return listenerMap.get(type);
    }

}

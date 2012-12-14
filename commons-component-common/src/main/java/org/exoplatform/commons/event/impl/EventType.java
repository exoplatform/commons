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

import org.exoplatform.services.jcr.observation.ExtendedEvent;


/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 13, 2012
 * 3:05:05 PM  
 */
public class EventType {

    public static final int CREATED = ExtendedEvent.NODE_ADDED;

    public static final int UPDATE = ExtendedEvent.PROPERTY_CHANGED;

    public static final int REMOVE = ExtendedEvent.NODE_REMOVED;

}

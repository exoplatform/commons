/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.commons.notification.mock;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

public class AddNodeEventListener implements EventListener {

  Session     session;

  public AddNodeEventListener() throws Exception {
  }

  public void onEvent(EventIterator evIter) {
    try {
      while (evIter.hasNext()) {
        Event ev = evIter.nextEvent();
        if (ev.getType() == Event.NODE_ADDED ) {
          Node node = (Node)session.getItem(ev.getPath());
          if(node.canAddMixin("exo:datetime")) {
            node.addMixin("exo:datetime");
            node.setProperty("exo:lastModifiedDate", Calendar.getInstance());
            node.setProperty("exo:dateCreated", Calendar.getInstance());
            node.save();
          }
          break;
        }
      }
    } catch (Exception e) {
    }
  }

  public void setSession(Session session) {
    this.session = session;
  }
}

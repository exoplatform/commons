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
package org.exoplatform.commons.search.driver.jcr;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.commons.search.Search;
import org.exoplatform.commons.search.SearchResult;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.Utils;

/**
 * Created by The eXo Platform SAS
 * Author : Canh Pham Van
 *          canhpv@exoplatform.com
 * Jan 3, 2013  
 */
public class JcrEventSearch implements Search{

  @Override
  public Collection<SearchResult> search(String query) {
    Collection<SearchResult> searchResults = new ArrayList<SearchResult>();
    try {
      int offset = 0;
      int limit = 0;      
      Collection<JcrSearchResult> jcrResults = JcrSearchService.search(JcrSearchService.buildSql("exo:calendarEvent", "CONTAINS(*, '" + query + "')", "", query), offset, limit);
      
      CalendarService calendarService = (CalendarService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarService.class);
      for (JcrSearchResult jcrResult: jcrResults){                
        String eventId = (String)jcrResult.getProperty("exo:id");        
        String calendarId = (String) jcrResult.getProperty("exo:calendarId");
        
        CalendarEvent calEvent = calendarService.getGroupEvent(eventId);                
        Calendar calendar = calendarService.getGroupCalendar(calendarId);
        
        SearchResult result = new SearchResult("event",calendar.getPrivateUrl());
        result.setTitle(calEvent.getSummary());
        result.setExcerpt(calEvent.getDescription()!=null?calEvent.getDescription():calEvent.getSummary());
        StringBuffer buf = new StringBuffer();
        buf.append(calEvent.getEventCategoryName());
        buf.append(" - ");
        SimpleDateFormat sdf = new SimpleDateFormat("EEEEE, MMMMMMMM d, yyyy K:mm a");
        buf.append(sdf.format(calEvent.getFromDateTime()));        
        buf.append(" - ");
        buf.append(calEvent.getLocation()!=null?calEvent.getLocation():calendar.getName());
        
        result.setDetail(buf.toString());        
        String    avatar = "/csResources/gadgets/events/skin/Events.png";
        result.setAvatar(avatar);
        searchResults.add(result);
      }      
    } catch (Exception e) {
      e.printStackTrace();
    } 

    return searchResults;
  }

}

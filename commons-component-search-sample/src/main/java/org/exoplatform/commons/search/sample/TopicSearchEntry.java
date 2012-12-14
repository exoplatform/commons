/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.commons.search.sample;

import java.util.Date;

import org.exoplatform.commons.search.SearchEntry;
import org.exoplatform.commons.search.SearchEntryId;
import org.exoplatform.commons.search.StandardEntry;
import org.exoplatform.forum.service.Topic;

/**
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Nov 21, 2012  
 */
public class TopicSearchEntry extends StandardEntry {
  public static final String ENTRY_COLLECTION = "forum";
  public static final String ENTRY_TYPE = "topic";
  
  public TopicSearchEntry(Topic topic, String categoryName, String forumName) {
    this.setId(new SearchEntryId(ENTRY_COLLECTION, ENTRY_TYPE, topic.getTopicName()));
    this.setTitle(topic.getTopicName());
    this.setExcerpt(topic.getDescription());
    this.setLastUpdateDate(topic.getModifiedDate().getTime());
    this.setLastUpdateAuthor(topic.getModifiedBy());
    this.setCreationAuthor(topic.getOwner());
    this.setCreationDate(topic.getCreatedDate().getTime());

    this.getContent().put("category", categoryName);
    this.getContent().put("forum", forumName);
  }

  public TopicSearchEntry(SearchEntry entry) {
    this.setId(entry.getId());
    this.setContent(entry.getContent());
  }

  @Override
  public String toString() {
    return this.getTitle() + " [" + this.getExcerpt() + " (created by " + this.getCreationAuthor() + " on " + new Date(this.getCreationDate()) + ")]";
  }

}

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


import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import org.exoplatform.commons.api.indexing.IndexingService;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.commons.api.search.SearchService;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.UserImpl;

/**
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Nov 21, 2012  
 */
public abstract class BaseTest extends TestCase implements SearchService {
  private final static Log LOG = ExoLogger.getLogger(BaseTest.class);
  protected IndexingService indexingService;
  
  protected User createUser(String userName, String firstName, String lastName, String email){
    UserImpl user = new UserImpl(userName);
    user.setId(userName);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmail(email);
    return user;
  }
  
  protected Topic createTopic(String name, String description, String owner){
    Topic topic = new Topic();
    topic.setTopicName(name);
    topic.setDescription(description);
    topic.setOwner(owner);
    topic.setCreatedDate(new Date());
    topic.setModifiedDate(new Date());
    return topic;
  }
  
  protected void search(String queryString){
    LOG.info("\n====================================\nSearching for '" + queryString + "' (categorized)...\nResults:");
    Map<String, Collection<SearchResult>> result = search(queryString, null, null, 0, 0, "", "");
    
    Iterator<String> iter = result.keySet().iterator();
    while(iter.hasNext()){
      String searchType = iter.next();
      LOG.info("\n" + searchType + ":");
      Collection<SearchResult> entries = result.get(searchType);
      Iterator<SearchResult> entriesIter = entries.iterator();
      while(entriesIter.hasNext()){
        LOG.info(" * " + entriesIter.next());
      }
    }
    
  }
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();    

    // users
    indexingService.add(new UserSearchEntry(createUser("john", "John", "Smith", "john@exoplatform.com")));
    indexingService.add(new UserSearchEntry(createUser("mary", "Mary", "Williams", "mary@exoplatform.com")));
    indexingService.add(new UserSearchEntry(createUser("jack", "Jack", "Miller", "jack@exoplatform.com")));
    indexingService.add(new UserSearchEntry(createUser("james", "James", "Davids", "james@exoplatform.com")));
    indexingService.add(new UserSearchEntry(createUser("ijohn", "John", "Indiana", "ijohn@exoplatform.com")));
    indexingService.add(new UserSearchEntry(createUser("cjohn", "John", "Felix Anthony Cena", "cjohn@exoplatform.com")));
    indexingService.add(new UserSearchEntry(createUser("anthony", "Anthony", "Hopkins", "anthony@exoplatform.com")));
    // forum topics
    indexingService.add(new TopicSearchEntry(createTopic("Elastic demo", "James's ElasticSearch demo", "james"), "elastic", "eXo Intranet forum"));
    indexingService.add(new TopicSearchEntry(createTopic("Solr search engine", "All about Solr", "john"), "solr", "eXo Intranet forum"));
    indexingService.add(new TopicSearchEntry(createTopic("Platform 4.0", "Platform 4's new features - by Mary", "mary"), "plf4", "eXo Community forum"));
    indexingService.add(new TopicSearchEntry(createTopic("Elastic in CloudWS", "Configuring Elastic search under cloudws environment", "john"), "elastic", "eXo Cloud Workspace forum"));
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    /*// users
    indexingService.delete(UserSearchEntryFactory.getEntryId("john"));
    indexingService.delete(UserSearchEntryFactory.getEntryId("mary"));
    indexingService.delete(UserSearchEntryFactory.getEntryId("jack"));
    indexingService.delete(UserSearchEntryFactory.getEntryId("james"));
    indexingService.delete(UserSearchEntryFactory.getEntryId("ijohn"));
    indexingService.delete(UserSearchEntryFactory.getEntryId("cjohn"));
    indexingService.delete(UserSearchEntryFactory.getEntryId("anthony"));
    // forum topics
    indexingService.delete(TopicSearchEntryFactory.getEntryId("Elastic demo"));
    indexingService.delete(TopicSearchEntryFactory.getEntryId("Solr search engine"));
    indexingService.delete(TopicSearchEntryFactory.getEntryId("Platform 4.0"));
    indexingService.delete(TopicSearchEntryFactory.getEntryId("Elastic in CloudWS"));*/
  }
  
}

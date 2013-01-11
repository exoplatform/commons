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

import org.exoplatform.commons.search.api.indexing.IndexingService;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumEventListener;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;

/**
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Nov 21, 2012  
 */
public class ForumEventIndexingListener extends ForumEventListener{

  @Override
  public void addTopic(Topic topic, String categoryId, String forumId){
    IndexingService indexingService = (IndexingService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IndexingService.class);    
    indexingService.add(new TopicSearchEntry(topic, categoryId, forumId));
  }

  @Override
  public void updateTopic(Topic topic, String categoryId, String forumId) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void addPost(Post post, String categoryId, String forumId, String topicId) {    
    
  }

  @Override
  public void updatePost(Post post, String categoryId, String forumId, String topicId) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveCategory(Category category) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveForum(Forum forum) {
    // TODO Auto-generated method stub
    
  }

}

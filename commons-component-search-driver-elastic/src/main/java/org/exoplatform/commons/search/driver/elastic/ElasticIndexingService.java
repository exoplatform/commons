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
package org.exoplatform.commons.search.driver.elastic;

import java.util.Iterator;
import java.util.Map;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.exoplatform.commons.search.api.indexing.IndexingService;
import org.exoplatform.commons.search.api.indexing.data.SearchEntry;
import org.exoplatform.commons.search.api.indexing.data.SearchEntryId;

/**
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Nov 21, 2012  
 */
public class ElasticIndexingService extends IndexingService{
//  private final static Log LOG = ExoLogger.getLogger(ElasticIndexingService.class);
  private Client client;
  
  public ElasticIndexingService(Client client){
    this.client = client;
  }  
  
  @Override
  public void add(SearchEntry searchEntry) {
    SearchEntryId id = searchEntry.getId();
    IndexRequest indexRequest = new IndexRequest(id.getCollection(), id.getType(), id.getName());
    Map<String, Object> entryContent = searchEntry.getContent();
    entryContent.put(DATE_INDEXED, System.currentTimeMillis()); // store meta data
    indexRequest.source(entryContent);

    indexRequest.refresh(true);
    client.index(indexRequest).actionGet();
  }

  @Override
  public void update(SearchEntryId id, Map<String, Object> changes){
    changes.put(LAST_UPDATE, System.currentTimeMillis()); // store meta data
    Iterator<String> it = changes.keySet().iterator();
    StringBuilder script = new StringBuilder();
    while(it.hasNext()){
      String key = it.next();
      script.append("ctx._source." + key + "=" + key + "; ");
    }
    
    UpdateRequest updateRequest  = new UpdateRequest(id.getCollection(), id.getType(), id.getName());
    updateRequest.script(script.toString(), changes);
    
    updateRequest.refresh(true);
    client.update(updateRequest).actionGet();
  }

  @Override
  public void delete(SearchEntryId id) {
    DeleteRequest deleteRequest = new DeleteRequest(id.getCollection(), id.getType(), id.getName());

    deleteRequest.refresh(true);
    client.delete(deleteRequest).actionGet();
  }
  
}
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
package org.exoplatform.commons.search.driver.solr;

import java.util.Map;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.exoplatform.commons.search.IndexingService;
import org.exoplatform.commons.search.SearchEntry;
import org.exoplatform.commons.search.SearchEntryId;

/**
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Nov 21, 2012  
 */
public class SolrIndexingService extends IndexingService{
//  private final static Log LOG = ExoLogger.getLogger(ElasticIndexingService.class);
  private SolrServer server;
  
  public SolrIndexingService(SolrServer server){
    this.server = server;
  }  
    
  @Override
  public void add(SearchEntry searchEntry) {
    try {
      SolrInputDocument doc = new SolrInputDocument();
      doc.addField("id", searchEntry.getId().toString());
      Map<String, Object> dynaFields = SolrUtils.toDynamicFields(searchEntry.getContent());
      for (Map.Entry<String, Object> entry : dynaFields.entrySet()) {
        doc.addField(entry.getKey(), entry.getValue());
      }
      server.add(doc);
      server.commit();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void update(SearchEntryId id, Map<String, Object> changes){
  }

  @Override
  public void delete(SearchEntryId id) {
    try {
      server.deleteById(id.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
}
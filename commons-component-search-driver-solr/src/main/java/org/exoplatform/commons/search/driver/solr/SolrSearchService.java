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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.exoplatform.commons.api.indexing.data.SearchEntry;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Nov 21, 2012  
 */
public class SolrSearchService {
  private final static Log LOG = ExoLogger.getLogger(SolrSearchService.class);
  
  private static SolrServer server;
  
  public static SolrServer getServer() {
    return server;
  }

  public static void setServer(SolrServer server) {
    SolrSearchService.server = server;
  }

  public static Collection<SearchResult> search(String query) {
    Collection<SearchResult> results = new ArrayList<SearchResult>();
    
    SolrQuery params = new SolrQuery();
    params.set("qt", "/select");
    params.set("q", query);

    QueryResponse rsp = null;
    try {
      rsp = server.query(params);
    } catch (SolrServerException e) {
      LOG.error(e.getMessage(), e);
    }
    SolrDocumentList docs = rsp.getResults();
    for (SolrDocument doc : docs) {
      String id = (String) doc.getFieldValue("id");
      String[] sa = id.split("/");
      if(3 == sa.length) {
        SearchResult result = new SearchResult();
        result.setUrl(new SearchEntry(sa[0], sa[1], sa[2], SolrUtils.fromDynamicFields(doc.getFieldValueMap())).toString());
        results.add(result);
      }
    }
    return results;
  }
}


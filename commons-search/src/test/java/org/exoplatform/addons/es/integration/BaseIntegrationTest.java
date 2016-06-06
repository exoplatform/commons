/*
 *
 *  * Copyright (C) 2003-2015 eXo Platform SAS.
 *  *
 *  * This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License
 *  as published by the Free Software Foundation; either version 3
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see<http://www.gnu.org/licenses/>.
 *
 */

package org.exoplatform.addons.es.integration;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.Version;
import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.cluster.metadata.AliasOrIndex;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.mapper.attachments.MapperAttachmentsPlugin;
import org.elasticsearch.node.Node;

import org.elasticsearch.plugin.deletebyquery.DeleteByQueryPlugin;
import org.elasticsearch.plugins.Plugin;
import org.exoplatform.addons.es.client.ElasticIndexingAuditTrail;
import org.exoplatform.addons.es.client.ElasticIndexingClient;
import org.exoplatform.addons.es.client.ElasticSearchingClient;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 * 10/1/15
 */
public class BaseIntegrationTest {
  private static final Log LOGGER = ExoLogger.getExoLogger(BaseIntegrationTest.class);
  protected static Node node;

  private static boolean propertiesSet;

  protected static ElasticIndexingClient elasticIndexingClient;
  protected static ElasticSearchingClient elasticSearchingClient;

  /**
   * Port used for embedded Elasticsearch.
   * The port is needed to set the system properties exo.es.index.server.url and exo.es.search.server.url that
   * are used by the services ElasticIndexingClient and ElasticSearchingClient. These services read these
   * system properties at init (constructor) and cannot be changed later (no setter). Since the eXo Container is
   * created only once for all the tests, the same instance of these services are used for all the tests, so
   * the port must be the same for all the tests. That's why it must be static.
   */
  private static Integer esPort = null;

  @BeforeClass
  public static void setUp() {
    startEmbeddedES();
    // If the test is executed standalone, the properties are not present at the
    // beginning
    // of the test, so they have to be removed at the end of the test to avoid
    // java.lang.AssertionError: System properties invariant violated from
    // com.carrotsearch.randomizedtesting
    // If another test ran before and set the properties, we must not remove the
    // properties
    // If we do, we'll get the same exception.
    propertiesSet = StringUtils.isBlank(System.getProperty("exo.es.index.server.url"));
    PropertyManager.setProperty("exo.es.index.server.url", "http://localhost:" + esPort);
    PropertyManager.setProperty("exo.es.search.server.url", "http://localhost:" + esPort);

    elasticIndexingClient = new ElasticIndexingClient(new ElasticIndexingAuditTrail());
    elasticSearchingClient = new ElasticSearchingClient(new ElasticIndexingAuditTrail());
  }

  @Before
  public void setup() {
    // Reset data
    deleteAllIndexesInES();
  }

  @AfterClass
  public static void cleanUrlProperties() {
    // Close ES Node
    if(node != null) {
      LOGGER.info("Embedded ES instance - Stopping");
      node.close();
      LOGGER.info("Embedded ES instance - Stopped");
    }

    if (propertiesSet) {
      System.clearProperty("exo.es.index.server.url");
      System.clearProperty("exo.es.indexing.batch.number");
      System.clearProperty("exo.es.indexing.replica.number.default");
      System.clearProperty("exo.es.indexing.shard.number.default");
      System.clearProperty("exo.es.search.server.url");
      System.clearProperty("jboss.i18n.generate-proxies");
    } else {
      //Reset server.url properties to default values
      PropertyManager.setProperty("exo.es.index.server.url", "http://127.0.0.1:9200");
      PropertyManager.setProperty("exo.es.search.server.url", "http://127.0.0.1:9200");
    }
  }

  /**
   * Start an Elasticsearch instance
   */
  private static void startEmbeddedES() {
    if(esPort == null) {
      try {
        esPort = getAvailablePort();
      } catch (IOException e) {
        fail("Cannot get available port : " + e.getMessage());
      }
    }

    // Init ES
    LOGGER.info("Embedded ES instance - Starting on port " + esPort);
    Settings.Builder elasticsearchSettings = Settings.settingsBuilder()
            .put(Node.HTTP_ENABLED, true)
            .put("network.host", "127.0.0.1")
            .put("http.port", esPort)
            .put("name", "esEmbeddedForTests" + esPort)
            .put("path.home", "target/es")
            .put("path.data", "target/es")
            .put("plugins.load_classpath_plugins", true);

    Environment environment = new Environment(elasticsearchSettings.build());
    Collection plugins = new ArrayList<>();
    Collections.<Class<? extends Plugin>>addAll(plugins, MapperAttachmentsPlugin.class, DeleteByQueryPlugin.class);
    node = new EmbeddedNode(environment, Version.CURRENT, plugins);
    node.start();
    //node = nodeBuilder().local(true).settings(elasticsearchSettings.build()).node();
    node.client().admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
    assertNotNull(node);
    assertFalse(node.isClosed());
    LOGGER.info("Embedded ES instance - Started");
    // Set URL of server in property
    NodesInfoRequest nodesInfoRequest = new NodesInfoRequest().transport(true);
    NodesInfoResponse response = node.client().admin().cluster().nodesInfo(nodesInfoRequest).actionGet();
    NodeInfo nodeInfo = response.iterator().next();
    InetSocketTransportAddress address = (InetSocketTransportAddress) nodeInfo.getHttp().getAddress().publishAddress();
    String url = "http://" + address.address().getHostName() + ":" + address.address().getPort();
    PropertyManager.setProperty("exo.es.index.server.url", url);
    PropertyManager.setProperty("exo.es.search.server.url", url);
  }

  /**
   * Get a random available port
   * @return
   * @throws IOException
   */
  private static int getAvailablePort() throws IOException {
    ServerSocket ss = null;
    DatagramSocket ds = null;
    try {
      ss = new ServerSocket(0);
      ss.setReuseAddress(true);
      ds = new DatagramSocket(0);
      ds.setReuseAddress(true);
      return ss.getLocalPort();
    } finally {
      if (ds != null) {
        ds.close();
      }

      if (ss != null) {
        try {
          ss.close();
        } catch (IOException e) {
                /* should not be thrown */
        }
      }
    }
  }

  protected void deleteAllIndexesInES() {
    SortedMap<String, AliasOrIndex> aliasAndIndexLookup = node.client().admin().cluster()
            .prepareState().execute().actionGet().getState().getMetaData().getAliasAndIndexLookup();
    for (String alias: aliasAndIndexLookup.keySet()) {
      node.client().admin().indices().prepareDelete(alias).execute().actionGet();
    }
    node.client().admin().indices().prepareRefresh().execute().actionGet();
  }

  protected boolean typeExists(String index, String type) {
    return node.client().admin().indices().prepareTypesExists(index).setTypes(type).execute().actionGet().isExists();
  }

  protected long documentNumber() {
    return node.client().prepareSearch().setSize(0).execute().actionGet().getHits().getTotalHits();
  }

  protected long documentNumber(String type) {
    return node.client().prepareSearch("_all").setSize(0).setQuery(QueryBuilders.termQuery("_type", type)).execute().actionGet().getHits().getTotalHits();
  }
}

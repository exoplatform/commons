/* 
* Copyright (C) 2003-2015 eXo Platform SAS.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see http://www.gnu.org/licenses/ .
*/
package org.exoplatform.addons.es.integration;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import org.exoplatform.addons.es.client.ElasticContentRequestBuilder;
import org.exoplatform.addons.es.client.ElasticIndexingAuditTrail;
import org.exoplatform.addons.es.dao.IndexingOperationDAO;
import org.exoplatform.addons.es.dao.impl.IndexingOperationDAOImpl;
import org.exoplatform.addons.es.domain.Document;
import org.exoplatform.addons.es.domain.IndexingOperation;
import org.exoplatform.addons.es.domain.OperationType;
import org.exoplatform.addons.es.index.IndexingOperationProcessor;
import org.exoplatform.addons.es.index.impl.ElasticIndexingOperationProcessor;
import org.exoplatform.addons.es.index.impl.ElasticIndexingServiceConnector;
import org.exoplatform.addons.es.search.ElasticSearchServiceConnector;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.junit.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 10/2/15
 */
public class SiteFilterIntTest extends BaseIntegrationTest {

  private static final String USERNAME = "thib";

  private static Connection conn;
  private static Liquibase liquibase;
  private IndexingOperationProcessor indexingOperationProcessor;
  private ElasticSearchServiceConnector elasticSearchServiceConnector;
  private IndexingOperationDAO dao;
  private ElasticIndexingServiceConnector testConnector;

  @BeforeClass
  public static void startDB () throws ClassNotFoundException, SQLException, LiquibaseException {
    Class.forName("org.hsqldb.jdbcDriver");
    conn = DriverManager.getConnection("jdbc:hsqldb:file:target/hsql-db", "sa", "");
    Database database = DatabaseFactory.getInstance()
        .findCorrectDatabaseImplementation(new JdbcConnection(conn));
    //Create Table
    liquibase = new Liquibase("./src/main/resources/db/changelog/exo-search.db.changelog-1.0.0.xml",
        new FileSystemResourceAccessor(), database);
    liquibase.update((String) null);
  }

  @AfterClass
  public static void stopDB () throws LiquibaseException, SQLException {
    if(liquibase != null) {
      liquibase.rollback(1000, null);
    }
    if(conn != null) {
      conn.close();
    }
  }

  @Before
  public void initServices() {
    super.setup();

    //Indexing Connector
    testConnector = mock(ElasticIndexingServiceConnector.class);
    when(testConnector.getType()).thenReturn("test");
    when(testConnector.getIndex()).thenReturn("test");
    when(testConnector.getShards()).thenReturn(1);
    when(testConnector.getReplicas()).thenReturn(1);
    when(testConnector.getMapping()).thenCallRealMethod();
    //IndexService
    dao = new IndexingOperationDAOImpl();
    ElasticContentRequestBuilder builder = new ElasticContentRequestBuilder();
    indexingOperationProcessor = new ElasticIndexingOperationProcessor(dao, elasticIndexingClient, builder, new ElasticIndexingAuditTrail(), null);
    indexingOperationProcessor.addConnector(testConnector);

    //Search connector
    elasticSearchServiceConnector = new ElasticSearchServiceConnector(getInitConnectorParams(), elasticSearchingClient);

    //Set identity
    setCurrentIdentity(USERNAME);
  }

  private void setCurrentIdentity(String userId, String... memberships) {
    Set<MembershipEntry> membershipEntrySet = new HashSet<>();
    if (memberships!=null) {
      for (String membership : memberships) {
        String[] membershipSplit = membership.split(":");
        membershipEntrySet.add(new MembershipEntry(membershipSplit[1], membershipSplit[0]));
      }
    }
    ConversationState.setCurrent(new ConversationState(new Identity(userId, membershipEntrySet)));
  }

  private InitParams getInitConnectorParams() {
    InitParams params = new InitParams();
    PropertiesParam constructorParams = new PropertiesParam();
    constructorParams.setName("constructor.params");
    constructorParams.setProperty("searchType", "test");
    constructorParams.setProperty("displayName", "test");
    constructorParams.setProperty("index", "test");
    constructorParams.setProperty("type", "test");
    constructorParams.setProperty("searchFields", "title");
    params.addParam(constructorParams);
    return params;
  }

  @Test
  public void test_searchIntranetSite_returnsIntranetDocument() throws IOException, InterruptedException {
    //Given
    dao.create(new IndexingOperation("1", "test", OperationType.CREATE));
    when(testConnector.create("1")).thenReturn(getSiteDocument("intranet"));
    indexingOperationProcessor.process();
    node.client().admin().indices().prepareRefresh().execute().actionGet();
    //When
    Collection<SearchResult> pages = elasticSearchServiceConnector.search(null, "test", getIntranetSiteInACollection(), 0, 20, null, null);
    //Then
    assertThat(pages.size(), is(1));
  }

  @Test
  public void test_searchIntranetSite_returnsNoDocumentAttachToOtherSite() throws IOException, InterruptedException {
    //Given
    dao.create(new IndexingOperation("1", "test", OperationType.CREATE));

    when(testConnector.create("1")).thenReturn(getSiteDocument("OtherSite"));
    indexingOperationProcessor.process();
    node.client().admin().indices().prepareRefresh().execute().actionGet();
    //When
    Collection<SearchResult> pages = elasticSearchServiceConnector.search(null, "test", getIntranetSiteInACollection(), 0, 20, null, null);
    //Then
    assertThat(pages.size(), is(0));
  }

  @Test
  public void test_searchIntranetSite_returnsDocumentNoAttachToSite() throws IOException, InterruptedException {
    //Given
    dao.create(new IndexingOperation("1", "test", OperationType.CREATE));
    when(testConnector.create("1")).thenReturn(getSiteDocument(null));
    indexingOperationProcessor.process();
    node.client().admin().indices().prepareRefresh().execute().actionGet();
    //When
    Collection<SearchResult> pages = elasticSearchServiceConnector.search(null, "test", getIntranetSiteInACollection(), 0, 20, null, null);
    //Then
    assertThat(pages.size(), is(1));
  }

  private Document getSiteDocument(String siteName) {
    Document document = new Document();
    document.addField("title", "A test document");
    if (siteName != null) document.setSites(new String[]{siteName});
    document.setPermissions(new HashSet<String>(Arrays.asList(USERNAME)));
    document.setId("1");
    return document;
  }

  private Collection<String> getIntranetSiteInACollection() {
    Collection<String> sites = new ArrayList<>();
    sites.add("intranet");
    return sites;
  }
  
}


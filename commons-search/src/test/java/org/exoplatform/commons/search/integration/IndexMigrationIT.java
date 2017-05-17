package org.exoplatform.commons.search.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.search.dao.IndexingOperationDAO;
import org.exoplatform.commons.search.dao.impl.IndexingOperationDAOImpl;
import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.commons.search.domain.IndexingOperation;
import org.exoplatform.commons.search.domain.OperationType;
import org.exoplatform.commons.search.es.client.ElasticContentRequestBuilder;
import org.exoplatform.commons.search.es.client.ElasticIndexingAuditTrail;
import org.exoplatform.commons.search.index.impl.ElasticIndexingOperationProcessor;
import org.exoplatform.commons.search.index.impl.ElasticIndexingServiceConnector;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 */
public class IndexMigrationIT extends BaseElasticsearchIT {
  private static Connection               conn;

  private static Liquibase                liquibase;

  private ElasticIndexingOperationProcessor indexingOperationProcessor;

  private IndexingOperationDAO dao;

  private EntityManagerService entityManagerService;

  private ElasticIndexingServiceConnector wikiConnector;

  @BeforeClass
  public static void startDB() throws ClassNotFoundException, SQLException, LiquibaseException {
    Class.forName("org.hsqldb.jdbcDriver");
    conn = DriverManager.getConnection("jdbc:hsqldb:file:target/hsql-db", "sa", "");
    Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));
    // Create Table
    liquibase = new Liquibase("./src/main/resources/db/changelog/exo-search.db.changelog-1.0.0.xml",
                              new FileSystemResourceAccessor(),
                              database);
    liquibase.update((String) null);
  }

  @AfterClass
  public static void stopDB() throws LiquibaseException, SQLException {
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

    // Indexing Connector
    wikiConnector = mock(ElasticIndexingServiceConnector.class);
    when(wikiConnector.getType()).thenReturn("wiki-page");
    when(wikiConnector.getIndex()).thenReturn("wiki-alias");
    when(wikiConnector.getCurrentIndex()).thenReturn("wiki");
    when(wikiConnector.getShards()).thenReturn(1);
    when(wikiConnector.getReplicas()).thenReturn(1);
    when(wikiConnector.getMapping()).thenCallRealMethod();
    // IndexService
    dao = new IndexingOperationDAOImpl();
    ElasticContentRequestBuilder builder = new ElasticContentRequestBuilder();
    entityManagerService = new EntityManagerService();
    entityManagerService.startRequest(null);
    InitParams initParams = new InitParams();
    ValueParam param = new ValueParam();
    param.setName("es.version");
    param.setValue(EmbeddedNode.ES_VERSION);
    initParams.addParameter(param);
    indexingOperationProcessor = new ElasticIndexingOperationProcessor(dao, elasticIndexingClient, builder, new ElasticIndexingAuditTrail(), entityManagerService, null, initParams);
    indexingOperationProcessor.addConnector(wikiConnector);
    indexingOperationProcessor.start();
  }

  @After
  public void tearDown() {
    entityManagerService.endRequest(null);
  }

  private void setCurrentIdentity(String userId, String... memberships) {
    Set<MembershipEntry> membershipEntrySet = new HashSet<>();
    if (memberships != null) {
      for (String membership : memberships) {
        String[] membershipSplit = membership.split(":");
        membershipEntrySet.add(new MembershipEntry(membershipSplit[1], membershipSplit[0]));
      }
    }
    ConversationState.setCurrent(new ConversationState(new Identity(userId, membershipEntrySet)));
  }

  @Test
  public void test_migration() throws IOException, InterruptedException {
    // Given
    setCurrentIdentity("Alice", "admin:/portal");

    // When
    when(wikiConnector.create("1")).thenReturn(createDocument("1"));
    when(wikiConnector.create("2")).thenReturn(createDocument("2"));
    when(wikiConnector.create("3")).thenReturn(createDocument("3"));

    dao.create(new IndexingOperation(null, "wiki-page", OperationType.INIT));
    dao.create(new IndexingOperation("1", "wiki-page", OperationType.CREATE));
    dao.create(new IndexingOperation("2", "wiki-page", OperationType.CREATE));
    dao.create(new IndexingOperation("3", "wiki-page", OperationType.CREATE));
    indexingOperationProcessor.process();

    node.client().admin().indices().prepareRefresh().execute().actionGet();

    assertEquals(elasticIndexingClient.sendCountIndexObjectsRequest("wiki"), 3);
    assertEquals(elasticIndexingClient.sendCountIndexObjectsRequest("wiki-alias"), 3);

    when(wikiConnector.getPreviousIndex()).thenReturn("wiki");
    when(wikiConnector.getCurrentIndex()).thenReturn("wiki_v2");

    indexingOperationProcessor.start();

    node.client().admin().indices().prepareRefresh().execute().actionGet();

    // Then
    int i = 0;
    while (elasticIndexingClient.sendIsIndexExistsRequest("wiki") && i < 5) {
      Thread.sleep(1000);
      i++;
    }

    assertFalse(elasticIndexingClient.sendIsIndexExistsRequest("wiki"));
    assertEquals(3, elasticIndexingClient.sendCountIndexObjectsRequest("wiki-alias"));
    assertEquals(3, elasticIndexingClient.sendCountIndexObjectsRequest("wiki_v2"));
  }

  private Document createDocument(String id) {
    Document document = new Document();
    document.addField("title", "RDBMS Guidelines");
    document.setPermissions(new HashSet<String>(Arrays.asList("Alice")));
    document.setId(id);
    return document;
  }

}

package org.exoplatform.commons.persistence.impl;

import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.apache.commons.dbcp.BasicDataSource;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.xml.ValuesParam;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class for Liquibase data initialization
 */
public class LiquibaseDataInitializerTest {

  @Test
  public void shouldSetDatasourceNameWhenDatasourceNameIsDefinedInConfiguration() {
    InitParams initParams = new InitParams();
    ValueParam datasourceNameValueParam = new ValueParam();
    datasourceNameValueParam.setName(LiquibaseDataInitializer.LIQUIBASE_DATASOURCE_PARAM_NAME);
    datasourceNameValueParam.setValue("datasource1");
    initParams.addParam(datasourceNameValueParam);

    LiquibaseDataInitializer liquibaseDataInitializer = new LiquibaseDataInitializer(initParams);

    Assert.assertEquals(liquibaseDataInitializer.getDatasourceName(), "datasource1");
  }

  @Test
  public void shouldUseDefaultDatasourceNameWhenDatasourceNameIsNotDefinedInConfiguration() {
    InitParams initParams = new InitParams();

    LiquibaseDataInitializer liquibaseDataInitializer = new LiquibaseDataInitializer(initParams);

    Assert.assertEquals(liquibaseDataInitializer.getDatasourceName(), LiquibaseDataInitializer.LIQUIBASE_DEFAULT_DATASOURCE_NAME);
  }

  @Test
  public void shouldSetContextsWhenContextsAreDefinedInConfiguration() {
    InitParams initParams = new InitParams();
    ValueParam contextsValueParam = new ValueParam();
    contextsValueParam.setName(LiquibaseDataInitializer.LIQUIBASE_CONTEXTS_PARAM_NAME);
    contextsValueParam.setValue("context1");
    initParams.addParam(contextsValueParam);

    LiquibaseDataInitializer liquibaseDataInitializer = new LiquibaseDataInitializer(initParams);

    Assert.assertEquals(liquibaseDataInitializer.getContexts(), "context1");
  }

  @Test
  public void shouldUseDefaultContextsWhenContextsAreNotDefinedInConfiguration() {
    InitParams initParams = new InitParams();

    LiquibaseDataInitializer liquibaseDataInitializer = new LiquibaseDataInitializer(initParams);

    Assert.assertEquals(liquibaseDataInitializer.getContexts(), LiquibaseDataInitializer.LIQUIBASE_DEFAULT_CONTEXTS);
  }

  @Test
  public void shouldNotCallLiquibaseWhenNoChangeLogs() throws LiquibaseException {
    // Init service with default values
    LiquibaseDataInitializer liquibaseDataInitializer = Mockito.spy(new LiquibaseDataInitializer(null));

    liquibaseDataInitializer.initData();

    Mockito.verify(liquibaseDataInitializer, Mockito.never()).applyChangeLog(Mockito.any(Database.class), Mockito.any(String.class));
  }

  /*
  @Test
  public void shouldCallLiquibaseWhenChangeLogsAreAdded() throws LiquibaseException, SQLException {
    // Init service with default values
    LiquibaseDataInitializer liquibaseDataInitializer = Mockito.spy(new LiquibaseDataInitializer(null));
    Mockito.doNothing().when(liquibaseDataInitializer).applyChangeLog(Mockito.any(Database.class), Mockito.any(String.class));
    Mockito.doReturn(new BasicDataSource()).when(liquibaseDataInitializer).getDatasource(Mockito.any(String.class));
    Mockito.doReturn(null).when(liquibaseDataInitializer).getDatabase(Mockito.any(DataSource.class));

    List<String> changeLogsPaths = new ArrayList<>(3);
    changeLogsPaths.add("changelog1");
    changeLogsPaths.add("changelog2");
    changeLogsPaths.add("changelog3");

    InitParams changeLogsPluginInitParams = new InitParams();
    ValuesParam changeLogsValuesParam = new ValuesParam();
    changeLogsValuesParam.setName(ChangeLogsPlugin.CHANGELOGS_PARAM_NAME);
    changeLogsValuesParam.setValues(changeLogsPaths);
    changeLogsPluginInitParams.addParam(changeLogsValuesParam);
    ChangeLogsPlugin changeLogsPlugin = new ChangeLogsPlugin(changeLogsPluginInitParams);
    liquibaseDataInitializer.addChangeLogsPlugin(changeLogsPlugin);

    liquibaseDataInitializer.initData();

    Mockito.verify(liquibaseDataInitializer, Mockito.times(3)).applyChangeLog(Mockito.any(Database.class), Mockito.any(String.class));
  }
  */
}

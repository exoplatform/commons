package org.exoplatform.jpa.settings.dao;

import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.jpa.CommonsDAOJPAImplTest;
import org.exoplatform.settings.jpa.entity.ContextEntity;
import org.exoplatform.settings.jpa.entity.ScopeEntity;
import org.exoplatform.settings.jpa.entity.SettingsEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class SettingsDAOTest extends CommonsDAOJPAImplTest {
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @After
  public void tearDown()  {
    settingsDAO.deleteAll();
    settingScopeDAO.deleteAll();
    settingContextDAO.deleteAll();
  }

  @Test
  public void testGetSetting() {
    String scopeGlobalName = Scope.GLOBAL.getName();
    String contextGlobalName = Context.GLOBAL.getName();

    //Given
    ScopeEntity scopeEntity = new ScopeEntity();
    scopeEntity.setType(scopeGlobalName);
    scopeEntity.setName(scopeGlobalName);
    scopeEntity = settingScopeDAO.create(scopeEntity);

    ContextEntity contextEntity = new ContextEntity();
    contextEntity.setType(contextGlobalName);
    contextEntity.setName(contextGlobalName);
    contextEntity = settingContextDAO.create(contextEntity);

    settingsDAO.create(new SettingsEntity().setName("My setting #1").setValue("My value #1").setScope(scopeEntity).setContext(contextEntity));
    settingsDAO.create(new SettingsEntity().setName("My setting #2").setValue("My value #2").setScope(scopeEntity).setContext(contextEntity));
    settingsDAO.create(new SettingsEntity().setName("My setting #3").setValue("My value #3").setScope(scopeEntity).setContext(contextEntity));

    //When
    SettingsEntity setting1 = settingsDAO.getSettingByContextAndScopeAndKey(contextGlobalName, contextGlobalName, scopeGlobalName, scopeGlobalName, "My setting #1");
    SettingsEntity setting2 = settingsDAO.getSettingByContextAndScopeAndKey(contextGlobalName, contextGlobalName, scopeGlobalName, scopeGlobalName, "My setting #2");
    SettingsEntity setting3 = settingsDAO.getSettingByContextAndScopeAndKey(contextGlobalName, contextGlobalName, scopeGlobalName, scopeGlobalName, "My setting #3");

    //Then
    assertNotNull(setting1);
    assertEquals(setting1.getName(), "My setting #1");
    assertNotNull(setting2);
    assertEquals(setting2.getName(), "My setting #2");
    assertNotNull(setting3);
    assertEquals(setting3.getName(), "My setting #3");
  }

  @Test
  public void testGetByUser() {
    String scopeGlobalName = Scope.GLOBAL.getName();

    //Given
    ScopeEntity scopeEntity = new ScopeEntity();
    scopeEntity.setType(scopeGlobalName);
    scopeEntity.setName(scopeGlobalName);
    scopeEntity = settingScopeDAO.create(scopeEntity);

    ContextEntity contextEntity = new ContextEntity();
    contextEntity.setType(Context.USER.getName());
    contextEntity.setName("foo");
    contextEntity = settingContextDAO.create(contextEntity);

    settingsDAO.create(new SettingsEntity().setName("My setting #1").setValue("My value #1").setScope(scopeEntity).setContext(contextEntity));
    settingsDAO.create(new SettingsEntity().setName("My setting #2").setValue("My value #2").setScope(scopeEntity).setContext(contextEntity));
    settingsDAO.create(new SettingsEntity().setName("My setting #3").setValue("My value #3").setScope(scopeEntity).setContext(contextEntity));

    //When
    List<SettingsEntity> settings = settingsDAO.getSettingsByContextTypeAndName(Context.USER.getName(), "foo");

    //Then
    assertEquals(settings.size(), 3);
  }

  @Test
  public void testGetByContext() {
    String scopeGlobalName = Scope.GLOBAL.getName();
    String contextGlobalName = Context.GLOBAL.getName();

    //Given
    ScopeEntity scopeEntity = new ScopeEntity();
    scopeEntity.setType(scopeGlobalName);
    scopeEntity.setName(scopeGlobalName);
    scopeEntity = settingScopeDAO.create(scopeEntity);

    ContextEntity contextEntity = new ContextEntity();
    contextEntity.setType(contextGlobalName);
    contextEntity.setName(contextGlobalName);
    contextEntity = settingContextDAO.create(contextEntity);

    settingsDAO.create(new SettingsEntity().setName("My setting #1").setValue("My value #1").setScope(scopeEntity).setContext(contextEntity));
    settingsDAO.create(new SettingsEntity().setName("My setting #2").setValue("My value #2").setScope(scopeEntity).setContext(contextEntity));
    settingsDAO.create(new SettingsEntity().setName("My setting #3").setValue("My value #3").setScope(scopeEntity).setContext(contextEntity));

    //When
    List<SettingsEntity> settings = settingsDAO.getSettingsByContextTypeAndName(contextGlobalName, contextGlobalName);

    //Then
    assertEquals(settings.size(), 3);
  }
}

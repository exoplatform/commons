package org.exoplatform.jpa.settings.dao;

import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.settings.jpa.entity.ContextEntity;
import org.exoplatform.settings.jpa.entity.ScopeEntity;
import org.exoplatform.settings.jpa.entity.SettingsEntity;
import org.exoplatform.jpa.impl.CommonsDAOJPAImplTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by exo on 3/8/17.
 */
public class SettingsDAOTest extends CommonsDAOJPAImplTest {
  @Before
  public void setUp() {
    super.setUp();
    settingsDAO.deleteAll();
  }

  @After
  public void tearDown()  {
    settingsDAO.deleteAll();
  }

  @Test
  public void testFindAllIds() {
    //Given
    settingsDAO.create(new SettingsEntity().setName("My setting #1").setValue("My value #1"));
    settingsDAO.create(new SettingsEntity().setName("My setting #2").setValue("My value #2"));

    //When
    List<Long> ids = settingsDAO.findAllIds(0, 10);

    //Then
    assertThat(ids.size(), is(2));
  }

  @Test
  public void testGetSetting() {
    //Given
    ScopeEntity scopeEntity = new ScopeEntity();
    scopeEntity.setType(Scope.GLOBAL.toString());
    scopeEntity.setName(Scope.GLOBAL.toString());
    ContextEntity contextEntity = new ContextEntity();
    contextEntity.setType(Context.GLOBAL.toString());
    contextEntity.setName(Context.GLOBAL.toString());
    settingsDAO.create(new SettingsEntity().setName("My setting #1").setValue("My value #1").setScope(scopeEntity).setContext(contextEntity));
    settingsDAO.create(new SettingsEntity().setName("My setting #2").setValue("My value #2").setScope(scopeEntity).setContext(contextEntity));
    settingsDAO.create(new SettingsEntity().setName("My setting #3").setValue("My value #3").setScope(scopeEntity).setContext(contextEntity));

    //When
    SettingsEntity setting1 = settingsDAO.getSetting(contextEntity, scopeEntity, "My setting #1");
    SettingsEntity setting2 = settingsDAO.getSetting(contextEntity, scopeEntity, "My setting #2");
    SettingsEntity setting3 = settingsDAO.getSetting(contextEntity, scopeEntity, "My setting #3");

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
    //Given
    ScopeEntity scopeEntity = new ScopeEntity();
    scopeEntity.setType(Scope.GLOBAL.toString());
    scopeEntity.setName(Scope.GLOBAL.toString());
    ContextEntity contextEntity = new ContextEntity();
    contextEntity.setType(Context.USER.toString());
    contextEntity.setName("foo");
    settingsDAO.create(new SettingsEntity().setName("My setting #1").setValue("My value #1").setScope(scopeEntity).setContext(contextEntity));
    settingsDAO.create(new SettingsEntity().setName("My setting #2").setValue("My value #2").setScope(scopeEntity).setContext(contextEntity));
    settingsDAO.create(new SettingsEntity().setName("My setting #3").setValue("My value #3").setScope(scopeEntity).setContext(contextEntity));

    //When
    List<SettingsEntity> settings = settingsDAO.getSettingsByUser("foo");

    //Then
    assertEquals(settings.size(), 3);
  }

  @Test
  public void testGetByScope() {
    //Given
    ScopeEntity scopeEntity = new ScopeEntity();
    scopeEntity.setType(Scope.APPLICATION.toString());
    scopeEntity.setName("foo");
    ContextEntity contextEntity = new ContextEntity();
    contextEntity.setType(Context.GLOBAL.toString());
    contextEntity.setName(Context.GLOBAL.toString());
    settingsDAO.create(new SettingsEntity().setName("My setting #1").setValue("My value #1").setScope(scopeEntity).setContext(contextEntity));
    settingsDAO.create(new SettingsEntity().setName("My setting #2").setValue("My value #2").setScope(scopeEntity).setContext(contextEntity));
    settingsDAO.create(new SettingsEntity().setName("My setting #3").setValue("My value #3").setScope(scopeEntity).setContext(contextEntity));

    //When
    List<SettingsEntity> settings = settingsDAO.getSettingsByScope(Scope.APPLICATION.id("foo"));

    //Then
    assertEquals(settings.size(), 3);
  }

  @Test
  public void testGetByContext() {
    //Given
    ScopeEntity scopeEntity = new ScopeEntity();
    scopeEntity.setType(Scope.GLOBAL.toString());
    scopeEntity.setName(Scope.GLOBAL.toString());
    ContextEntity contextEntity = new ContextEntity();
    contextEntity.setType(Context.GLOBAL.toString());
    contextEntity.setName(Context.GLOBAL.toString());
    settingsDAO.create(new SettingsEntity().setName("My setting #1").setValue("My value #1").setScope(scopeEntity).setContext(contextEntity));
    settingsDAO.create(new SettingsEntity().setName("My setting #2").setValue("My value #2").setScope(scopeEntity).setContext(contextEntity));
    settingsDAO.create(new SettingsEntity().setName("My setting #3").setValue("My value #3").setScope(scopeEntity).setContext(contextEntity));

    //When
    List<SettingsEntity> settings = settingsDAO.getSettingsByContext(Context.GLOBAL.id(Context.GLOBAL.toString()));

    //Then
    assertEquals(settings.size(), 3);
  }
}

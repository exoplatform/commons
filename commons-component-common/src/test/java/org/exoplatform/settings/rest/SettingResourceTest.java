package org.exoplatform.settings.rest;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.services.rest.impl.*;
import org.exoplatform.services.rest.tools.ByteArrayContainerResponseWriter;
import org.exoplatform.services.rest.tools.ResourceLauncher;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

public class SettingResourceTest extends BaseCommonsTestCase {

  protected ProviderBinder providers;

  protected ResourceBinder binder;

  protected RequestHandlerImpl requestHandler;

  protected ResourceLauncher launcher;

  private SettingService settingService;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    binder = getContainer().getComponentInstanceOfType(ResourceBinder.class);
    requestHandler = getContainer().getComponentInstanceOfType(RequestHandlerImpl.class);
    launcher = new ResourceLauncher(requestHandler);
    DependencySupplier dependencySupplier = getContainer().getComponentInstanceOfType(DependencySupplier.class);
    settingService = getContainer().getComponentInstanceOfType(SettingService.class);

    // reset default providers to be sure it is clean.
    ProviderBinder.setInstance(new ProviderBinder());
    providers = ProviderBinder.getInstance();

    binder.clear();

    ApplicationContextImpl.setCurrent(new ApplicationContextImpl(null, null, providers, dependencySupplier));

    binder.addResource(SettingResource.class, null);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();

    // Delete all settings
    Arrays.asList(Context.GLOBAL, Context.USER).stream().forEach(context -> {
      Map<Scope, Map<String, SettingValue<String>>> settings = settingService.getSettingsByContext(context);
      settings.forEach(
              (scope, scopeSettings) -> scopeSettings.keySet()
                      .forEach(settingKey -> settingService.remove(context, scope, settingKey)));
    });
  }

  protected void startSessionAs(String user, Collection<MembershipEntry> memberships) {
    Identity identity = new Identity(user, memberships);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
  }

  public void testReturnSettingWhenSuperUserGetsGlobalSetting() throws Exception {
    // Given
    startSessionAs("root", new HashSet<>());
    settingService.set(Context.GLOBAL, Scope.GLOBAL, "testKey", new SettingValue<>("testValue"));

    // When
    ByteArrayContainerResponseWriter responseWriter = new ByteArrayContainerResponseWriter();
    ContainerResponse resp = launcher.service("GET", "/v1/settings/GLOBAL/GLOBAL/testKey", "", null, null, responseWriter, null);

    // Then
    assertNotNull(resp);
    assertEquals(200, resp.getStatus());
    JSONObject jsonObject = new JSONObject(new String(responseWriter.getBody()));
    assertNotNull(jsonObject);
    assertEquals(4, jsonObject.length());
    JSONObject context = (JSONObject) jsonObject.get("context");
    assertEquals("GLOBAL", context.get("name"));
    assertEquals("GLOBAL", context.get("id"));
    JSONObject scope = (JSONObject) jsonObject.get("scope");
    assertEquals("GLOBAL", scope.get("name"));
    assertEquals(JSONObject.NULL, scope.get("id"));
    assertEquals("testKey", (String) jsonObject.get("key"));
    assertEquals("testValue", (String) jsonObject.get("value"));
  }

  public void testDoNotReturnSettingWhenUserGetsGlobalSetting() throws Exception {
    // Given
    startSessionAs("john", new HashSet<>());
    settingService.set(Context.GLOBAL, Scope.GLOBAL, "testKey", new SettingValue<>("testValue"));

    // When
    ByteArrayContainerResponseWriter responseWriter = new ByteArrayContainerResponseWriter();
    ContainerResponse resp = launcher.service("GET", "/v1/settings/GLOBAL/GLOBAL/testKey", "", null, null, responseWriter, null);

    // Then
    assertNotNull(resp);
    assertEquals(401, resp.getStatus());
  }

  public void testReturnSettingWhenUserGetsOwnUserSetting() throws Exception {
    // Given
    startSessionAs("john", Arrays.asList(new MembershipEntry("/platform/users", "member")));
    settingService.set(Context.USER.id("john"), Scope.GLOBAL, "testJohnKey", new SettingValue<>("testJohnValue"));

    // When
    ByteArrayContainerResponseWriter responseWriter = new ByteArrayContainerResponseWriter();
    ContainerResponse resp = launcher.service("GET", "/v1/settings/USER/GLOBAL/testJohnKey", "", null, null, responseWriter, null);

    // Then
    assertNotNull(resp);
    assertEquals(200, resp.getStatus());
    JSONObject jsonObject = new JSONObject(new String(responseWriter.getBody()));
    assertNotNull(jsonObject);
    assertEquals(4, jsonObject.length());
    JSONObject context = (JSONObject) jsonObject.get("context");
    assertEquals("USER", context.get("name"));
    assertEquals("john", context.get("id"));
    JSONObject scope = (JSONObject) jsonObject.get("scope");
    assertEquals("GLOBAL", scope.get("name"));
    assertEquals(JSONObject.NULL, scope.get("id"));
    assertEquals("testJohnKey", (String) jsonObject.get("key"));
    assertEquals("testJohnValue", (String) jsonObject.get("value"));
  }

  public void testNotReturnSettingWhenUserGetsOtherUserSetting() throws Exception {
    // Given
    startSessionAs("john", Arrays.asList(new MembershipEntry("/platform/users", "member")));
    settingService.set(Context.USER.id("mary"), Scope.GLOBAL, "testMaryKey", new SettingValue<>("testMaryValue"));

    // When
    ByteArrayContainerResponseWriter responseWriter = new ByteArrayContainerResponseWriter();
    ContainerResponse resp = launcher.service("GET", "/v1/settings/USER,mary/GLOBAL/testMaryKey", "", null, null, responseWriter, null);

    // Then
    assertNotNull(resp);
    assertEquals(401, resp.getStatus());
  }

  public void testReturnScopeSettingWhenSuperUserGetsScopeSetting() throws Exception {
    // Given
    startSessionAs("root", new HashSet<>());
    settingService.set(Context.GLOBAL, Scope.SPACE.id("MySpace"), "testMySpaceKey", new SettingValue<>("testMySpaceValue"));

    // When
    ByteArrayContainerResponseWriter responseWriter = new ByteArrayContainerResponseWriter();
    ContainerResponse resp = launcher.service("GET", "/v1/settings/GLOBAL/SPACE,MySpace/testMySpaceKey", "", null, null, responseWriter, null);

    // Then
    assertNotNull(resp);
    assertEquals(200, resp.getStatus());
    JSONObject jsonObject = new JSONObject(new String(responseWriter.getBody()));
    assertNotNull(jsonObject);
    assertEquals(4, jsonObject.length());
    JSONObject context = (JSONObject) jsonObject.get("context");
    assertEquals("GLOBAL", context.get("name"));
    assertEquals("GLOBAL", context.get("id"));
    JSONObject scope = (JSONObject) jsonObject.get("scope");
    assertEquals("SPACE", scope.get("name"));
    assertEquals("MySpace", scope.get("id"));
    assertEquals("testMySpaceKey", (String) jsonObject.get("key"));
    assertEquals("testMySpaceValue", (String) jsonObject.get("value"));
  }

  public void testNotReturnSettingWhenSuperUserGetsNonExistingSetting() throws Exception {
    // Given
    startSessionAs("root", new HashSet<>());
    settingService.set(Context.GLOBAL, Scope.GLOBAL, "testKey", new SettingValue<>("testValue"));

    // When
    ByteArrayContainerResponseWriter responseWriter = new ByteArrayContainerResponseWriter();
    ContainerResponse resp = launcher.service("GET", "/v1/settings/GLOBAL/GLOBAL/nonExistingSettingKey", "", null, null, responseWriter, null);

    // Then
    assertNotNull(resp);
    assertEquals(404, resp.getStatus());
  }

  public void testNotReturnSettingWhenSuperUserGetsExistingSettingWithWrongScope() throws Exception {
    // Given
    startSessionAs("root", new HashSet<>());
    settingService.set(Context.GLOBAL, Scope.APPLICATION.id("wiki"), "testWikiKey", new SettingValue<>("testWikiValue"));

    // When
    ByteArrayContainerResponseWriter responseWriter = new ByteArrayContainerResponseWriter();
    ContainerResponse resp = launcher.service("GET", "/v1/settings/GLOBAL/APPLICATION,calendar/testWikiKey", "", null, null, responseWriter, null);

    // Then
    assertNotNull(resp);
    assertEquals(404, resp.getStatus());
  }

  public void testNotReturnSettingWhenSuperUserGetsSettingWithWrongFormat() throws Exception {
    // Given
    startSessionAs("root", new HashSet<>());
    settingService.set(Context.GLOBAL, Scope.APPLICATION.id("wiki"), "testWikiKey", new SettingValue<>("testWikiValue"));

    // When
    ByteArrayContainerResponseWriter responseWriter = new ByteArrayContainerResponseWriter();
    ContainerResponse resp = launcher.service("GET", "/v1/settings/GLOBAL/APPLICATION:wiki/testWikiKey", "", null, null, responseWriter, null);

    // Then
    assertNotNull(resp);
    assertEquals(404, resp.getStatus());
  }

  public void testSettingCreatedWhenSuperUserSetsGlobalSetting() throws Exception {
    // Given
    startSessionAs("root", new HashSet<>());

    // When
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("content-type", MediaType.APPLICATION_JSON);
    ContainerResponse resp = launcher.service("PUT", "/v1/settings/GLOBAL/GLOBAL/testKey", "", headers, "{\"value\":\"testValue\"}".getBytes(), null);

    // Then
    assertNotNull(resp);
    assertEquals(200, resp.getStatus());
    SettingValue<?> settingValue = settingService.get(Context.GLOBAL, Scope.GLOBAL, "testKey");
    assertNotNull(settingValue);
    assertNotNull(settingValue.getValue());
    assertEquals(settingValue.getValue().toString(), "testValue");
  }

  public void testSettingNotCreatedWhenUserSetsGlobalSetting() throws Exception {
    // Given
    startSessionAs("john", Arrays.asList(new MembershipEntry("/platform/users", "member")));

    // When
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("content-type", MediaType.APPLICATION_JSON);
    ContainerResponse resp = launcher.service("PUT", "/v1/settings/GLOBAL/GLOBAL/testJohnKey", "", headers, "{\"value\":\"testJohnValue\"}".getBytes(), null);

    // Then
    assertNotNull(resp);
    assertEquals(401, resp.getStatus());
    SettingValue<?> settingValue = settingService.get(Context.GLOBAL, Scope.GLOBAL, "testJohnKey");
    assertNull(settingValue);
  }

  public void testSettingCreatedWhenUserSetsOwnUserSetting() throws Exception {
    // Given
    startSessionAs("john", Arrays.asList(new MembershipEntry("/platform/users", "member")));

    // When
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("content-type", MediaType.APPLICATION_JSON);
    ContainerResponse resp = launcher.service("PUT", "/v1/settings/USER/GLOBAL/testJohnKey", "", headers, "{\"value\":\"testJohnValue\"}".getBytes(), null);

    // Then
    assertNotNull(resp);
    assertEquals(200, resp.getStatus());
    SettingValue<?> settingValue = settingService.get(Context.USER, Scope.GLOBAL, "testJohnKey");
    assertNotNull(settingValue);
    assertNotNull(settingValue.getValue());
    assertEquals(settingValue.getValue().toString(), "testJohnValue");
  }

  public void testSettingNotCreatedWhenUserSetsOtherUserSetting() throws Exception {
    // Given
    startSessionAs("john", Arrays.asList(new MembershipEntry("/platform/users", "member")));

    // When
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("content-type", MediaType.APPLICATION_JSON);
    ContainerResponse resp = launcher.service("PUT", "/v1/settings/USER,mary/GLOBAL/testJohnKey", "", headers, "{\"value\":\"testJohnValue\"}".getBytes(), null);

    // Then
    assertNotNull(resp);
    assertEquals(401, resp.getStatus());
    SettingValue<?> settingValue = settingService.get(Context.USER.id("mary"), Scope.GLOBAL, "testJohnKey");
    assertNull(settingValue);
  }

  public void testSettingCreatedWhenSuperUserSetsScopeSetting() throws Exception {
    // Given
    startSessionAs("root", new HashSet<>());

    // When
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("content-type", MediaType.APPLICATION_JSON);
    ContainerResponse resp = launcher.service("PUT", "/v1/settings/GLOBAL/SPACE,MySpace/testMySpaceKey", "", headers, "{\"value\":\"testMySpaceValue\"}".getBytes(), null);

    // Then
    assertNotNull(resp);
    assertEquals(200, resp.getStatus());
    SettingValue<?> settingValue = settingService.get(Context.GLOBAL, Scope.SPACE.id("MySpace"), "testMySpaceKey");
    assertNotNull(settingValue);
    assertNotNull(settingValue.getValue());
    assertEquals(settingValue.getValue().toString(), "testMySpaceValue");
  }
}
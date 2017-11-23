package org.exoplatform.settings.rest;

import io.swagger.annotations.*;
import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1/settings")
@Api(tags = "/v1/settings", value = "/v1/settings", description = "Managing settings")
public class SettingResource implements ResourceContainer {

  private SettingService settingService;

  private UserACL userACL;

  public SettingResource(SettingService settingService, UserACL userACL) {
    this.settingService = settingService;
    this.userACL = userACL;
  }

  @Path("/{context}/{scope}/{settingKey}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Gets a specific setting value",
          httpMethod = "GET",
          response = Response.class,
          notes = "This returns the requested setting value in the following cases: <br/><ul><li>the authenticated user is the super user</li><li>the requested setting is a setting of the authenticated user (Context=USER)</li></ul>")
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse (code = 400, message = "Invalid query input"),
          @ApiResponse (code = 401, message = "User does not have permissions to get it"),
          @ApiResponse (code = 404, message = "Setting does not exist"),
          @ApiResponse (code = 500, message = "Internal server error")})
  public Response getSetting(@ApiParam(value = "Context - Format 'contextName,contextId' where 'contextId' is optional. Example: GLOBAL or USER,john", required = true) @PathParam("context") String contextParams,
                             @ApiParam(value = "Scope - Format 'scopeName,scopeId' where 'scopeId' is optional. Example: GLOBAL or APPLICATION,wiki or SPACE,marketing", required = true) @PathParam("scope") String scopeParams,
                             @ApiParam(value = "Setting key", required = true) @PathParam("settingKey") String settingKey) {
    if(StringUtils.isEmpty(contextParams) || StringUtils.isEmpty(scopeParams) || StringUtils.isEmpty(settingKey)) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Context, scope and setting key are mandatory").build();
    }

    Context context = extractContext(contextParams);

    // only the user can get its own settings
    if (!hasPermissions(context)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    Scope scope = extractScope(scopeParams);

    SettingValue<?> settingValue = settingService.get(context, scope, settingKey);

    if(settingValue == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    SettingEntity settingEntity = new SettingEntity(context, scope, settingKey, settingValue.getValue().toString());

    return Response.ok(settingEntity, MediaType.APPLICATION_JSON).build();
  }

  @Path("/{context}/{scope}/{settingKey}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Sets a specific setting value",
          httpMethod = "PUT",
          response = Response.class,
          notes = "This creates or updates the given setting value in the following cases: <br/><ul><li>the authenticated user is the super user</li><li>the given setting is a setting of the authenticated user (Context=USER)</li></ul>")
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse (code = 400, message = "Invalid query input"),
          @ApiResponse (code = 401, message = "User does not have permissions to update it"),
          @ApiResponse (code = 500, message = "Internal server error")})
  public Response setSetting(@ApiParam(value = "Context - Format 'contextName,contextId' where 'contextId' is optional. Example: GLOBAL or USER,john", required = true) @PathParam("context") String contextParams,
                             @ApiParam(value = "Scope - Format 'scopeName,scopeId' where 'scopeId' is optional. Example: GLOBAL or APPLICATION,wiki or SPACE,marketing", required = true) @PathParam("scope") String scopeParams,
                             @ApiParam(value = "Setting key", required = true) @PathParam("settingKey") String settingKey,
                             @ApiParam(value = "Setting value", required = true) SettingValueEntity settingValue) {
    if(StringUtils.isEmpty(contextParams) || StringUtils.isEmpty(scopeParams) || StringUtils.isEmpty(settingKey) || settingValue == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Context, scope and setting key and value are mandatory").build();
    }

    Context context = extractContext(contextParams);

    // only the user can update its own settings
    if (!hasPermissions(context)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    Scope scope = extractScope(scopeParams);

    settingService.set(context, scope, settingKey, SettingValue.create(settingValue.getValue()));

    return Response.ok().build();
  }

  /**
   * Check if user has permissions to read/write settings.
   * An user can read/write its own settings only.
   * The super user can do anything.
   * @param context The context of the setting
   * @return true if the operation is authorized for the current user
   */
  private boolean hasPermissions(Context context) {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    if(currentUser != null) {
      return (context.getName().toUpperCase().equals(Context.USER.getName())
              && (context.getId() == null || currentUser.equals(context.getId())))
                || (currentUser.equals(userACL.getSuperUser()));
    }
    return false;
  }

  /**
   * Extract Scope object from REST input param. The input param has the following pattern : {scopeName},{scopeId}
   * The scopeId is optional.
   * Examples :
   * * APPLICATION,wiki
   * * SPACE,marketing
   * * GLOBAL
   * @param scopeParams REST input param for scope
   * @return The Scope object
   */
  private Scope extractScope(String scopeParams) {
    String scopeName = scopeParams;
    String scopeId = null;
    int scopeIndex = scopeParams.indexOf(",");
    if(scopeIndex > 0) {
      scopeName = scopeParams.substring(0, scopeIndex);
      scopeId = scopeParams.substring(scopeIndex + 1);
    }
    return new Scope(scopeName, scopeId);
  }

  /**
   * Extract Context object from REST input param. The input param has the following pattern : {contextName},{contextId}
   * The contextId is optional.
   * Examples :
   * * USER,john
   * * GLOBAL
   * @param contextParams REST input param for context
   * @return The Context object
   */
  private Context extractContext(String contextParams) {
    String contextName = contextParams;
    String contextId = null;
    int contextIndex = contextParams.indexOf(",");
    if(contextIndex > 0) {
      contextName = contextParams.substring(0, contextIndex);
      contextId = contextParams.substring(contextIndex + 1);
    }
    if(contextName.equals("GLOBAL")) {
      return Context.GLOBAL;
    }
    return new Context(contextName, contextId);
  }
}

package org.exoplatform.commons.search.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.exoplatform.commons.api.search.SearchService;
import org.exoplatform.commons.api.search.data.SearchType;
import org.exoplatform.commons.search.driver.jcr.JcrSearchService;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.resource.ResourceContainer;

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
public class UnifiedSearch implements ResourceContainer {
  // Search types constants
  public static String FILE="file";
  public static String DOCUMENT="document";
  public static String DISCUSSION="forum"; //TODO: Canh to check this
  public static String TASK="task";
  public static String EVENT="event";
  public static String PAGE="page";
  public static String WIKI="wiki";
  public static String SPACE="space";
  public static String PEOPLE="people";
  public static String QUESTION="question";
  public static String ACTIVITY="activity";
  
  private static final CacheControl cacheControl;
  static {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
  }

  public UnifiedSearch(){
    // TODO: move all config to portlet war or PLF's configuration.properties
    try {
      InputStream registryJson = this.getClass().getResourceAsStream("/conf/registry.json");
      if(null!=registryJson) SearchService.setRegistry(new java.util.Scanner(registryJson).useDelimiter("\\A").next());
      
      Properties props = new Properties();
      props.load(this.getClass().getResourceAsStream("/conf/configuration.properties"));      
      JcrSearchService.IGNORED_TYPES = props.getProperty("jcr-ignored-types").split(",");
      JcrSearchService.IGNORED_FIELDS = props.getProperty("jcr-ignored-fields").split(",");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  @GET
  public Response search(@QueryParam("q") String query, @QueryParam("sites") String sites, @QueryParam("types") String types, @QueryParam("offset") int offset, @QueryParam("limit") int limit, @QueryParam("sort") String sort, @QueryParam("order") String order) {
    try {
      // sql mode (for testing)
      if(query.startsWith("SELECT")) return Response.ok(SearchService.search(query, Arrays.asList("all"), Arrays.asList("jcrNode"), 0, 0, "jcrScore()", "DESC"), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
      return Response.ok(SearchService.search(query, Arrays.asList(sites.split(",\\s*")), Arrays.asList(types.split(",\\s*")), offset, limit, sort, order), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }

  @GET
  @Path("/registry")
  public static Response getRegistry() {
    return Response.ok(SearchService.getRegistry(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }

  @GET
  @Path("/registry={json}")  
  @Consumes(MediaType.APPLICATION_JSON)
  public static Response setRegistry(@PathParam("json") String json) {
    try {
      SearchService.setRegistry(json);
      return Response.ok(SearchService.getRegistry(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    } catch (Exception e) {
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }

  @GET
  @Path("/register/{searchType}")  
  @Consumes(MediaType.APPLICATION_JSON)
  public static Response registerSearchType(@PathParam("searchType") String searchType_json){
    SearchService.register(new SearchType(searchType_json));
    return Response.ok(SearchService.getRegistry(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }

  @GET
  @Path("/unregister/{searchType}")  
  @Consumes(MediaType.APPLICATION_JSON)
  public static Response unregisterSearchType(@PathParam("searchType") String searchTypeName){
    SearchService.unregister(searchTypeName);
    return Response.ok(SearchService.getRegistry(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }

  
  @GET
  @Path("/sites")
  public static Response getAllPortalNames() throws Exception {
    UserPortalConfigService dataStorage = (UserPortalConfigService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(UserPortalConfigService.class);
    return Response.ok(dataStorage.getAllPortalNames(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }
  
}

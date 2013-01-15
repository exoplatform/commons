package org.exoplatform.commons.search.service;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.exoplatform.commons.api.search.SearchService;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
public class UnifiedSearchService implements ResourceContainer {
  // Search types constants
  public static String FILE="file";
  public static String DOCUMENT="document";
  public static String DISCUSSION="forum";
  public static String TASK="task";
  public static String EVENT="event";
  public static String PAGE="page";
  public static String WIKI="wiki";
  public static String SPACE="space";
  public static String PEOPLE="people";
  public static String QUESTION="question";
  public static String ACTIVITY="activity";
  
  private final static Log LOG = ExoLogger.getLogger(UnifiedSearchService.class);
  
  private static final CacheControl cacheControl;
  static {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
  }

  private static Map<String, SearchType> registry = new HashMap<String, SearchType>(); //a map that stores all the search type handled by SearchService 
  
  /**
   * Get all SearchTypes from registry
   * @return Map<String, SearchType>
   */
  public static Map<String, SearchType> getRegistry() {
    return registry;
  }

  /**
   * Set registry 
   * @param registry
   */
  public static void setRegistry(Map<String, SearchType> registry) {
    UnifiedSearchService.registry = registry;
  }

  /**
   * Parser json string to map and push map into registry
   * @param json must follow to json format
   */
  public static void setRegistry(String json) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      Map<String, SearchType> reg = mapper.readValue(json, new TypeReference<Map<String, SearchType>>(){});
      UnifiedSearchService.registry = reg;
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }
  /**
   * Register a search type
   * @param searchType
   */
  public static void register(SearchType searchType) {
    registry.put(searchType.getName(), searchType);
  }

  /**
   * Remove SearchType from registry
   * @param searchTypeName
   */
  public static void unregister(String searchTypeName) {
    registry.remove(searchTypeName);
  }

  /**
   * Check SearchType is exist or not
   * @param searchTypeName
   * @return
   */
  public static boolean isRegistered(String searchTypeName) {
    return registry.containsKey(searchTypeName);
  }
  
  
  public UnifiedSearchService(){
    // TODO: move all config to portlet war or PLF's configuration.properties
    try {
      InputStream registryJson = this.getClass().getResourceAsStream("/conf/registry.json");
      if(null!=registryJson) setRegistry(new java.util.Scanner(registryJson).useDelimiter("\\A").next());
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }
  
  @GET
  public Response search(@QueryParam("q") String query, @QueryParam("sites") String sites, @QueryParam("types") String types, @QueryParam("offset") int offset, @QueryParam("limit") int limit, @QueryParam("sort") String sort, @QueryParam("order") String order) {
    try {
      SearchService searchService = (SearchService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SearchService.class);
      // sql mode (for testing)
      if(query.startsWith("SELECT")) return Response.ok(searchService.search(query, Arrays.asList("all"), Arrays.asList("jcrNode"), 0, 0, "jcrScore()", "DESC"), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
      return Response.ok(searchService.search(query, Arrays.asList(sites.split(",\\s*")), Arrays.asList(types.split(",\\s*")), offset, limit, sort, order), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }

  @GET
  @Path("/registry")
  public static Response REST_getRegistry() {
    return Response.ok(getRegistry(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }

  @GET
  @Path("/registry={json}")  
  @Consumes(MediaType.APPLICATION_JSON)
  public static Response REST_setRegistry(@PathParam("json") String json) {
    try {
      setRegistry(json);
      return Response.ok(getRegistry(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    } catch (Exception e) {
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }

  @GET
  @Path("/register/{searchType}")  
  @Consumes(MediaType.APPLICATION_JSON)
  public static Response registerSearchType(@PathParam("searchType") String searchType_json){
    register(new SearchType(searchType_json));
    return Response.ok(getRegistry(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }

  @GET
  @Path("/unregister/{searchType}")  
  @Consumes(MediaType.APPLICATION_JSON)
  public static Response unregisterSearchType(@PathParam("searchType") String searchTypeName){
    unregister(searchTypeName);
    return Response.ok(getRegistry(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }
  
  @GET
  @Path("/sites")
  public static Response getAllPortalNames() {
    try {
      UserPortalConfigService dataStorage = (UserPortalConfigService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(UserPortalConfigService.class);
      return Response.ok(dataStorage.getAllPortalNames(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }

  
  // temporary for testing, user setting will be stored using "setting" feature
  private static Map<String, UserSetting> USER_SETTINGS = new HashMap<String, UserSetting>();

  @GET
  @Path("/setting")
  public static Response getUserSetting() {
    UserSetting defaultSetting = new UserSetting(10, Arrays.asList("all"), false, false, false);
    String userId = ConversationState.getCurrent().getIdentity().getUserId();
    if(null==userId || userId.isEmpty() || !USER_SETTINGS.containsKey(userId)) return Response.ok(defaultSetting, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    return Response.ok(USER_SETTINGS.get(userId), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }
  
  @POST
  @Path("/setting")
  public static Response setUserSetting(@FormParam("resultsPerPage") int resultsPerPage, @FormParam("searchTypes") String searchTypes, @FormParam("searchCurrentSiteOnly") boolean searchCurrentSiteOnly, @FormParam("hideSearchForm") boolean hideSearchForm, @FormParam("hideFacetsFilter") boolean hideFacetsFilter) {
    String userId = ConversationState.getCurrent().getIdentity().getUserId();
    if(null!=userId && !userId.isEmpty()) {
      USER_SETTINGS.put(userId, new UserSetting(resultsPerPage, Arrays.asList(searchTypes.split(",")), searchCurrentSiteOnly, hideSearchForm, hideFacetsFilter));
      return Response.ok("ok", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    }
    return Response.ok("nok: userId = "+userId, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  } 
  
}

package org.exoplatform.commons.search.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.exoplatform.commons.api.search.SearchService;
import org.exoplatform.commons.api.search.SearchServiceConnector;
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
  // temporary for testing, user setting will be stored using "setting" feature
  private static Map<String, SearchSetting> SEARCH_SETTINGS = new HashMap<String, SearchSetting>();
  private static Map<String, SearchSetting> QUICKSEARCH_SETTINGS = new HashMap<String, SearchSetting>();
  private static List<String> ENABLED_SEARCHTYPES = Arrays.asList("file", "document", "wiki", "page", "discussion", "people", "space", "event", "task", "question", "activity", "jcrNode");
  
  private static SearchSetting defaultSearchSetting = new SearchSetting(10, Arrays.asList("all"), false, false, false);
  private static SearchSetting defaultQuicksearchSetting = new SearchSetting(5, Arrays.asList("all"), true, true, true);
  
  private final static Log LOG = ExoLogger.getLogger(UnifiedSearchService.class);
  
  private static final CacheControl cacheControl;
  static {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
  }

  public static List<String> getEnabledSearchTypes(){
    return ENABLED_SEARCHTYPES;
  }
  
  @GET
  public Response search(@QueryParam("q") String sQuery, @QueryParam("sites") String sSites, @QueryParam("types") String sTypes, @QueryParam("offset") String sOffset, @QueryParam("limit") String sLimit, @QueryParam("sort") String sSort, @QueryParam("order") String sOrder) {
    if(null==sQuery || sQuery.isEmpty()) return Response.ok("", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();

    String userId = ConversationState.getCurrent().getIdentity().getUserId();
    SearchSetting searchSetting = null==userId || userId.isEmpty() || !SEARCH_SETTINGS.containsKey(userId) ? defaultSearchSetting : SEARCH_SETTINGS.get(userId);

    List<String> sites = null==sSites ? Arrays.asList("all") : Arrays.asList(sSites.split(",\\s*"));
    List<String> types = null==sTypes ? searchSetting.getSearchTypes() : Arrays.asList(sTypes.split(",\\s*"));
    int offset = null==sOffset || sOffset.isEmpty() ? 0 : Integer.parseInt(sOffset);
    int limit = null==sLimit || sLimit.isEmpty() ? 0 : Integer.parseInt(sLimit);
    String sort = null==sSort || sSort.isEmpty() ? "jcrScore()" : sSort;
    String order = null==sOrder || sOrder.isEmpty() ? "DESC" : sOrder;
    
    try {
      SearchService searchService = (SearchService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SearchService.class);
      // sql mode (for testing)
      if(sQuery.startsWith("SELECT")) return Response.ok(searchService.search(sQuery, Arrays.asList("all"), Arrays.asList("jcrNode"), 0, 0, "jcrScore()", "DESC"), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
      return Response.ok(searchService.search(sQuery, sites, types, offset, limit, sort, order), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }

  
  @GET
  @Path("/registry")
  public static Response REST_getRegistry() {
    SearchService searchService = (SearchService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SearchService.class);
    Map<String, SearchServiceConnector> registry = new HashMap<String, SearchServiceConnector>();
    for(SearchServiceConnector connector:searchService.getConnectors()) {
        registry.put(connector.getSearchType(), connector);
    }
    return Response.ok(Arrays.asList(registry, ENABLED_SEARCHTYPES), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
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

  
  @GET
  @Path("/setting")
  public static Response getSearchSetting() {
    String userId = ConversationState.getCurrent().getIdentity().getUserId();
    if(null==userId || userId.isEmpty() || !SEARCH_SETTINGS.containsKey(userId)) return Response.ok(defaultSearchSetting, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    return Response.ok(SEARCH_SETTINGS.get(userId), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }
  
  @POST
  @Path("/setting")
  public static Response setSearchSetting(@FormParam("resultsPerPage") int resultsPerPage, @FormParam("searchTypes") String searchTypes, @FormParam("searchCurrentSiteOnly") boolean searchCurrentSiteOnly, @FormParam("hideSearchForm") boolean hideSearchForm, @FormParam("hideFacetsFilter") boolean hideFacetsFilter) {
    String userId = ConversationState.getCurrent().getIdentity().getUserId();
    if(null!=userId && !userId.isEmpty()) {
      SEARCH_SETTINGS.put(userId, new SearchSetting(resultsPerPage, Arrays.asList(searchTypes.split(",")), searchCurrentSiteOnly, hideSearchForm, hideFacetsFilter));
      return Response.ok("ok", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    }
    return Response.ok("nok: userId = "+userId, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  } 

  
  @GET
  @Path("/setting/quicksearch")
  public static Response getQuicksearchSetting() {
    String userId = ConversationState.getCurrent().getIdentity().getUserId();
    if(null==userId || userId.isEmpty() || !QUICKSEARCH_SETTINGS.containsKey(userId)) return Response.ok(defaultQuicksearchSetting, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    return Response.ok(QUICKSEARCH_SETTINGS.get(userId), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }
  
  @POST
  @Path("/setting/quicksearch")
  public static Response setQuicksearchSetting(@FormParam("resultsPerPage") int resultsPerPage, @FormParam("searchTypes") String searchTypes, @FormParam("searchCurrentSiteOnly") boolean searchCurrentSiteOnly) {
    String userId = ConversationState.getCurrent().getIdentity().getUserId();
    if(null!=userId && !userId.isEmpty()) {
      QUICKSEARCH_SETTINGS.put(userId, new SearchSetting(resultsPerPage, Arrays.asList(searchTypes.split(",")), searchCurrentSiteOnly, true, true));
      return Response.ok("ok", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    }
    return Response.ok("nok: userId = "+userId, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  } 
  
    
  @POST
  @Path("/enabled-searchtypes")
  public static Response setEnabledSearchtypes(@FormParam("searchTypes") String searchTypes) {
    Collection<String> roles = ConversationState.getCurrent().getIdentity().getRoles();    
    if(!roles.isEmpty() && roles.contains("administrators")) {//only administrators can set this
      ENABLED_SEARCHTYPES = Arrays.asList(searchTypes.split(",\\s*"));
      return Response.ok("ok", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    }
    return Response.ok("nok: administrators only", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  } 

  
}

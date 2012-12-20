package org.exoplatform.commons.search.service;

import java.io.InputStream;

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

import org.exoplatform.commons.search.SearchService;
import org.exoplatform.commons.search.driver.jcr.JcrSearchService;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.resource.ResourceContainer;

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
public class UnifiedSearch implements ResourceContainer {
  private static final CacheControl cacheControl;
  static {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
  }

  public UnifiedSearch(){
    //searchService = (SearchService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SearchService.class);
    //SearchService.setRegistry("{\"people\":{\"name\":\"people\",\"displayName\":\"People\",\"properties\":{\"userId\":{\"exo:contact\":\"exo:id\",\"soc:profiledefinition\":\"void-username\"},\"userName\":{\"exo:contact\":\"exo:name\",\"soc:profiledefinition\":\"void-fullName\"}},\"handler\":\"org.exoplatform.commons.search.entrytype.People\"}}");
    //SearchService.registerEntryType(new SearchEntryType("people", "People", "{\"userId\": {\"exo:contact\": \"exo:id\", \"soc:profiledefinition\": \"void-username\"}, \"userName\": {\"exo:contact\": \"exo:name\", \"soc:profiledefinition\": \"void-fullName\"}}", "org.exoplatform.commons.search.entrytype.People"));
    //SearchService.registerEntryType(new SearchEntryType("content", "Content", null, Content.class));    
    
    // TODO: move all config to portlet war
    JcrSearchService.setSearchScope(getTextFromFile("/json/jcr-search-scope.json")); 
    SearchService.setRegistry(getTextFromFile("/json/registry.json"));
  }
  
  @GET
  public Response search(@QueryParam("q") String query, @QueryParam("categorized") boolean categorized) {
    try {
      if(categorized) {
        return Response.ok(SearchService.categorizedSearch(query), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
      } else {
        return Response.ok(SearchService.search(query), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }

  //for testing
  @GET
  @Path("/registry")
  public static Response getTypeMap() {
    return Response.ok(SearchService.getRegistry(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }

  @GET
  @Path("/registry={json}")  
  @Consumes(MediaType.APPLICATION_JSON)
  public static Response setTypeMap(@PathParam("json") String json) {
    try {
      SearchService.setRegistry(json);
      return Response.ok(SearchService.getRegistry(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    } catch (Exception e) {
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }

  @GET
  @Path("/register/{entryType}")  
  @Consumes(MediaType.APPLICATION_JSON)
  public static Response mapJcrTypes(@PathParam("entryType") String entryType_json){
    //SearchService.registerSearchType(new SearchType(entryType_json));
    return Response.ok(SearchService.getRegistry(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }

  private String getTextFromFile(String filePath){
    InputStream is = this.getClass().getResourceAsStream(filePath);
    return new java.util.Scanner(is).useDelimiter("\\A").next();
  }
}

package org.exoplatform.commons.search.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.exoplatform.commons.search.SearchService;
import org.exoplatform.commons.search.driver.jcr.JcrSearchService;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.resource.ResourceContainer;

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
public class UnifiedSearch implements ResourceContainer {
  private SearchService searchService;
  
  private static final CacheControl cacheControl;
  static {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
  }

  public UnifiedSearch(){
    searchService = (SearchService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SearchService.class);
  }
  
  @GET
  public Response search(@QueryParam("q") String query) {
    try {
      return Response.ok(searchService.categorizedSearch(query), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }
  
  //for testing with jcr only
  @GET
  @Path("/props")
  public Response jcrNodeProperties(@QueryParam("node") String nodePath) {
    try {
      return Response.ok(JcrSearchService.getJcrNodeProperties(nodePath), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }

}

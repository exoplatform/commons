package org.example;

import java.io.IOException;

import javax.inject.Inject;

import juzu.Path;
import juzu.Resource;
import juzu.Response;
import juzu.View;

import org.exoplatform.commons.juzu.ajax.Ajax;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Controller {

  @Inject
  @Path("index.gtmpl") 
  org.example.templates.index index;
  
  @View
  public void index() throws IOException {
    index.ok();
  }
  
  @Resource @Ajax
  public Response.Content foo() {
    return Response.content(200, "Hello Platform 4");
  }
}

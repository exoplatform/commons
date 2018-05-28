package org.exoplatform.commons.resource;

import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.*;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * This REST Service deserves resource bundles in JSON format
 */
@Path("/i18n/bundle")
public class ResourceBundleREST implements ResourceContainer {

  private static final Log      LOG = ExoLogger.getLogger(ResourceBundleREST.class);

  private ResourceBundleService resourceBundleService;

  private LocaleConfigService   localeConfigService;

  public ResourceBundleREST(ResourceBundleService resourceBundleService, LocaleConfigService localeConfigService) {
    this.resourceBundleService = resourceBundleService;
    this.localeConfigService = localeConfigService;
  }

  @GET
  @Path("{name}-{lang}.json")
  @Produces(MediaType.APPLICATION_JSON)
  @SuppressWarnings("unchecked")
  public Response getBundleContent(@PathParam("name") String resourceBundleName, @PathParam("lang") String lang) {
    if (StringUtils.isBlank(resourceBundleName) || StringUtils.isBlank(lang)) {
      return Response.status(400).build();
    }
    LocaleConfig localeConfig = localeConfigService.getLocaleConfig(lang);
    if (localeConfig == null) {
      LOG.warn("Locale '{}' is not supported", lang);
      return Response.status(400).build();
    }
    ResourceBundle resourceBundle = resourceBundleService.getResourceBundle(resourceBundleName, localeConfig.getLocale());
    ResourceBundle defaultResourceBundle = resourceBundleService.getResourceBundle(resourceBundleName,
                                                                                   localeConfigService.getDefaultLocaleConfig()
                                                                                                      .getLocale());
    if (resourceBundle == null) {
      if (defaultResourceBundle == null) {
        LOG.warn("resourceBundleName '{}' wasn't found", resourceBundleName);
        return Response.status(404).build();
      } else {
        resourceBundle = defaultResourceBundle;
      }
    }

    JSONObject resultJSON = new JSONObject();

    Enumeration<String> keys = resourceBundle.getKeys();
    while (keys.hasMoreElements()) {
      String key = (String) keys.nextElement();
      resultJSON.put(key, resourceBundle.getString(key));
    }
    keys = defaultResourceBundle.getKeys();
    while (keys.hasMoreElements()) {
      String key = (String) keys.nextElement();
      resultJSON.putIfAbsent(key, resourceBundle.getString(key));
    }
    return Response.ok(resultJSON.toJSONString(), MediaType.APPLICATION_JSON).build();
  }

}

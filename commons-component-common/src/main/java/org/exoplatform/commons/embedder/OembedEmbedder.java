/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.embedder;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @since 4.0.0-GA  
 */

public class OembedEmbedder extends AbstractEmbedder {
  private static final Log     LOG            = ExoLogger.getLogger(OembedEmbedder.class);

  private static final String  EMBED_TITLE    = "title";

  private static final String  EMBED_DESC     = "description";

  private static final String  EMBED_PROVIDER = "provider_name";

  private static final String  EMBED_URL      = "url";

  private static final String  EMBED_HTML     = "html";

  private static final String  EMBED_TYPE     = "type";

  private static final String  EMBED_THUMBNAIL_URL  = "thumbnail_url";

  private static final String  EMBED_THUMBNAIL_WIDTH  = "thumbnail_width";

  private static final String  EMBED_THUMBNAIL_HEIGHT  = "thumbnail_height";

  private static final String  EMBED_THUMBNAIL  = "thumbnail";

  private static final Pattern SHORTEN_DAILY_MOTION_PATTERN = Pattern.compile("http://dai\\.ly/.*");

  /**
   * constructor
   * 
   * @param initParams
   */
  public OembedEmbedder(InitParams initParams) {
    super(initParams);
  }

  public Pattern getOembedURLPattern() {
    return getPattern();
  }

  /**
   * processes input link and returns data wrapped into a model called ExoMedia
   * 
   * @return ExoMedia object that corresponds to the link.
   */
  @Override
  public ExoMedia getExoMedia() {
    URL urlObj = getOembedUrl(url);
    if (urlObj == null) {
      return null;
    }
    JSONObject oembedData = getJSONObject(urlObj);
    if (oembedData == null) {
      return null;
    }
    return jsonToExoMedia(oembedData);
  }

  private URL getOembedUrl(String url) {
    try {
      for (Pattern pattern : schemeEndpointMap.keySet()) {
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
          String endpoint = schemeEndpointMap.get(pattern);
          String scheme = "http";
          try {
            PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
            scheme = portalRequestContext.getRequest().getScheme();
          } catch (Exception e) {
            LOG.info("Cannot get scheme from Portal Request Context");
          }
          // COMMONS-513: Workaround for sharing non-secure dailymotion short link pattern.
          // https://www.dailymotion.com/services/oembed?format=json&url=http://dai.ly/xxxxx does not work. Instead,
          // https://www.dailymotion.com/services/oembed?format=json&url=https://dai.ly/xxxxx is working.
          Matcher shortenMatcher = SHORTEN_DAILY_MOTION_PATTERN.matcher(url);
          if (shortenMatcher.find()) {
            url = correctURIString(url, "https", true);
          }
          return new URL(correctURIString(String.format(endpoint, url),scheme, false));
        }
      }
    } catch (MalformedURLException e) {
      LOG.warn("Can't get oembed url for oembed request", e);
    }
    return null;
  }

  private ExoMedia jsonToExoMedia(JSONObject jsonObject) {
    ExoMedia mediaObject = new ExoMedia();

    try {
      mediaObject.setTitle(jsonObject.getString(EMBED_TITLE));
      mediaObject.setHtml(jsonObject.getString(EMBED_HTML)
              .replaceFirst("width=\\\"[0-9]*\\\"","width=\"330\"")
              .replaceFirst("height=\\\"[0-9]*\\\"","height=\"200\""));
      mediaObject.setType(jsonObject.getString(EMBED_TYPE));
      mediaObject.setProvider(jsonObject.getString(EMBED_PROVIDER));
      mediaObject.setDescription(jsonObject.has(EMBED_DESC) ? jsonObject.getString(EMBED_DESC) : "");
      mediaObject.setUrl(jsonObject.has(EMBED_URL) ? jsonObject.getString(EMBED_URL) : "");
      if (jsonObject.has(EMBED_THUMBNAIL_URL)) {
        mediaObject.setThumbnailUrl(jsonObject.getString(EMBED_THUMBNAIL_URL));
      } else if (jsonObject.has(EMBED_THUMBNAIL)) {
        mediaObject.setThumbnailUrl(jsonObject.getString(EMBED_THUMBNAIL));
      }
      if (jsonObject.has(EMBED_THUMBNAIL_HEIGHT)) {
        mediaObject.setThumbnailHeight(jsonObject.getString(EMBED_THUMBNAIL_HEIGHT));
      }
      if (jsonObject.has(EMBED_THUMBNAIL_WIDTH)) {
        mediaObject.setThumbnailWidth(jsonObject.getString(EMBED_THUMBNAIL_WIDTH));
      }

      if ("Flickr".equals(mediaObject.getProvider())) {
        mediaObject.setHtml(mediaObject.getHtml().replaceAll("<script.+</script>", ""));
      }
      return mediaObject;
    } catch (JSONException e) {
      LOG.warn("Can't convert JSONObject to ExoMedia object", e);
      return null;
    }
  }

  @Override
  protected Log getExoLogger() {
    return LOG;
  }
}

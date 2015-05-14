/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * @since 4.0.0-GA  
 */

public class YoutubeEmbedder extends AbstractEmbedder {
  private static final Log LOG = ExoLogger.getLogger(YoutubeEmbedder.class); 
  
  private static final Pattern YOUTUBE_ID_PATTERN = Pattern
      .compile("(youtu\\.be\\/|youtube\\.com\\/(watch\\?(.*&)?v=|(embed|v)\\/))([^\\?&\"'>]+)");
  private static final String YOUTUBE_SRC = "http://www.youtube.com/embed/%s?enablejsapi=1";

  /**
   * constructor
   * @param initParams
   */
  public YoutubeEmbedder(InitParams initParams) {
    super(initParams);
  }
  
  public Pattern getYouTubeURLPattern() {
    return getPattern();
  }

  /**
   * Processes input link and returns data wrapped into a model called ExoMedia.
   * 
   * @return ExoMedia object that corresponds to the link.
   */
  public ExoMedia getExoMedia() {
    String feedsURL = null;
    for(Pattern pattern : schemeEndpointMap.keySet()) {
      Matcher matcher = pattern.matcher(url);
      if(matcher.find()) {
        feedsURL = schemeEndpointMap.get(pattern);
      } else {
        return null;
      }
    }
    
    //
    try {
      Matcher matcher = YOUTUBE_ID_PATTERN.matcher(url);
      
      String youtubeId = null;
      while (matcher.find()) {
        youtubeId = matcher.group(5);
      }
       
      String youTubeFeedURL = String.format(feedsURL, youtubeId);
      URL reqURL = new URL(youTubeFeedURL);
      JSONObject jsonObject = getJSONObject(reqURL);
      JSONObject entryObject = jsonObject.getJSONObject("entry");
      //
      String html = buildIFramePlayer(youtubeId);
      
      if (html == null) {
        return null;
      }
      
      JSONObject mediaGroupObject = entryObject.getJSONObject("media$group");
      String title = entryObject.getJSONObject("title").getString("$t");
      String description = "";
      if (mediaGroupObject.has("media$description") == true) {
        description = mediaGroupObject.getJSONObject("media$description").getString("$t");
      }
      String thumbnailURL = "";
      String thumbnailWidth = "";
      String thumbnailHeight = "";
      if (mediaGroupObject.has("media$thumbnail") == true) {
        JSONObject thumbnail = mediaGroupObject.getJSONArray("media$thumbnail").getJSONObject(0);
        thumbnailURL = thumbnail.getString("url");
        thumbnailWidth = thumbnail.getString("width");
        thumbnailHeight = thumbnail.getString("height");
      }

      //
      ExoMedia mediaObject = new ExoMedia();
      mediaObject.setTitle(title);
      mediaObject.setHtml(html);
      mediaObject.setDescription(description);
      mediaObject.setThumbnailUrl(thumbnailURL);
      mediaObject.setThumbnailHeight(thumbnailHeight);
      mediaObject.setThumbnailWidth(thumbnailWidth);

      return mediaObject;    
    } catch (JSONException e) {
      LOG.debug("Any syntax error cause to JSON exception.", e);
      return null;
    } catch (IOException e) {
      LOG.debug("Problem with IO when open url.", e);
      return null;
    } catch (Exception e) {
      LOG.debug("Problem occurred when get data from youtube link.", e);
      return null;
    }
  }
  
  private String buildIFramePlayer(String youtubeId) throws JSONException {
    if (youtubeId == null) {
      LOG.info("Returned content url not match the pattern to get content source.");
      return null;
    }
    String youTubeSRC = String.format(YOUTUBE_SRC, youtubeId);
    StringBuilder contentURL = new StringBuilder();
    contentURL.append("<iframe id='player' type='text/html' width='330' height='200' frameborder='0'")
              .append(" src=\'").append(youTubeSRC).append("'/>");
    return contentURL.toString();
  }

  @Override
  protected Log getExoLogger() {
    return LOG;
  }
}

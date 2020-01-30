/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.embedder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import javax.ws.rs.core.UriBuilder;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public abstract class AbstractEmbedder implements Embedder {

  protected Map<Pattern, String> schemeEndpointMap = new HashMap<Pattern, String>();
  protected String url;
  private Pattern pattern;

  AbstractEmbedder(InitParams initParams) {
    Iterator<ValueParam> it = initParams.getValueParamIterator();
    ValueParam valueParam = null;
    while(it.hasNext()) {
      valueParam = it.next();
      String reg = getValue(valueParam.getName());
      String feedsURL = getValue(valueParam.getValue());
      pattern = Pattern.compile(reg);
      schemeEndpointMap.put(pattern, feedsURL);
    }
  }
  
  protected Pattern getPattern() {
    return this.pattern;
  }

  private String getValue(String input) {
    return input.replaceAll("&amp;", "&");
  }

  @Override
  public void setUrl(String url) {
    this.url = url;
  }
  
  /** Correct URI String
   *
   * @param uriString URI string to correct
   * @param scheme scheme to set
   * @param force if force is false, only set again scheme when scheme is missing. Otherwise, always set it
   * @return
   */

  public String correctURIString(String uriString, String scheme, boolean force) {
    URI uri = UriBuilder.fromUri(uriString).build();
    if (uri.getScheme() == null || force) {
      uri = UriBuilder.fromUri(uri.toString()).scheme(scheme).build();
    }
    return uri.toString();
  }

  protected JSONObject getJSONObject(URL url) {

    BufferedReader bufferedReader;
    try {
      bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
      StringBuffer stringBuffer = new StringBuffer();
      String eachLine = null;

      while ((eachLine = bufferedReader.readLine()) != null) {
        stringBuffer.append(eachLine);
      }
      bufferedReader.close();
      String jsonString = stringBuffer.toString();
      if (StringUtils.isBlank(jsonString)) {
        getExoLogger().warn("Empty response returned from '" + url + "'");
        return null;
      }
      return new JSONObject(jsonString);
    } catch (Exception e) {
      getExoLogger().warn("Can't get json from url: " + url.toString());
      return null;
    }
  }
  
  protected abstract Log getExoLogger();
}

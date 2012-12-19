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
package org.exoplatform.commons.search.entrytype;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.commons.search.SearchEntry;
import org.exoplatform.commons.search.SearchService;
import org.exoplatform.commons.search.SimpleEntry;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.groovyscript.GroovyTemplate;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
* Created by The eXo Platform SAS
* Author : Tung Vu Minh
*          tungvm@exoplatform.com
* Nov 21, 2012  
*/
public class Space extends SimpleEntry {
  private static final String TEMPLATE_FILE = "/template/search-entry/space.gtmpl";

  public Space(SearchEntry entry) {
    super(entry);
  }
  
  @Override
  public String getHtml() {
    try {
      SearchService searchService = (SearchService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SearchService.class);
      Map<String, String> details = searchService.getEntryDetail(this.getId());
      String spaceUrl = details.get("spaceUrl");

      SpaceService spaceSvc = (SpaceService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);
      org.exoplatform.social.core.space.model.Space space = spaceSvc.getSpaceByUrl(spaceUrl);

      Map<String, String> binding = new HashMap<String, String>();
      binding.put("spaceUrl", spaceUrl);
      binding.put("displayName", space.getDisplayName());
      binding.put("description", space.getDescription());
      binding.put("shortName", space.getDisplayName());
      String avatarUrl = space.getAvatarUrl();
      if(null==avatarUrl) avatarUrl = "/social-resources/skin/ShareImages/SpaceImages/SpaceLogoDefault_61x61.gif";
      binding.put("avatarUrl", avatarUrl);
      binding.put("members", String.valueOf(space.getMembers().length));
      binding.put("visibility", space.getVisibility());

      // super's content, for debugging
      binding.put("url", this.getUrl());
      binding.put("title", this.getTitle());
      binding.put("excerpt", this.getExcerpt());
      binding.put("details", details.toString());

      return new GroovyTemplate(new InputStreamReader(this.getClass().getResourceAsStream(TEMPLATE_FILE))).render(binding);
    } catch (Exception e) {
      e.printStackTrace();
      return super.getHtml();
    }
  }
}

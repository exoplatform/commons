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
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;


/**
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Nov 21, 2012  
 */
public class People extends SimpleEntry {
  private static final String TEMPLATE_FILE = "/template/search-entry/people.gtmpl";
  
  public People(SearchEntry entry) {
    this.setId(entry.getId());
    this.setContent(entry.getContent());
  }
  
  @Override
  public String getHtml() {    
    try {
      SearchService searchService = (SearchService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SearchService.class);
      Map<String, String> details = searchService.getEntryDetail(this.getId());
      String username = details.get("userId");

      IdentityManager identityManager = (IdentityManager)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
      Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, true);
      Profile profile = identity.getProfile();

      String fullName = profile.getFullName();
      String email = profile.getEmail();
      
      String position = profile.getPosition();
      if(null == position) position = "";
      String avatarUrl = profile.getAvatarUrl();      
      if(null == avatarUrl) avatarUrl = "/social-resources/skin/ShareImages/Avatar.gif";

      Map<String, String> binding = new HashMap<String, String>();
      // super's content, for debugging
      binding.put("url", this.getUrl());
      binding.put("title", this.getTitle());
      binding.put("excerpt", this.getExcerpt());
      binding.put("details", details.toString());
      
      binding.put("fullName", fullName);
      binding.put("position", position);
      binding.put("email", email);
      binding.put("avatarUrl", avatarUrl);
      binding.put("profileUrl", profile.getUrl());
      return new GroovyTemplate(new InputStreamReader(this.getClass().getResourceAsStream(TEMPLATE_FILE))).render(binding);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

}

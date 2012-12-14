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

/*
[exo:contact]
repository/collaboration/Users/john_spencer/ApplicationData/ContactApplication/contacts/john_spencer (score = 2891)
exo:lastModifier = __system
exo:fullName = John Spencer
exo:emailAddress = act.spencer@gmail.com
exo:lastName = Spencer
exo:categories = defaultjohn_spencer,/platform/users
exo:isOwner = true
exo:ownerId = john_spencer
exo:id = john_spencer
jcr:mixinTypes = exo:datetime,exo:sortable,exo:owneable,exo:modify
exo:name = john_spencer
exo:index = 1000
exo:lastModifiedDate = Mon Jan 03 23:33:05 ICT 2011
exo:firstName = John
exo:owner = __system
exo:dateModified = Mon Jan 03 23:33:05 ICT 2011
jcr:primaryType = exo:contact
exo:dateCreated = Mon Jan 03 23:33:05 ICT 2011
exo:lastUpdated = Mon Jan 03 23:33:04 ICT 2011

[soc:profiledefinition]
repository/social/production/soc:providers/soc:organization/soc:john/soc:profile (score = 3217)
soc:parentId = 604b46ee7f000001005212eb23005d21
void-email = john.smith@acme.exoplatform.com
exo:lastModifier = john
void-position = Big boss
void-fullName = John Smith
jcr:uuid = 604b46fd7f00000101876f02f008cd40
jcr:mixinTypes = exo:sortable,exo:owneable,exo:modify,exo:datetime
void-lastName = Smith
void-username = john
exo:name = soc:profile
void-firstName = John
exo:index = 1000
exo:lastModifiedDate = Tue Dec 11 10:41:28 ICT 2012
index-skills =
exo:owner = __system
exo:dateModified = Mon Dec 03 17:24:04 ICT 2012
jcr:primaryType = soc:profiledefinition
exo:dateCreated = Mon Dec 03 17:24:04 ICT 2012
*/
public class People extends SimpleEntry {
  //private static final String ENTRY_TYPE = "people";
  
  public People(SearchEntry entry) {
    this.setId(entry.getId());
    this.setContent(entry.getContent());
  }
  
  @Override
  public String getHtml() {    
    String html = ""; //TODO: move to a template file
    html = html + "<div class='entry'>";
    html = html + " <div style='padding-top: 5px; height: 60px;'>";
    html = html + "  <div style='float: left; padding-top: 3px;'>";
    html = html + "    <img width='40' height='40' title='${fullName}' src='${avatarUrl}' alt='${fullName}'>";
    html = html + "  </div>";
    html = html + "  <div style='padding-left: 10px; overflow: hidden;'>";
    html = html + "    <div><a style='font-weight: bold;' href='${profileUrl}'>${fullName}</a></div>";
    html = html + "    <div style='font-size: x-small; margin-top: -3px;'>${position}</div>";
    html = html + "    <div style='font-size: small; color: grey;'>${email}</div>";
    html = html + "  </div>";
    html = html + " </div>";
    html = html + "</div>";
    
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
      binding.put("fullName", fullName);
      binding.put("position", position);
      binding.put("email", email);
      binding.put("avatarUrl", avatarUrl);
      binding.put("profileUrl", profile.getUrl());
      
      return new GroovyTemplate(html).render(binding);
    } catch (Exception e) {
      e.printStackTrace();
      return html; //TODO: return as much info as possible
    }
  }

}

/**
 * Copyright (C) 2012 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

(function(jQuery){
 
function UISpaceSwitcher() {
};

UISpaceSwitcher.prototype.init = function(uicomponentId, baseRestUrl, socialBaseRestUrl, defaultValueForTextSearch, selectSpaceAction, invalidingCacheTime) {
  jQuery(window).ready(function(){
    var me = eXo.commons.UISpaceSwitcher;
    me.initAfterLoaded(uicomponentId, baseRestUrl, socialBaseRestUrl, defaultValueForTextSearch, selectSpaceAction, invalidingCacheTime);
  });
}

UISpaceSwitcher.prototype.initAfterLoaded = function(uicomponentId, baseRestUrl, socialBaseRestUrl, defaultValueForTextSearch, selectSpaceAction, invalidingCacheTime) {
  var me = eXo.commons.UISpaceSwitcher;
  
  if (!me.dataStorage) {
    me.dataStorage = new Object();
  }
  
  var storage = me.dataStorage[uicomponentId];
  if (!storage) {
    storage = new Object();
    me.dataStorage[uicomponentId] = storage;
  }
  
  storage.accessibleSpaceRestUrl = baseRestUrl + "/wiki/spaces/accessibleSpaces/";
  storage.portalSpaceRestUrl = baseRestUrl + "/wiki/spaces/portalSpaces/";
  storage.mySpaceRestUrl = baseRestUrl + "/wiki/spaces/mySpaces/";
  storage.recentlyVisitedSpaceRestUrl = socialBaseRestUrl + "/social/spaces/lastVisitedSpace/list.json";
  storage.lastSearchKeyword = "";
  storage.defaultValueForTextSearch = defaultValueForTextSearch;
  storage.selectSpaceAction = selectSpaceAction;
  storage.invalidingCacheTime = invalidingCacheTime;
  storage.lastTimeSendRequest = 0;
  
  var wikiSpaceSwitcher = document.getElementById(uicomponentId);
  var textField = jQuery(wikiSpaceSwitcher).find("input.spaceSearchText")[0];
  textField.value = defaultValueForTextSearch;
  
  textField.onkeydown = function() {
    me.onTextSearchChange(uicomponentId);
  };
  
  textField.onkeypress = function() {
    me.onTextSearchChange(uicomponentId);
  };
  
  textField.onkeyup = function() {
    me.onTextSearchChange(uicomponentId);
  };
  
  textField.onfocus = function() {
    if (textField.value == storage.defaultValueForTextSearch) {
      textField.value = "";
    }
    textField.className = "spaceSearchText focus"
  };
  
  textField.onclick = function() {
    var event = event || window.event;
    event.cancelBubble = true;
  };
  
  // When textField lost focus
  textField.onblur = function() {
    if (textField.value == "") {
      textField.value = storage.defaultValueForTextSearch;
      textField.className = "spaceSearchText lostFocus";
    }
  };

  // hide the popup when clicking outside
  jQuery(document).mouseup(function (e) {
    var me = eXo.commons.UISpaceSwitcher;
    var isNeedToClosePopup = true;
    for(var id in me.dataStorage) {
      var wikiSpaceSwitcher = document.getElementById(id);
      if (wikiSpaceSwitcher) {
        var container = jQuery(wikiSpaceSwitcher);
        if (jQuery.browser.msie) {
          for (var i = 0; i < container.length; i++) {
            if (container[i] == e.target) {
              isNeedToClosePopup = false;
              break;
            }
          }
        } else {
          if (container.is(e.target) || container.has(e.target).length != 0) {
            isNeedToClosePopup = false;
          }
        }
      }
    }
    
    if (isNeedToClosePopup) {
      me.closeAllPopups();
    }
    e.cancelBubble = true;
  });
};

UISpaceSwitcher.prototype.closeAllPopups = function() {
  var spaceChooserPopups = jQuery(document).find(".spaceChooserPopup");
  for (var i = 0; i < spaceChooserPopups.length; i++) {
    spaceChooserPopups[i].style.display = "none";
  }
};

UISpaceSwitcher.prototype.closePopup = function(closeTag) {
  var popup = closeTag.parentNode.parentNode;
  popup.style.display = "none";
};

UISpaceSwitcher.prototype.initSpaceInfo = function(uicomponentId, username, mySpaceLabel, portalSpaceId, portalSpaceLabel, noSpaceLabel, spaceLabel) {
  jQuery(window).ready(function(){
    var me = eXo.commons.UISpaceSwitcher;
    me.initSpaceInfoAfterReady(uicomponentId, username, mySpaceLabel, portalSpaceId, portalSpaceLabel, noSpaceLabel, spaceLabel);
  });
}

UISpaceSwitcher.prototype.initSpaceInfoAfterReady = function(uicomponentId, username, mySpaceLabel, portalSpaceId, portalSpaceLabel, noSpaceLabel, spaceLabel) {
  var me = eXo.commons.UISpaceSwitcher;
  var storage = me.dataStorage[uicomponentId];
  storage.mySpaceLabel = mySpaceLabel;
  storage.username = username;
  storage.portalSpaceId = portalSpaceId;
  storage.portalSpaceLabel = portalSpaceLabel;
  storage.noSpaceLabel = noSpaceLabel;
  storage.spaceLabel = spaceLabel;
}

UISpaceSwitcher.prototype.initConfig = function(uicomponentId, isShowPortalSpace, isShowUserSpace, isAutoResize) {
  jQuery(window).ready(function(){
    var me = eXo.commons.UISpaceSwitcher;
    me.initConfigAfterReady(uicomponentId, isShowPortalSpace, isShowUserSpace, isAutoResize);
  });
}

UISpaceSwitcher.prototype.initConfigAfterReady = function(uicomponentId, isShowPortalSpace, isShowUserSpace, isAutoResize) {
  var me = eXo.commons.UISpaceSwitcher;
  var storage = me.dataStorage[uicomponentId];
  storage.isShowPortalSpace = isShowPortalSpace;
  storage.isShowUserSpace = isShowUserSpace;
  
  // Auto resize
  if (isAutoResize) {
    var spaceSwitcher = document.getElementById(uicomponentId);
    if (spaceSwitcher) {
      var spacePopup = jQuery(spaceSwitcher).find("ul.spaceChooserPopup")[0];
      var dropDownButton = jQuery(spaceSwitcher).find("div.spaceChooser")[0];
  	  if (spacePopup && dropDownButton) {
  	    spacePopup.style.width = dropDownButton.offsetWidth-2 + "px";
  	  }
    }
  }
}

UISpaceSwitcher.prototype.initSpaceData = function(uicomponentId) {
  var me = eXo.commons.UISpaceSwitcher;
  var storage = me.dataStorage[uicomponentId];
  var wikiSpaceSwitcher = document.getElementById(uicomponentId);
  
  // Reset search textbox to default value
  var textField = jQuery(wikiSpaceSwitcher).find("input.spaceSearchText")[0];
  textField.value = storage.defaultValueForTextSearch;
  
  // Init data
  me.getRecentlyVisitedSpace(uicomponentId, 10);
  me.renderPortalSpace(uicomponentId, "portalSpace");
  me.renderUserSpace(uicomponentId, "userSpace");
}

UISpaceSwitcher.prototype.getRecentlyVisitedSpace = function(uicomponentId, numberOfResults) {
  var me = eXo.commons.UISpaceSwitcher;
  var storage = me.dataStorage[uicomponentId];
  jQuery.ajax({
    async : false,
    url : storage.recentlyVisitedSpaceRestUrl + "?appId=Wiki&offset=0&limit=" + numberOfResults,
    type : 'GET',
    data : '',
    success : function(data) {
      me.renderSpacesFromSocialRest(data, uicomponentId, "spaceList");
    }
  });
};

UISpaceSwitcher.prototype.searchSpaces = function(keyword, uicomponentId) {
  var me = eXo.commons.UISpaceSwitcher;
  var storage = me.dataStorage[uicomponentId];
  jQuery.ajax({
    async : false,
    url : storage.accessibleSpaceRestUrl + "?keyword=",
    type : 'GET',
    data : '',
    success : function(data) {
      storage.dataList = data;
      me.renderSpaces(data, uicomponentId, "spaceList", keyword);
    }
  });
};

UISpaceSwitcher.prototype.renderUserSpace = function(uicomponentId, containerClazz) {
  var me = eXo.commons.UISpaceSwitcher;
  var storage = me.dataStorage[uicomponentId];
  if (!storage.isShowUserSpace) {
    return;
  }
  
  var wikiSpaceSwitcher = document.getElementById(uicomponentId);
  var container = jQuery(wikiSpaceSwitcher).find('li.' + containerClazz)[0];
  var userSpaceId = "/user/" + storage.username;
  var userSpaceName = storage.mySpaceLabel;
  
  var spaceDiv = "<a class='spaceOption hover' id='UISpaceSwitcher_" + userSpaceId 
      + "' title='" + userSpaceName 
      + "' alt='" + userSpaceName 
      + "' onclick=\"eXo.commons.UISpaceSwitcher.onChooseSpace('" + userSpaceId + "', '" + uicomponentId + "')\">" 
       + "<i class='uiIconWikiMyWiki'></i>"
       + userSpaceName +
       "</a>";
  container.innerHTML = spaceDiv;
};

UISpaceSwitcher.prototype.renderPortalSpace = function(uicomponentId, containerClazz) {
  var me = eXo.commons.UISpaceSwitcher;
  var storage = me.dataStorage[uicomponentId];
  if (!storage.isShowPortalSpace) {
    return;
  }
  
  var wikiSpaceSwitcher = document.getElementById(uicomponentId);
  var container = jQuery(wikiSpaceSwitcher).find('li.' + containerClazz)[0];

  var spaceDiv = "<a class='spaceOption hover' id='UISpaceSwitcher_" + storage.portalSpaceId 
      + "' title='" + storage.portalSpaceLabel 
      + "' alt='" + storage.portalSpaceLabel 
      + "' onclick=\"eXo.commons.UISpaceSwitcher.onChooseSpace('" + storage.portalSpaceId + "', '" + uicomponentId +"')\">"
         + "<i class='uiIconWikiWiki'></i>"
         + storage.portalSpaceLabel + 
       "</a>";
  container.innerHTML = spaceDiv;
}

UISpaceSwitcher.prototype.renderSpacesFromSocialRest = function(dataList, uicomponentId, containerClazz) {
  var me = eXo.commons.UISpaceSwitcher;
  var storage = me.dataStorage[uicomponentId];
  var wikiSpaceSwitcher = document.getElementById(uicomponentId);
  var container = jQuery(wikiSpaceSwitcher).find('li.' + containerClazz)[0];
  var spaces = dataList.spaces;
  if (spaces) {
    var groupSpaces = '<ul>';
    //groupSpaces += "<div class='spaceOption spaceTitle' id='UISpaceSwitcher_spaceTitle'>" + storage.spaceLabel + "</div>";
    for (i = 0; i < spaces.length; i++) {
      var spaceId = spaces[i].groupId;
      var name = spaces[i].displayName;
      var avatarUrl = spaces[i].avatarUrl;
      groupSpaces += me.createSpaceNode(spaceId, name, uicomponentId, avatarUrl);
    }
    groupSpaces += "</ul>";
    container.innerHTML = groupSpaces;
    me.processContainerHeight(spaces.length, container);
    jQuery("#UISpaceSwitcher_spaceTitle").parent().show();
  } else {
    container.innerHTML = "<div class='spaceOption noSpace' id='UISpaceSwitcher_nospace'>" + storage.noSpaceLabel + "</div>";
    me.processContainerHeight(0, container);
    jQuery("#UISpaceSwitcher_spaceTitle").parent().hide();
    jQuery(".spaceChooserPopup .spaceSearchText").parent().hide();
  }  
}

UISpaceSwitcher.prototype.processContainerHeight = function(resultLength, container) {
  if (resultLength > 10) {
    container.style.height = (32 * 11) + "px";
  } else {
    container.style.height = (32 * (resultLength + 1)) + "px";
  }
}

UISpaceSwitcher.prototype.createSpaceNode = function(spaceId, name, uicomponentId, avatarUrl) {
  var spaceDiv = "<li style='display:block' class='spaceOption hover' id='UISpaceSwitcher_" + spaceId + "' >" 
	  + "<a onclick=\"eXo.commons.UISpaceSwitcher.onChooseSpace('" + spaceId + "', '" + uicomponentId + "')\">"
        + "<image src='" + avatarUrl + "' width='19' alt='" + name + "'/>"
         + name +
		 "</a>"
      + "</li>";
  return spaceDiv;
}

UISpaceSwitcher.prototype.renderSpaces = function(dataList, uicomponentId, containerClazz, keyword) {
  var me = eXo.commons.UISpaceSwitcher;
  var storage = me.dataStorage[uicomponentId];
  var wikiSpaceSwitcher = document.getElementById(uicomponentId);
  var container = jQuery(wikiSpaceSwitcher).find('li.' + containerClazz)[0];
  var spaces = dataList.jsonList;
  var groupSpaces = '<ul>';
//  groupSpaces += "<div class='spaceOption spaceTitle' id='UISpaceSwitcher_spaceTitle'>" + storage.spaceLabel + "</div>";
  var matchCount = 0;
  keyword = jQuery.trim(keyword);

  for (i = 0; i < spaces.length; i++) {
    var spaceId = spaces[i].spaceId;
    var name = spaces[i].name;
    var avatarUrl = spaces[i].avatarUrl;
    
    if (name.toLowerCase().indexOf(keyword.toLowerCase()) != -1) {
      var type = spaces[i].type;
      if (type == 'user') {
        name = storage.mySpaceLabel;
      }
      groupSpaces += me.createSpaceNode(spaceId, name, uicomponentId, avatarUrl);
      matchCount = matchCount + 1;
    }
  }
  groupSpaces += "</ul>";
  
  if (matchCount > 0) {
    container.innerHTML = groupSpaces;
    me.processContainerHeight(matchCount, container);
    jQuery("#UISpaceSwitcher_spaceTitle").parent().show();
  } else {
    container.innerHTML = "<div class='spaceOption noSpace' id='UISpaceSwitcher_nospace'>" + storage.noSpaceLabel + "</div>";
    jQuery("#UISpaceSwitcher_spaceTitle").parent().hide();
    me.processContainerHeight(0, container);
  }
};

UISpaceSwitcher.prototype.onChooseSpace = function(spaceId, uicomponentId) {
  var me = eXo.commons.UISpaceSwitcher;
  var storage = me.dataStorage[uicomponentId];
  var url = decodeURIComponent(storage.selectSpaceAction);
  url = url.substr(0, url.length - 2) + "&spaceId=" + spaceId + "')";
  eval(url);
}

UISpaceSwitcher.prototype.openComboBox = function(event, spaceChooserDiv) {
  var event = event || window.event;
  event.cancelBubble = true;
  var me = eXo.commons.UISpaceSwitcher;
  var wikiSpaceSwitcher = spaceChooserDiv.parentNode;
  var uicomponentId = wikiSpaceSwitcher.id;
  var spaceChooserPopup = jQuery(wikiSpaceSwitcher).find("ul.spaceChooserPopup")[0];
  if (spaceChooserPopup.style.display == "none") {
    spaceChooserPopup.style.display = "block";
  } else {
    spaceChooserPopup.style.display = "none";
  }
  me.initSpaceData(uicomponentId);
};

UISpaceSwitcher.prototype.onTextSearchChange = function(uicomponentId) {
  var me = eXo.commons.UISpaceSwitcher;
  var storage = me.dataStorage[uicomponentId];
  var wikiSpaceSwitcher = document.getElementById(uicomponentId);
  var textSearch = jQuery(wikiSpaceSwitcher).find("input.spaceSearchText")[0].value;
  
  if (textSearch != storage.lastSearchKeyword) {
    storage.lastSearchKeyword = textSearch;
    if (storage.dataList && (new Date().getTime() - storage.lastTimeSendRequest < storage.invalidingCacheTime)) {
      me.renderSpaces(storage.dataList, uicomponentId, "spaceList", textSearch);
    } else {
      storage.lastTimeSendRequest = new Date().getTime(); 
      me.searchSpaces(textSearch, uicomponentId);
    }
  }
};

if(!eXo.commons) eXo.commons={};
eXo.commons.UISpaceSwitcher = new UISpaceSwitcher();
return eXo.commons.UISpaceSwitcher;

})(jQuery);

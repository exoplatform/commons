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
  var textField = jQuery(wikiSpaceSwitcher).find("input.SpaceSearchText")[0];
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
    textField.className = "SpaceSearchText Focus"
  };
  
  textField.onclick = function() {
    var event = event || window.event;
    event.cancelBubble = true;
  };
  
  // When textField lost focus
  textField.onblur = function() {
    if (textField.value == "") {
      textField.value = storage.defaultValueForTextSearch;
      textField.className = "SpaceSearchText LostFocus";
    }
  };

  // hide the popup when clicking outside
  jQuery(document).mouseup(function (e) {
    var me = eXo.commons.UISpaceSwitcher;
    var isNeedToClosePopup = true;
    for(var id in me.dataStorage) {
      var wikiSpaceSwitcher = document.getElementById(id);
      var container = jQuery(wikiSpaceSwitcher);
      if (container.is(e.target) || container.has(e.target).length != 0) {
        isNeedToClosePopup = false;
      }
    }
    
    if (isNeedToClosePopup) {
      me.closeAllPopups();
    }
    e.cancelBubble = true;
  });
};

UISpaceSwitcher.prototype.closeAllPopups = function() {
  var spaceChooserPopups = document.getElementsByClassName('SpaceChooserPopup');
  for (var i = 0; i < spaceChooserPopups.length; i++) {
    spaceChooserPopups[i].style.display = "none";
  }
};

UISpaceSwitcher.prototype.closePopup = function(closeTag) {
  var popup = closeTag.parentNode.parentNode;
  popup.style.display = "none";
};

UISpaceSwitcher.prototype.initSpaceInfo = function(uicomponentId, username, mySpaceLabel, portalSpaceId, portalSpaceLabel, noSpaceLabel) {
  var me = eXo.commons.UISpaceSwitcher;
  var storage = me.dataStorage[uicomponentId];
  storage.mySpaceLabel = mySpaceLabel;
  storage.username = username;
  storage.portalSpaceId = portalSpaceId;
  storage.portalSpaceLabel = portalSpaceLabel;
  storage.noSpaceLabel = noSpaceLabel;
}

UISpaceSwitcher.prototype.initConfig = function(uicomponentId, isShowPortalSpace, isShowUserSpace) {
  var me = eXo.commons.UISpaceSwitcher;
  var storage = me.dataStorage[uicomponentId];
  storage.isShowPortalSpace = isShowPortalSpace;
  storage.isShowUserSpace = isShowUserSpace;
}

UISpaceSwitcher.prototype.initSpaceData = function(uicomponentId) {
  var me = eXo.commons.UISpaceSwitcher;
  var storage = me.dataStorage[uicomponentId];
  var wikiSpaceSwitcher = document.getElementById(uicomponentId);
  
  // Reset search textbox to default value
  var textField = jQuery(wikiSpaceSwitcher).find("input.SpaceSearchText")[0];
  textField.value = storage.defaultValueForTextSearch;
  
  // Init data
  me.getRecentlyVisitedSpace(uicomponentId, 10);
  me.renderPortalSpace(uicomponentId, "PortalSpace");
  me.renderUserSpace(uicomponentId, "UserSpace");
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
      me.renderSpacesFromSocialRest(data, uicomponentId, "SpaceList");
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
      me.renderSpaces(data, uicomponentId, "SpaceList", keyword);
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
  var container = jQuery(wikiSpaceSwitcher).find('div.' + containerClazz)[0];
  var userSpaceId = "/user/" + storage.username;
  var userSpaceName = storage.mySpaceLabel;
  
  var spaceDiv = "<div class='SpaceOption' id='UISpaceSwitcher_" + userSpaceId 
      + "' title='" + userSpaceName 
      + "' alt='" + userSpaceName 
      + "' onclick=\"eXo.commons.UISpaceSwitcher.onChooseSpace('" + userSpaceId + "', '" + uicomponentId + "')\">" 
        + "<image src='/CommonsResources/skin/DefaultSkin/commons/SpaceSwitcher/images/MyWiki.png' width='24px' alt='" + userSpaceName + "'/>"
        + "<span style='float:none; margin-left:6px;'  >" + userSpaceName + " </span>"
      + "</div>";
  container.innerHTML = spaceDiv;
};

UISpaceSwitcher.prototype.renderPortalSpace = function(uicomponentId, containerClazz) {
  var me = eXo.commons.UISpaceSwitcher;
  var storage = me.dataStorage[uicomponentId];
  if (!storage.isShowPortalSpace) {
    return;
  }
  
  var wikiSpaceSwitcher = document.getElementById(uicomponentId);
  var container = jQuery(wikiSpaceSwitcher).find('div.' + containerClazz)[0];

  var spaceDiv = "<div class='SpaceOption' id='UISpaceSwitcher_" + storage.portalSpaceId 
      + "' title='" + storage.portalSpaceLabel 
      + "' alt='" + storage.portalSpaceLabel 
      + "' onclick=\"eXo.commons.UISpaceSwitcher.onChooseSpace('" + storage.portalSpaceId + "', '" + uicomponentId +"')\">" 
        + "<image src='/CommonsResources/skin/DefaultSkin/commons/SpaceSwitcher/images/CompanyWiki.png' width='24px' alt='" + storage.portalSpaceLabel + "'/>"
        + "<span style='float:none; margin-left:6px;'  >" + storage.portalSpaceLabel + " </span>"
      + "</div>";
  container.innerHTML = spaceDiv;
}

UISpaceSwitcher.prototype.renderSpacesFromSocialRest = function(dataList, uicomponentId, containerClazz) {
  var me = eXo.commons.UISpaceSwitcher;
  var storage = me.dataStorage[uicomponentId];
  var wikiSpaceSwitcher = document.getElementById(uicomponentId);
  var container = jQuery(wikiSpaceSwitcher).find('div.' + containerClazz)[0];
  var spaces = dataList.spaces;
  if (spaces) {
    var groupSpaces = '';
    for (i = 0; i < spaces.length; i++) {
      var spaceId = spaces[i].groupId;
      var name = spaces[i].displayName;
      var avatarUrl = spaces[i].avatarUrl;
      groupSpaces += me.createSpaceNode(spaceId, name, uicomponentId, avatarUrl);
    }
    container.innerHTML = groupSpaces;
    me.processContainerHeight(spaces.length, container);
  } else {
    container.innerHTML = "<div class='SpaceOption' style='text-align:center' id='UISpaceSwitcher_nospace'>" + storage.noSpaceLabel + "</div>";
    me.processContainerHeight(1, container);
  }  
}

UISpaceSwitcher.prototype.processContainerHeight = function(resultLength, container) {
  if (resultLength > 10) {
    container.style.height = (31 * 10) + "px";
  } else {
    container.style.height = (31 * resultLength) + "px";
  }
}

UISpaceSwitcher.prototype.createSpaceNode = function(spaceId, name, uicomponentId, avatarUrl) {
  var spaceDiv = "<div class='SpaceOption' id='UISpaceSwitcher_" + spaceId 
      + "' title='" + name 
      + "' alt='" + name 
      + "' onclick=\"eXo.commons.UISpaceSwitcher.onChooseSpace('" + spaceId + "', '" + uicomponentId + "')\">" 
        + "<image src='" + avatarUrl + "' width='24px' alt='" + name + "'/>"
        + "<span style='float:none; margin-left:6px;'  >" + name + " </span>"
      + "</div>";
  return spaceDiv;
}

UISpaceSwitcher.prototype.renderSpaces = function(dataList, uicomponentId, containerClazz, keyword) {
  var me = eXo.commons.UISpaceSwitcher;
  var storage = me.dataStorage[uicomponentId];
  var wikiSpaceSwitcher = document.getElementById(uicomponentId);
  var container = jQuery(wikiSpaceSwitcher).find('div.' + containerClazz)[0];
  var spaces = dataList.jsonList;
  var groupSpaces = '';
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
  
  if (matchCount > 0) {
    container.innerHTML = groupSpaces;
    me.processContainerHeight(matchCount, container);
  } else {
    container.innerHTML = "<div class='SpaceOption' style='text-align:center' id='UISpaceSwitcher_nospace'>" + storage.noSpaceLabel + "</div>";
    me.processContainerHeight(1, container);
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
  var spaceChooserPopup = jQuery(wikiSpaceSwitcher).find("div.SpaceChooserPopup")[0];
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
  var textSearch = jQuery(wikiSpaceSwitcher).find("input.SpaceSearchText")[0].value;
  
  if (textSearch != storage.lastSearchKeyword) {
    storage.lastSearchKeyword = textSearch;
    if (storage.dataList && (new Date().getTime() - storage.lastTimeSendRequest < storage.invalidingCacheTime)) {
      me.renderSpaces(storage.dataList, uicomponentId, "SpaceList", textSearch);
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

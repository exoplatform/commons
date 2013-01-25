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

UISpaceSwitcher.prototype.init = function(uicomponentId, baseRestUrl, socialBaseRestUrl, defaultValueForTextSearch, selectSpaceAction) {
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
      textField.className = "SpaceSearchText LostFocus"
    }
  };
  
  // Hide popup when user click outside
  document.onclick = function() {
    var wikiSpaceSwitcher = document.getElementById(uicomponentId);
    var spaceChooserPopups = document.getElementsByClassName('SpaceChooserPopup');
    for (var i = 0; i < spaceChooserPopups.length; i++) {
      spaceChooserPopups[i].style.display = "none";
    }
  };
};

UISpaceSwitcher.prototype.initSpaceInfo = function(uicomponentId, username, mySpaceLabel, portalSpaceId, portalSpaceName) {
  var me = eXo.commons.UISpaceSwitcher;
  var storage = me.dataStorage[uicomponentId];
  storage.mySpaceLabel = mySpaceLabel;
  storage.username = username;
  storage.portalSpaceId = portalSpaceId;
  storage.portalSpaceName = portalSpaceName;
}

UISpaceSwitcher.prototype.initSpaceData = function(uicomponentId) {
  var me = eXo.commons.UISpaceSwitcher;
  var storage = me.dataStorage[uicomponentId];
  var wikiSpaceSwitcher = document.getElementById(uicomponentId);
  
  // Reset search textbox to empty
  jQuery(wikiSpaceSwitcher).find("input.SpaceSearchText")[0].value = storage.defaultValueForTextSearch;
  
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
    url : storage.accessibleSpaceRestUrl + "?keyword=" + keyword,
    type : 'GET',
    data : '',
    success : function(data) {
      me.renderSpaces(data, uicomponentId, "SpaceList");
    }
  });
};

UISpaceSwitcher.prototype.renderUserSpace = function(uicomponentId, containerClazz) {
  var me = eXo.commons.UISpaceSwitcher;
  var storage = me.dataStorage[uicomponentId];
  var wikiSpaceSwitcher = document.getElementById(uicomponentId);
  var container = jQuery(wikiSpaceSwitcher).find('div.' + containerClazz)[0];
  var userSpaceId = "/user/" + storage.username;
  var userSpaceName = storage.mySpaceLabel;
  
  var spaceDiv = "<div class='SpaceOption' id='UISpaceSwitcher_" + userSpaceId 
      + "' title='" + userSpaceName 
      + "' alt='" + userSpaceName 
      + "' onclick=\"eXo.commons.UISpaceSwitcher.onChooseSpace('" + userSpaceId + "', '" + uicomponentId + "')\">" 
      + userSpaceName + "</div>";
  container.innerHTML = spaceDiv;
};

UISpaceSwitcher.prototype.renderPortalSpace = function(uicomponentId, containerClazz) {
  var me = eXo.commons.UISpaceSwitcher;
  var storage = me.dataStorage[uicomponentId];
  var wikiSpaceSwitcher = document.getElementById(uicomponentId);
  var container = jQuery(wikiSpaceSwitcher).find('div.' + containerClazz)[0];
  
  var spaceDiv = "<div class='SpaceOption' id='UISpaceSwitcher_" + storage.portalSpaceId 
      + "' title='" + storage.portalSpaceName 
      + "' alt='" + storage.portalSpaceName 
      + "' onclick=\"eXo.commons.UISpaceSwitcher.onChooseSpace('" + storage.portalSpaceId + "', '" + uicomponentId +"')\">" 
      + storage.portalSpaceName + "</div>";
  container.innerHTML = spaceDiv;
}

UISpaceSwitcher.prototype.renderSpacesFromSocialRest = function(dataList, uicomponentId, containerClazz) {
  var me = eXo.commons.UISpaceSwitcher;
  var storage = me.dataStorage[uicomponentId];
  storage.dataList = dataList;
  
  var wikiSpaceSwitcher = document.getElementById(uicomponentId);
  var container = jQuery(wikiSpaceSwitcher).find('div.' + containerClazz)[0];
  var spaces = dataList.spaces;
  if (spaces) {
    var groupSpaces = '';
    for (i = 0; i < spaces.length; i++) {
      var spaceId = spaces[i].groupId;
      var name = spaces[i].displayName;
      groupSpaces += me.createSpaceNode(spaceId, name, uicomponentId);
    }
    container.innerHTML = groupSpaces;
  }
}

UISpaceSwitcher.prototype.createSpaceNode = function(spaceId, name, uicomponentId) {
  var spaceDiv = "<div class='SpaceOption' id='UISpaceSwitcher_" + spaceId 
      + "' title='" + name 
      + "' alt='" + name 
      + "' onclick=\"eXo.commons.UISpaceSwitcher.onChooseSpace('" + spaceId + "', '" + uicomponentId + "')\">" 
      + name + "</div>";
  return spaceDiv;
}

UISpaceSwitcher.prototype.renderSpaces = function(dataList, uicomponentId, containerClazz) {
  var me = eXo.commons.UISpaceSwitcher;
  var storage = me.dataStorage[uicomponentId];
  storage.dataList = dataList;
  
  var wikiSpaceSwitcher = document.getElementById(uicomponentId);
  var container = jQuery(wikiSpaceSwitcher).find('div.' + containerClazz)[0];
  var spaces = dataList.jsonList;
  var groupSpaces = '';

  for (i = 0; i < spaces.length; i++) {
    var spaceId = spaces[i].spaceId;
    var name = spaces[i].name;
    var type = spaces[i].type;
    if (type == 'user') {
      name = storage.mySpaceLabel;
    }
    groupSpaces += me.createSpaceNode(spaceId, name, uicomponentId);
  }
  container.innerHTML = groupSpaces;
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
    me.searchSpaces(textSearch, uicomponentId);
  }
};


if(!eXo.commons) eXo.commons={};
eXo.commons.UISpaceSwitcher = new UISpaceSwitcher();
return eXo.commons.UISpaceSwitcher;

})(jQuery);
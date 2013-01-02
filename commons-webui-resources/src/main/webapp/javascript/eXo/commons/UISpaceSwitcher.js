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
function UISpaceSwitcher() {
};

UISpaceSwitcher.prototype.init = function(uicomponentId, baseRestUrl, defaultValueForTextSearch, selectSpaceAction) {
  var me = eXo.commons.UISpaceSwitcher;
  me.accessibleSpaceRestUrl = baseRestUrl + "/wiki/spaces/accessibleSpaces/";
  me.portalSpaceRestUrl = baseRestUrl + "/wiki/spaces/portalSpaces/";
  me.mySpaceRestUrl = baseRestUrl + "/wiki/spaces/mySpaces/";
  me.lastSearchKeyword = "";
  me.defaultValueForTextSearch = defaultValueForTextSearch;
  me.selectSpaceAction = selectSpaceAction;
  me.uicomponentId = uicomponentId;
  
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
    if (textField.value == me.defaultValueForTextSearch) {
      textField.value = "";
    }
    textField.className="SpaceSearchText Focus"
  };
  
  textField.onclick = function() {
    var event = event || window.event;
    event.cancelBubble = true;
  };
  
  // When textField lost focus
  textField.onblur = function() {
    if (textField.value == "") {
      textField.value = me.defaultValueForTextSearch;
      textField.className="SpaceSearchText LostFocus"
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

UISpaceSwitcher.prototype.initSpaceInfo = function(username, mySpaceLabel, portalSpaceId, portalSpaceName) {
  var me = eXo.commons.UISpaceSwitcher;
  me.mySpaceLabel = mySpaceLabel;
  me.username = username;
  me.portalSpaceId = portalSpaceId;
  me.portalSpaceName = portalSpaceName;
}

UISpaceSwitcher.prototype.initSpaceData = function() {
  var me = eXo.commons.UISpaceSwitcher;
  me.requestSpaceList("", me.uicomponentId);
  me.renderPortalSpace(me.uicomponentId, "PortalSpace");
  me.renderUserSpace(me.uicomponentId, "UserSpace");
}

UISpaceSwitcher.prototype.requestSpaceList = function(keyword, uicomponentId) {
  var me = eXo.commons.UISpaceSwitcher;
  jQuery.ajax({
    async : false,
    url : me.accessibleSpaceRestUrl + "?keyword=" + keyword,
    type : 'GET',
    data : '',
    success : function(data) {
      me.renderSpaces(data, uicomponentId, "SpaceList");
    }
  });
};

UISpaceSwitcher.prototype.renderUserSpace = function(uicomponentId, containerClazz) {
  var me = eXo.commons.UISpaceSwitcher;
  var wikiSpaceSwitcher = document.getElementById(uicomponentId);
  var container = jQuery(wikiSpaceSwitcher).find('div.' + containerClazz)[0];
  var userSpaceId = "/user/" + me.username;
  var userSpaceName = me.mySpaceLabel;
  
  var spaceDiv = "<div class='SpaceOption' id='UISpaceSwitcher_" + userSpaceId 
      + "' title='" + userSpaceName 
      + "' alt='" + userSpaceName 
      + "' onclick=\"eXo.commons.UISpaceSwitcher.onChooseSpace('" + userSpaceId + "')\">" 
      + userSpaceName + "</div>";
  container.innerHTML = spaceDiv;
};

UISpaceSwitcher.prototype.renderPortalSpace = function(uicomponentId, containerClazz) {
  var me = eXo.commons.UISpaceSwitcher;
  var wikiSpaceSwitcher = document.getElementById(uicomponentId);
  var container = jQuery(wikiSpaceSwitcher).find('div.' + containerClazz)[0];
  
  var spaceDiv = "<div class='SpaceOption' id='UISpaceSwitcher_" + me.portalSpaceId 
      + "' title='" + me.portalSpaceName 
      + "' alt='" + me.portalSpaceName 
      + "' onclick=\"eXo.commons.UISpaceSwitcher.onChooseSpace('" + me.portalSpaceId + "')\">" 
      + me.portalSpaceName + "</div>";
  container.innerHTML = spaceDiv;
}

UISpaceSwitcher.prototype.renderSpaces = function(dataList, uicomponentId, containerClazz) {
  var me = eXo.commons.UISpaceSwitcher;
  me.dataList = dataList;
  
  var wikiSpaceSwitcher = document.getElementById(uicomponentId);
  var container = jQuery(wikiSpaceSwitcher).find('div.' + containerClazz)[0];
  var spaces = dataList.jsonList;
  var groupSpaces = '';

  for (i = 0; i < spaces.length; i++) {
    var spaceId = spaces[i].spaceId;
    var name = spaces[i].name;
    var type = spaces[i].type;
    
    if (type == 'user') {
      name = me.mySpaceLabel;
    }
    
    var spaceDiv = "<div class='SpaceOption' id='UISpaceSwitcher_" + spaceId 
      + "' title='" + name 
      + "' alt='" + name 
      + "' onclick=\"eXo.commons.UISpaceSwitcher.onChooseSpace('" + spaceId + "')\">" 
      + name + "</div>";
    groupSpaces += spaceDiv;
  }
  container.innerHTML = groupSpaces;
};

UISpaceSwitcher.prototype.onChooseSpace = function(spaceId) {
  var me = eXo.commons.UISpaceSwitcher;
  var url = decodeURIComponent(me.selectSpaceAction);
  url = url.substr(0, url.length - 2) + "&spaceId=" + spaceId + "')";
  eval(url);
}

UISpaceSwitcher.prototype.openComboBox = function(event, spaceChooserDiv) {
  var event = event || window.event;
  event.cancelBubble = true;
  var me = eXo.commons.UISpaceSwitcher;
  me.initSpaceData();
  var wikiSpaceSwitcher = spaceChooserDiv.parentNode;
  var spaceChooserPopup = jQuery(wikiSpaceSwitcher).find("div.SpaceChooserPopup")[0];

  if (spaceChooserPopup.style.display == "none") {
    spaceChooserPopup.style.display = "block";
  } else {
    spaceChooserPopup.style.display = "none";
  }
};

UISpaceSwitcher.prototype.onTextSearchChange = function(uicomponentId) {
  var me = eXo.commons.UISpaceSwitcher;
  var wikiSpaceSwitcher = document.getElementById(uicomponentId);
  var textSearch = jQuery(wikiSpaceSwitcher).find("input.SpaceSearchText")[0].value;
  
  if (textSearch != me.lastSearchKeyword) {
    me.lastSearchKeyword = textSearch;
    me.requestSpaceList(textSearch, uicomponentId);
  }
};

if(!eXo.commons) eXo.commons = {}
eXo.commons.UISpaceSwitcher = new UISpaceSwitcher();
_module.UISpaceSwitcher = eXo.commons.UISpaceSwitcher;
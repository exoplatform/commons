
if (!eXo) 
  eXo = {};

if (!eXo.commons) 
  eXo.commons = {};

function DocumentSelector(){
  this.getFoldersAndFiles = "";
  this.deleteFolderOrFile = "";
  this.createFolder = "";
  this.workspaceParam = "";
  this.nodePathParam = "";
  this.parentPathParam = "";
  this.isFolderOnlyParam = "";
  this.folderNameParam = "";
  this.xmlHttpRequest = false;
  this.workspaceName = "";
  this.selectedTreeNode = null;
  this.selectFile = null;
  this.selectFileLink = null;
  this.selectFolderLink = null;
  this.allowDeleteItem = true;
  this.dataId = null;
  this.uiComponent = null;
  this.rootPath = null;
};

DocumentSelector.prototype.init = function(uicomponentId, restContext, workspaceName, _rootPath){
  var me = eXo.commons.DocumentSelector;
  this.uiComponent = document.getElementById(uicomponentId);
  this.selectFileLink = eXo.core.DOMUtil.findFirstDescendantByClass(this.uiComponent, "a", "SelectFile");
  this.selectFolderLink = eXo.core.DOMUtil.findFirstDescendantByClass(this.uiComponent, "a", "SelectFolder");
  this.restContext = restContext;
  this.getFoldersAndFilesURL = restContext + this.getFoldersAndFiles;
  this.deleteFolderOrFileURL = restContext + this.deleteFolderOrFile;
  this.createFolderURL = restContext + this.createFolder;
  this.workspaceName = workspaceName;
  this.rootPath = _rootPath;
  var url = this.getFoldersAndFilesURL;
  url += "?" + this.workspaceParam + "=" + this.workspaceName;
  url += "&" + this.nodePathParam + "=" + this.rootPath;
  url += "&" + this.isFolderOnlyParam + "=true";
  var data = me.request(url);
  me.buildTree(null, data);
  me.actionBreadcrumbs(this.rootPath);
};

DocumentSelector.prototype.buildTree = function(treeNode, data){
  var me = eXo.commons.DocumentSelector;
  var xmlTreeNodes = data;
  var folderContainer = xmlTreeNodes.getElementsByTagName("Folders")[0];
  var nodeList = folderContainer.getElementsByTagName("Folder");
  var childrenHTML = eXo.commons.DocumentSelector.renderTreeNode(nodeList);
  if (!treeNode) {
    var uiLeftWorkspace = document.getElementById('LeftWorkspace');
    if (uiLeftWorkspace) 
      rootHtml = '<div class="Node" onclick="event.cancelBubble=true;eXo.commons.DocumentSelector.colExpNode(this);">'
      rootHtml += '<div class="CollapseIcon" style="display:none;" >'
      rootHtml += '<a id="'+this.rootPath+'" class="NodeIcon Folder Selected" path="'+this.rootPath+'" name="'+this.rootPath+'" href="javascript:void(0);" >'+this.rootPath+'</a></div>';
      rootHtml += childrenHTML
      uiLeftWorkspace.innerHTML = rootHtml;	
  }
  else {
    var childrenContainer = eXo.core.DOMUtil.findFirstDescendantByClass(treeNode, "div", "ChildrenContainer");
    if (childrenContainer) {
      treeNode.removeChild(childrenContainer);
    }
    treeNode.innerHTML += childrenHTML;
  }
};

DocumentSelector.prototype.colExpNode = function(treeNode){
  var me = eXo.commons.DocumentSelector;
  var domUtil = eXo.core.DOMUtil;
  if (!treeNode) 
    return;
  var iconElt = domUtil.getChildrenByTagName(treeNode, "div")[0];
  var nextElt = domUtil.findNextElementByTagName(iconElt, "div");
  
  if (!nextElt || nextElt.className != "ChildrenContainer") {
    me.renderChildren(treeNode);
    var childrenCotainer = domUtil.findFirstChildByClass(treeNode, "div", "ChildrenContainer");
    if (childrenCotainer) {
      iconElt = domUtil.findPreviousElementByTagName(childrenCotainer, "div");
      iconElt.className = 'CollapseIcon';
    }
  }
  else {
    if (nextElt.style.display != 'block') {
      nextElt.style.display = 'block';
      iconElt.className = 'CollapseIcon';
    }
    else {
      nextElt.style.display = 'none';
      iconElt.className = 'ExpandIcon';
    }
  }
};

DocumentSelector.prototype.expNode = function(treeNode){
  var me = eXo.commons.DocumentSelector;
  var domUtil = eXo.core.DOMUtil;
  if (!treeNode) 
    return;
  var iconElt = domUtil.getChildrenByTagName(treeNode, "div")[0];
  var nextElt = domUtil.findNextElementByTagName(iconElt, "div");
  
  if (!nextElt || nextElt.className != "ChildrenContainer") {
    me.renderChildren(treeNode);
    var childrenCotainer = domUtil.findFirstChildByClass(treeNode, "div", "ChildrenContainer");
    if (childrenCotainer) {
      iconElt = domUtil.getChildrenByTagName(treeNode, "div")[0]
      iconElt.className = 'CollapseIcon';
    }
  }
  else {
    nextElt.style.display = 'block';
    iconElt.className = 'CollapseIcon';
  }
};

DocumentSelector.prototype.renderChildren = function(treeNode){
  var domUtil = eXo.core.DOMUtil;
  var me = eXo.commons.DocumentSelector;
  var currentNode = domUtil.findFirstDescendantByClass(treeNode, "a", "NodeIcon");
  if (currentNode != null) {
    var nodePath = currentNode.getAttribute("path");
    var url = this.getFoldersAndFilesURL;
    url += "?" + this.workspaceParam + "=" + this.workspaceName;
    url += "&" + this.nodePathParam + "=" + nodePath;
    url += "&" + this.isFolderOnlyParam + "=true";
    var data = me.request(url);
    me.buildTree(treeNode, data);
  }
  else 
    return;
};

DocumentSelector.prototype.renderTreeNode = function(nodeList){
  var treeHTML = '';
  if (nodeList && nodeList.length > 0) {
    treeHTML += '<div class="ChildrenContainer" style="display:block;">';
    for (var i = 0; i < nodeList.length; i++) {
      var strName = nodeList[i].getAttribute("name");
      var path = nodeList[i].getAttribute("path");
      var hasChild = nodeList[i].getAttribute("hasChild");
      treeHTML += '<div class="Node" onclick="event.cancelBubble=true;eXo.commons.DocumentSelector.colExpNode(this);">';
      if (hasChild == "true") {
        treeHTML += '<div class="ExpandIcon">';
      }
      else {
        treeHTML += '<div class="NoneIcon">';
      }
      treeHTML += '<a title="' + decodeURIComponent(strName) + '" href="javascript:void(0);" class="NodeIcon Folder" onclick="event.cancelBubble=true;eXo.commons.DocumentSelector.viewDetails(this);eXo.commons.DocumentSelector.colExpNode(this.parentNode.parentNode);" name="' + decodeURIComponent(strName) + '" id="' + path + '" path="' + path + '">';
      treeHTML += strName;
      treeHTML += '</a>';
      treeHTML += '</div>';
      treeHTML += '</div>';
    }
    treeHTML += '</div>';
  }
  return treeHTML;
};

DocumentSelector.prototype.viewDetails = function(folderNode){
  var me = eXo.commons.DocumentSelector;
  var domUtil = eXo.core.DOMUtil;
  me.submitSelectedFolder(folderNode);
  me.renderBreadcrumbs(folderNode, null);
  if (me.selectedTreeNode) {
    var oldItemNode = domUtil.findFirstDescendantByClass(me.selectedTreeNode, "a", "NodeIcon");
    if (oldItemNode){
    domUtil.removeClass(oldItemNode, "Selected");
    }
  }
  me.selectedTreeNode = domUtil.findAncestorByClass(folderNode, "Node");
  domUtil.addClass(folderNode, "Selected");
  // To avoid case IE can't focus if item is invisible
  try {
    folderNode.focus();
  } catch (e){    
  }  
  var nodePath = folderNode.getAttribute("path");
  var url = this.getFoldersAndFilesURL;
  url += "?" + this.workspaceParam + "=" + this.workspaceName;
  url += "&" + this.nodePathParam + "=" + nodePath;
  url += "&" + this.isFolderOnlyParam + "=false";
  var data = me.request(url);
  var folderContainer = data.getElementsByTagName("Folders")[0];
  var folderList = folderContainer.getElementsByTagName("Folder");
  var fileContainer = data.getElementsByTagName("Files")[0];
  var fileList = fileContainer.getElementsByTagName("File");
  
  // Render data from response
  var rightWS = document.getElementById('RightWorkspace');
  var tblRWS = eXo.core.DOMUtil.findDescendantsByTagName(rightWS, "table")[0];
  var rowsRWS = eXo.core.DOMUtil.findDescendantsByTagName(tblRWS, "tr");
  if (rowsRWS && rowsRWS.length > 0) {
    for (var i = 0; i < rowsRWS.length; i++) {
      if (i > 0) 
        tblRWS.deleteRow(rowsRWS[i].rowIndex);
    }
  }
  if ((!fileList || fileList.length <= 0) && (!folderList || folderList.length <= 0)) {
    var tdNoContent = tblRWS.insertRow(1).insertCell(0);
    tdNoContent.innerHTML = "There is no folder or file";
    tdNoContent.className = "Item TRNoContent";
    if (me.allowDeleteItem == true) {
      tdNoContent.setAttribute("colspan", 4);
    }
    else {
      tdNoContent.setAttribute("colspan", 3);
    }
    tdNoContent.userLanguage = "UserLanguage.NoContent";
    return;
  }
  else {
    var listItem = '';
    var clazz = 'OddItem';
    var k = 0;
    for (var i = 0; i < folderList.length; i++) {
      k = i + 1;
      if (clazz == 'EventItem') {
        clazz = 'OddItem';
      }
      else 
        if (clazz == 'OddItem') {
          clazz = 'EventItem';
        }
      var clazzItem = me.getClazzIcon(folderList[i].getAttribute("nodeType"));
      var path = folderList[i].getAttribute("path");
      var nodeType = folderList[i].getAttribute("folderType");
      var name = folderList[i].getAttribute("name");
      var newRow = tblRWS.insertRow(i + 1);
      newRow.className = clazz + " Cell";
      var cellZero = newRow.insertCell(0);
      cellZero.onclick = function(){
        eXo.commons.DocumentSelector.browseFolder(this);
      }
      cellZero.innerHTML = '<a class="Item Folder ' + clazzItem + '" name="' + name + '" path="' + path + '" nodeType="' + nodeType + '" onclick="javascript:void(0);">' + decodeURIComponent(name) + '</a>';
      newRow.insertCell(1);
      newRow.insertCell(2);
      if (me.allowDeleteItem == true) {
        var cellThird = newRow.insertCell(3);
        cellThird.onclick = function(){
          eXo.commons.DocumentSelector.remove(this);
        }
        cellThird.innerHTML = '<a class="Item" name="' + name + '"  path="' + path + '" onclick="javascript:void(0);">Remove</a>';
      }
    }
    for (var j = 0; j < fileList.length; j++) {
      if (clazz == 'EventItem') {
        clazz = 'OddItem';
      }
      else 
        if (clazz == 'OddItem') {
          clazz = 'EventItem';
        }
      var path = fileList[j].getAttribute("path");
      var nodeType = fileList[j].getAttribute("nodeType");
      var nodeTypeIcon = nodeType.replace(":", "_") + "48x48Icon Folder";
      var node = fileList[j].getAttribute("name");
      var size = fileList[j].getAttribute("size");
      if (size < 1024) 
        size += '&nbsp;Byte(s)';
      else if (size >1024 && size <(1024*1024)) {
        size = (Math.round(size / 1024 *100))/100;
        size += '&nbsp;KB';
      } else {
          size = (Math.round(size/(1024 *1024) *100))/100;
        size += '&nbsp;MB';
      }
      var tblRWS = eXo.core.DOMUtil.findDescendantsByTagName(rightWS, "table")[0];
      var clazzItem = me.getClazzIcon(fileList[j].getAttribute("nodeType"));
      var newRow = tblRWS.insertRow(k + j + 1);
      newRow.className = clazz + " Cell";
      var cellZero = newRow.insertCell(0);
      cellZero.onclick = function(){
        eXo.commons.DocumentSelector.submitSelectedFile(this);
      }
      cellZero.innerHTML = '<a class="Item ' + clazzItem + '" path="' + path + '" nodeType="' + nodeType + '" style ="overflow:hidden;" title="' + decodeURIComponent(node) + '" onclick="javascript:void(0);">' + decodeURIComponent(node) + '</a>';
      newRow.insertCell(1).innerHTML = '<div class="Item">' + fileList[j].getAttribute("dateCreated") + '</div>';
      newRow.insertCell(2).innerHTML = '<div class="Item">' + size + '</div>';
      if (me.allowDeleteItem == true) {
        var cellThird = newRow.insertCell(3);
        cellThird.onclick = function(){
          eXo.commons.DocumentSelector.remove(this);
        }
        cellThird.innerHTML = '<a class="Item" name="' + node + '"  path="' + path + '" onclick="javascript:void(0);">Remove</a>';
      }
    }
  }
};

DocumentSelector.prototype.submitSelectedFile = function(tableCell){
  var me = eXo.commons.DocumentSelector;
  var domUtil = eXo.core.DOMUtil;
  var detailNode = domUtil.getChildrenByTagName(tableCell, "a")[0];
  var nodePath = detailNode.getAttribute("path");
  if (me.selectFileLink) {
    var link = me.selectFileLink.href;
    var endParamIndex = link.lastIndexOf("')");
    if (endParamIndex > 0)
      link = link.substring(0, endParamIndex) + "&"+ me.dataId +"=" + nodePath + "')";
    window.location = link;
  }
  if (me.selectFile) {
    if (eXo.core.DOMUtil.hasClass(me.selectFile, "Selected")) {
      eXo.core.DOMUtil.removeClass(me.selectFile, "Selected");
    }
  }
  me.selectFile = tableCell.parentNode;
  eXo.core.DOMUtil.addClass(me.selectFile, "Selected");
  var folderNode = domUtil.findFirstDescendantByClass(me.selectedTreeNode, "a", "NodeIcon");
  me.renderBreadcrumbs(folderNode, detailNode.getAttribute("title"));
};

DocumentSelector.prototype.submitSelectedFolder = function(folderNode){
  var me = eXo.commons.DocumentSelector;
  var path = folderNode.getAttribute("path");
  if (me.selectFolderLink) {
    var link = me.selectFolderLink.href;
    var endParamIndex = link.lastIndexOf("')");
    if (endParamIndex > 0)
      link = link.substring(0, endParamIndex) + "&"+ me.dataId +"=" + path + "')";
    window.location = link;
  }
};

DocumentSelector.prototype.browseFolder = function(tableCell){
  var me = eXo.commons.DocumentSelector;
  var domUtil = eXo.core.DOMUtil;
  var detailNode = domUtil.getChildrenByTagName(tableCell, "a")[0];
  var nodePath = detailNode.getAttribute("path");
  if (me.selectedTreeNode) {
    me.expNode(me.selectedTreeNode);
    var folderNode = document.getElementById(nodePath);
    var treeNode = domUtil.findAncestorByClass(folderNode, "Node");
    me.viewDetails(domUtil.findFirstDescendantByClass(treeNode, "a", "NodeIcon"));
  }
};

DocumentSelector.prototype.remove = function(tableCell){
  var me = eXo.commons.DocumentSelector;
  var domUtil = eXo.core.DOMUtil;
  var detailNode = domUtil.getChildrenByTagName(tableCell, "a")[0];
  var name = detailNode.getAttribute("name");
  var r = confirm("Are you sure you want remove " + name + " ?");
  if (r == false) 
    return;
  var nodePath = detailNode.getAttribute("path");
  var url = me.deleteFolderOrFileURL;
  url += "?" + me.workspaceParam + "=" + me.workspaceName;
  url += "&" + me.nodePathParam + "=" + nodePath;
  try {
    me.request(url);
    if (me.selectedTreeNode) {
      me.renderChildren(me.selectedTreeNode);
      var childrenContainer = eXo.core.DOMUtil.findFirstDescendantByClass(me.selectedTreeNode, "div", "ChildrenContainer");
      var iconElt = eXo.core.DOMUtil.getChildrenByTagName(me.selectedTreeNode, "div")[0];
      if (domUtil.hasClass(iconElt, "ExpandIcon")) 
        domUtil.replaceClass(iconElt, "ExpandIcon", "CollapseIcon");
      if (!childrenContainer) {
        if (domUtil.hasClass(iconElt, "ExpandIcon")) 
          domUtil.replaceClass(iconElt, "ExpandIcon", "NoneIcon");
        if (domUtil.hasClass(iconElt, "CollapseIcon")) 
          domUtil.replaceClass(iconElt, "CollapseIcon", "NoneIcon");
      }
      me.viewDetails(domUtil.findFirstDescendantByClass(me.selectedTreeNode, "a", "NodeIcon"));
    }
  } 
  catch (e) {
    window.console.error(e);
  }
};

DocumentSelector.prototype.newFolder = function(inputFolderName){
  var me = eXo.commons.DocumentSelector;
  var msg_select_folder = inputFolderName.getAttribute("msg_select_folder");
  var msg_enter_folder_name = inputFolderName.getAttribute("msg_enter_folder_name");
  var msg_empty_folder_name = inputFolderName.getAttribute("msg_empty_folder_name");
  
  var domUtil = eXo.core.DOMUtil;
  if (!me.selectedTreeNode) {
    alert(msg_select_folder);
    return;
  }
  
  var folderName = prompt(msg_enter_folder_name, "");
  if (folderName == null || folderName == "") {
    alert(msg_empty_folder_name);
    return;
  }
  
  var itemNode = domUtil.findFirstDescendantByClass(me.selectedTreeNode, "a", "NodeIcon");
  var nodePath = itemNode.getAttribute("path");
  var url = me.createFolderURL;
  url += "?" + me.workspaceParam + "=" + me.workspaceName;
  url += "&" + me.parentPathParam + "=" + nodePath;
  url += "&" + me.folderNameParam + "=" + folderName;
  try {
    me.request(url);
    if (me.selectedTreeNode) {
      me.renderChildren(me.selectedTreeNode);
      var iconElt = eXo.core.DOMUtil.getChildrenByTagName(me.selectedTreeNode, "div")[0];
      if (domUtil.hasClass(iconElt, "NoneIcon")) {
        domUtil.replaceClass(iconElt, "NoneIcon", "CollapseIcon");
      }
      if (domUtil.hasClass(iconElt, "ExpandIcon")) { 
        domUtil.replaceClass(iconElt, "ExpandIcon", "CollapseIcon");
      }
      var itemNode = domUtil.findFirstDescendantByClass(me.selectedTreeNode, "a", "NodeIcon");
      me.viewDetails(itemNode);
    }
  } catch (e) {
    window.console.error(e);
  }
};

DocumentSelector.prototype.actionBreadcrumbs = function(nodePath){
  var me = eXo.commons.DocumentSelector;
  var folderNode = document.getElementById(nodePath);
  var treeNode = eXo.core.DOMUtil.findAncestorByClass(folderNode, "Node");
  me.viewDetails(folderNode);
  me.expNode(treeNode);
}

DocumentSelector.prototype.renderBreadcrumbs = function(folderNode, fileName){
  var breadcrumbContainer = document.getElementById("BreadcumbsContainer");
  breadcrumbContainer.innerHTML = '';
  var beforeNode = null;
  var fileNode = null;
  
  if (fileName) {
    fileNode = document.createElement("div");
    fileNode.className = 'BreadcumbTab';
    fileNode.innerHTML = '<a class="Selected">' + decodeURIComponent(fileName) + '</a>';
    breadcrumbContainer.appendChild(fileNode);
  }
  beforeNode = fileNode;
  while (folderNode.className != "LeftWorkspace") {
    var curName = folderNode.getAttribute("name");
    var nodePath = folderNode.getAttribute("path");
    
    if (curName && curName != this.rootPath) {
      var tmpNode = document.createElement("div");
      tmpNode.className = 'BreadcumbTab';
      var strHTML = '';
      var strOnclick = "eXo.commons.DocumentSelector.actionBreadcrumbs('" + nodePath + "');";
      if (beforeNode == null) {
        strHTML += '<a class="Selected" href="javascript:void(0);" onclick="' + strOnclick + '">' + decodeURIComponent(curName) + '</a>';
        tmpNode.innerHTML = strHTML;
        breadcrumbContainer.appendChild(tmpNode);
      }
      else {
        strHTML += '<a class="Nomal" href="javascript:void(0);" onclick="' + strOnclick + '">' + decodeURIComponent(curName) + '</a>';
        strHTML += '<div class="RightArrowIcon"><span></span></div>';
        tmpNode.innerHTML = strHTML;
        breadcrumbContainer.insertBefore(tmpNode, beforeNode);
      }
      beforeNode = tmpNode;
    }
    folderNode = folderNode.parentNode;
    if (folderNode != null && folderNode.className == 'ChildrenContainer') {
      folderNode = folderNode.parentNode;
      if (folderNode.className != "LeftWorkspace") {
        folderNode = folderNode.getElementsByTagName('div')[0].getElementsByTagName('a')[0];
      }
    }
  }
};

DocumentSelector.prototype.getClazzIcon = function(nodeType){
  var strClassIcon = '';
  if (!nodeType) {
    strClassIcon = ".nt_file";
    return strClassIcon;
  }
  strClassIcon = nodeType.replace("/", "_").replace(":", "_").toLowerCase() + "16x16Icon";
  return strClassIcon;
};

DocumentSelector.prototype.request = function(url){
  var xmlHttpRequest = false;
  if (window.XMLHttpRequest) {
    xmlHttpRequest = new window.XMLHttpRequest();
    xmlHttpRequest.open("GET", url, false);
    xmlHttpRequest.send("");
    if (xmlHttpRequest.responseXML) 
      return xmlHttpRequest.responseXML;
  }
  else 
    if (ActiveXObject("Microsoft.XMLDOM")) { // for IE
      xmlHttpRequest = new ActiveXObject("Microsoft.XMLDOM");
      xmlHttpRequest.async = false;
      xmlHttpRequest.load(urlRequestXML);
      return xmlHttpRequest;
    }
  return null;
};

String.prototype.trunc = function(n, useWordBoundary){
  var toLong = this.length > n, s_ = toLong ? this.substr(0, n - 1) : this;
  s_ = useWordBoundary && toLong ? s_.substr(0, s_.lastIndexOf(' ')) : s_;
  return toLong ? s_ + '...' : s_;
};

eXo.commons.DocumentSelector = new DocumentSelector();
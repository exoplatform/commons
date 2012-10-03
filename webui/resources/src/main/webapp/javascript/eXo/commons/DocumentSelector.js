function DocumentSelector(){
  this.defaultDriveType = "personal";
  this.getDrives = "";
  this.getFoldersAndFiles = "";
  this.deleteFolderOrFile = "";
  this.createFolder = "";
  this.isFolderOnlyParam = "";
  this.folderNameParam = "";
  this.driveTypeParam = "";
  this.driveNameParam = "";
  this.workspaceNameParam = "";
  this.currentFolderParam = "";
  this.itemPathParam = "";
  this.xmlHttpRequest = false;
  this.selectFile = null;
  this.selectFileLink = null;
  this.selectFolderLink = null;
  this.allowDeleteItem = true;
  this.dataId = null;
  this.selectedItem = null;
};

function DocumentItem(){
  driveType = null;
  driveName = null;
  workspaceName = null;
  currentFolder = null;
  titlePath = null;
  jcrPath = null;
  titlePath = null;
};

DocumentSelector.prototype.init = function(uicomponentId, restContext){
  var me = _module.DocumentSelector;
  this.uiComponent = document.getElementById(uicomponentId);
  this.selectFileLink = jQuery("a.SelectFile:first",this.uiComponent);
  this.selectFolderLink = jQuery("a.SelectFolder:first",this.uiComponent);  
  this.getDrivesURL = restContext + this.getDrives;
  this.getFoldersAndFilesURL = restContext + this.getFoldersAndFiles;
  this.deleteFolderOrFileURL = restContext + this.deleteFolderOrFile;
  this.createFolderURL = restContext + this.createFolder;
  var documentItem = new DocumentItem();
  documentItem.driveType = this.defaultDriveType;
  me.renderDetails(documentItem);
};

DocumentSelector.prototype.changeDrive = function(selectBox){
  var documentItem = new DocumentItem();
  documentItem.driveType = selectBox.value;
  eXo.commons.DocumentSelector.renderDetails(documentItem);
};

DocumentSelector.prototype.renderDetails = function(documentItem) {
  var me = eXo.commons.DocumentSelector;
  // Clear old data
  var rightWS = jQuery("div.RightWorkspace:first", this.uiComponent);
  var actionBar = jQuery("div.ActionBar:first", this.uiComponent);
  var tblRWS = jQuery("table:first", this.uiComponent);  
  //remove all children:tr except for the first in tblRWS
  jQuery("tr:gt(0)",tblRWS).remove();
  
  me.selectedItem = documentItem;
  if (!me.selectedItem || !me.selectedItem.driveName) { 
    actionBar.hide();
  } else {
    actionBar.show();
  }
  if (!me.selectedItem.currentFolder){
    me.selectedItem.currentFolder ='';
    me.selectedItem.titlePath ='';
  }
  me.renderBreadcrumbs(documentItem, null);
  if (!documentItem.driveName) {
    me.renderDrives(tblRWS, documentItem);
  } else {    
    me.renderDetailsFolder(tblRWS, documentItem);
  }
};

DocumentSelector.prototype.renderDrives = function(tableContainer, documentItem) {
 var me = _module.DocumentSelector;
 var driveType = documentItem.driveType;
 var url = this.getDrivesURL;
 url += "?" + this.driveTypeParam + "=" + driveType;
 var data = me.request(url);
 var folderContainer = jQuery("Folders:first",data);
var folderList = jQuery("Folder",folderContainer);
 if (!folderList || folderList.length <= 0) {   
/*
   var tdNoContent = tableContainer.insertRow(1).insertCell(0);
   tdNoContent.innerHTML = "There is no drive";
   tdNoContent.className = "Item TRNoContent";
*/
   //if (me.allowDeleteItem == true) {
     //tdNoContent.setAttribute("colspan", 4);
   //} else {
     //tdNoContent.setAttribute("colspan", 3);
   //}   
   jQuery("tbody", tableContainer).append('<tr><td></td></tr>');
   var tdNoContent = jQuery("td:first",jQuery("tr:last", tableContainer));
   tdNoContent.html("There is no drive");
   tdNoContent.addClass("Item TRNoContent");
   return;
 }
 var clazz = 'EventItem';
 var k = 0;
 for ( var i = 0; i < folderList.length; i++) {
   k = i + 1;
   //if (clazz == 'EventItem') {
     //clazz = 'OddItem';
   //} else if (clazz == 'OddItem') {
     //clazz = 'EventItem';
   //}
   var name = folderList[i].getAttribute("name");
   var driveName = folderList[i].getAttribute("name");
   var nodeType = folderList[i].getAttribute("nodeType");
   var workspaceName = folderList[i].getAttribute("workspaceName");
   var canAddChild = folderList[i].getAttribute("canAddChild");
  /*
   var newRow = tableContainer.insertRow(i + 1);
   newRow.className = clazz + " Cell";
   var cellZero = newRow.insertCell(0);
   cellZero.onclick = function() {
     _module.DocumentSelector.browseFolder(this);
   }
   */
   var cellZero_innerHTML = '<a class="Item Drive_'+ nodeType+ '" name="' + name
        + '" driveType="' + driveType+ '" driveName="' + driveName
        + '" workspaceName="' + workspaceName + '" canAddChild="' + canAddChild
        + '" onclick="javascript:void(0);">' + name
        + '</a>';
      
   jQuery("tr",tableContainer).eq(i).after('<tr></tr>');
   var newRow = jQuery("tr",tableContainer).eq(i+1);
   newRow.addClass(clazz + " Cell");
   newRow.append('<td></td>');
   var cellZero = jQuery("td",newRow).eq(0);
   cellZero.html(cellZero_innerHTML);
   cellZero.click(function() {
     _module.DocumentSelector.browseFolder(this);
   });

   //newRow.insertCell(1);
   //newRow.insertCell(2);
   //if (me.allowDeleteItem == true) {
     //newRow.insertCell(3);
   //}     
 }
};

DocumentSelector.prototype.renderDetailsFolder = function(tableContainer,documentItem) {
  var me = _module.DocumentSelector;
  var driveType = documentItem.driveType;
  var driveName = documentItem.driveName;
  var workSpaceName = documentItem.workspaceName;
  var currentFolder = documentItem.currentFolder;
  if (!currentFolder)
    currentFolder = "";
  var url = this.getFoldersAndFilesURL;
  url += "?" + this.driveNameParam + "=" + driveName;
  url += "&" + this.workspaceNameParam + "=" + workSpaceName;
  url += "&" + this.currentFolderParam + "=" + currentFolder;
  url += "&" + this.isFolderOnlyParam + "=false";
  // To avoid the problem ajax caching on IE (issue: COMMONS-109)
  url += "&dummy=" + new Date().getTime();
  var data = me.request(url);
  var folderContainer = data.getElementsByTagName("Folders")[0];
  var folderList = folderContainer.getElementsByTagName("Folder");
  var fileContainer = data.getElementsByTagName("Files")[0];
  var fileList = fileContainer.getElementsByTagName("File");

  if ((!fileList || fileList.length <= 0)
      && (!folderList || folderList.length <= 0)) {
/*
    var tdNoContent = tableContainer.insertRow(1).insertCell(0);
    tdNoContent.innerHTML = "There is no folder or file";
    tdNoContent.className = "Item TRNoContent";
*/
    //if (me.allowDeleteItem == true) {
      //tdNoContent.setAttribute("colspan", 4);
    //} else {
      //tdNoContent.setAttribute("colspan", 3);
    //}    
   jQuery("tbody", tableContainer).append('<tr><td></td></tr>');
   var tdNoContent = jQuery("td:first",jQuery("tr:last", tableContainer));

   tdNoContent.html("There is no folder or file");
   tdNoContent.addClass("Item TRNoContent");
    return;
  } else {
    var listItem = '';
    var clazz = 'EventItem';
    var k = 0;
    for ( var i = 0; i < folderList.length; i++) {
      k = i + 1;
      //if (clazz == 'EventItem') {
        //clazz = 'OddItem';
      //} else if (clazz == 'OddItem') {
        //clazz = 'EventItem';
      //}
      var clazzItem = me.getClazzIcon(folderList[i].getAttribute("nodeType"));
      var jcrPath = folderList[i].getAttribute("path");
      var nodeType = folderList[i].getAttribute("folderType");
      var name = folderList[i].getAttribute("name");
      var title = folderList[i].getAttribute("title");
      var titlePath = folderList[i].getAttribute("titlePath");
      
      var childFolder = folderList[i].getAttribute("currentFolder");
      var canRemove = folderList[i].getAttribute("canRemove");
      var canAddChild = folderList[i].getAttribute("canAddChild");

      /*
      var newRow = tableContainer.insertRow(i + 1);
      newRow.className = clazz + " Cell";
      
      var cellZero = newRow.insertCell(0);
      cellZero.onclick = function() {
        _module.DocumentSelector.browseFolder(this);
      }
     */     
      var cellZero_innerHTML = '<a class="Item IconDefault ' + clazzItem + '" name="'
          + name + '" title="'+ title  + '" driveType="' + driveType + '" driveName="'
          + driveName + '"workSpaceName="' + workSpaceName + '" canAddChild="' + canAddChild
          + '" currentFolder="' + childFolder + '" titlePath="'+ titlePath  + '" jcrPath="' + jcrPath
          + '" onclick="javascript:void(0);">' + title
          + '</a>';
      
      jQuery("tr",tableContainer).eq(i).after('<tr></tr>');
      var newRow = jQuery("tr",tableContainer).eq(i+1);
      newRow.addClass(clazz + " Cell");
      newRow.append('<td></td>');
      var cellZero = jQuery("td",newRow).eq(0);
      cellZero.html(cellZero_innerHTML);
      cellZero.click(function() {
   	   _module.DocumentSelector.browseFolder(this);
      });

      //newRow.insertCell(1);
      //newRow.insertCell(2);
      //if (me.allowDeleteItem == true) {
        //var cellThird = newRow.insertCell(3);
        //cellThird.onclick = function() {
          //eXo.commons.DocumentSelector.remove(this);
        //}
        //cellThird.innerHTML = '<a class="Item" name="' + name + '"driveName="'
            //+ driveName + '"workSpaceName="' + workSpaceName + '"itemPath="'
            //+ childFolder + '" onclick="javascript:void(0);">Remove</a>';
      //}
    }
    for ( var j = 0; j < fileList.length; j++) {
      //if (clazz == 'EventItem') {
        //clazz = 'OddItem';
      //} else if (clazz == 'OddItem') {
        //clazz = 'EventItem';
      //}
      var jcrPath = fileList[j].getAttribute("path");
      var nodeType = fileList[j].getAttribute("nodeType");
      var nodeTypeIcon = nodeType.replace(":", "_") + "48x48Icon Folder";
      var node = fileList[j].getAttribute("name");
      var title = fileList[j].getAttribute("title");
      var size = fileList[j].getAttribute("size");
      if (size < 1024)
        size += '&nbsp;Byte(s)';
      else if (size > 1024 && size < (1024 * 1024)) {
        size = (Math.round(size / 1024 * 100)) / 100;
        size += '&nbsp;KB';
      } else {
        size = (Math.round(size / (1024 * 1024) * 100)) / 100;
        size += '&nbsp;MB';
      }
      var clazzItem = me.getClazzIcon(fileList[j].getAttribute("nodeType"));
/*
      var newRow = tableContainer.insertRow(k + j + 1);
      newRow.className = clazz + " Cell";
      var cellZero = newRow.insertCell(0);
      cellZero.tabIndex = "1";
      cellZero.onclick = function() {
        _module.DocumentSelector.submitSelectedFile(this);
      }
*/
      var cellZero_innerHTML = '<a class="Item ' + clazzItem + '" jcrPath="'
          + jcrPath + '" name="'+ node+'" title ="'+ title +'" onclick="javascript:void(0);">'
          + title + '</a>';     

      jQuery("tr",tableContainer).eq(k+j).after('<tr></tr>');
      var newRow = jQuery("tr",tableContainer).eq(k+j+1);
      newRow.addClass(clazz + " Cell");
      newRow.append('<td></td>');
      var cellZero = jQuery("td",newRow).eq(0);
      cellZero.html(cellZero_innerHTML);
      cellZero.click(function() {
   	   _module.DocumentSelector.submitSelectedFile(this) ;
      });
      //newRow.insertCell(1).innerHTML = '<div class="Item">' + fileList[j]
          //.getAttribute("dateCreated") + '</div>';
      //newRow.insertCell(2).innerHTML = '<div class="Item">' + size + '</div>';
      //if (me.allowDeleteItem == true) {
        //var cellThird = newRow.insertCell(3);
        //cellThird.onclick = function() {
          //eXo.commons.DocumentSelector.remove(this);
        //}
        //cellThird.innerHTML = '<a class="Item"  name="' + node
            //+ '" driveName="' + driveName + '"workSpaceName="'
            //+ workSpaceName + '"itemPath="' + currentFolder + '/' + node
            //+ '" onclick="javascript:void(0);">Remove</a>';
      //}
    }
  }
};

DocumentSelector.prototype.selectUploadedFile = function(fileName){
  var rightWS = jQuery("div.RightWorkspace:first", this.uiComponent);
  var tblRWS = jQuery("table:first", rightWS);  
  /*
  var items = jQuery("a.Item", tblRWS);
  for ( var j = 0; j < items.length; j++) {
    var item = items[j];
    var itemName = item.attr("name");
    if (itemName && itemName == fileName) {
      item.parentNode.onclick();
      item.parentNode.focus();
    }
  }  
  */
  var selectedItem = jQuery('a.Item[name="'+fileName+'"]', tblRWS).parent();
  selectedItem.click();
  selectedItem.focus();  
};


DocumentSelector.prototype.submitSelectedFile = function(tableCell){
  var me = _module.DocumentSelector;   
  var detailNode = jQuery("a:first-child",tableCell);
  var nodePath = detailNode.attr("jcrPath");
  var fileName = detailNode.attr("title");
    
  if (me.selectFileLink) {
    var link = me.selectFileLink.attr("href");
    var endParamIndex = link.lastIndexOf("')");
    if (endParamIndex > 0)
      link = link.substring(0, endParamIndex) + "&"+ me.dataId +"=" + encodeURI(nodePath) + "')";
    window.location = link;
  }
  if (me.selectFile) {
    if (me.selectFile.hasClass("Selected")) {
      me.selectFile.removeClass("Selected");
    }
  }
  me.selectFile = jQuery(tableCell).parent();
  me.selectFile.addClass("Selected");
  if (me.selectedItem) {
    me.renderBreadcrumbs(me.selectedItem, fileName);
  }
};

DocumentSelector.prototype.submitSelectedFolder = function(documentItem){
  var me = _module.DocumentSelector;
  var workspaceName = documentItem.workspaceName;
  var jcrPath = documentItem.jcrPath;
  if (me.selectFolderLink) {
    var link = me.selectFolderLink.attr("href");
    var endParamIndex = link.lastIndexOf("')");
    if (endParamIndex > 0)
      link = link.substring(0, endParamIndex) + "&" + me.dataId + "="
          + workspaceName + encodeURI(jcrPath) + "')";
    window.location = link;
  }
};

DocumentSelector.prototype.browseFolder = function(tableCell){
  var me = _module.DocumentSelector;
  var detailNode = jQuery("a:first-child",tableCell);
  var documentItem = new DocumentItem();
  documentItem.driveType = detailNode.attr("driveType");
  documentItem.driveName = detailNode.attr("driveName");
  documentItem.workspaceName = detailNode.attr("workspaceName");
  documentItem.currentFolder = detailNode.attr("currentFolder");
  documentItem.jcrPath = detailNode.attr("jcrPath");
  documentItem.canAddChild = detailNode.attr("canAddChild");
  documentItem.titlePath = detailNode.attr("titlePath");
  me.renderDetails(documentItem);
  me.submitSelectedFolder(documentItem);
};

DocumentSelector.prototype.remove = function(tableCell) {
  var me = _module.DocumentSelector;
  var detailNode = jQuery("a:first-child",tableCell);
  var name = detailNode.attr("name");
  var r = confirm("Are you sure you want remove " + name + " ?");
  if (r == false)
    return;
  var driveName = detailNode.attr("driveName");
  var workspaceName = detailNode.attr("workspaceName");
  var itemPath = detailNode.attr("itemPath");
  var url = me.deleteFolderOrFileURL;
  url += "?" + me.driveNameParam + "=" + driveName;
  url += "&" + me.workspaceNameParam + "=" + workspaceName;
  url += "&" + me.itemPathParam + "=" + itemPath;
  me.request(url);
  if (me.selectedItem) {
    me.renderDetails(me.selectedItem);
  }

};

DocumentSelector.prototype.newFolder = function(inputFolderName){
  var me = _module.DocumentSelector;   
  var msg_new_folder_not_allow = inputFolderName.getAttribute("msg_new_folder_not_allow");
  var msg_select_folder = inputFolderName.getAttribute("msg_select_drive");
  var msg_enter_folder_name = inputFolderName.getAttribute("msg_enter_folder_name");
  var msg_empty_folder_name = inputFolderName.getAttribute("msg_empty_folder_name");
  
  if (!me.selectedItem || !me.selectedItem.driveName) {
    alert(msg_select_folder);
    return;
  }

  var folderName = prompt(msg_enter_folder_name, "");
  if (folderName == null || folderName == "") {
    alert(msg_empty_folder_name);
    return;
  }

  var canAddChild = me.selectedItem.canAddChild;
  if (canAddChild == "false") {
    alert(msg_new_folder_not_allow);
    return;
  }
  var driveName = me.selectedItem.driveName;
  var workspaceName = me.selectedItem.workspaceName;
  var url = me.createFolderURL;
  url += "?" + me.driveNameParam + "=" + driveName;
  url += "&" + me.workspaceNameParam + "=" + workspaceName;
  url += "&" + me.currentFolderParam + "=" + me.selectedItem.currentFolder;
  url += "&" + me.folderNameParam + "=" + folderName;  
  me.request(url);
  me.renderDetails(me.selectedItem);
};

DocumentSelector.prototype.actionBreadcrumbs = function(element) {
  var documentItem = new DocumentItem();  
  documentItem.driveType = element.getAttribute("driveType");
  documentItem.driveName = element.getAttribute("driveName");
  documentItem.workspaceName = element.getAttribute("workspaceName");
  documentItem.currentFolder = element.getAttribute("currentFolder");
  documentItem.titlePath = element.getAttribute("titlePath");
  _module.DocumentSelector.renderDetails(documentItem);
}

DocumentSelector.prototype.renderBreadcrumbs = function(documentItem, fileName) {
  var breadcrumbContainer = jQuery("div.BreadcumbsContainer:first",this.uiComponent);
  breadcrumbContainer.html('');
  var breadCrumbObject = new BreadCrumbs();
  breadCrumbObject.breadCrumb = breadcrumbContainer;
  if (fileName){
    breadCrumbObject.renderFileName(documentItem,fileName);
  } else if (documentItem.currentFolder){
    breadCrumbObject.renderFolder(documentItem);    
  } else if (documentItem.driveName){
    breadCrumbObject.renderDrive(documentItem);
  } else {
    breadCrumbObject.renderDriveType(documentItem);
  }   
  jQuery("a:last",breadcrumbContainer).toggleClass('Normal Selected');;  
};

function BreadCrumbs() {
  breadCrumb = null;
  
  BreadCrumbs.prototype.renderDriveType = function(documentItem) {
    if (this.breadCrumb){
      this.appendBreadCrumbNode(documentItem, null);
    }
  };

  BreadCrumbs.prototype.renderDrive = function(documentItem) {
    if (this.breadCrumb) {
      var tmpDocumentItem = new DocumentItem();
      tmpDocumentItem.driveType = documentItem.driveType;
      this.renderDriveType(tmpDocumentItem);
      this.appendBreadCrumbNode(documentItem, documentItem.driveName);
    }
  };

  BreadCrumbs.prototype.renderFolder = function(documentItem) {
    if (this.breadCrumb) {
      var tmpDocumentItem = new DocumentItem();
      tmpDocumentItem.driveType = documentItem.driveType;
      tmpDocumentItem.driveName = documentItem.driveName;
      tmpDocumentItem.workspaceName = documentItem.workspaceName;
      this.renderDrive(tmpDocumentItem);
      var breadCrumbItem = documentItem.currentFolder.split("/");
      var breadCrumbTitle = documentItem.titlePath.split("/");
      if (breadCrumbItem != "") {
        tmpDocumentItem.currentFolder = '';
        tmpDocumentItem.titlePath = '';
        for ( var i = 0; i < breadCrumbItem.length; i++) {
          tmpDocumentItem.currentFolder += breadCrumbItem[i];
          tmpDocumentItem.titlePath += breadCrumbTitle[i];
          this.appendBreadCrumbNode(tmpDocumentItem, breadCrumbTitle[i]);
          tmpDocumentItem.currentFolder += "/";
          tmpDocumentItem.titlePath += "/";
        }
      }
    }
  };
  

  BreadCrumbs.prototype.renderFileName = function(documentItem, fileName) {
  if (this.breadCrumb) {
      this.renderFolder(documentItem)
      var fileNode = document.createElement("div");
      fileNode.className = 'BreadcumbTab';
      fileNode.innerHTML = '<a class="Normal">' + "/"
          + fileName + '</a>';
      this.breadCrumb.append(fileNode);
    }
  };
  
  BreadCrumbs.prototype.appendBreadCrumbNode = function(documentItem, name) {
    var node = document.createElement("div");
    var className = 'Normal';
    if (name ==null){
      name ='';
      className= 'HomeIcon';
    } else {
      name = "/" + name;
    }
    var driveType = (documentItem.driveType) ? ' driveType="'
        + documentItem.driveType + '"' : "";
    var driveName = (documentItem.driveName) ? ' driveName="'
        + documentItem.driveName + '"' : "";
    var workspaceName = (documentItem.workspaceName) ? ' workspaceName="'
        + documentItem.workspaceName + '"' : "";
    var currentFolder = (documentItem.currentFolder) ? ' currentFolder="'
        + documentItem.currentFolder + '"' : "";
    var titlePath = (documentItem.titlePath) ? ' titlePath="' + documentItem.titlePath + '"'
        : "";
    node.className = 'BreadcumbTab';
    node.innerHTML = '<a class="'
        + className
        + '"'
        + driveType
        + driveName
        + workspaceName
        + currentFolder
        + titlePath
        + '" href="javascript:void(0);" onclick="eXo.commons.DocumentSelector.actionBreadcrumbs(this);">'
        + name + '</a>&nbsp;&nbsp;';
    this.breadCrumb.append(node);
  };
};

DocumentSelector.prototype.getClazzIcon = function(nodeType){
  var strClassIcon = '';
  if (!nodeType) {
    strClassIcon = ".nt_file";
    return strClassIcon;
  }
  strClassIcon = nodeType.replace("/", "_").replace(":", "_").toLowerCase()  + "16x16Icon";
  return strClassIcon;
};

DocumentSelector.prototype.request = function(url){
  var res;
  jQuery.ajax({
    url: url,
    type: "GET",
    async: false,
    success: function(data) {
      res = data;
    }
  });
 return res;
}

String.prototype.trunc = function(n, useWordBoundary){
  var toLong = this.length > n, s_ = toLong ? this.substr(0, n - 1) : this;
  s_ = useWordBoundary && toLong ? s_.substr(0, s_.lastIndexOf(' ')) : s_;
  return toLong ? s_ + '...' : s_;
};

_module.DocumentSelector = new DocumentSelector();

if(!window.eXo.commons) window.eXo.commons={}
window.eXo.commons.DocumentSelector = _module.DocumentSelector;

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
  this.selectFileLink = jQuery("a.selectFile:first",this.uiComponent);
  this.selectFolderLink = jQuery("a.selectFolder:first",this.uiComponent);  
  this.getDrivesURL = restContext + this.getDrives;
  this.getFoldersAndFilesURL = restContext + this.getFoldersAndFiles;
  this.deleteFolderOrFileURL = restContext + this.deleteFolderOrFile;
  this.createFolderURL = restContext + this.createFolder;
  var documentItem = new DocumentItem();
  documentItem.driveType = this.defaultDriveType;
  me.resetDropDownBox();
  me.renderDetails(documentItem);
};

DocumentSelector.prototype.resetDropDownBox = function() {
	var dropDownBox = jQuery('#DriveTypeDropDown'); 
	var btn = dropDownBox.find('div.btn');
	jQuery(btn).removeClass('btn-primary');
	var options = jQuery(dropDownBox).find('ul>li');
	jQuery.each(options, function(idx, el) {
	    var hiddenVal = jQuery(btn).find('span').text();
	    var elVal = jQuery(el).find('a').text();
	    if( jQuery.trim(elVal) === jQuery.trim(hiddenVal) ) {
	      jQuery(el).hide();
	    } else {
	      jQuery(el).show();
	    }
	});
}

DocumentSelector.prototype.changeDrive = function(selectedDrive) {
  _module.DocumentSelector.resetDropDownBox();
  var documentItem = new DocumentItem();
  documentItem.driveType = selectedDrive;
  eXo.commons.DocumentSelector.renderDetails(documentItem);
};

DocumentSelector.prototype.renderDetails = function(documentItem) {
  var me = eXo.commons.DocumentSelector;
  // Clear old data
  var actionBar = jQuery("div.actions:first", this.uiComponent);
  
  // reset
  jQuery('#ListRecords>li').remove();
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
    me.renderDrives(documentItem);
  } else {    
    me.renderDetailsFolder(documentItem);
  }
};

DocumentSelector.prototype.renderDrives = function(documentItem) {

 var me = _module.DocumentSelector;
 var driveType = documentItem.driveType;
 var url = this.getDrivesURL;
 url += "?" + this.driveTypeParam + "=" + driveType;
 var data = me.request(url);
 
 var folderContainer = jQuery("Folders:first", data);
 var folderList = jQuery("Folder", folderContainer);

 var listRecords = jQuery('#ListRecords');

 // reset
 //listRecords.remove();

 if (!folderList || folderList.length <= 0) { 
    var item = jQuery('<li/>', {
                              'class' : 'listItem'
                            });
    item.html("There is no drive");
    item.addClass("Item TRNoContent");
    listRecords.append(item);
    return;
 }

 for ( var i = 0; i < folderList.length; i++) {
   var name = folderList[i].getAttribute("name");
   var driveName = folderList[i].getAttribute("name");
   var nodeType = folderList[i].getAttribute("nodeType");
   var workspaceName = folderList[i].getAttribute("workspaceName");
   var canAddChild = folderList[i].getAttribute("canAddChild");
   var uiIconFolder = me.getClazzIcon(folderList[i].getAttribute("nodeType"))
   var iconEl = jQuery('<i/>', {
                              'class' : uiIconFolder
                            });

   var link = jQuery('<a/>',{
      'class' : 'Item Drive_'+ nodeType,
      'driveType' : driveType,
      'driveName' : driveName,
      'workspaceName' : workspaceName,
      'name' : name,
      'canAddChild' : canAddChild,
      'href' : 'javascript:void(0);',
      'text' : name
    }).on('click', function() {
      _module.DocumentSelector.browseFolder(this);
    }).append(iconEl);
    
    var item = jQuery('<li/>', {
                              'class' : 'listItem'
                            }).append(link);

    listRecords.append(item);
 }
};

DocumentSelector.prototype.renderDetailsFolder = function(documentItem) {
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

  var listRecords = jQuery('#ListRecords');

  if ((!fileList || fileList.length <= 0)
      && (!folderList || folderList.length <= 0)) {
	  var item = jQuery('<li/>', {
	                      'class' : 'listItem'
	                    });
    item.html("There is no folder or file");
    item.addClass("Item TRNoContent");
    listRecords.append(item);
    return;
  }
	  
	for ( var i = 0; i < folderList.length; i++) { // render folders
	  var folderIcon = me.getClazzIcon(folderList[i].getAttribute("nodeType"));
	  var jcrPath = folderList[i].getAttribute("path");
	  var nodeType = folderList[i].getAttribute("folderType");
	  var name = folderList[i].getAttribute("name");
	  var title = folderList[i].getAttribute("title");
	  var titlePath = folderList[i].getAttribute("titlePath");
	  
	  var childFolder = folderList[i].getAttribute("currentFolder");
	  var canRemove = folderList[i].getAttribute("canRemove");
	  var canAddChild = folderList[i].getAttribute("canAddChild");
    var workspaceName = folderList[i].getAttribute("workspaceName");
    
    var iconEl = jQuery('<i/>', {
                              'class' : folderIcon
                            });

    var link = jQuery('<a/>',{
      'class' : 'Item',
      'name' : name,
      'title' : title,
      'driveType' : driveType,
      'driveName' : driveName,
      'workspaceName' : workspaceName,
      'currentFolder' : childFolder,
      'canAddChild' : canAddChild,
      'titlePath' : titlePath,
      'jcrPath' : jcrPath,
      'href' : 'javascript:void(0);',
      'text' : title
     }).on('click', function() {
      _module.DocumentSelector.browseFolder(this);
     }).append(iconEl);
    
     var item = jQuery('<li/>', {
                              'class' : 'listItem'
                            }).append(link);

     listRecords.append(item);
    } // end for
    
    for ( var j = 0; j < fileList.length; j++) { // render files
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
      
      var fileIcon = me.getClazzIcon(fileList[j].getAttribute("nodeType"));
    
    
	    var iconEl = jQuery('<i/>', {
	                              'class' : fileIcon
	                            });
	
	    var link = jQuery('<a/>',{
	      'class' : 'Item',
	      'name' : node,
	      'title' : title,
	      'jcrPath' : jcrPath,
	      'href' : 'javascript:void(0);',
	      'text' : title
	     }).on('click', function() {
	      _module.DocumentSelector.submitSelectedFile(this);
	     }).append(iconEl);
	    
	     var item = jQuery('<li/>', {
	                              'class' : 'listItem'
	                            }).append(link);
	
	     listRecords.append(item);
    }
};

DocumentSelector.prototype.submitSelectedFile = function(item){
  var me = _module.DocumentSelector;   
  var nodePath = jQuery(item).attr("jcrPath");
  var fileName = jQuery(item).attr("title");
    
  if (me.selectFileLink) {
    var link = me.selectFileLink.attr("href");
    var endParamIndex = link.lastIndexOf("')");
    if (endParamIndex > 0)
      link = link.substring(0, endParamIndex) + "&"+ me.dataId +"=" + encodeURI(nodePath) + "')";
    window.location = link;
  }
  if (me.selectFile) {
    if (me.selectFile.hasClass("selected")) {
      me.selectFile.removeClass("selected");
    }
  }
  me.selectFile = jQuery(item).parent();
  me.selectFile.addClass("selected");
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

DocumentSelector.prototype.browseFolder = function(link){
  var me = _module.DocumentSelector;
  var documentItem = new DocumentItem();
  documentItem.driveType = jQuery(link).attr("driveType");
  documentItem.driveName = jQuery(link).attr("driveName");
  documentItem.workspaceName = jQuery(link).attr("workspaceName");
  documentItem.currentFolder = jQuery(link).attr("currentFolder");
  documentItem.jcrPath = jQuery(link).attr("jcrPath");
  documentItem.canAddChild = jQuery(link).attr("canAddChild");
  documentItem.titlePath = jQuery(link).attr("titlePath");
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
  var breadcrumbContainer = jQuery("ul.breadcrumb:first",this.uiComponent);
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
  jQuery("a:last",breadcrumbContainer).toggleClass('normal active');;  
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
      var fileNode = document.createElement("li");
      fileNode.className = '';
      fileNode.innerHTML = '<span class="uiIconMiniArrowRight">&nbsp;</span>' +
      										 '<a href="javascript:void(0);" class="normal">' + 
      												fileName + 
      										 '</a>' ;
      this.breadCrumb.append(fileNode);
    }
  };
  
  BreadCrumbs.prototype.appendBreadCrumbNode = function(documentItem, name) {
    //if (name == null) return;
    
    
    var appendedNode = jQuery('<li/>');
    
    var className = 'normal';
    if (name ==null){
      name ='';
      className= 'uiIconHome';
    } else {
      name = "" + name;
    }
    
    var anchorEl = jQuery('<a/>',{
      'class' : className,
      'driveType' : documentItem.driveType,
      'driveName' : documentItem.driveName,
      'workspaceName' : documentItem.workspaceName,
      'currentFolder' : documentItem.currentFolder,
      'titlePath' : (documentItem.titlePath) ?  documentItem.titlePath : "",
      'href' : 'javascript:void(0);',
      'text' : name
    }).on('click', function() {
      eXo.commons.DocumentSelector.actionBreadcrumbs(this);
    });
    
    if ( (name.length > 0) && ((appendedNode.find('span.uiIconMiniArrowRight')).length == 0) ) {
        var iconEl = jQuery('<span/>', {
          'class' : 'uiIconMiniArrowRight'
        });
        
        appendedNode.append(iconEl);
        appendedNode.append(anchorEl);
        this.breadCrumb.append(appendedNode);
    } else {
        this.breadCrumb.append(anchorEl);
    }
    
  };
};

DocumentSelector.prototype.getClazzIcon = function(nodeType){
  var strClassIcon = '';
  if (!nodeType) {
    strClassIcon = ".nt_file";
    return strClassIcon;
  }
  strClassIcon = nodeType.replace("", "_").replace(":", "_").toLowerCase()  + "16x16Icon";
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

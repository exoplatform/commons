(function(CssIconFile, jQuery, gtnbase){
	var _module = {};
	
/********** Document Selector ***********/
	
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
  CssIconFile.init();
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
   var uiIconFolder = "uiIcon16x16FolderDefault " + CssIconFile.getCssClassByType(nodeType);
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
      'rel' : 'tooltip', 
      'data-placement' : 'bottom', 
      'data-original-title' : name
    }).on('click', function() {
      _module.DocumentSelector.browseFolder(this);
    }).append(iconEl).append(name);
    
    link.tooltip();

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
	  var jcrPath = folderList[i].getAttribute("path");
	  var nodeType = folderList[i].getAttribute("folderType");
	  var name = folderList[i].getAttribute("name");
	  var title = folderList[i].getAttribute("title");
	  var titlePath = folderList[i].getAttribute("titlePath");
	  var folderIcon = "uiIcon16x16FolderDefault " + CssIconFile.getCssClassByType(folderList[i].getAttribute("nodeType"));

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
      'driveType' : driveType,
      'driveName' : driveName,
      'workspaceName' : workspaceName,
      'currentFolder' : childFolder,
      'canAddChild' : canAddChild,
      'titlePath' : titlePath,
      'jcrPath' : jcrPath,
      'href' : 'javascript:void(0);',
      'rel':'tooltip',
      'data-placement':'bottom',
      'data-original-title' : title
     }).on('click', function() {
      _module.DocumentSelector.browseFolder(this);
     }).append(iconEl).append(title);

     link.tooltip();
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
      
      var fileIcon = CssIconFile.getCssClassByNameAndType(node, nodeType);
    
    
	    var iconEl = jQuery('<i/>', {
	                              'class' : fileIcon
	                            });

	    var link = jQuery('<a/>',{
	      'class' : 'Item',
	      'name' : node,
	      'jcrPath' : jcrPath,
	      'data-file-type':nodeType,
	      'href' : 'javascript:void(0);',
	      'rel':'tooltip',
	      'data-placement':'bottom',
	      'data-original-title' : title
	     }).on('click', function() {
	      _module.DocumentSelector.submitSelectedFile(this);
	     }).append(iconEl);

	     link.tooltip().append(title);
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
  var fileType = jQuery(item).attr("data-file-type");
    
  if (me.selectFileLink) {
    var link = me.selectFileLink.attr("href");
    var endParamIndex = link.lastIndexOf("')");
    if (endParamIndex > 0) {
      link = link.substring(0, endParamIndex) + "&"+ me.dataId +"=" + encodeURI(nodePath) + "&filetype=" + encodeURI(fileType) +"')";
    }
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
  var msg_invalid_folder_name = inputFolderName.getAttribute("msg_invalid_folder_name");
  var folder_name_standard = /^[\w.\s-]+$/;
  
  if (!me.selectedItem || !me.selectedItem.driveName) {
    alert(msg_select_folder);
    return;
  }

  var folderName = prompt(msg_enter_folder_name, "");
  if (folderName === null) {
    return;
  }

  if ( folderName == "" ) {
    alert(msg_empty_folder_name);
    return;
  }
  
  if ( !folder_name_standard.test(folderName) ) {
    alert(msg_invalid_folder_name);
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
      className= 'uiIconTree uiIconLightGray';
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
	var iconEl = "";
	if(documentItem.currentFolder && documentItem.currentFolder.length > 0) {
	  iconEl = jQuery('<span/>', {
	    'class' : 'uiIconMiniArrowRight'
	  });
	} else {
	  iconEl = jQuery('<span/>', {
	  });
	}
        
        appendedNode.append(iconEl);
        appendedNode.append(anchorEl);
        this.breadCrumb.append(appendedNode);
    } else {
        this.breadCrumb.append(anchorEl);
    }
    
  };
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
};

String.prototype.trunc = function(n, useWordBoundary){
  var toLong = this.length > n, s_ = toLong ? this.substr(0, n - 1) : this;
  s_ = useWordBoundary && toLong ? s_.substr(0, s_.lastIndexOf(' ')) : s_;
  return toLong ? s_ + '...' : s_;
};

_module.DocumentSelector = new DocumentSelector();

if(!window.eXo.commons) window.eXo.commons={}
window.eXo.commons.DocumentSelector = _module.DocumentSelector;



/********** UIDSUpload ***********/

function UIDSUpload() {
	  this.listUpload = new Array();
	  this.isAutoUpload = true;
	 
	  // this.listLimitMB = new Array();
	  var Browser = gtnbase.Browser;
	  
	};
	/**
	 * 
	 * @param {String}
	 *          uploadId identifier upload
	 * @param {boolean}
	 *          isAutoUpload auto upload or none
	 */
	UIDSUpload.prototype.initUploadEntry = function(uploadId, isAutoUpload) {  
		_module.UIDSUpload.isAutoUpload = isAutoUpload;
	  this.restContext = eXo.env.portal.context+ "/" + eXo.env.portal.rest+ "/managedocument/uploadFile" ;
	  this.createUploadEntry(uploadId, isAutoUpload);
	};


	UIDSUpload.prototype.createUploadEntry = function(uploadId, isAutoUpload) {
	  var me = _module.UIDSUpload;
	  var iframe = document.getElementById(uploadId+'uploadFrame');
	  var idoc = iframe.contentWindow.document ;
	  if (gtnbase.Browser.gecko) {
	    idoc.open();
	    idoc.close();
	    me.createUploadEntryForFF(idoc, uploadId, isAutoUpload);
	    return;
	  }
	  var uploadAction = me.restContext + "/upload?" ;
	  uploadAction += "uploadId=" + uploadId ;
	  
	  var uploadHTML = "";
	  uploadHTML += "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Strict//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'>";
	  uploadHTML += "<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en' lang='" +eXo.core.I18n.lang+ "' dir='" +eXo.core.I18n.dir+ "'>";
	  uploadHTML += "<head>";
	  uploadHTML += "<style type='text/css'>";
	  uploadHTML += this.getStyleSheetContent();
	  uploadHTML += "</style>";
	  uploadHTML += "<script type='text/javascript'>var eXo = parent.eXo</script>";
	  uploadHTML += "</head>";
	  uploadHTML += "<body style='margin: 0px; border: 0px;'>";
	  uploadHTML += this.getUploadContent(uploadId, uploadAction, isAutoUpload);
	  uploadHTML += "</body>";
	  uploadHTML += "</html>";

	  if (navigator.userAgent.toLowerCase().indexOf('chrome') > -1) {
	    // workaround for Chrome
	    // When submit in iframe with Chrome, the iframe.contentWindow.document
	    // seems not be reconstructed correctly
		  
	    if(idoc.open) idoc.open();
	    if(idoc.close) idoc.close();
	    var doc = idoc.documentElement || idoc;
	    doc.innerHTML = uploadHTML;
	  } else {
	    idoc.open();
	    idoc.write(uploadHTML);
	    idoc.close();
	  }
	};

	UIDSUpload.prototype.createUploadEntryForFF = function(idoc, uploadId, isAutoUpload){
	  var uploadAction = _module.UIDSUpload.restContext + "/upload?" ;
	  uploadAction += "uploadId=" + uploadId+"&action=upload" ; 
	    
	  var newDoctype = document.implementation.createDocumentType('html','-//W3C//DTD XHTML 1.0 Transitional//EN','http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'); 
	  if (idoc.doctype) {
	    idoc.replaceChild(newDoctype, idoc.doctype);
	  }
	  idoc.lang = eXo.core.I18n.lang;
	  idoc.xmlns = 'http://www.w3.org/1999/xhtml';
	  idoc.dir = eXo.core.I18n.dir;
	   
	  idoc.head = idoc.head || idoc.getElementsByTagName('head')[0];
	  var script = document.createElement('script');
	  script.type = "text/javascript";
	  script.text = "var eXo = parent.eXo";
	  idoc.head = idoc.head || idoc.getElementsByTagName('head')[0];
	  idoc.head.appendChild(script);

	  var style = document.createElement('style');
	  style.type = "text/css"; 
	  var styleText = this.getStyleSheetContent();
	  var cssText = document.createTextNode(styleText);
	  style.appendChild(cssText);
	  idoc.head.appendChild(style);
	  
	  idoc.body.innerHTML= this.getUploadContent(uploadId, uploadAction, isAutoUpload);
	}

	UIDSUpload.prototype.getUploadContent = function(uploadId, uploadAction, isAutoUpload) {
	  var container = parent.document.getElementById(uploadId);
	  var uploadIframe = jQuery("#"+uploadId+"UploadIframe",container);
	  var uploadText = uploadIframe.title;
	  
    var idFile = 'File' + new Date().getTime();
    var uploadHTML = "";  
    uploadHTML += "  <form id='"+uploadId+"' class='UIDSUploadForm' style='margin: 0px; padding: 0px' action='"+uploadAction+"' enctype='multipart/form-data' method='post'>";
    uploadHTML += "    <div class='BrowseDiv'>";
    if(gtnbase.Browser.isIE()) {
          uploadHTML += "      <a class='BrowseLink' href='javascript:void(0);'>";
          uploadHTML += "        <label for='" + idFile + "' style='width: 26px; height: 26px; display:block'>";
          uploadHTML += "          <input style=\"position:absolute; left:-5000px;\" type='file' name='" + idFile + "' size='1' id='" + idFile + "' class='FileHidden' value='' onchange='parent.eXo.commons.UIDSUpload.upload(this, " + uploadId + ")'/>";
          uploadHTML += "        </label>";
          uploadHTML += "      </a>";
        } else {
          uploadHTML += "      <a class=\"BrowseLink\" onclick=\"(function(elm) { document.getElementById('" + idFile + "').click();})(this)\">";
          uploadHTML += "        <input type='file' name='file' size='1' style='display:none' id='" + idFile + "' class='FileHidden' value='' onchange='parent.eXo.commons.UIDSUpload.upload(this, " + uploadId + ")'/>";
          uploadHTML += "      </a>";
        }
    uploadHTML += "    </div>";
    uploadHTML += "  </form>";
    return uploadHTML;
	}

	UIDSUpload.prototype.getStyleSheetContent = function(){
	  var styleText = "";
	  styleText += "body { margin:0; padding:0}";
	  styleText += ".BrowseDiv {position: relative; }";
	  styleText += ".FileHidden { opacity: 0; overflow: hidden; position: absolute; height: 26px; bottom: 0px; right: 0px; -moz-opacity:0 ; filter:alpha(opacity: 0); cursor: pointer;} ";  
	  styleText += ".BrowseLink { font-family: Arial, Helvetica, sans-serif; text-decoration: none; border: 1px solid transparent; width: 24px; height: 24px; display: block;";
	  styleText += "     text-decoration: none;";
	  styleText += "    background: url('/CommonsResources/skin/less/DocumentSelector/images/fileShareUpload.png') no-repeat 3px center;}";
	  styleText += ".BrowseLink:hover { background-color: #F9F9F9; border: 1px solid #CFCFCF; text-decoration: none; border-radius: 3px; box-shadow: 0 1px 2px 0 #FFFFFF; cursor: pointer;";
	  styleText += "    background: url('/CommonsResources/skin/less/DocumentSelector/images/fileShareUploadBlue.png') no-repeat 3px center;}";
	  return styleText;
	}

	/**
	 * Refresh progress bar to update state of upload progress
	 * 
	 * @param {String}
	 *          elementId identifier of upload bar frame
	 */
	UIDSUpload.prototype.refeshProgress = function(elementId) {
	  var me = _module.UIDSUpload;
	  var documentSelector = _module.DocumentSelector;
	  var list =  me.listUpload;
	  var selectedItem = documentSelector.selectedItem;
	  if(!selectedItem) return;
	  if(list.length < 1) return;
	  var currentFolder = selectedItem.currentFolder;
	  if (!currentFolder) currentFolder ='';
	  var url = me.restContext + "/control?" ;  
	  url += "action=progress" + "&workspaceName=" + selectedItem.workspaceName
	  + "&driveName=" + selectedItem.driveName + "&currentFolder="
	  + currentFolder + "&currentPortal=" + eXo.env.portal.portalName + "&language="
	  + eXo.env.portal.language +"&uploadId=" + elementId;  
	  var data = null; 
	  if(list.length > 0) {
	    var data = documentSelector.request(url);
	    setTimeout("eXo.commons.UIDSUpload.refeshProgress('" + elementId + "');", 1000); 
	  } 
	  else {
	  return;
	  }
	    var container = parent.document.getElementById(elementId);
	    if (!data) {
	      this.abortUpload(elementId);
	      //var message = eXo.core.DOMUtil.findFirstChildByClass(container, "div", "LimitMessage").innerHTML ;
	      var message = jQuery("div.LimitMessage:first-child").html();
	      alert(message.replace("{0}", response.upload[id].size)) ;
	      return;
	    } else {
	    var element = document.getElementById(elementId+"ProgressIframe");
	    var nodeList = data.getElementsByTagName("UploadProgress");
	    if(!nodeList) return;
	    var oProgress;
	    if(nodeList.length > 0) oProgress = nodeList[0];
	    var percent = oProgress.getAttribute("percent");
	    var fileName = oProgress.getAttribute("fileName");
	    var limit = oProgress.getAttribute("limit");
	    var unit = oProgress.getAttribute("unit");
	    var total = oProgress.getAttribute("totalSize");
	    if (limit && unit) {
	    	var realLimit = limit;
	    	if (unit == "KB") realLimit *= 1024;
	    	if (unit == "MB") realLimit *= 1024 * 1024;
	    	if (unit == "GB") realLimit *= 1024 * 1024 * 1024;
	    	if (realLimit < total) {
    		  this.abortUpload(elementId);
    		  //var message = eXo.core.DOMUtil.findFirstChildByClass(container, "div", "LimitMessage").innerHTML ;
		      var message = jQuery("div.LimitMessage:first-child").html();
		      alert(message.replace("{0}", limit).replace("{1}", unit)) ;
		      return;
	    	}
	    }
            //progress Bar Label
	    var progressBarFrame = jQuery("div.progressBarFrame:first");
  	    var percentVal = jQuery("div.percent",progressBarFrame);
            percentVal.html(percent+"%");
	    var bar = jQuery("div.bar:first",progressBarFrame);
	    bar.css("width", percent + "%");
	   // var progressBarLabel = jQuery("div.ProgressBarLabel:first",container).html(percent + "%");   
	     if(percent == 100) {
	      me.listUpload.remove(elementId);
	      if (!fileName || fileName=="") {
	        alert(container.getAttribute("upload_failed"));
	      }
 		var progressBarFrame = jQuery("div.progressBarFrame:first");
	      bar.css("width", 0 + "%");
	      progressBarFrame.hide() ;  
	      me.saveUploaded(elementId, fileName);
	      documentSelector.renderDetails(selectedItem);
	      //var refreshUpload = eXo.core.DOMUtil.findFirstDescendantByClass(container, "a", "RefreshUpload") ; 
	      var refreshUpload = jQuery("a.RefreshUpload:first",container);
	      if (refreshUpload){
	        eval(refreshUpload.attr("href"));
	      }
	     }
	   }
	};

	/**
	 * Send request to save uploaded file
	 * 
	 * @param {String}
	 *          uploadId identifier of uploaded file
	 */
	UIDSUpload.prototype.saveUploaded = function(uploadId, fileName) {
	  var me = _module.UIDSUpload;
	  var selectedItem = _module.DocumentSelector.selectedItem;
	  var url = me.restContext + "/control?" ;  
	  url += "action=save" + "&workspaceName=" + selectedItem.workspaceName
	  + "&driveName=" + selectedItem.driveName + "&currentFolder="
	  + selectedItem.currentFolder + "&currentPortal=" + eXo.env.portal.portalName + "&language="
	  + eXo.env.portal.language +"&uploadId=" + uploadId + "&fileName=" + encodeURIComponent(fileName);
	  var responseText = ajaxAsyncGetRequest(url, false);
	};


	/**
	 * Show uploaded state when upload has just finished a file
	 * 
	 * @param {String}
	 *          id uploaded identifier
	 * @param {String}
	 *          fileName uploaded file name
	 */
	UIDSUpload.prototype.showUploaded = function(id) {
	  _module.UIDSUpload.listUpload.remove(id);
	  var container = parent.document.getElementById(id);
	  var element = document.getElementById(id+"ProgressIframe");
	  element.innerHTML =  "<span></span>";
	  
	  //var uploadIframe = eXo.core.DOMUtil.findDescendantById(container, id+"UploadIframe");
	  var uploadIframe = jQuery("#"+id+"UploadIframe",container);

	  uploadIframe.show();
	  //var progressIframe = eXo.core.DOMUtil.findDescendantById(container, id+"ProgressIframe");
	  var progressIframe = jQuery("#"+id+"ProgressIframe",container);
	  progressIframe.hide();
	  
	  //var progressBarFrame = eXo.core.DOMUtil.findFirstDescendantByClass(container, "div", "ProgressBarFrame") ;
	  var progressBarFrame = jQuery("div.progressBarFrame:first") ;
	  progressBarFrame.hide() ;
	  var tmp = element.parent();
	  var temp = tmp.parent();
	  // TODO: dang.tung - always return true even we reload browser
	  var  input = parent.document.getElementById('input' + id);
	  input.value = "true" ;  
	};
	/**
	 * Abort upload process
	 * 
	 * @param {String}
	 *          id upload identifier
	 */
	UIDSUpload.prototype.abortUpload = function(id) {
	  var me = _module.UIDSUpload;
	  var idUpload=jQuery("div.uiUploadArea .UIDSUploadInput").attr("id");
	  me.listUpload.remove(idUpload);
	  
	  var container = jQuery(parent.document.getElementById(idUpload));
	  var uploadIframe = container.find("#"+idUpload+"UploadIframe");
	  uploadIframe.show();
	  me.createUploadEntry(idUpload, me.isAutoUpload);
	  var progressIframe = container.find("#"+idUpload+"ProgressIframe") ;
	  progressIframe.hide('fast', function() {
		  var url_ = _module.UIDSUpload.restContext + "/control?" ;
		  url_ += "uploadId=" +idUpload+"&action=abort" ;
		  jQuery.ajax({
			  url: url_,
			  type: "GET",
			  async: false,
			  headers: {"Cache-Control" : "max-age=86400"}
		  });
	  });

	  var tmp = progressIframe.parent();
	  var temp = tmp.parent();
	// var child = eXo.core.DOMUtil.getChildrenByTagName(temp,"label");
	// child[0].style.visibility = "visible" ;
	  //var progressBarFrame = eXo.core.DOMUtil.findFirstDescendantByClass(container, "div", "ProgressBarFrame") ;
	  var progressBarFrame = jQuery("div.progressBarFrame:first");
	  progressBarFrame.hide() ;
	  //var selectFileFrame = eXo.core.DOMUtil.findFirstDescendantByClass(container, "div", "SelectFileFrame") ;
	  var selectFileFrame = jQuery("div.SelectFileFrame:first",container);
	  selectFileFrame.hide() ;
	   
	  var  input = parent.document.getElementById('input' + idUpload);
	  input.value = "false";
	};

	/**
	 * Start upload file
	 * 
	 * @param {Object}
	 *          clickEle
	 * @param {String}
	 *          id
	 */
	UIDSUpload.prototype.upload = function(clickEle, id) {
	  //var DOMUtil = eXo.core.DOMUtil;
	  var me = _module.UIDSUpload;
	  var selectedItem = _module.DocumentSelector.selectedItem;
	  var container = parent.document.getElementById(id);
	  var uploadIFrame = parent.document.getElementById(id+"UploadIframe");
	  var uploadFrame = parent.document.getElementById(id+"uploadFrame");
	  if (!selectedItem || !selectedItem.driveName) {
	    alert(uploadIFrame.getAttribute("select_drive"));
	    file.value == '';
	    return;
	  }

	  var canAddChild = selectedItem.canAddChild;
	  if (canAddChild == "false") {
	    alert(uploadIFrame.getAttribute("permission_required"));
	    file.value == '';
	    return;
	  }

	  var form = uploadFrame.contentWindow.document.getElementById(id);

	  var file  = jQuery(clickEle ,form);
	  if(file.attr("value") == null || file.attr("value") == '') return;  
	  var fileName = file.attr("value").replace(/C:\\fakepath\\/i, '');
	  jQuery(".fileNameLabel").html(fileName)
                            .attr({'rel':'tooltip', 'data-placement':'bottom', 'data-original-title' : fileName})
                            .tooltip();
    
	  var progressBarFrame = jQuery("div.progressBarFrame:first");
	  progressBarFrame.show() ;  
	  
	  var progressBarLabel = jQuery("div.pull-left percent:first-child",progressBarFrame);
	  progressBarLabel.html("0%") ;
	  
	  var  input = parent.document.getElementById('input' + id);
	  input.value = "true";
	  
	
	  var uploadIframe = jQuery("#"+id+"UploadIframe",container);
	  uploadIframe.hide();
	
	 var progressIframe = jQuery("#"+id+"ProgressIframe",container);
	 progressIframe.hide();

	var tmp = progressIframe.parent();
	 var temp = tmp.parent();
	  
	  form.submit() ;
	  
	  var list = me.listUpload;

	  if(list.length == 0) {
	    me.listUpload.push(form.id);
	    setTimeout("parent.window.eXo.commons.UIDSUpload.refeshProgress('" + id + "');", 1000);
	  } else {
	    me.listUpload.push(form.id);  
	  }
	};

	UIDSUpload.prototype.validate = function(name) {
	  if(name.indexOf(':')>=0 || name.indexOf('/')>=0 || name.indexOf('\\')>=0 || name.indexOf('|')>=0 || name.indexOf('^')>=0 || name.indexOf('#')>=0 ||
	    name.indexOf(';')>=0 || name.indexOf('[')>=0 || name.indexOf(']')>=0 || name.indexOf('{')>=0 || name.indexOf('}')>=0 || name.indexOf('<')>=0 || name.indexOf('>')>=0 || name.indexOf('*')>=0 ||
	    name.indexOf('\'')>=0 || name.indexOf('\"')>=0 || name.indexOf('+')>=0) {
	    return false;
	  } else {
	    return true;
	  }
	}

	/*-------Array utils------*/
	Array.prototype.remove = function (element) {
	  var result = false ;
	  var array = [] ;
	  for (var i = 0; i < this.length; i++) {
	    if (this[i] == element) {
	      result = true ;
	    } else {
	      array.push(this[i]) ;
	    }
	  }
	  this.length = 0;
	  for (var i = 0; i < array.length; i++) {
	    this.push(array[i]) ;
	  }
	  array = null ;
	  return result ;
	} ;

	/*--------------------------*/

_module.UIDSUpload = new UIDSUpload();
if(!window.eXo.commons) window.eXo.commons={}
window.eXo.commons.UIDSUpload = _module.UIDSUpload;

return _module;

})(CssIconFile, jQuery, gtnbase)

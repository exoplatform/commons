/**
 * Refactoring: Groupe exoeditor.js and eXoConfig.js
**/

(function(jQuery,gtnbase){
	var _module = {};

/********** Editor ***********/
	function EXOCKEDITOR() {
	}


	EXOCKEDITOR.prototype.loadScript = function (url, callback){
	  var script = document.createElement("script")
	  script.type = "text/javascript";
	  if (script.readyState){ // IE
	    script.onreadystatechange = function(){
	      if (script.readyState == "loaded" || script.readyState == "complete"){
	        script.onreadystatechange = null;
	        callback();
	      }
	    };

	  } else { // Others
	    script.onload = function(){
	      callback();
	    };
	  }
	  script.src = url;
	  document.getElementsByTagName("head")[0].appendChild(script);
	};


	EXOCKEDITOR.prototype.replaceByCKEditor = function(textAreaId, config) {
	  var instances = CKEDITOR.instances[textAreaId]; if (instances) instances.destroy(true);
	  if (!config) {
	    CKEDITOR.replace(textAreaId).on('key', function(ev) {var me = ev.editor; me.element.setValue(me.getData()); });
	  } else {
	    CKEDITOR.replace(textAreaId, config).on('key', function(ev) {var me = ev.editor; me.element.setValue(me.getData()); });
	  }
	};

	EXOCKEDITOR.prototype.makeCKEditor = function(textAreaId, config) {
	  
	  if (!window.CKEDITOR) {
	    EXOCKEDITOR.loadScript(window.CKEDITOR_BASEPATH + 'ckeditor.js', function() {EXOCKEDITOR.replaceByCKEditor(textAreaId, config);});
	  } else {
	    EXOCKEDITOR.replaceByCKEditor(textAreaId, config);
	  }
	};
	﻿EXOCKEDITOR = new EXOCKEDITOR();

	// function updateck() {
	// for ( instance in CKEDITOR.instances ) {
	// if (document.getElementById(instance)==null) { clearInterval(intckeditor); }
	// document.getElementById(instance).value =
	// CKEDITOR.instances[instance].getData();
	// }
	// }

	// This code is mandatory to update CKEditor instances as they don't sync
	// natively in Ajax Popup with Chrome and Safari Browser
	// var intckeditor = setInterval("updateck()", 1000);


	eXo.commons.ExoEditor = ﻿EXOCKEDITOR;

	_module.﻿EXOCKEDITOR = eXo.commons.ExoEditor;
	
	

	
/********** Editor config ***********/
	﻿﻿﻿/*
	eXo config plugins
*/

CKEDITOR.eXoPath = CKEDITOR.basePath.substr(0, CKEDITOR.basePath.indexOf("ckeditor/"));

// config to add custom plugin	
(function() {CKEDITOR.plugins.addExternal('content',CKEDITOR.eXoPath+'eXoPlugins/content/','plugin.js');})();
(function() {CKEDITOR.plugins.addExternal('insertGadget',CKEDITOR.eXoPath+'eXoPlugins/insertGadget/','plugin.js');})();
(function() {CKEDITOR.plugins.addExternal('insertPortalLink',CKEDITOR.eXoPath+'eXoPlugins/insertPortalLink/','plugin.js');})();

CKEDITOR.editorConfig = function( config ){
	config.resize_enabled = false; // config to disable editor resizing in CKEDITOR
	config.extraPlugins = 'content,insertGadget,insertPortalLink';
	config.toolbarCanCollapse = false;
	//config.uiColor = '#AADC6E';
	config.toolbar_Default = [
		['Source','Templates'],
		['Cut','Copy','PasteText','-','SpellCheck'],
		['Undo','Redo','-','RemoveFormat'],
		'/',
		['Bold','Italic','Underline','Strike'],
		['NumberedList','BulletedList'],
		['Link','Unlink','Anchor'],
		['Image','Flash','Table','SpecialChar'],
		['TextColor','BGColor'],
		['Maximize', 'ShowBlocks'],
		['Style','Format','Font','FontSize']
	] ;

	config.toolbar_Basic = [
		['Source','-','Bold','Italic','Underline','Strike','-','NumberedList','BulletedList','Outdent','Indent'],
		['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
		'/',
		['Blockquote','-','Link','Unlink', 'ShowBlocks'],		
		['Style','Format','Font','FontSize','-','Maximize']
	] ;

	config.toolbar_CompleteWCM = [
		['Source','Templates','ShowBlocks'],
		['Cut','Copy','PasteText','-','SpellCheck','-','Undo','Redo'],
		['insertGadget.btn','Flash','Table','SpecialChar', 'content.btn', 'Image'], 
		'/',	
		['Bold','Italic','Underline','Strike','-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock','-','NumberedList','BulletedList','-','TextColor','BGColor','-','RemoveFormat'],
		['Link','insertPortalLink.btn','Unlink','Anchor'],
		'/',
		['Style','Format','Font','FontSize', '-' ,'Maximize']
	] ;
	
	config.toolbar_BasicWCM = [
		['Source','-','Bold','Italic','Underline','Strike','-','NumberedList','BulletedList','Outdent','Indent'],
		['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
		['Blockquote','-','Link','Unlink','insertPortalLink.btn','content.btn', 'Image','-','Maximize','ShowBlocks'],	
		['Style','Format','Font','FontSize']
	] ;

	config.toolbar_SuperBasicWCM = [
		   ['Source','-','Bold','Italic','Underline'],
		   ['-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
		   ['-','Link','Unlink','insertPortalLink.btn','insertGadget.btn','content.btn', 'Image'],	
	] ;
};

eXo.commons.ExoEditorConfig = CKEDITOR;

_module.config = eXo.commons.ExoEditorConfig;

return _module;
}
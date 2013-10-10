﻿﻿﻿/*
	eXo config plugins
*/

CKEDITOR.eXoPath = CKEDITOR.basePath.substr(0, CKEDITOR.basePath.indexOf("ckeditor/"));

// config to add custom plugin	
(function() {CKEDITOR.plugins.addExternal('content','/eXoWCMResources/eXoPlugins/content/','plugin.js');})();
(function() {CKEDITOR.plugins.addExternal('insertGadget','/eXoWCMResources/eXoPlugins/insertGadget/','plugin.js');})();
(function() {CKEDITOR.plugins.addExternal('insertPortalLink','/eXoWCMResources/eXoPlugins/insertPortalLink/','plugin.js');})();
(function() {CKEDITOR.plugins.addExternal('acceptInline','/eXoWCMResources/eXoPlugins/acceptInline/','plugin.js');})();
(function() {CKEDITOR.plugins.addExternal('cancelInline','/eXoWCMResources/eXoPlugins/cancelInline/','plugin.js');})();
(function() {CKEDITOR.plugins.addExternal('helpBBCode','/forumResources/eXoPlugins/helpBBCode/','plugin.js');})();

CKEDITOR.editorConfig = function( config ){
	config.extraPlugins = 'content,insertGadget,insertPortalLink,scayt,wsc,acceptInline,cancelInline,onchange,helpBBCode,syntaxhighlight';
	config.toolbarCanCollapse = false;
	config.skin = 'moono';
	config.allowedContent = true;
	config.resize_enabled = true;
	config.scayt_autoStartup = true;
	config.language = eXo.env.portal.language || 'en';
	config.pasteFromWordRemoveFontStyles = false;
	config.pasteFromWordRemoveStyles = false;
        config.syntaxhighlight_lang = 'java';
	config.syntaxhighlight_hideControls = true;


	config.toolbar_Default = [
		['Source','Templates'],
		['Cut','Copy','Paste','PasteText','PasteFromWord','-','Find','Replace','SelectAll','Scayt'],
		['Undo','Redo','-','RemoveFormat'],
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
		['Source','Templates'],
		['Cut','Copy','Paste','PasteText','PasteFromWord','-','Find','Replace','SelectAll','Scayt','-','Undo','Redo'],
		['insertGadget.btn','Flash','Table','SpecialChar', 'content.btn', 'Image'], 		['Bold','Italic','Underline','Strike','-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock','-','NumberedList','BulletedList','-','TextColor','BGColor','-','RemoveFormat'],
		['Link','insertPortalLink.btn','Unlink','Anchor'],		
		['Style','Format','Font','FontSize', '-' ,'Maximize']
	] ;
	
	config.toolbar_BasicWCM = [
		['Source','-','Bold','Italic','Underline','Strike'],
    ['-','NumberedList','BulletedList','Outdent','Indent'],
		['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock','Blockquote'],
		['-','Link','Unlink','insertPortalLink.btn','content.btn', 'Image'],
    ['-','Maximize','ShowBlocks','Style','Format','Font','FontSize']
	] ;

	config.toolbar_SuperBasicWCM = [
		   ['Source','-','Bold','Italic','Underline'],
		   ['-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
		   ['-','Link','Unlink','insertPortalLink.btn','insertGadget.btn','content.btn', 'Image'],	
	] ;

	config.toolbar_InlineEdit = [
		['Cut','Copy','Paste','PasteText','PasteFromWord','-','Find','Replace','SelectAll','Scayt','-','Undo','Redo'],
		['insertGadget.btn','Flash','Table','SpecialChar', 'content.btn', 'Image'],
                ['Bold','Italic','Underline','Strike','-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
		'/',
                ['NumberedList','BulletedList','-','TextColor','BGColor','-','RemoveFormat'],		
		['Link','insertPortalLink.btn','Unlink','Anchor'],		
		['Style','Format','Font','FontSize'],
		['-','acceptInline.btn','cancelInline.btn']
	] ;
	config.toolbar_InlineEditTitle = [
		['Bold','Italic','Underline','Strike'],    		
		['-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock','Blockquote'],
    		['-','Style','Format','Font','FontSize']
	] ;

	config.toolbar_Forum = [
		['Maximize','-','Cut','Copy','PasteText','-','Undo','Redo','-','Bold','Italic','Underline'],
		['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
		['NumberedList','BulletedList','Outdent','Indent','-','TextColor'],
		['Blockquote', 'Syntaxhighlight','helpBBCode.btn']
	] ;

	config.toolbar_FAQ = [
		['Maximize','-','Cut','Copy','PasteText','-','Undo','Redo','-','Bold','Italic','Underline'],
		['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],	
		['NumberedList','BulletedList','Outdent','Indent','-','TextColor'],
		['Blockquote', 'Syntaxhighlight','helpBBCode.btn']
	] ;

};

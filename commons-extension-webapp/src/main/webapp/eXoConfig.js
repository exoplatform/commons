/*
	eXo config plugins
*/
// force env when using the eXo Android app (the eXo Android app uses a custom user agent which
// is not known by CKEditor and which makes it not initialize the editor)
var userAgent = navigator.userAgent.toLowerCase();
if(userAgent != null && userAgent.indexOf('exo/') == 0 && userAgent.indexOf('(android)') > 0) {
  CKEDITOR.env.mobile = true;
  CKEDITOR.env.chrome = true;
  CKEDITOR.env.gecko = false;
  CKEDITOR.env.webkit = true;
}

CKEDITOR.eXoPath = CKEDITOR.basePath.substr(0, CKEDITOR.basePath.indexOf("ckeditor/"));

CKEDITOR.editorConfig = function( config ){
  // config to add custom plugin  
  CKEDITOR.plugins.addExternal('content','/eXoWCMResources/eXoPlugins/content/','plugin.js');
  CKEDITOR.plugins.addExternal('insertPortalLink','/commons-extension/eXoPlugins/insertPortalLink/','plugin.js');
  CKEDITOR.plugins.addExternal('simpleLink','/commons-extension/eXoPlugins/simpleLink/','plugin.js');
  CKEDITOR.plugins.addExternal('confirmBeforeReload','/commons-extension/eXoPlugins/confirmBeforeReload/','plugin.js');
  CKEDITOR.plugins.addExternal('acceptInline','/eXoWCMResources/eXoPlugins/acceptInline/','plugin.js');
  CKEDITOR.plugins.addExternal('cancelInline','/eXoWCMResources/eXoPlugins/cancelInline/','plugin.js');

	config.extraPlugins = 'content,insertPortalLink,acceptInline,cancelInline,onchange,syntaxhighlight,confirmBeforeReload';
	config.removePlugins = 'scayt,wsc';
	config.toolbarCanCollapse = false;
	config.skin = 'moono-exo,/commons-extension/ckeditor/skins/moono-exo/';
	config.allowedContent = true;
	config.resize_enabled = true;
	config.language = eXo.env.portal.language || 'en';
	config.pasteFromWordRemoveFontStyles = false;
	config.pasteFromWordRemoveStyles = false;
        config.syntaxhighlight_lang = 'java';
	config.syntaxhighlight_hideControls = true;

  // style inside the editor
	config.contentsCss = '/commons-extension/ckeditorCustom/contents.css';


	config.toolbar_Default = [
		['Source','Templates'],
		['Cut','Copy','Paste','PasteText','PasteFromWord','-','Find','Replace','SelectAll'],
		['Undo','Redo','-','RemoveFormat'],
		['Bold','Italic','Underline','Strike'],
		['NumberedList','BulletedList'],
		['Link','Unlink','Anchor'],
		['Image','Flash','Table','SpecialChar'],
		['TextColor','BGColor'],
		['Maximize', 'ShowBlocks'],
		['Styles','Format','Font','FontSize']
	] ;

	config.toolbar_Basic = [
		['Source','-','Bold','Italic','Underline','Strike','-','NumberedList','BulletedList','Outdent','Indent'],
		['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
		'/',
		['Blockquote','-','Link','Unlink', 'ShowBlocks'],		
		['Styles','Format','Font','FontSize','-','Maximize']
	] ;
	config.toolbar_Comment = [
  		['Bold','Italic','Underline','Strike','-','NumberedList','BulletedList','Outdent','Indent'],
  		['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
  		'/',
  		['Blockquote','-','Link','Unlink', 'ShowBlocks'],		
  		['Styles','Format','Font','FontSize','-','Maximize']
  	] ;
	
	config.toolbar_CompleteWCM = [
		['Source','Templates'],
		['Cut','Copy','Paste','PasteText','PasteFromWord','-','Find','Replace','SelectAll','-','Undo','Redo'],
		['Flash','Table','SpecialChar', 'content.btn', 'Image'], 		['Bold','Italic','Underline','Strike','-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock','-','NumberedList','BulletedList','-','TextColor','BGColor','-','RemoveFormat'],
		['Link','insertPortalLink.btn','Unlink','Anchor'],		
		['Styles','Format','Font','FontSize', '-' ,'Maximize']
	] ;
	
	config.toolbar_BasicWCM = [
		['Source','-','Bold','Italic','Underline','Strike'],
    ['-','NumberedList','BulletedList','Outdent','Indent'],
		['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock','Blockquote'],
		['-','Link','Unlink','insertPortalLink.btn','content.btn', 'Image'],
    ['-','Maximize','ShowBlocks','Styles','Format','Font','FontSize']
	] ;

	config.toolbar_SuperBasicWCM = [
		   ['Source','-','Bold','Italic','Underline'],
		   ['-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
		   ['-','Link','Unlink','insertPortalLink.btn','content.btn', 'Image'],
	] ;

	config.toolbar_InlineEdit = [
		['Cut','Copy','Paste','PasteText','PasteFromWord','-','Find','Replace','SelectAll','-','Undo','Redo'],
		['Flash','Table','SpecialChar', 'content.btn', 'Image'],
                ['Bold','Italic','Underline','Strike','-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
		'/',
                ['NumberedList','BulletedList','-','TextColor','BGColor','-','RemoveFormat'],		
		['Link','insertPortalLink.btn','Unlink','Anchor'],		
		['Styles','Format','Font','FontSize'],
		['-','acceptInline.btn','cancelInline.btn']
	] ;
	config.toolbar_InlineEditTitle = [
		['Bold','Italic','Underline','Strike'],    		
		['-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock','Blockquote'],
    		['-','Styles','Format','Font','FontSize']
	] ;

	config.toolbar_Forum = [
		['Source','Maximize','-','Cut','Copy','PasteText','-','Undo','Redo','-','Bold','Italic','Underline'],
		['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
		['NumberedList','BulletedList','Outdent','Indent','-','TextColor'],
		['Link','Unlink','-','Blockquote', 'Syntaxhighlight','Smiley']
	] ;

	config.toolbar_FAQ = [
		['Maximize','-','Cut','Copy','PasteText','-','Undo','Redo','-','Bold','Italic','Underline'],
		['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],	
		['NumberedList','BulletedList','Outdent','Indent','-','TextColor'],
		['Blockquote', 'Syntaxhighlight']
	] ;

};

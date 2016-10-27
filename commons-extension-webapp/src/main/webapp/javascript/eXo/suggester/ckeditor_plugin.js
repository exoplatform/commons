require(['SHARED/jquery'],function($) {
  var defaultOptions = {
    type: 'mix'
  };

  var fillingCharSequence = CKEDITOR.tools.repeat( '\u200b', 7 );
  function initSuggester(editor, config) {
    // WYSIWYG mode when switching from source mode
    if (editor.mode != 'source') {
      editor.document.getBody().$.contentEditable = true;
      config = $.extend(true, {}, config, {iframe: editor.window.getFrame().$});
      $(editor.document.getBody().$).suggester(config);
    }
    // Source mode when switching from WYSIWYG
    else {
      $(editor.container.$).find(".cke_source").suggester(config);
    }
  }

  function getContent(editor, textData) {
    var val = textData;
    if (editor.mode != 'source') {
      val = $(editor.document.getBody().$).suggester("replaceMentions", textData);
    } else {
      val = $(editor.container.$).find(".cke_source").suggester("replaceMentions", textData);
    }
    val = val.replace( fillingCharSequence, '');
    val = val.replace(/&nbsp;/ig, ' ');

    return val;
  }

  CKEDITOR.plugins.add( 'suggester', {
    init : function( editor ) {
      var config = editor.config.suggester;
      if (config == undefined) config = {};
      config = $.extend(true, {}, defaultOptions, config);

      editor.on('mode', function(e) {
        initSuggester(this, config);
      });
      editor.on('instanceReady', function() {
        //initSuggester(editor, config);
      });
      editor.on('getData', function(evt) {
        var data = evt.data;
        data.dataValue = getContent(evt.editor, data.dataValue);
      });
    }
  });
});
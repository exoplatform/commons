require(['SHARED/jquery', 'SHARED/suggester'],function($) {
  var defaultOptions = {
    type: 'mix'
  };
  var suggesterShowing = false;

  var fillingCharSequence = CKEDITOR.tools.repeat( '\u200b', 7 );
  function initSuggester(editor, config) {
    var $inputor = false;

    if (editor.mode != 'source') {
      editor.document.getBody().$.contentEditable = true;
      config = $.extend(true, {}, config, {iframe: editor.window.getFrame().$});
      $inputor = $(editor.document.getBody().$);
      $inputor.suggester(config);
    } else {
      $inputor = $(editor.container.$).find(".cke_source");
      $inputor.suggester(config);
    }

    if ($inputor) {
      var alias = config.alias ? '-' + config.alias + '.atwho' : '.atwho';
      $inputor.on('shown' + alias, function() {
        suggesterShowing = true;
      });
      $inputor.on('hidden' + alias, function() {
        suggesterShowing = false;
      });
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
      editor.on( 'key', function( event ) {
        if (suggesterShowing && (event.data.keyCode == 13 || event.data.keyCode == 10)) {
          event.cancel();
        }
      });
    }
  });
});
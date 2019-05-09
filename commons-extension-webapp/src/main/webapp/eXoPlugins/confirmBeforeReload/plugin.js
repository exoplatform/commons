require(['SHARED/jquery'],function($) {
  function handleBeforeunload(editor) {
    $(window).on( 'beforeunload', evt => {
      if ( editor.checkDirty() ) {
        return false;
      }
    });
  }

  CKEDITOR.plugins.add( 'confirmBeforeReload', {
    init : function(editor) {
      editor.on('instanceReady', function(evt) {
        handleBeforeunload(editor);
      });
    }
  });
});
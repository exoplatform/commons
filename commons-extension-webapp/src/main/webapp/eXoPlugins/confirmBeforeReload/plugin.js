require(['SHARED/jquery'],function($) {
  function handleBeforeUnload(editor) {
    $(window).on( 'beforeunload', evt => {
      if ( $(`.${editor.id}`).length && $(`.${editor.id}`).is(":visible") && editor.getData().trim() && editor.checkDirty() ) {
        return false;
      }
    });
  }

  CKEDITOR.plugins.add( 'confirmBeforeReload', {
    init : function(editor) {
      editor.on('instanceReady', function(evt) {
        handleBeforeUnload(editor);
      });
    }
  });
});

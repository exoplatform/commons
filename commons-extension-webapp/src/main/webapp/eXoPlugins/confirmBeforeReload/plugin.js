require(['SHARED/jquery'],function($) {
  function handleBeforeUnload(editor) {
    if (editor && !editor.beforeReloadHandled) {
      editor.beforeReloadHandled = true;
      $(window).on( 'beforeunload', evt => {
        if (editor) {
          if ( $(`.${editor.id}`).length && $(`.${editor.id}`).is(":visible") && editor.getData().trim() && editor.checkDirty() ) {
            return false;
          }
        }
      });
    }
  }

  CKEDITOR.plugins.add( 'confirmBeforeReload', {
    init : function(editor) {
      if (editor) {
        editor.on('instanceReady', function(evt) {
          handleBeforeUnload(editor);
        });
      }
    }
  });
});
